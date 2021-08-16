package org.apache.servicecomb.darklaunch.oper;


public interface Condition {
  String key();

  String expected();

  void setActual(String key, Object actual);

  boolean match();
}
