package main;

import java.util.HashSet;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Scope extends HashSet<Variable> {

  private final Scope parent;
  private final boolean isClass;

  public boolean inScope(String varName) {
    if(super.stream().anyMatch(var -> var.getName().equals(varName))) {
      return true;
    }
    return parent == null ? false : parent.inScope(varName);
  }

  public Variable getVariable(String name) {
    Optional<Variable> optionalVar = super.stream().filter(var -> var.getName().equals(name)).findAny();
    return optionalVar.orElseGet(() -> parent == null ? null : parent.getVariable(name));
  }
}
