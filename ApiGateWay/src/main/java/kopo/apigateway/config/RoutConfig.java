package kopo.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class RoutConfig {

    // 게이트웨이로 접근되는 모든 요청에 대해 URL 분리하기
    @Bean
    public RouteLocator getewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()
                .route(r -> r.path("/user/v1/**").uri("http://user-service.kopo-trainee20.svc.cluster.local:13000"))            // 유저 관리 서버 주소
                .route(r -> r.path("/quiz/v1/**").uri("http://prevention-service.kopo-trainee20.svc.cluster.local:14000"))
                .route(r -> r.path("/facility/v1/**").uri("http://prevention-service.kopo-trainee20.svc.cluster.local:14000"))
                .route(r -> r.path("/analyze/v1/**").uri("http://analyze-service.kopo-trainee20.svc.cluster.local:15000"))  // 분석 서버 주소
                .route(r -> r.path("/friend/v1/**").uri("http://user-service.kopo-trainee20.svc.cluster.local:13000"))

//                .route(r -> r.path("/signaling/**")  // 화상채팅(웹 소켓 - 시그널링 서버)
//                        .filters(f -> f.addRequestHeader("Connection", "Upgrade")   // WebSocket 업그레이드 처리
//                                .addRequestHeader("Upgrade", "websocket")
//                                .addResponseHeader("Access-Control-Allow-Origin", "프로메모리아 프론트 서버")  // CORS 설정
//                                .addResponseHeader("Access-Control-Allow-Credentials", "true"))          // 필요 시 추가 (쿠키 허용)
//                        .uri("ws://VIDEO-CHAT-SERVICE")  // 화상 채팅 서버 주소
//                )
                .build();
    }


}
