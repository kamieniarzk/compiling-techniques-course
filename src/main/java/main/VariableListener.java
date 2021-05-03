package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import main.Java8Parser.ArgumentListContext;
import main.Java8Parser.AssignmentContext;

import main.Java8Parser.BlockContext;
import main.Java8Parser.ClassBodyContext;
import main.Java8Parser.ExpressionNameContext;
import main.Java8Parser.FieldDeclarationContext;
import main.Java8Parser.FieldModifierContext;
import main.Java8Parser.FormalParameterListContext;
import main.Java8Parser.IfThenStatementContext;
import main.Java8Parser.LastFormalParameterContext;
import main.Java8Parser.LocalVariableDeclarationContext;

import main.Java8Parser.MethodBodyContext;
import main.Java8Parser.MethodDeclarationContext;
import main.Java8Parser.MethodDeclaratorContext;
import main.Java8Parser.TypeArgumentListContext;
import main.Java8Parser.TypeVariableContext;

import main.Java8Parser.UnaryExpressionContext;
import main.Java8Parser.VariableDeclaratorContext;
import main.Java8Parser.VariableDeclaratorIdContext;
import main.Java8Parser.VariableDeclaratorListContext;

public class MyListener extends Java8BaseListener {

  private Map<Variable, Integer> variables = new HashMap<>();

  public List<String> errors = new ArrayList<>();
  private Stack<Scope> scopes = new Stack<>();

  public Scope getScope() {
    return scopes.peek();
  }

  public MyListener() {
    scopes.push(new Scope(null, false));
  }

  @Override
  public void enterMethodDeclarator(final MethodDeclaratorContext ctx) {
    TerminalNode node = ctx.Identifier();
    String methodName = node.getText();

    if (Character.isUpperCase(methodName.charAt(0))) {
      String error = String.format("Method %s is uppercased!", methodName);
      errors.add(error);
    }

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
    variableNames.forEach(var -> getScope().add(Variable.builder()
        .name(var.variableDeclaratorId().Identifier().getText())
        .type(variableType)
        .line(ctx.start.getLine())
        .isField(true)
        .build()));
  }

  @Override
  public void enterClassBody(final ClassBodyContext ctx) {
    scopes.push(new Scope(scopes.peek(), true));
  }

  @Override
  public void exitClassBody(final ClassBodyContext ctx) {
    scopes.peek().forEach(var -> {
      if (!var.isUsed()) {
        System.out.println(var);
      }
    });
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
      getScope().add(Variable.builder()
          .type(lastFormalParameter.formalParameter().unannType().getText())
          .name(lastFormalParameter.formalParameter().variableDeclaratorId().Identifier().getText())
          .line(lastFormalParameter.formalParameter().start.getLine())
          .build());
    }
    if (parameterList.formalParameters() != null){
      parameterList.formalParameters().formalParameter().forEach(methodParam -> getScope().add(Variable.builder()
          .line(methodParam.start.getLine())
          .name(methodParam.variableDeclaratorId().Identifier().getText())
          .type(methodParam.unannType().getText())
          .build()));
    }
  }

  @Override
  public void exitBlock(final BlockContext ctx) {
    scopes.peek().forEach(var -> {
      if (!var.isUsed()) {
        System.out.println(var);
      }
    });
    scopes.pop();
  }


  @Override
  public void enterTypeArgumentList(final TypeArgumentListContext ctx) {
    super.enterTypeArgumentList(ctx);
  }

  @Override
  public void enterTypeVariable(final TypeVariableContext ctx) {
    TerminalNode node = ctx.Identifier();
  }

  @Override
  public void exitTypeVariable(final TypeVariableContext ctx) {
    super.exitTypeVariable(ctx);
  }

  @Override
  public void enterVariableDeclaratorList(final VariableDeclaratorListContext ctx) {
    super.enterVariableDeclaratorList(ctx);
  }

  @Override
  public void exitVariableDeclaratorList(final VariableDeclaratorListContext ctx) {
    super.exitVariableDeclaratorList(ctx);
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
  public void exitVariableDeclarator(final VariableDeclaratorContext ctx) {
    super.exitVariableDeclarator(ctx);
  }

  @Override
  public void enterVariableDeclaratorId(final VariableDeclaratorIdContext ctx) {
    super.enterVariableDeclaratorId(ctx);
  }


  @Override
  public void enterExpressionName(final ExpressionNameContext ctx) {
    Variable variable = getScope().getVariable(ctx.getText());

    if (variable != null) {
      variable.setUsed(true);
    } else {
      System.err.format("Error at line %d, variable %s was not declared in this scope.\n", ctx.start.getLine(), ctx.getText());
    }
  }

//  @Override
//  public void enterUnaryExpression(final UnaryExpressionContext ctx) {
//    Variable variable = getScope().getVariable(ctx.getText());
//
//    if (variable != null) {
//      variable.setUsed(true);
//    }
//  }

  @Override
  public void exitArgumentList(final ArgumentListContext ctx) {
    super.exitArgumentList(ctx);
  }


  @Override
  public void enterLocalVariableDeclaration(final LocalVariableDeclarationContext ctx) {
    String variableType = ctx.unannType().getText();
    List<VariableDeclaratorContext> variableNames = ctx.variableDeclaratorList().variableDeclarator();
    variableNames.forEach(var -> getScope().add(Variable.builder()
        .name(var.variableDeclaratorId().Identifier().getText())
        .type(variableType)
        .line(ctx.start.getLine())
        .isField(false)
        .build()));

  }

  @Override
  public void visitErrorNode(final ErrorNode node) {
    super.visitErrorNode(node);
  }

  @Override
  public void enterAssignment(final AssignmentContext ctx) {
    String varName = ctx.start.getText();
    Variable variable = getScope().getVariable(varName);

    if (variable != null) {
      variable.setUsed(true);
    }
  }
}
