package com.yammer.dropwizard.jetty.tests;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.PatternLayoutEncoder;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.yammer.dropwizard.jetty.AggregateRequestLog;
import com.yammer.metrics.core.Clock;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.UserIdentity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.security.Principal;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class AsyncRequestLogTest {
    private final Clock clock = mock(Clock.class);
    @SuppressWarnings("unchecked")
    private final Appender<IAccessEvent> appender = mock(Appender.class);
    private final AppenderAttachableImpl<IAccessEvent> appenders = new AppenderAttachableImpl<IAccessEvent>();
    private final AggregateRequestLog asyncRequestLog = new AggregateRequestLog(appenders);

    private final Request request = mock(Request.class);
    private final Response response = mock(Response.class);
    private final AsyncContinuation continuation = mock(AsyncContinuation.class);

    private final PatternLayout layout = new PatternLayout();
    private final PatternLayoutEncoder encoder = new PatternLayoutEncoder();

    @Before
    public void setUp() throws Exception {

        when(continuation.isInitial()).thenReturn(true);

        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getTimeStamp()).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
        when(request.getMethod()).thenReturn("GET");
        when(request.getUri()).thenReturn(new HttpURI("/test/things?yay"));
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getAsyncContinuation()).thenReturn(continuation);
        when(request.getDispatchTime()).thenReturn(TimeUnit.SECONDS.toMillis(1353042048));

        when(response.getStatus()).thenReturn(200);
        when(response.getContentCount()).thenReturn(8290L);

        when(clock.time()).thenReturn(TimeUnit.SECONDS.toMillis(1353042049));

        appenders.addAppender(appender);

        asyncRequestLog.start();
        layout.start();
    }

    @After
    public void tearDown() throws Exception {
        if (asyncRequestLog.isRunning()) {
            asyncRequestLog.stop();
        }
    }

    @Test
    public void startsAndStops() throws Exception {
        asyncRequestLog.stop();

        verify(appender, timeout(1000)).stop();
    }

    @Test
    public void logsRequests() throws Exception {
//        final IAccessEvent event = logAndCapture();

//        assertThat(layout.doLayout(event))
//                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 1000 2000");
    }

    @Test
    public void logsForwardedFor() throws Exception {
        when(request.getHeader(HttpHeaders.X_FORWARDED_FOR)).thenReturn("123.123.123.123");

//        final IAccessEvent event = logAndCapture();
//      assertThat(layout.doLayout(event))
//                .isEqualTo("123.123.123.123 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 1000 2000");
    }

    @Test
    public void logsPrincipal() throws Exception {
        final Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("coda");

        final UserIdentity identity = mock(UserIdentity.class);
        when(identity.getUserPrincipal()).thenReturn(principal);

        final Authentication.User user = mock(Authentication.User.class);
        when(user.getUserIdentity()).thenReturn(identity);

        when(request.getAuthentication()).thenReturn(user);

//        final IAccessEvent event = logAndCapture();
//        assertThat(layout.doLayout(event))
//                .isEqualTo("10.0.0.1 - coda [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" 200 8290 1000 2000");
    }

    @Test
    public void logsAsyncContinuations() throws Exception {
        when(continuation.isInitial()).thenReturn(false);

//        final IAccessEvent event = logAndCapture();

//        assertThat(layout.doLayout(event))
//                .isEqualTo("10.0.0.1 - - [16/Nov/2012:05:00:47 +0000] \"GET /test/things?yay HTTP/1.1\" Async 8290 1000 2000");
    }

    private IAccessEvent logAndCapture() {
        asyncRequestLog.log(request, response);

        final ArgumentCaptor<IAccessEvent> captor = ArgumentCaptor.forClass(IAccessEvent.class);
        verify(appender, timeout(1000)).doAppend(captor.capture());

        return captor.getValue();
    }
}
