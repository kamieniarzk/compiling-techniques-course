package main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Variable {
  private int blockStart;
  private int blockStop;
  private String name;
  private String type;
  private int line;
  private boolean isUsed;
}
