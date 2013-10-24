package com.yammer.dropwizard.config;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.yammer.dropwizard.jetty.AggregateRequestLog;
import com.yammer.dropwizard.logging.LogbackAccessFactory;

import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.slf4j.LoggerFactory;

import static com.yammer.dropwizard.config.LoggingConfiguration.ConsoleConfiguration;
import static com.yammer.dropwizard.config.LoggingConfiguration.FileConfiguration;

// TODO: 11/7/11 <coda> -- document RequestLogHandlerFactory
// TODO: 11/7/11 <coda> -- test RequestLogHandlerFactory

public class RequestLogHandlerFactory {

    private final RequestLogConfiguration config;
    private final String name;

    public RequestLogHandlerFactory(RequestLogConfiguration config, String name) {
        this.config = config;
        this.name = name;
    }
    
    public boolean isEnabled() {
        return config.getConsoleConfiguration().isEnabled() ||
                config.getFileConfiguration().isEnabled() ||
                config.getSyslogConfiguration().isEnabled();
    }

    public RequestLogHandler build() {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);
        final LoggerContext context = logger.getLoggerContext();

        final AppenderAttachableImpl<IAccessEvent> appenders = new AppenderAttachableImpl<IAccessEvent>();

        final ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            final ConsoleAppender<IAccessEvent> appender = LogbackAccessFactory.buildConsoleAppender(console,
                context,
                console.getLogFormat());
            appender.start();
            appenders.addAppender(appender);
        }

        final FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            final FileAppender<IAccessEvent> appender = LogbackAccessFactory.buildFileAppender(file,
                                                                                          context,
                                                                                          file.getLogFormat());

            appender.start();
            appenders.addAppender(appender);
        }

        LoggingConfiguration.SyslogConfiguration syslogConfiguration = config.getSyslogConfiguration();
        if (syslogConfiguration.isEnabled()) {
            throw new UnsupportedOperationException("syslog config for access log is not supported yet.");
        }

        final RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(new AggregateRequestLog(appenders));

        return handler;
    }
}
