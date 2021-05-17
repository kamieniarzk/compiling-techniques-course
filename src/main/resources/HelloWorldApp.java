class MyOwn {
  private int counter;
  public static final String name;

  public static void main(String[] args) {
    int amount = 0;
    //    myVar++;
    System.out.println(this.name); // Display the string.
  }
}

class HelloWorldApp extends MyOwn {
//  private int counter;
  public static final String name;

  public static void main(String[] args) {
//    myVar++;
    this.gay++;
//    counter++;
    System.out.println(this.name); // Display the string.
    System.out.println(this.gay);
  }
}

