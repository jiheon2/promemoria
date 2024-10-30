package kopo.apigateway.jwt;

// JWT 토큰 종류에 대한 설정
// 토큰은 2가지 타입으로 구분, 생성 방법은 동일하지만 만료 시간은 다름
public enum JwtTokenType {

    ACCESS_TOKEN,
    REFRESH_TOKEN
}
