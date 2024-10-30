package kopo.poly.apigateway.config;

import kopo.poly.apigateway.filter.JwtAuthenticationFilter;
import kopo.poly.apigateway.handler.AccessDeniedHandler;
import kopo.poly.apigateway.handler.LoginServerAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;


@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AccessDeniedHandler accessDeniedHandler;  // 인증 에러 처리(토큰에 값이 없는 경우)
    private final LoginServerAuthenticationEntryPoint loginServerAuthenticationEntryPoint; // 인가 에러 처리(권한이 없는 경우)
    // JWT을 통한 인증 방식을 사용하는 필터(JwtAuthenticationFilter)
    // 초기 Spring Filter는 Spring에 제어가 불가능했지만, 현재는 제어가 가능함
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 인증, 인가 설정 정의
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {

        log.info(this.getClass().getName() + ".filterChain Start!");

        http.csrf(ServerHttpSecurity.CsrfSpec::disable);  // POST 방식 전송을 위해 csrf 막기
        http.cors(ServerHttpSecurity.CorsSpec::disable);  // CORS 사용하지 않음

        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable); // 로그인 기능 사용하지 않음

        // 인증 에러 처리
        http.exceptionHandling(exceptionHandlingSpec ->
                exceptionHandlingSpec.accessDeniedHandler(accessDeniedHandler));

        // 인가 에러 처리
        http.exceptionHandling(exceptionHandlingSpec ->
                exceptionHandlingSpec.authenticationEntryPoint(loginServerAuthenticationEntryPoint));

        // stateless 방식의 애플리케이션이 되도록 설정
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        // 페이지 접속 권한 설정(허용된 사용자만 해당 페이지에 접속 가능)
        // 요청 API(url)마다 권한을 다르게 설정함
        http.authorizeExchange(authz -> authz
                .pathMatchers("/videoChat/v1/**").permitAll()   // 화상채팅
                .pathMatchers("/analyze/v1/**").permitAll()     // 분석 서비스
                .pathMatchers("/prevention/v1/**").permitAll()  // 예방 서비스
                .pathMatchers("/user/v1/**").permitAll()        // 유저 서비스
                .pathMatchers("/join/v1/**").permitAll()        // 회원가입
                .pathMatchers("/ss/**").permitAll()  // Spring Security 로그인
                .anyExchange().permitAll()
        );

        http.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC);

        log.info(this.getClass().getName() + ".filterChain End!");

        return http.build();
    }
}
