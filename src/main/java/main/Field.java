package main;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Field extends Variable {

  @Builder
  public Field(final String name, final String type, final int line, final boolean isUsed, final Clazz clazz) {
    super(name, type, line, isUsed, clazz);
  }

  public String toString() {
    return String.format("Field %s in class %s at line %d, of type %s was declared but never used.", name, clazz.getName(), line, type);
  }
}
