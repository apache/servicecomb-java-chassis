package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class PercentagePrefixConfigurableMatcherTest {

  public static final PercentagePrefixConfigurableMatcher MATCHER = new PercentagePrefixConfigurableMatcher();

  public static final String TEST_RAW_PATTERN = "%{pattern}t %{test pattern}C %{test pattern}t";

  @Test
  public void testMatch() {
    AccessLogItemLocation location;
    location = MATCHER.match(TEST_RAW_PATTERN, 0);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(0)
            .setEnd(11)
            .setPlaceHolder(AccessLogItemTypeEnum.DATETIME_CONFIGURABLE));

    location = MATCHER.match(TEST_RAW_PATTERN, 12);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(12)
            .setEnd(28)
            .setPlaceHolder(AccessLogItemTypeEnum.COOKIE));

    location = MATCHER.match(TEST_RAW_PATTERN, 29);
    Assert.assertEquals(
        location,
        new AccessLogItemLocation()
            .setStart(29)
            .setEnd(45)
            .setPlaceHolder(AccessLogItemTypeEnum.DATETIME_CONFIGURABLE));
  }

  @Test
  public void testNotMatch() {
    AccessLogItemLocation location = MATCHER.match("notmatch", 0);
    Assert.assertNull(location);
  }

  @Test
  public void testNotMatchWithPrefix() {
    AccessLogItemLocation location = MATCHER.match("%{notmatch}x", 0);
    Assert.assertNull(location);
  }
}