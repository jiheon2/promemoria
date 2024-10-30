package kopo.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kopo.userservice.auth.JwtTokenProvider;
import kopo.userservice.auth.TokenType;
import kopo.userservice.controller.response.CommonResponse;
import kopo.userservice.dto.TokenDTO;
import kopo.userservice.dto.UserDTO;
import kopo.userservice.service.UserInterface;
import kopo.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequestMapping(value = "/user/v1")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserInterface userInterface;

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
