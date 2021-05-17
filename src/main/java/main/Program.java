package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.IOUtils;

import main.antlr.Java8Lexer;
import main.antlr.Java8Parser;
import main.exception.ProgramException;
import main.listener.domain.Clazz;
import main.listener.logic.ClassDeclarationListener;
import main.listener.logic.VariableListener;
import main.listener.domain.variable.LocalVariable;
import main.listener.domain.variable.MethodParameter;
import main.listener.domain.variable.Variable;

public class Program {

  public static void main(String[] args) {

    String javaClassContent = "";

    try {
      FileInputStream fis = new FileInputStream("src/main/resources/HelloWorldApp.java");
      javaClassContent = IOUtils.toString(fis, StandardCharsets.UTF_8);
    } catch (IOException e) {
      System.err.println("Error while reading input.");
    }

    Java8Lexer java8Lexer = new Java8Lexer(CharStreams.fromString(javaClassContent));

    CommonTokenStream tokens = new CommonTokenStream(java8Lexer);
    Java8Parser parser = new Java8Parser(tokens);
    ParseTree tree = parser.compilationUnit();

    ParseTreeWalker walker = new ParseTreeWalker();
    ClassDeclarationListener classDeclarationListener = new ClassDeclarationListener();

    walker.walk(classDeclarationListener, tree);

    classDeclarationListener.setSuperClasses();

    List<Clazz> allInputClasses = classDeclarationListener.getAllClasses();

    VariableListener variableListener = new VariableListener(allInputClasses);

    try {
      walker.walk(variableListener, tree);
    } catch (ProgramException e) {
      exitWithError(e.getMessage());
    }


    List<Variable> unusedVariables = variableListener.getUnusedVariables();

    unusedVariables.forEach(Program::printUnusedVariable);
  }


  public static void exitWithError(String message) {
    System.err.println(message);
    System.exit(-1);
  }

  public static void printUnusedVariable(Variable var) {
    String messageToPrint;

    if (var instanceof MethodParameter) {
      messageToPrint = String.format("Method parameter %s in class %s of type %s at line %d was never used.",
          var.getName(), var.getClazz().getName(), var.getType(), var.getLine());
    } else if (var instanceof LocalVariable) {
      messageToPrint = String.format("Local variable %s in class %s of type %s at line %d was declared but never used.",
          var.getName(), var.getClazz().getName(), var.getType(), var.getLine());
    } else {
      messageToPrint = String.format("Field %s in class %s of type %s at line %d was never used.",
          var.getName(), var.getClazz().getName(), var.getType(), var.getLine());
    }

    System.out.println(messageToPrint);
  }
}
