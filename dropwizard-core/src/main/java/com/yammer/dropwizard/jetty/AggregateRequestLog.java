package com.yammer.dropwizard.jetty;

import ch.qos.logback.access.jetty.JettyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import java.util.Iterator;

/**
 */
public class AggregateRequestLog extends AbstractLifeCycle implements RequestLog {

    private final AppenderAttachableImpl<IAccessEvent> appenders;

    public AggregateRequestLog(AppenderAttachableImpl<IAccessEvent> appenders) {
        this.appenders = appenders;
    }


    @Override
    protected void doStart() throws Exception {
        final Iterator<Appender<IAccessEvent>> iterator = appenders.iteratorForAppenders();
        while (iterator.hasNext()) {
            iterator.next().start();
        }
    }

    @Override
    protected void doStop() throws Exception {
        final Iterator<Appender<IAccessEvent>> iterator = appenders.iteratorForAppenders();
        while (iterator.hasNext()) {
            iterator.next().stop();
        }
    }

    @Override
    public void log(Request request, Response response) {
      final JettyServerAdapter serverAdapter = new JettyServerAdapter(request, response);
      final AccessEvent event = new AccessEvent(request, response, serverAdapter);

      appenders.appendLoopOnAppenders(event);
    }
}
