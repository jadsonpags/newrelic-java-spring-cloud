package com.newrelic.labs;


import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.InboundHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;

class InboundWrapper implements InboundHeaders {
    private static Logger logger = LoggerFactory.getLogger(InboundWrapper.class);
    private ServerHttpResponse response;
    public InboundWrapper(ServerHttpResponse response) {
        this.response = response;
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.HTTP;
    }

    @Override
    public String getHeader(String name) {
        String value = this.response.getHeaders().getFirst(name);
        return value;
    }
}
