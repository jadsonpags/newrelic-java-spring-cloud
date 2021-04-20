package com.newrelic.labs;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceHeaderGlobalFilter implements GlobalFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    @Trace(dispatcher = true)
    public Mono<Void> filter (
            final ServerWebExchange exchange,
            final GatewayFilterChain chain
    ) {
        final ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        TraceOutboundWrapper outboundHeaders = new TraceOutboundWrapper(requestBuilder);

        final Token token = NewRelic.getAgent().getTransaction().getToken();
        NewRelic.getAgent().getTransaction().getTracedMethod().addOutboundRequestHeaders(outboundHeaders);

        final ServerHttpRequest originRequest = requestBuilder.build();
        final ServerWebExchange outboundExchange = exchange.mutate().request(originRequest).build();

        logger.info("Request URI {} with headers {}",outboundExchange.getRequest().getURI() , outboundExchange.getRequest().getHeaders());

        final Mono<Void> result = chain.filter(outboundExchange).then(Mono.fromRunnable(()->{
            exchange.getRequest().getHeaders().forEach((headerName, values) -> {
                logger.info("the request header is {} = {}",
                        headerName,
                        StringUtils.collectionToCommaDelimitedString(values));
            });

            ServerHttpResponse  serverHttpResponse = exchange.getResponse();
            token.link();
            serverHttpResponse.getHeaders().forEach((headerName, values) -> {
                logger.info("the response header is {} = {}",
                        headerName,
                        StringUtils.collectionToCommaDelimitedString(values));
            });
        }));

        token.expire();

        return result;
    }
}