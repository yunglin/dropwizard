package com.yammer.dropwizard.jetty;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class MyAccessEvent extends AccessEvent {

  private String serverName;

  public MyAccessEvent(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServerAdapter adapter) {
    super(httpRequest, httpResponse, adapter);
  }

  public String getServerName() {
    if (serverName == null) {
      try {
        // damn jetty request would throw exception here.
        serverName = super.getServerName();
      } catch (IllegalStateException e) {
        serverName = NA;
      }

    }
    return serverName;
  }


}
