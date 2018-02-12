package org.apache.servicecomb.transport.rest.vertx.accesslog.element;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryOnlyElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathIncludeQueryElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class AccessLogItemFactoryTest {
  private static final String PATTERN = "test %{EEE, dd MMM yyyy HH:mm:ss zzz}t cs-uri-query cs-uri %{VARNAME1}i";

  private static final List<AccessLogItemLocation> locationList = Arrays.asList(
      new AccessLogItemLocation().setStart(0).setEnd(5).setPlaceHolder(AccessLogItemTypeEnum.TEXT_PLAIN),
      new AccessLogItemLocation().setStart(5).setEnd(38).setPlaceHolder(AccessLogItemTypeEnum.DATETIME_CONFIGURABLE),
      new AccessLogItemLocation().setStart(39).setEnd(51).setPlaceHolder(AccessLogItemTypeEnum.QUERY_STRING),
      new AccessLogItemLocation().setStart(52).setEnd(58).setPlaceHolder(AccessLogItemTypeEnum.URL_PATH_WITH_QUERY),
      new AccessLogItemLocation().setStart(59).setEnd(71).setPlaceHolder(AccessLogItemTypeEnum.REQUEST_HEADER)
  );

  @Test
  public void testCreateAccessLogItem() {
    List<AccessLogElement> itemList = new AccessLogItemFactory().createAccessLogItem(PATTERN, locationList);
    Assert.assertEquals(5, itemList.size());
    Assert.assertEquals(PlainTextElement.class, itemList.get(0).getClass());
    Assert.assertEquals(DatetimeConfigurableElement.class, itemList.get(1).getClass());
    Assert.assertEquals(QueryOnlyElement.class, itemList.get(2).getClass());
    Assert.assertEquals(UriPathIncludeQueryElement.class, itemList.get(3).getClass());
    Assert.assertEquals(RequestHeaderElement.class, itemList.get(4).getClass());
  }
}