package org.apache.servicecomb.darklaunch.oper;

public class OrCondition extends AbstractCondition {
  private Condition[] conditions;

  public OrCondition(Condition... conditions) {
    super("or", "or");
    this.conditions = conditions;
  }

  @Override
  public void setActual(String key, Object actual) {
    for (Condition condition : this.conditions) {
      condition.setActual(key, actual);
    }
  }

  @Override
  public boolean match() {
    for (Condition condition : this.conditions) {
      if (condition.match()) {
        return true;
      }
    }
    return false;
  }
}
