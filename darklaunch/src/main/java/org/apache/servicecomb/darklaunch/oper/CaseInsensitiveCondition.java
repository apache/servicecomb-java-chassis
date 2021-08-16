package org.apache.servicecomb.darklaunch.oper;

public class CaseInsensitiveCondition extends AbstractCondition {
  private Condition condition;

  public CaseInsensitiveCondition(Condition condition) {
    super(condition.key(), condition.expected());
    this.condition = condition;
  }

  @Override
  public void setActual(String key, Object actual) {
    if (null == actual) {
      condition.setActual(key, null);
      return;
    }
    actual = actual.toString().toLowerCase();
    condition.setActual(key, actual);
  }

  @Override
  public boolean match() {
    return condition.match();
  }
}
