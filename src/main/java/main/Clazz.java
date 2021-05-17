package main;

import java.util.HashSet;
import java.util.Optional;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class Clazz extends HashSet<Field> {
  private String name;
  private String parentName;
  private Clazz parent;
  private int line;

  public Field getField(String name) {
    Optional<Field> optionalVar = super.stream().filter(var -> var.getName().equals(name)).findAny();
    return optionalVar.orElseGet(() -> parent == null ? null : parent.getField(name));
  }
}
