package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(UriConfigutation.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator buildRouteLocatorBean(RouteLocatorBuilder builder, UriConfigutation uriConfigutation) {

        String httpUri = uriConfigutation.getHttpbin();

        return builder.routes()
                .route(predicateSpec -> predicateSpec.path("/get")
                        .filters(gatewayFilterSpec ->
                                gatewayFilterSpec.addRequestHeader("Hello", "World"))
						.uri(httpUri))

                .route(predicateSpec -> predicateSpec
                        .host("*.circuitbreaker.com")
                        .filters(gatewayFilterSpec -> gatewayFilterSpec
                                .circuitBreaker(config -> config
                                        .setName("myCmd")
                                        .setFallbackUri("forward:/fallback")))
                        .uri(httpUri))

                .build();
    }

    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }
}

@ConfigurationProperties
class UriConfigutation {
    private String httpbin = "http://httpbin.org:80";

    public String getHttpbin() {
        return httpbin;
    }

    public void setHttpbin(String httpbin) {
        this.httpbin = httpbin;
    }
}
