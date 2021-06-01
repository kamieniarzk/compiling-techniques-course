package main;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ProgramTest {

  private String[] inputPaths = new String[]{
      "src/main/resources/Input1.java",
      "src/main/resources/Input2.java",
      "src/main/resources/Input3.java"};

  @Test
  public void validTestCases() {
    Program.main(inputPaths);
  }

  @Disabled
  @Test
  public void errorTestCase1() {
    Program.main(new String[]{"src/main/resources/Input4.java"});
  }

  @Disabled
  @Test
  void errorTestCase2() {
    Program.main(new String[]{"src/main/resources/Input4.java"});
  }
}