class A {
  private int counter;

  public static void main(String[] args) {
    // main function that prints the supplied args and the number of args supplied
    for (String arg : args) {
      System.out.println(arg);
      counter ++;
    }
    System.out.format("There were %d args supplied.", counter);
  }
}
