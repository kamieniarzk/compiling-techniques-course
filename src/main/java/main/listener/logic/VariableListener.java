package main.listener.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;

import main.antlr.Java8Parser.EnhancedForStatementContext;
import main.antlr.Java8Parser.MethodInvocation_lfno_primaryContext;
import main.listener.domain.Clazz;
import main.listener.domain.Scope;
import main.antlr.Java8BaseListener;
import main.antlr.Java8Parser.AssignmentContext;

import main.antlr.Java8Parser.BlockContext;
import main.antlr.Java8Parser.ClassBodyContext;
import main.antlr.Java8Parser.ExpressionNameContext;
import main.antlr.Java8Parser.FieldAccessContext;
import main.antlr.Java8Parser.FieldAccess_lf_primaryContext;
import main.antlr.Java8Parser.FieldAccess_lfno_primaryContext;
import main.antlr.Java8Parser.FieldDeclarationContext;
import main.antlr.Java8Parser.FormalParameterListContext;
import main.antlr.Java8Parser.LastFormalParameterContext;
import main.antlr.Java8Parser.LocalVariableDeclarationContext;

import main.antlr.Java8Parser.MethodBodyContext;
import main.antlr.Java8Parser.MethodDeclarationContext;
import main.antlr.Java8Parser.NormalClassDeclarationContext;
import main.antlr.Java8Parser.SuperclassContext;

import main.antlr.Java8Parser.VariableDeclaratorContext;
import main.exception.ProgramException;
import main.listener.domain.variable.Field;
import main.listener.domain.variable.LocalVariable;
import main.listener.domain.variable.MethodParameter;
import main.listener.domain.variable.Variable;

public class VariableListener extends Java8BaseListener {

  private final Stack<Scope> scopes = new Stack<>();

  private final Map<Variable, Integer> variableLineMap = new LinkedHashMap<>();

  private final List<Clazz> allClasses;

  private Clazz currentClass;

  public VariableListener(Collection<Clazz> classes) {
    allClasses = new ArrayList<>(classes);
    scopes.push(new Scope(null));
  }

