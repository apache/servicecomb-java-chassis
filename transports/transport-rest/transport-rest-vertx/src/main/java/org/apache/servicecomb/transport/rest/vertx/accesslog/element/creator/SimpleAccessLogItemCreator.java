package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationMillisecondElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationSecondElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.FirstLineOfRequestElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalHostElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalPortElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.MethodElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryOnlyElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RemoteHostElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.StatusElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.TraceIdElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathIncludeQueryElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathOnlyElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.VersionOrProtocolElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

public class SimpleAccessLogItemCreator implements AccessLogItemCreator {
  private static final Map<AccessLogItemTypeEnum, AccessLogElement> SIMPLE_ACCESSLOG_ITEM_MAP = new HashMap<>();

  static {
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.HTTP_METHOD, new MethodElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.HTTP_STATUS, new StatusElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.DURATION_IN_SECOND, new DurationSecondElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.DURATION_IN_MILLISECOND, new DurationMillisecondElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.REMOTE_HOSTNAME, new RemoteHostElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.LOCAL_HOSTNAME, new LocalHostElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.LOCAL_PORT, new LocalPortElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.RESPONSE_SIZE, new BytesWrittenElement("0"));
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.RESPONSE_SIZE_CLF, new BytesWrittenElement("-"));
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.FIRST_LINE_OF_REQUEST, new FirstLineOfRequestElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.URL_PATH, new UriPathOnlyElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.QUERY_STRING, new QueryOnlyElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.URL_PATH_WITH_QUERY, new UriPathIncludeQueryElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.REQUEST_PROTOCOL, new VersionOrProtocolElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.DATETIME_DEFAULT, new DatetimeConfigurableElement());
    SIMPLE_ACCESSLOG_ITEM_MAP.put(AccessLogItemTypeEnum.SCB_TRACE_ID, new TraceIdElement());
  }

  @Override
  public AccessLogElement create(String rawPattern, AccessLogItemLocation location) {
    return SIMPLE_ACCESSLOG_ITEM_MAP.get(location.getPlaceHolder());
  }
}
