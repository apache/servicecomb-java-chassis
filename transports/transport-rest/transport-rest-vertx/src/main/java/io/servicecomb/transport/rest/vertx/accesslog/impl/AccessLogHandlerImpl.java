package io.servicecomb.transport.rest.vertx.accesslog.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogHandler;
import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import io.vertx.ext.web.RoutingContext;

public class AccessLogHandlerImpl implements AccessLogHandler {
  private static Logger LOGGER = LoggerFactory.getLogger("accesslog");

  private static AccessLogElement[] accessLogElements;

  public AccessLogHandlerImpl(String rowLogPattern, AccessLogPatternParser accessLogPatternParser) {
    List<AccessLogElementExtraction> extractionList = accessLogPatternParser.parsePattern(rowLogPattern);

    accessLogElements = new AccessLogElement[extractionList.size()];
    for (int i = 0; i < extractionList.size(); ++i) {
      accessLogElements[i] = extractionList.get(i).getAccessLogElement();
    }
  }

  @Override
  public void handle(RoutingContext context) {
    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(System.currentTimeMillis())
        .setRoutingContext(context);

    context.addBodyEndHandler(v -> log(accessLogParam));

    context.next();
  }

  private void log(AccessLogParam accessLogParam) {
    StringBuilder log = new StringBuilder(128);
    accessLogParam.setEndMillisecond(System.currentTimeMillis());

    AccessLogElement[] accessLogElements = getAccessLogElements();
    for (int i = 0; i < accessLogElements.length; ++i) {
      log.append(accessLogElements[i].getFormattedElement(accessLogParam));
    }

    LOGGER.info(log.toString());
  }

  private AccessLogElement[] getAccessLogElements() {
    return accessLogElements;
  }
}
