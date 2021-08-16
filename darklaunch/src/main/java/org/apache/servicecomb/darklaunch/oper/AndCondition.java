package org.apache.servicecomb.darklaunch.oper;

public class AndCondition extends AbstractCondition {
  private Condition[] conditions;

  public AndCondition(Condition... conditions) {
    super("and", "and");
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
      if (!condition.match()) {
        return false;
      }
    }
    return true;
  }
}
