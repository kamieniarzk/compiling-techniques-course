package main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
//@Builder
public abstract class Variable {

  protected String name;
  protected String type;
  protected int line;
  protected boolean isUsed;
  protected Clazz clazz;
}
