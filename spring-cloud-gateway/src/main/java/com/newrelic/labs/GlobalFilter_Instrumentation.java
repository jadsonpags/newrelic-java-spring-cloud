package com.newrelic.labs;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

import reactor.core.publisher.Mono;

@Weave(type = MatchType.Interface, originalName="org.springframework.cloud.gateway.filter.GlobalFilter")
public abstract class GlobalFilter_Instrumentation {

	@Trace(dispatcher = true, async = true)
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		final Token token = NewRelic.getAgent().getTransaction().getToken();

		final ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
		TraceOutboundWrapper outboundHeaders = new TraceOutboundWrapper(requestBuilder);

		NewRelic.getAgent().getTransaction().getTracedMethod().addOutboundRequestHeaders(outboundHeaders);

		final ServerHttpRequest originRequest = requestBuilder.build();
		final ServerWebExchange outboundExchange = exchange.mutate().request(originRequest).build();


		final Mono<Void> result = chain.filter(outboundExchange).then(Mono.fromRunnable(()->{
			token.link();
		}));

		token.expire();

		return result;
	}
}
