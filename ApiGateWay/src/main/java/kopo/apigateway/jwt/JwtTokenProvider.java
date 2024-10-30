package kopo.apigateway.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kopo.apigateway.dto.TokenDTO;
import kopo.apigateway.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.token.creator}")
    private String creator;

    @Value("${jwt.token.access.valid.time}")
    private long accessTokenValidTime;

    @Value("${jwt.token.access.name}")
    private String accessTokenName;

    @Value("${jwt.token.refresh.valid.time}")
    private long refreshTokenValidTime;

    @Value("${jwt.token.refresh.name}")
    private String refreshTokenName;

    public static final String HEADER_PREFIX = "Bearer";  // Bearer 토큰 사용을 위한 선언

    /**
     * #  토큰 생성하기
     * JWT 토큰(Access Token, Refresh Token) 생성
     *
     * @param userId    회원 아이디(ex: soyoung123)
     * @param roles     회원 권한
     * @return          인증 처리한 정보(로그인 성공, 실패)
     */
    public String createToken(String userId, String roles) {

        log.info(this.getClass().getName() + ".토큰 생성(createToken) 시작!");

        log.info("회원 ID : " + userId);

        Claims claims = Jwts.claims()
                        .setIssuer(creator)  // JWT 토큰 생성자 기입
                        .setSubject(userId); // 회원 아이디 저장

        claims.put("roles", roles); // JWT Payload에 정의된 기본 옵션 외 정보를 추가 - 사용자 권한 추가
        Date now = new Date();

        log.info(this.getClass().getName() + ".토큰 생성(createToken) 종료!");

        // 보안키 문자들을 JWT Key 형태로 변경하기
         SecretKey secret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

        // Builder를 통해 토큰 생성
        return Jwts.builder()
                .setClaims(claims) // 정보 저장
                .setIssuedAt(now)  // 토큰 발행 시간
                .setExpiration(new Date(now.getTime() + (accessTokenValidTime * 1000)))  // Set Expire Time
                .signWith(secret, SignatureAlgorithm.HS256)  // 사용할 암호화 알고리즘
                .compact();
    }

    /**
     * # 토큰에 저장된 정보 가져오기
     * JWT 토큰(Access Token, Refresh Token)에 저장된 값 가져오기
     *
     * @param token 토큰
     * @return 회원 아이디(ex: soyoung123)
     */
    public TokenDTO getTokenInfo(String token) {
        log.info(this.getClass().getName() + ".토큰에 저장된 정보 가져오기(getTokenInfo) 시작!");

        // 보안키 문자들을 JWT Key 형태로 변경하기
         SecretKey secret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

        // JWT 토큰 정보
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

        String userId = CmmUtil.nvl(claims.getSubject());
        String role = CmmUtil.nvl((String)claims.get("roles"));  // Loginservice 생성된 토큰의 권한명과 동일

        log.info("회원 ID : " + userId);
        log.info("회원 권한 : " + role);

        // TokenDTO은 자바 17의 Record 객체 사용했기에 빌더패턴 적용
        TokenDTO rDTO = TokenDTO.builder().userId(userId).role(role).build();

        log.info(this.getClass().getName() + ".토큰에 저장된 정보 가져오기(getTokenInfo) 종료!");

        return rDTO;
    }

    /**
     * # 매 요청마다 JWT 토큰(Access Token, Refresh Token)으로 받은 아이디 (권한 정보는 사용 안 함)를 Spring Security 인증 정보에 넣어 권한에 맞게 접근 제어하도록 처리함
     * --> Spring Security용 인증 토큰 생성
     * <p>
     * (JWT 토큰이 있다 == 로그인 되었다, Spring Security용 인증 토큰이 있다 == 로그인된 사용자의 요청이다)
     * <p>
     * JWT는 웹 브라우저의 쿠키에 저장되어 있기에, 모든 요청마다 토큰이 같이 전달됨
     * 토큰 값에 저장된 권한과 사용자의 권한이 맞는지 체크
     *
     * @param token 토큰
     * @return 인증 처리한 정보(로그인 성공, 실패)
     */
    public Authentication getAuthentication(String token) {

        log.info(this.getClass().getName() + ".getAuthentication Start!");
        log.info("getAuthentication : " + token);

        // 토큰에 저장된 정보 가져오기
        TokenDTO rDTO = getTokenInfo(token);

        // JWT 토큰에 저장된 사용자 아이디
        String userId = CmmUtil.nvl(rDTO.userId());
        String roles = CmmUtil.nvl(rDTO.role());

        log.info("ID : " + userId);
        log.info("ROLES : " + roles);

        Set<GrantedAuthority> pSet = new HashSet<>();
        // DB에 저장된 Role이 있는 경우에만 실행
        if(roles.length() > 0) {
            for(String role : roles.split(",")) {
                pSet.add(new SimpleGrantedAuthority(role));
            }
        }

        log.info(this.getClass().getName() + ".getAuthentication End!");

        // Spring Security가 로그인 성공된 정보를 Spring Security에서 사용하기 위해
        // Spring Security용 UsernamePasswordAuthenticationToken 생성
        // JWT 토큰에 저장된 권한에 맞춰 Spring Security가 권한 체크하도록 Filter 호출
        return new UsernamePasswordAuthenticationToken(userId, "", pSet);

    }

    /**
     * # 토큰 가져오기
     * 쿠키 및 HTTP 인증 헤더(Bearer) 저장된 JWT 토큰(Access Token, Refresh Token) 가져오기
     * <p>
     * 쿠키 : Access Token, Refresh Token 저장 / HTTP 인증 헤더 : Bearer 토큰으로 Access Token만 저장
     *
     * @param request   request 정보
     * @param tokenType token 유형
     * @return 쿠키에 저장된 토큰 값
     */
    public String resolveToken(ServerHttpRequest request, JwtTokenType tokenType) {
        log.info(this.getClass().getName() + ".토큰 가져오기(resolveToken) 시작!");

        String token = "";
        String tokenName = "";

        if(tokenType == JwtTokenType.ACCESS_TOKEN) {
            tokenName = accessTokenName;
        } else if(tokenType == JwtTokenType.REFRESH_TOKEN) {
            tokenName = refreshTokenName;
        }

        HttpCookie cookie = request.getCookies().getFirst(tokenName);

        // 쿠키가 존재하면, 쿠키에서 토큰 값 꺼내기
        if(cookie != null) {
                    token = CmmUtil.nvl(cookie.getValue());
        }

        // Cookies에 토큰이 존재하지 않으면, Bearer 토큰에 값이 있는지 확인함
        if(token.length() == 0) {
            String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            log.info("bearerToken : " + bearerToken);

            if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)) {
                token = bearerToken.substring(7);
            }

            log.info("bearerToken token : " + token);
        }

        log.info(this.getClass().getName() + ".토큰 가져오기(resolveToken) 종료!");

        return token;
    }

    /**
     *  # JWT 토큰(ACCESS TOKEN, REFRESH TOKEN) 상태 확인
     *
     * @param token  토큰
     * @return   상태 정보(EXPIRED, ACCESS, DENIED)
     */
    public JwtStatus validateToken(String token) {

        log.info(this.getClass().getName() + ".토큰 상태 가져오기(validateToken) 시작합니다.");

        if (token.length() > 0) {

            try {

                // 보안키 문자들을 JWT Key 형태로 변경하기
                SecretKey secret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

                // JWT 토큰 정보
                Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

                // 토큰 만료여부 체크
                // claims : JWT의 페이로드(데이터 부분)에서 클레임들을 포함하는 객체
                // getBody() : JWT의 페이로드를 가져오는 메서드
                // getExpiration() : 토큰의 만료 시간을 가져오는 메서드
                // new Data() : 현재 시간을 나타내는 객체 생성
                // before(new Date()) : 토큰의 만료 시간이 현재 시간보다 이전인지 확인함 이게 참이라면 만료된 토큰이라는 뜻(더이상 유효한 토큰이 아님)
                if (claims.getBody().getExpiration().before(new Date())) {

                    log.info(this.getClass().getName() + ".토큰 상태 가져오기(validateToken) 종료합니다.");

                    return JwtStatus.EXPIRED; // 기간 만료

                } else {

                    log.info(this.getClass().getName() + ".토큰 상태 가져오기(validateToken) 종료합니다.");

                    return JwtStatus.ACCESS;  // 유효한 토큰
                }

            } catch (ExpiredJwtException e) {

                log.info(this.getClass().getName() + ".토큰 상태 가져오기(validateToken) 종료합니다.");

                // 만료된 경우에는 refresh token을 확인하기 위해서
                return JwtStatus.EXPIRED; // 혹시 모르니 Exception으로 한번 더 체크
            } catch (JwtException | IllegalArgumentException e) {
                log.info("JwtException : {}", e);

                log.info(this.getClass().getName() + ".토큰 상태 가져오기(validateToken) 종료합니다.");

                return JwtStatus.DENIED;
            }
        } else {

            log.info(this.getClass().getName() + ".토큰 상태 가져오기(validateToken) 종료합니다.");

            return JwtStatus.DENIED;
        }

    }


}
