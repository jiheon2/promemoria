package kopo.userservice.config;

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
                        .logoutSuccessUrl("http://localhost:11000/user/login.html") // 로그아웃 처리 URL

                ).sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
