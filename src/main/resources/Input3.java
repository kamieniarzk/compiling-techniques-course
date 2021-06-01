class Vehicle {
  String make;
  String model;
  int year;

  public String getModel() {
    return this.model;
  }

  public String getMake() {
    return this.make;
  }
}

class Car extends Vehicle {
  private double engineCapacity;

  public Car(String make, double engineCapacity) {
    this.make = make;
    this.engineCapacity = engineCapacity;
  }

  public randomFunction() {
    int someLocalVariable = 1000;
    String someOtherLocalVariable;

    if (someLocalVariable == 1000) {
      int someCounter = 200;
      if (someCounter == 200) {
        int someOtherCounter = 300;
        if (someOtherCounter == 300) {
          int someCounterInNestedBlock;
        }
      }
    }
  }
}

class BMWCar extends Car {
  private String suspensionType;

  public BMWCar(String suspensionType) {
    this.suspensionType = suspensionType;
  }

  public BMWCar fromCar(Car car) {
    BMWCar bmwCar = new BMWCar("defaultSuspensionType");
    bmwCar.make = car.getMake();
    bmwCar.model = car.getModel();
  }
}
