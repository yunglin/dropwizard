package com.yammer.dropwizard.logging;

import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;

import org.eclipse.jetty.server.Request;

/**
 *
 */
public class ResponseTimeConverter extends AccessConverter {
  @Override
  public String convert(IAccessEvent event) {
    if (event.getRequest() instanceof Request) {
      Request jettyRequest = (Request) event.getRequest();
      if (jettyRequest.getDispatchTime() > 0) {
        long elapse = event.getTimeStamp() - jettyRequest.getDispatchTime();
        return String.valueOf(elapse);
      }
    }
    return "-";
  }
}
