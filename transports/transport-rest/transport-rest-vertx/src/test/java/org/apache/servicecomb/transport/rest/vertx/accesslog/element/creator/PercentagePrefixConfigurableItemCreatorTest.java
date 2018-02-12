package org.apache.servicecomb.transport.rest.vertx.accesslog.element.creator;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class PercentagePrefixConfigurableItemCreatorTest {
  private static final String PATTERN = "test %{EEE, dd MMM yyyy HH:mm:ss zzz}t %{VARNAME1}i %{VARNAME2}o %{VARNAME3}C";

  private static final PercentagePrefixConfigurableItemCreator CREATOR = new PercentagePrefixConfigurableItemCreator();

  @Test
  public void testCreateDatetimeConfigurableItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(5).setEnd(38).setPlaceHolder(
        AccessLogItemTypeEnum.DATETIME_CONFIGURABLE);

    AccessLogElement item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(DatetimeConfigurableElement.class, item.getClass());
    Assert.assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz", ((DatetimeConfigurableElement) item).getPattern());
  }

  @Test
  public void testCreateRequestHeaderItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(39).setEnd(51).setPlaceHolder(
        AccessLogItemTypeEnum.REQUEST_HEADER);

    AccessLogElement item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(RequestHeaderElement.class, item.getClass());
    Assert.assertEquals("VARNAME1", ((RequestHeaderElement) item).getIdentifier());
  }


  @Test
  public void testCreateResponseHeaderItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(52).setEnd(64).setPlaceHolder(
        AccessLogItemTypeEnum.RESPONSE_HEADER);

    AccessLogElement item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(ResponseHeaderElement.class, item.getClass());
    Assert.assertEquals("VARNAME2", ((ResponseHeaderElement) item).getIdentifier());
  }

  @Test
  public void testCreateCookieItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(65).setEnd(77).setPlaceHolder(
        AccessLogItemTypeEnum.COOKIE);

    AccessLogElement item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(CookieElement.class, item.getClass());
    Assert.assertEquals("VARNAME3", ((CookieElement) item).getIdentifier());
  }

  @Test
  public void testPlainTextItem() {
    AccessLogItemLocation location = new AccessLogItemLocation().setStart(0).setEnd(5)
        .setPlaceHolder(AccessLogItemTypeEnum.TEXT_PLAIN);

    AccessLogElement item = CREATOR.create(PATTERN, location);

    Assert.assertEquals(PlainTextElement.class, item.getClass());
    Assert.assertEquals("test ", ((PlainTextElement) item).getFormattedElement(null));
  }
}