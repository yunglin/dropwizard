package com.yammer.dropwizard.logging;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.google.common.base.Optional;

import java.util.TimeZone;

import static com.yammer.dropwizard.config.LoggingConfiguration.ConsoleConfiguration;
import static com.yammer.dropwizard.config.LoggingConfiguration.FileConfiguration;

public class LogbackAccessFactory {
  private LogbackAccessFactory() { /* singleton */ }

  private static class RequestLogLayout extends PatternLayout {

    public RequestLogLayout(TimeZone timeZone) {
      getDefaultConverterMap().put("elapse", ResponseTimeConverter.class.getName());
      setPattern("%h %l %u [%t{dd/MMM/yyyy:HH:mm:ss Z," + timeZone.getID() + "}] \"%r\" %s %b");
    }
  }

  public static FileAppender<IAccessEvent> buildFileAppender(FileConfiguration file,
                                                             LoggerContext context,
                                                             Optional<String> logFormat) {
    final RequestLogLayout formatter = new RequestLogLayout(file.getTimeZone());
    formatter.setContext(context);
    for (String format : logFormat.asSet()) {
      formatter.setPattern(format);
    }
    formatter.start();

    final FileAppender<IAccessEvent> appender =
        file.isArchive() ? new RollingFileAppender<IAccessEvent>() :
            new FileAppender<IAccessEvent>();

    appender.setAppend(true);
    appender.setContext(context);
    appender.setLayout(formatter);
    appender.setFile(file.getCurrentLogFilename());
    appender.setPrudent(false);

    if (file.isArchive()) {

      final DefaultTimeBasedFileNamingAndTriggeringPolicy<IAccessEvent> triggeringPolicy =
          new DefaultTimeBasedFileNamingAndTriggeringPolicy<IAccessEvent>();
      triggeringPolicy.setContext(context);

      final TimeBasedRollingPolicy<IAccessEvent> rollingPolicy = new TimeBasedRollingPolicy<IAccessEvent>();
      rollingPolicy.setContext(context);
      rollingPolicy.setFileNamePattern(file.getArchivedLogFilenamePattern());
      rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
          triggeringPolicy);
      triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
      rollingPolicy.setMaxHistory(file.getArchivedFileCount());

      ((RollingFileAppender<IAccessEvent>) appender).setRollingPolicy(rollingPolicy);
      ((RollingFileAppender<IAccessEvent>) appender).setTriggeringPolicy(triggeringPolicy);

      rollingPolicy.setParent(appender);
      rollingPolicy.start();
    }

    appender.stop();
    appender.start();

    return appender;
  }

  public static ConsoleAppender<IAccessEvent> buildConsoleAppender(ConsoleConfiguration console,
                                                                   LoggerContext context,
                                                                   Optional<String> logFormat) {
    final RequestLogLayout formatter = new RequestLogLayout(console.getTimeZone());
    formatter.setContext(context);
    for (String format : logFormat.asSet()) {
      formatter.setPattern(format);
    }
    formatter.start();

    final ConsoleAppender<IAccessEvent> appender = new ConsoleAppender<IAccessEvent>();
    appender.setContext(context);
    appender.setLayout(formatter);
    appender.start();

    return appender;
  }

}
