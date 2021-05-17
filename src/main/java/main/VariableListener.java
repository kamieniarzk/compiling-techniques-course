package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.ErrorNode;

import lombok.Getter;
import main.Java8Parser.ArgumentListContext;
import main.Java8Parser.AssignmentContext;

import main.Java8Parser.BlockContext;
import main.Java8Parser.ClassBodyContext;
import main.Java8Parser.ExpressionNameContext;
import main.Java8Parser.FieldAccessContext;
import main.Java8Parser.FieldDeclarationContext;
import main.Java8Parser.FormalParameterListContext;
import main.Java8Parser.LastFormalParameterContext;
import main.Java8Parser.LocalVariableDeclarationContext;

import main.Java8Parser.MethodBodyContext;
import main.Java8Parser.MethodDeclarationContext;
import main.Java8Parser.NormalClassDeclarationContext;
import main.Java8Parser.PostfixExpressionContext;
import main.Java8Parser.SuperclassContext;

import main.Java8Parser.VariableDeclaratorContext;

public class VariableListener extends Java8BaseListener {

  private Map<String, Integer> variables = new LinkedHashMap<>();

  private Map<Variable, Integer> variableLineMap = new LinkedHashMap<>();

  private Map<Field, Integer> fields = new LinkedHashMap<>();

  private Clazz currentClass;

  private final List<Clazz> allClasses;

  @Getter
  private Stack<Clazz> classes = new Stack<>();

  private Stack<Scope> scopes = new Stack<>();

  private Scope getCurrentScope() {
    return scopes.peek();
  }

  public VariableListener(Collection<Clazz> classes) {
    allClasses = new ArrayList<>(classes);
    scopes.push(new Scope(null, false));
  }

  public List<String> getVariables() {
    return variables.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Entry::getKey)
        .collect(Collectors.toList());
  }

  public List<LocalVariable> getUnusedLocalVariables() {
    return variableLineMap.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Entry::getKey)
        .filter(var -> var instanceof LocalVariable)
        .map(LocalVariable.class::cast)
        .collect(Collectors.toList());
  }

  public List<MethodParameter> getUnusedMethodParameters() {
    return variableLineMap.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Entry::getKey)
        .filter(var -> var instanceof MethodParameter)
        .map(MethodParameter.class::cast)
        .collect(Collectors.toList());
  }

  public List<Field> getAllUnusedFields() {
    return fields.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Entry::getKey)
        .filter(field -> !field.isUsed)
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
  public void exitFieldDeclaration(final FieldDeclarationContext ctx) {
    super.exitFieldDeclaration(ctx);
  }

  @Override
  public void exitMethodBody(final MethodBodyContext ctx) {
    super.exitMethodBody(ctx);
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
    scopes.push(new Scope(scopes.peek(), true));
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
//    scopes.peek().forEach(var -> {
//      if (!var.isUsed()) {
////        System.out.println(var);
//        variableLineMap.put(var, var.getLine());
//        variables.put(var.toString(), var.getLine());
//      }
//    });
//    getCurrentScope().forEach(var -> fields.put(var, var.getLine()));
    currentClass.forEach(field -> fields.put(field, field.getLine()));
    scopes.pop();
  }

  @Override
  public void enterBlock(final BlockContext ctx) {
    scopes.push(new Scope(scopes.peek(), false));
    if (ctx.parent instanceof MethodBodyContext) {
      MethodDeclarationContext methodDeclaration = (MethodDeclarationContext) ctx.parent.parent;
      FormalParameterListContext parameterList = methodDeclaration.methodHeader().methodDeclarator().formalParameterList();
      parseMethodParameters(parameterList);
    }
  }

  public void parseMethodParameters(FormalParameterListContext parameterList) {
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

  @Override
  public void enterFieldAccess(final FieldAccessContext ctx) {
    final String fieldName = ctx.Identifier().toString();
    final Field field = currentClass.getField(fieldName);

    if (field != null) {
      field.isUsed = true;
    } else {
      throw new ProgramException("Field not declared.");
    }

  }

  @Override
  public void exitBlock(final BlockContext ctx) {
//    scopes.peek().forEach(var -> {
//      if (!var.isUsed()) {
////        System.out.println(var);
//        variables.put(var.toString(), var.getLine());
//
//      }
//    });
    getCurrentScope().forEach(var -> variableLineMap.put(var, var.getLine()));
    scopes.pop();
  }


  @Override
  public void enterVariableDeclarator(final VariableDeclaratorContext ctx) {
//    Variable declaredVariable = Variable.builder()
//        .name(ctx.getText())
//        .line(ctx.depth())
//        .type(ctx.parent.parent.getText())
//        .build();
//
//    scopes.peek().add(declaredVariable);
  }

  @Override
  public void enterPostfixExpression(final PostfixExpressionContext ctx) {
    super.enterPostfixExpression(ctx);
  }

  @Override
  public void enterExpressionName(final ExpressionNameContext ctx) {
    if (ctx.ambiguousName().toString().equals("this") || ctx.ambiguousName().toString().equals("super")) {
      System.out.println("witam w bmw x5");
    }

    System.out.println("Inside enterExpressionName");
    Variable localVariable = getCurrentScope().getVariable(ctx.getText());
    Field field = currentClass.getField(ctx.getText());

    if (localVariable != null) {
      localVariable.setUsed(true);
    }

    if (field != null) {
      field.setUsed(true);
    } else {
      System.err.format("Error at line %d, variable %s was not declared in this scope.\n", ctx.start.getLine(), ctx.getText());
      System.exit(-1);
    }
  }


  @Override
  public void exitArgumentList(final ArgumentListContext ctx) {
    super.exitArgumentList(ctx);
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
  public void visitErrorNode(final ErrorNode node) {
    super.visitErrorNode(node);
  }

  @Override
  public void enterAssignment(final AssignmentContext ctx) {
    String varName = ctx.start.getText();
    Variable localVariable = getCurrentScope().getVariable(varName);

    if (localVariable != null) {
      localVariable.setUsed(true);
    }
  }
}
