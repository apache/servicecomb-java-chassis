package org.apache.servicecomb.darklaunch.oper;

public class NotEqualCondition extends AbstractCondition {

  public NotEqualCondition(String key, String expected) {
    super(key, expected);
  }

  @Override
  public boolean match() {
    SupportedType type = this.getType();
    if (type == SupportedType.NUMBER) {
      return compareNum(this.getActual(), this.expected()) != 0;
    } else if (type == SupportedType.STRING) {
      return ((String) this.getActual()).compareTo(this.expected()) != 0;
    } else {
      return false;
    }
  }
}
