package main;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalVariable extends Variable {

  @Builder
  public LocalVariable(final String name, final String type, final int line, final boolean isUsed, final Clazz clazz) {
    super(name, type, line, isUsed, clazz);
  }

//  public String toString() {
//    return !isMethod ? String.format("Local variable %s in class %s at line %d, of type %s was declared but never used.", name, clazz.getName(), line, type)
//        : String.format("Method parameter %s in class %s at line %d, of type %s was never used.", name, clazz.getName(), line, type);
//  }
}
