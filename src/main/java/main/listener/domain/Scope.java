package main.listener.domain;

import java.util.HashSet;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import main.listener.domain.variable.Variable;

@RequiredArgsConstructor
public class Scope extends HashSet<Variable> {

  private final Scope parent;

  public Variable getVariable(String name) {
    Optional<Variable> optionalVar = super.stream().filter(var -> var.getName().equals(name)).findAny();
    return optionalVar.orElseGet(() -> parent == null ? null : parent.getVariable(name));
  }
}
