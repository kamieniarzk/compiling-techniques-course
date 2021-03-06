package main.listener.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ErrorNode;

import main.listener.domain.Clazz;
import main.antlr.Java8BaseListener;
import main.antlr.Java8Parser.NormalClassDeclarationContext;
import main.antlr.Java8Parser.SuperclassContext;
import main.exception.ProgramException;

public class ClassDeclarationListener extends Java8BaseListener {

  private Stack<Clazz> classes;

  private Clazz getCurrentClass() {
    return classes.peek();
  }

  public ClassDeclarationListener() {
    classes = new Stack<>();
  }

  public List<Clazz> getAllClasses() {
    return new ArrayList<>(classes);
  }

  @Override
  public void enterNormalClassDeclaration(final NormalClassDeclarationContext ctx) {
    final String className = ctx.Identifier().getText();
    final boolean classAlreadyExists = classes.stream()
        .anyMatch(clazz -> clazz.getName().equals(className));

    if (classAlreadyExists) {
      throw new ProgramException("Error at line " + ctx.start.getLine() + ", class with identifier " + className + " already exists!");
    }

    Clazz clazz = Clazz.builder()
        .name(className)
        .line(ctx.start.getLine())
        .build();

    classes.push(clazz);
  }

  @Override
  public void enterSuperclass(final SuperclassContext ctx) {
    final String superClassName = ctx.classType().Identifier().toString();
    getCurrentClass().setParentName(superClassName);

    Clazz parent = classes.stream()
        .filter(clazz -> clazz.getName().equals(superClassName))
        .findAny()
        .orElseThrow(() -> new ProgramException("Super class " + superClassName + " at line " + ctx.start.getLine() + " is not defined."));

    getCurrentClass().setParent(parent);
  }

  @Override
  public void visitErrorNode(final ErrorNode node) {
    super.visitErrorNode(node);
    System.exit(-1);
  }

  public void setSuperClasses() {
    classes.forEach(clazz -> {
      final String parentName = clazz.getParentName();
      if (parentName != null) {
        Clazz parentClass = classes.stream()
            .filter(c -> c.getName().equals(parentName))
            .findAny()
            .orElseThrow(() -> new ProgramException("Super class " + parentName + " at line " + clazz.getLine() + " is not defined."));
        clazz.setParent(parentClass);
      }
    });
  }
}
