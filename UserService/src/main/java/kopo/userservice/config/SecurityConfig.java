package kopo.userservice.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.token.access.name}")
    private String accessTokenName;

    @Value("${jwt.token.refresh.name}")
    private String refreshTokenName;

    @Bean
    public PasswordEncoder passwordEncoder() {

        log.info("Password Encoder 실행 : {}", this.getClass().getName());

        return new BCryptPasswordEncoder();

    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("Filter Chain 실행 : {}", this.getClass().getName());

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(login -> login
                        .loginPage("/user/login.html") // 로그인 페이지 html
                        .loginProcessingUrl("/user/v1/loginProc") // 로그인 수행
                        .usernameParameter("userId")
                        .passwordParameter("userPw")
                        .successForwardUrl("/user/v1/loginSuccess") // 로그인 성공 URL
                        .failureForwardUrl("/user/v1/loginFail") // 로그인 실패 URL
                ).logout(logout -> logout
                        .logoutUrl("/user/v1/logout") // 로그아웃 요청 URL
                        .clearAuthentication(true)
                        .deleteCookies(accessTokenName, refreshTokenName)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            log.info("로그아웃 성공");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json; charset=UTF-8");
                            response.getWriter().write("{\"result\": \"1\", \"msg\": \"로그아웃 되었습니다.\"}");
                            response.getWriter().flush();
                        })
                ).sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // CORS 설정을 위한 Bean 추가
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:12000"); // 허용할 출처 패턴
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
