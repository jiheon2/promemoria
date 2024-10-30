package kopo.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kopo.userservice.auth.AuthInfo;
import kopo.userservice.auth.JwtTokenProvider;
import kopo.userservice.auth.TokenType;
import kopo.userservice.controller.response.CommonResponse;
import kopo.userservice.dto.MsgDTO;
import kopo.userservice.dto.TokenDTO;
import kopo.userservice.dto.UserDTO;
import kopo.userservice.service.UserInterface;
import kopo.userservice.service.impl.RedisService;
import kopo.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequestMapping(value = "/user/v1")
@RequiredArgsConstructor
@RestController
public class UserController {

    @Value("${jwt.token.access.valid.time}")
    private long accessTokenValidTime;

    @Value("${jwt.token.access.name}")
    private String accessTokenName;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserInterface userInterface;
    private final RedisService redisService;

    @PostMapping(value = "loginSuccess")
    public ResponseEntity<CommonResponse> loginSuccess(
            @AuthenticationPrincipal AuthInfo authInfo, HttpServletResponse response) throws Exception {

        log.info(this.getClass().getName() + ".loginSuccess 실행");

        // Spring Security에 저장된 정보 가져오기
        UserDTO rDTO = Optional.ofNullable(authInfo.userDTO())
                .orElseGet(() -> UserDTO.builder().build());

        String userId = CmmUtil.nvl(rDTO.userId());
        String userRoles = CmmUtil.nvl(rDTO.roles());

        log.info("userId : " + userId);
        log.info("userRoles : " + userRoles);

        // Access Token 생성
        String accessToken = jwtTokenProvider.createToken(userId, userRoles, TokenType.ACCESS_TOKEN);
        log.info("accessToken : " + accessToken);

        ResponseCookie cookie = ResponseCookie.from(accessTokenName, accessToken)
                .domain("")
                .path("/")
                .maxAge(accessTokenValidTime)
                .httpOnly(true)
                .build();

        // 기존쿠키 모두 삭제하고, Cookie에 Access Token 저장하기
        response.setHeader("Set-Cookie", cookie.toString());

        cookie = null;

        // Refresh Token 생성
        // Refresh Token은 보안상 노출되면, 위험하기에 Refresh Token은 DB에 저장하고,
        // DB를 조회하기 위한 값만 Refresh Token으로 생성함
        // Refresh Token은 Access Token에 비해 만료시간을 길게 설정함
        String refreshToken = jwtTokenProvider.createToken(userId, userRoles, TokenType.REFRESH_TOKEN);

        log.info("refreshToken : " + refreshToken);

        // 레디스에 리프레시 토큰 저장
        redisService.setValues(refreshToken, userId);

        // 결과 메시지 전달하기
        MsgDTO dto = MsgDTO.builder().result(1).msg(userId + "님 로그인이 성공하였습니다.").build();

        log.info(this.getClass().getName() + ".loginSuccess End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }

    @PostMapping(value = "loginFail")
    public ResponseEntity<CommonResponse> loginFail() {

        log.info(this.getClass().getName() + ".loginFail 실행");

        MsgDTO dto = MsgDTO.builder().result(0).msg("로그인 실패").build();

        log.info(this.getClass().getName() + ".loginFail 종료");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));
    }

    @PostMapping(value = "getToken")
    private TokenDTO getToken(HttpServletRequest request) {

        log.info("토큰 정보 가져오기 : {}", this.getClass().getName());

        // JWT AccessToken 가져오기
        String jwtAccessToken = CmmUtil.nvl(jwtTokenProvider.resolveToken(request, TokenType.ACCESS_TOKEN));
        log.info("jwtAccessToken : {}", jwtAccessToken);

        TokenDTO dto = Optional.ofNullable(jwtTokenProvider.getTokenInfo(jwtAccessToken))
                .orElseGet(() -> TokenDTO.builder().build());

        log.info("TokenDTO : {}", dto);

        log.info("토큰 정보 가져오기 종료 : {}", this.getClass().getName());

        return dto;
    }

    @PostMapping(value = "userInfo")
    public ResponseEntity<CommonResponse> getUserInfo(@RequestParam("userId") String userId) {
        log.info("getUserInfo 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.getUserInfo(userId)));
    }

    @PostMapping(value = "findId")
    public ResponseEntity<CommonResponse> findId(@Valid @RequestParam("userName") String userName,
                                                 @Valid @RequestParam("userEmail") String userEmail) {
        log.info("findId 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.findId(userName, userEmail)));
    }

    @PostMapping(value = "resetPw")
    public ResponseEntity<CommonResponse> resetPw(@Valid @RequestParam("userId") String userId,
                                                  @Valid @RequestParam("userEmail") String userEmail) {
        log.info("resetPw 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.resetPw(userId, userEmail)));
    }

    @PostMapping(value = "updateUserInfo")
    public ResponseEntity<CommonResponse> updateUserInfo(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        log.info("updateUserInfo 실행 : {}", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getErrors(bindingResult);
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.updateUserInfo(userDTO)));
    }

    @PostMapping(value = "updatePassword")
    public ResponseEntity<CommonResponse> updatePassword(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {

        log.info("updatePassword 실행 : {}", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getErrors(bindingResult);
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.updatePassword(userDTO)));
    }

    @PostMapping(value = "deleteUser")
    public ResponseEntity<CommonResponse> deleteUser(@Valid @RequestParam("userId") String userId) {
        log.info("deleteUser 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.deleteUser(userId)));
    }

    @PostMapping(value = "existUser")
    public ResponseEntity<CommonResponse> existUser(@Valid @RequestParam("userId") String userId) {
        log.info("existUser 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.existUser(userId)));
    }

    @PostMapping(value = "existEmail")
    public ResponseEntity<CommonResponse> existEmail(@Valid @RequestParam("userEmail") String userEmail) {

        log.info("existEmail 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.existEmail(userEmail)));
    }

    @PostMapping(value = "registerUser")
    public ResponseEntity<CommonResponse> registerUser(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {

        log.info("registerUser 실행 : {}", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            return CommonResponse.getErrors(bindingResult);
        }

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), userInterface.registerUser(userDTO)));
    }
}
