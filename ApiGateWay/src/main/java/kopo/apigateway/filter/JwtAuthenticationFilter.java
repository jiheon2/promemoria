package kopo.apigateway.filter;

import kopo.apigateway.dto.TokenDTO;
import kopo.apigateway.jwt.JwtStatus;
import kopo.apigateway.jwt.JwtTokenProvider;
import kopo.apigateway.jwt.JwtTokenType;
import kopo.apigateway.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Slf4j
@Component  // 필터도 스프링에서 관리 가능
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    @Value("${jwt.token.access.valid.time}")
    private long accessTokenValidTime;

    @Value("${jwt.token.access.name}")
    private String accessTokenName;

    // JWT Token 객체
    private final JwtTokenProvider jwtTokenProvider;


    /**
     * 쿠키에 저장된 JWT 토큰 삭제 구조 정의
     *
     * @param tokenName  토큰 이름
     * @return           쿠키 구조
     */
    public ResponseCookie deleteTokenCookie(String tokenName) {

        log.info(this.getClass().getName() + ".쿠키에 저장된 JWT 토큰 삭제(deleteTokenCookie) Start!");

        log.info("tokenName : " + tokenName);

        ResponseCookie cookie = ResponseCookie.from(tokenName, "")
                .maxAge(0).build();

        log.info(this.getClass().getName() + ".쿠키에 저장된 JWT 토큰 삭제(deleteTokenCookie) End!");

        return cookie;
    }

    /**
     * 쿠키에 저장할 JWT 구조 정의
     *
     * @param tokenName      토큰 이름
     * @param tokenValidTime 토큰 유효시간
     * @param token          저장할 토큰
     * @return               쿠키 구조
     */

    public ResponseCookie createTokenCookie(String tokenName, long tokenValidTime, String token) {

        log.info(this.getClass().getName() + "생성된 토큰 쿠키에 저장(createTokenCookie) Start!");

        log.info("tokenName : " + tokenName);
        log.info("token : " + token);

        ResponseCookie cookie = ResponseCookie.from(tokenName, token)
//                .domain("kopo-traine-front-servic-059de-100291067-f4b8e239e81a.kr.lb.naverncp.com")
                .domain("kopo-traine-front-servic-059de-100291067-f4b8e239e81a.kr.lb.naverncp.com")
                .path("/")
                .maxAge(tokenValidTime)
//                .httpOnly(true)
                .build();

        log.info(this.getClass().getName() + "생성된 토큰 쿠키에 저장(createTokenCookie) End!");

        return cookie;

    }


    // 모든 API 요청에 대해 처리할 Filter 내용 정의
    // 게이트웨이로 요청 들어오면 제일 먼저 실행되는 함수
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request =  exchange.getRequest();
        ServerHttpResponse response =  exchange.getResponse();

        log.info(this.getClass().getName() + ".Filter Start!");

        log.info("request : " + request);
        log.info("들어온 요청 url(request) : " + request.getPath());

        // 쿠키 or HTTP 인증 헤더에서 Access Token 가져오기 -> 2번째로 jwtTokenProvider의 resolveToken 실행
        String accessToken = CmmUtil.nvl(jwtTokenProvider.resolveToken(request, JwtTokenType.ACCESS_TOKEN));

        // jwtTokenProvider의 resolveToken 작업 끝나면 이어서 실행
        log.info("access token : " + accessToken);

        // Access Token 유효기간 검증하기
        JwtStatus accessTokenStatus = jwtTokenProvider.validateToken(accessToken);

        log.info("access token status : " + accessTokenStatus);

        // 유효기간 검증하기
        if(accessTokenStatus == JwtStatus.ACCESS) {

            // 토큰이 유효하면, 토큰으로부터 사용자의 정보를 받아온다.
            // 받은 정보 : soyoung123 아이디의 권한을 Spring Security에 저장함
            Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

            // Security Context에 Authentication 객체를 저장한다.
            // 사용자 ID를 헤더에 추가
            String userId = authentication.getName();  // 또는 jwtTokenProvider.getUserId(accessToken)

            // 기존 요청을 새 요청으로 빌드하면서 사용자 ID를 헤더에 추가
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", userId)
                    .build();

            // Security Context에 Authentication 객체를 저장한다.
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } else if (accessTokenStatus == JwtStatus.DENIED || accessTokenStatus == JwtStatus.EXPIRED) {

            // Access Token이 만료되면, Refresh Token 유효한지 체크
            String refreshToken = CmmUtil.nvl(jwtTokenProvider.resolveToken(request, JwtTokenType.REFRESH_TOKEN));

            // Refresh Token 유효기간 검증
            JwtStatus refreshTokenStatus = jwtTokenProvider.validateToken(refreshToken);

            log.info("refresh token status : " + refreshTokenStatus);

            // Refresh Token이 유효하면, Access Token 재발급
            if(refreshTokenStatus == JwtStatus.ACCESS) {

                // Refresh Token에 저장된 정보 가져오기
                TokenDTO rDTO = Optional.ofNullable(jwtTokenProvider.getTokenInfo(refreshToken))
                        .orElseGet(() -> TokenDTO.builder().build());

                String userId = CmmUtil.nvl(rDTO.userId());
                String userRoles = CmmUtil.nvl(rDTO.role());

                log.info("refresh Token_userID : " + userId);
                log.info("refresh token_userRoles : " + userRoles);

                // Access Token 재발급
                String reAccessToken = jwtTokenProvider.createToken(userId, userRoles);

                // 만약, 기존 존재하는 Access Token있다면 삭제(deleteTokenCookie 반환값이 공백이라서 초기화 시키는 작업)
                response.addCookie(this.deleteTokenCookie(accessTokenName));

                // 재발급된 Access Token을 쿠키에 저장
                response.addCookie(this.createTokenCookie(accessTokenName, accessTokenValidTime, reAccessToken));

                // 토큰이 유효하면 토큰으로부터 유저 정보를 받아온다.
                // 받은 유저의 정보는 Spring Security에 저장
                Authentication authentication = jwtTokenProvider.getAuthentication(reAccessToken);

                // 사용자 ID를 헤더에 추가
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-ID", userId)
                        .build();

                // Spring Security 인증 토큰을 Spring Security 객체에 저장
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }
            // Refresh Token 만료 또는 오휴는 별도의 처리 로직이 필요 없음.
            // Jwt Filter에서 Spring Security 인증 정보를 저장하지 않으면, 그 뒤에 어차피 Spring Security가 에러 처리 진행함
            // 그래서 로그만 찍는다!
            else if (refreshTokenStatus == JwtStatus.EXPIRED) {
                log.info("Refresh Token 만료 - 스프링 시큐리티가 로그인 페이지로 이동 시킵니다.");
            } else {
                log.info("Refresh Token 오류 - 스프링 시큐리티가 로그인 페이지로 이동 시킵니다.");
            }

        }

        log.info(this.getClass().getName() + ".Filter End!");


        return chain.filter(exchange);
    }

}
