package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogConfiguration;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class SimpleItemMatcherTest {

  private static final SimpleItemMatcher MATCHER = new SimpleItemMatcher();

  public static final String PATTERN = "%h - - %t %r %s %B";

  @Test
  public void testMatch() {
    AccessLogItemLocation location = MATCHER.match(PATTERN, 0);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(0)
            .setEnd(2)
            .setPlaceHolder(AccessLogItemTypeEnum.REMOTE_HOSTNAME));

    location = MATCHER.match(PATTERN, 3);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(7)
            .setEnd(9)
            .setPlaceHolder(AccessLogItemTypeEnum.DATETIME_DEFAULT));

    location = MATCHER.match(PATTERN, 17);
    Assert.assertNull(location);
  }

  @Test
  public void testNotMatch() {
    AccessLogItemLocation location = MATCHER.match("notmatch", 0);
    Assert.assertNull(location);
  }
}