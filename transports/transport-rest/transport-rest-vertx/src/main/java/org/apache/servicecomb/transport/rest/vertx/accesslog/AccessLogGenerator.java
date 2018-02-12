package org.apache.servicecomb.transport.rest.vertx.accesslog;

import java.util.List;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItemFactory;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;

/**
 * Accept {@link AccessLogParam} and generate access log.
 * <br/>
 * Each AccessLogParam for a line of access log.
 */
public class AccessLogGenerator {
  /**
   * traversal this array to generate access log segment.
   */
  private AccessLogItem[] accessLogItems;

  private AccessLogItemFactory accessLogItemFactory = new AccessLogItemFactory();

  public AccessLogGenerator(String rawPattern, AccessLogPatternParser accessLogPatternParser) {
    List<AccessLogItemLocation> locationList = accessLogPatternParser.parsePattern(rawPattern);

    List<AccessLogItem> itemList = accessLogItemFactory.createAccessLogItem(rawPattern, locationList);
    accessLogItems = new AccessLogItem[itemList.size()];
    accessLogItems = itemList.toArray(accessLogItems);
  }

  public String generateLog(AccessLogParam accessLogParam) {
    StringBuilder log = new StringBuilder(128);
    accessLogParam.setEndMillisecond(System.currentTimeMillis());

    AccessLogItem[] accessLogItems = getAccessLogItems();
    for (int i = 0; i < accessLogItems.length; ++i) {
      log.append(accessLogItems[i].getFormattedItem(accessLogParam));
    }

    return log.toString();
  }


  private AccessLogItem[] getAccessLogItems() {
    return accessLogItems;
  }
}