  public List<Variable> getUnusedVariables() {
    return variableLineMap.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Entry::getKey)
        .filter(var -> !var.isUsed())
        .collect(Collectors.toList());
  }

  @Override
  public void enterSuperclass(final SuperclassContext ctx) {
    final String superClassName = ctx.classType().Identifier().toString();

    Clazz parent = allClasses.stream()
        .filter(clazz -> clazz.getName().equals(superClassName))
        .findAny()
        .orElseThrow(() -> new ProgramException("Super class " + superClassName + " at line " + ctx.start.getLine() + " is not defined."));

    currentClass.setParent(parent);
  }

  @Override
  public void enterFieldDeclaration(final FieldDeclarationContext ctx) {
    String variableType = ctx.unannType().getText();
    List<VariableDeclaratorContext> variableNames = ctx.variableDeclaratorList().variableDeclarator();

    variableNames.forEach(var -> currentClass.add(Field.builder()
        .name(var.variableDeclaratorId().Identifier().getText())
        .type(variableType)
        .line(ctx.start.getLine())
        .clazz(currentClass)
        .build()));
  }

  @Override
  public void enterClassBody(final ClassBodyContext ctx) {
    scopes.push(new Scope(scopes.peek()));
  }

  @Override
  public void enterNormalClassDeclaration(final NormalClassDeclarationContext ctx) {
    final String className = ctx.Identifier().getText();

    this.currentClass = allClasses.stream()
        .filter(clazz -> clazz.getName().equals(className))
        .findAny()
        .orElseThrow(() -> new ProgramException("Unexpected error."));
  }

  @Override
  public void exitClassBody(final ClassBodyContext ctx) {
    currentClass.forEach(field -> variableLineMap.put(field, field.getLine()));
    scopes.pop();
  }

  @Override
  public void enterBlock(final BlockContext ctx) {
    scopes.push(new Scope(scopes.peek()));
    if (ctx.parent instanceof MethodBodyContext) {
      MethodDeclarationContext methodDeclaration = (MethodDeclarationContext) ctx.parent.parent;
      FormalParameterListContext parameterList = methodDeclaration.methodHeader().methodDeclarator().formalParameterList();
      if (parameterList != null) {
        parseMethodParameters(parameterList);
      }
    }
  }

  @Override
  public void enterFieldAccess_lf_primary(final FieldAccess_lf_primaryContext ctx) {
    handleFieldAccess(ctx.Identifier(), ctx.start.getLine());
  }

  @Override
  public void enterFieldAccess_lfno_primary(final FieldAccess_lfno_primaryContext ctx) {
    handleFieldAccess(ctx.Identifier(), ctx.start.getLine());
  }

  @Override
  public void enterFieldAccess(final FieldAccessContext ctx) {
    handleFieldAccess(ctx.Identifier(), ctx.start.getLine());
  }

  @Override
  public void exitBlock(final BlockContext ctx) {
    getCurrentScope().forEach(var -> variableLineMap.put(var, var.getLine()));
    scopes.pop();
  }



  @Override
  public void enterExpressionName(final ExpressionNameContext ctx) {
    String variableName;

    if (ctx.ambiguousName() != null) {
      variableName = ctx.ambiguousName().Identifier().getText();
      handleVariable(variableName, ctx);
      String fieldName = ctx.Identifier().getText();
      String className = getCurrentScope().getVariable(variableName).getType();
      handleFieldObjectAccess(fieldName, className, ctx.start.getLine());
    } else {
      variableName = ctx.Identifier().getText();
      handleVariable(variableName, ctx);
    }
  }

  @Override
  public void enterMethodInvocation_lfno_primary(final MethodInvocation_lfno_primaryContext ctx) {
    String objectName = ctx.typeName().Identifier().getText();
    handleVariable(objectName, null);
  }

  @Override
  public void enterLocalVariableDeclaration(final LocalVariableDeclarationContext ctx) {
    String variableType = ctx.unannType().getText();
    List<VariableDeclaratorContext> variableNames = ctx.variableDeclaratorList().variableDeclarator();
    variableNames.forEach(var -> getCurrentScope().add(LocalVariable.builder()
        .name(var.variableDeclaratorId().Identifier().getText())
        .type(variableType)
        .line(ctx.start.getLine())
        .clazz(currentClass)
        .build()));
  }

  @Override
  public void enterAssignment(final AssignmentContext ctx) {
    String varName = ctx.start.getText();
    Variable localVariable = getCurrentScope().getVariable(varName);

    if (localVariable != null) {
      localVariable.setUsed(true);
    }
  }

  @Override
  public void enterEnhancedForStatement(final EnhancedForStatementContext ctx) {
    LocalVariable localVariable = LocalVariable.builder()
        .name(ctx.variableDeclaratorId().Identifier().getText())
        .type(ctx.unannType().getText())
        .line(ctx.start.getLine())
        .clazz(currentClass)
        .build();

    getCurrentScope().add(localVariable);
  }

  private void handleFieldAccess(final TerminalNode identifier, final int line) {
    final String fieldName = identifier.toString();
    final Field field = currentClass.getField(fieldName);

    if (field != null) {
      field.setUsed(true);
    } else {
      throw new ProgramException("Field " + fieldName + " in class " + currentClass.getName() + " at line " + line + " not declared.");
    }
  }

  private void parseMethodParameters(FormalParameterListContext parameterList) {
    LastFormalParameterContext lastFormalParameter = parameterList.lastFormalParameter();
    if (lastFormalParameter != null) {
      getCurrentScope().add(MethodParameter.builder()
          .type(lastFormalParameter.formalParameter().unannType().getText())
          .name(lastFormalParameter.formalParameter().variableDeclaratorId().Identifier().getText())
          .line(lastFormalParameter.formalParameter().start.getLine())
          .clazz(currentClass)
          .build());
    }
    if (parameterList.formalParameters() != null){
      parameterList.formalParameters().formalParameter().forEach(methodParam -> getCurrentScope().add(MethodParameter.builder()
          .line(methodParam.start.getLine())
          .name(methodParam.variableDeclaratorId().Identifier().getText())
          .type(methodParam.unannType().getText())
          .clazz(currentClass)
          .build()));
    }
  }

  private Scope getCurrentScope() {
    return scopes.peek();
  }

  private Optional<Clazz> getClassByName(String name) {
    return allClasses.stream()
        .filter(clazz -> clazz.getName().equals(name))
        .findAny();
  }

  private void handleFieldObjectAccess(String fieldName, String className, int line) {
    final Optional<Clazz> clazz = getClassByName(className);

    if (clazz.isPresent()) {
      Clazz existingClazz = clazz.get();
      Field field = existingClazz.getField(fieldName);
      if (field != null) {
        field.setUsed(true);
      } else {
        throw new ProgramException("Error at line " + line + ", field " + fieldName + " does not exist in class " + existingClazz.getName());
      }
    }

  }

  private void handleVariable(String variableName, ExpressionNameContext ctx) {
    Variable localVariable = getCurrentScope().getVariable(variableName);
    Field field = currentClass.getField(variableName);

    if (localVariable != null) {
      localVariable.setUsed(true);
      return;
    } else if (field != null) {
      field.setUsed(true);
      return;
    }

    throw new ProgramException(String.format("Error at line %d, variable %s was not declared in this scope.\n", ctx.start.getLine(), ctx.Identifier().getText()));
  }
}
