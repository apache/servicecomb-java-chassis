package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogConfiguration;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class SimpleItemMatcherTest {

  private static final SimpleItemMatcher MATCHER = new SimpleItemMatcher();

  public static final String PATTERN = AccessLogConfiguration.DEFAULT_PATTERN;

  @Test
  public void testMatch() {
    AccessLogItemLocation location = MATCHER.match(PATTERN, 0);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(0)
            .setEnd(2)
            .setPlaceHolder(AccessLogItemTypeEnum.REMOTE_HOSTNAME));

    location = MATCHER.match(PATTERN, 7);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(7)
            .setEnd(9)
            .setPlaceHolder(AccessLogItemTypeEnum.DATETIME_DEFAULT));

    location = MATCHER.match(PATTERN, 10);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(10)
            .setEnd(12)
            .setPlaceHolder(AccessLogItemTypeEnum.FIRST_LINE_OF_REQUEST));

    location = MATCHER.match(PATTERN, 13);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(13)
            .setEnd(15)
            .setPlaceHolder(AccessLogItemTypeEnum.HTTP_STATUS));

    location = MATCHER.match(PATTERN, 16);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(16)
            .setEnd(18)
            .setPlaceHolder(AccessLogItemTypeEnum.RESPONSE_SIZE));
  }

  @Test
  public void testNotMatch() {
    AccessLogItemLocation location = MATCHER.match("notmatch", 0);
    Assert.assertNull(location);
  }
}