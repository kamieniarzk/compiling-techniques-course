class A {
  public static final String name;

  public static void main(String[] args) {
    int amount = 0;
  }

  public int getAverage(int[] numbers) {
    if (numbers.length == 0) {
      return 0;
    }

    int sum = 0;

    for (int number : numbers) {
      sum += number;
    }

    return sum / numbers.length;
  }
}

class B extends A {

  public static void main(String[] args) {
    A a = new A();

    this.name++;
    System.out.println(name);
  }

  public int getSum(int[] numbers) {
    int sum = 0;
    for (int i = 0; i < numbers.length; i++) {
      sum += i;
    }

    return sum;
  }
}
