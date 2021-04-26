package com.newrelic.labs;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.OutboundHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class TraceOutboundWrapper implements OutboundHeaders {
    private static Logger logger = LoggerFactory.getLogger(TraceOutboundWrapper.class);
    private ServerHttpRequest.Builder requestBuilder;
    public TraceOutboundWrapper(ServerHttpRequest.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.HTTP;
    }

    @Override
    public void setHeader(String name, String value) {
        this.requestBuilder.header(name, value);
    }

}
