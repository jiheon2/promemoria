package kopo.poly.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Configuration
public class PreFlightCorsConfiguration {

    private static final String ALLOWED_HEADERS = "x-requested-with, authorization, Content-Type";
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS";

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "https://www.프로메모리아 프론트 서버",  // 프론트 서버
            "https://api.프로메모리아 게이트 웨이"   // 게이트 웨이 자기 자신(http로 써뒀어도 알아서 https로 가니까 괜찮음)
    );

    private static final String MAX_AGE = "3600";

    // true 로 설정할 경우 allowed_origin 에 '*' 을 입력할 수 없고,
    // http://localhost:3000 이렇게 특정해줘야 한다.
    private static final String ALLOWED_CREDENTIALS = "true";


    @Bean
    @Order(-1) // 우선 순위를 최상으로 지정
    public WebFilter corsFilter() {

        System.out.println(">>>>> preflight 전용 CORS 필터 실행");

        return (ServerWebExchange ctx, WebFilterChain chain) -> {

            ServerHttpRequest request = ctx.getRequest();

            // 사전요청일 경우에만 수행(PreFlight)
            if (CorsUtils.isPreFlightRequest(request)) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                String requestOrigin = request.getHeaders().getOrigin();
                String allowedOrigin = "";
                if (ALLOWED_ORIGINS.contains(requestOrigin)) {
                    allowedOrigin = requestOrigin;
                    headers.add("Access-Control-Allow-Origin", requestOrigin);
                } else {
                    // 허용되지 않은 경우 에러 응답 등을 처리
                }
                headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
                headers.add("Access-Control-Max-Age", MAX_AGE);
                headers.add("Access-Control-Allow-Headers",ALLOWED_HEADERS);
                headers.add("Access-Control-Allow-Credentials",ALLOWED_CREDENTIALS);

                System.out.println("허용된 origin : " + allowedOrigin);  // 이게 출력되지 않는 이유는 preflight 요청이 들어오지 않았기 때문

                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(ctx);
        };
    }
}