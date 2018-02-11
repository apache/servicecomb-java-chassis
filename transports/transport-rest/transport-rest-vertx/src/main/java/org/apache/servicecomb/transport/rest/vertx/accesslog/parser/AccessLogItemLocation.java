package org.apache.servicecomb.transport.rest.vertx.accesslog.parser;

import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

import com.google.common.base.Objects;

public class AccessLogItemLocation {
  private int start;

  private int end;

  private AccessLogItemTypeEnum placeHolder;

  public int getStart() {
    return start;
  }

  public AccessLogItemLocation setStart(int start) {
    this.start = start;
    return this;
  }

  public int getEnd() {
    return end;
  }

  public AccessLogItemLocation setEnd(int end) {
    this.end = end;
    return this;
  }

  public AccessLogItemTypeEnum getPlaceHolder() {
    return placeHolder;
  }

  public AccessLogItemLocation setPlaceHolder(AccessLogItemTypeEnum placeHolder) {
    this.placeHolder = placeHolder;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AccessLogItemLocation{");
    sb.append("start=").append(start);
    sb.append(", end=").append(end);
    sb.append(", placeHolder=").append(placeHolder);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().isAssignableFrom(o.getClass())) {
      return false;
    }
    AccessLogItemLocation that = (AccessLogItemLocation) o;
    return start == that.start
        && end == that.end
        && placeHolder == that.placeHolder;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(start, end, placeHolder);
  }
}
