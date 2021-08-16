package org.apache.servicecomb.darklaunch.oper;

public abstract class AbstractCondition implements Condition {
  private String key;

  private String expected;

  private Object actual;

  private SupportedType type = SupportedType.UNKNON;

  public AbstractCondition(String key, String expected) {
    assertValueNotNull(key, expected);
    this.key = key;
    this.expected = expected;
  }

  @Override
  public String key() {
    return this.key;
  }

  @Override
  public String expected() {
    return this.expected;
  }

  @Override
  public void setActual(String key, Object actual) {
    assertValueNotNull(key, "");
    if (this.key.equals(key)) {
      this.type = SupportedType.STRING;
    } else if (actual instanceof Number) {
      this.type = SupportedType.NUMBER;
    }
    this.actual = actual;
  }

  protected void assertValueNotNull(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("Key can be null.");
    }
    if (value == null) {
      throw new IllegalArgumentException("Argument can not be null. key = " + key);
    }
  }

  public static int compareNum(Object num, String anotherNum) {
    try {
      if (num instanceof Integer) {
        return ((Integer) num).compareTo(Integer.valueOf(anotherNum));
      }
      if (num instanceof Long) {
        return ((Long) num).compareTo(Long.valueOf(anotherNum));
      }
      if (num instanceof Double) {
        return ((Double) num).compareTo(Double.valueOf(anotherNum));
      }
      if (num instanceof Float) {
        return ((Float) num).compareTo(Float.valueOf(anotherNum));
      }
    } catch (NumberFormatException e) {
      return 1;
    }
    return 1;
  }

  public Object getActual() {
    return actual;
  }

  public SupportedType getType() {
    return type;
  }
}
