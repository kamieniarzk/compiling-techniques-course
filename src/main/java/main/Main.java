package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.IOUtils;

public class Main {

  public static void main(String[] args) {

    String javaClassContent = "";

    try {
      FileInputStream fis = new FileInputStream("src/main/resources/HelloWorldApp.java");
      javaClassContent = IOUtils.toString(fis, StandardCharsets.UTF_8);
    } catch (IOException e) {
      System.err.println("Error while reading input.");
    }


//    String javaClassContent = "public class SampleClass { int siema; void DoSomething(){" +
//        "siema = 8;} }";
    Java8Lexer java8Lexer = new Java8Lexer(CharStreams.fromString(javaClassContent));

    CommonTokenStream tokens = new CommonTokenStream(java8Lexer);
    Java8Parser parser = new Java8Parser(tokens);
    ParseTree tree = parser.compilationUnit();

    ParseTreeWalker walker = new ParseTreeWalker();
    MyListener listener= new MyListener();

    walker.walk(listener, tree);

  }
}
