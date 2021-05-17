package main.listener.domain.variable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.listener.domain.Clazz;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class Variable {
  private String name;
  private String type;
  private int line;
  private boolean isUsed;
  private Clazz clazz;
}
