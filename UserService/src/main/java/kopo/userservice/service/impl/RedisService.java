package kopo.userservice.service.impl;

import kopo.userservice.dto.MsgDTO;
import kopo.userservice.service.RedisInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService implements RedisInterface {

    private final RedisTemplate<String, String> redisTemplate;

    // 키-밸류 설정
    @Override
    public void setValues(String token, String userId) throws Exception {

        log.info(this.getClass().getName() + ".setValues 실행");

        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(token, userId, Duration.ofMinutes(10));

        log.info(this.getClass().getName() + ".setValues 종료");
    }

    // 키값으로 벨류 가져오기
    @Override
    public String getValues(String token) throws Exception {

        log.info(this.getClass().getName() + ".getValues 실행");

        ValueOperations<String, String> values = redisTemplate.opsForValue();

        log.info(this.getClass().getName() + ".getValues 종료");

        return values.get(token);
    }

    // 키-벨류 삭제
    @Override
    public void delValues(String token) throws Exception {

        log.info(this.getClass().getName() + ".delValues 실행");

        redisTemplate.delete(token.substring(7));

        log.info(this.getClass().getName() + ".deValues 종료");
    }

    @Override
    public int saveAuthCode(String phoneNumber, String authCode) throws Exception {

        int res;

        log.info("saveAuthCode start!");
        try {
            redisTemplate.opsForValue().set(phoneNumber, authCode, 5, TimeUnit.MINUTES);
            log.info("저장정보 : {} / {}", phoneNumber, authCode);

            res = 1;
            log.info("saveAuthCode end!");
        } catch (Exception e) {
            log.error("저장 실패: ", e);
            res = 0;
        }

        return res;
    }

    @Override
    public MsgDTO verifyAuthCode(String phoneNumber, String authCode) throws Exception {

        log.info("verifyAuthCode start!");

        MsgDTO dto = null;
        String msg = "";
        int res = 0;

        try {
            String storedAuthCode = redisTemplate.opsForValue().get(phoneNumber);
            log.info("가져온 인증 코드 : " + storedAuthCode);

            if (storedAuthCode == null) {
                msg = "인증 코드가 존재하지 않습니다.";
                log.info(msg);
                res = 0;
            } else if (storedAuthCode.equals(authCode)) {
                msg = "인증 성공";
                redisTemplate.delete(phoneNumber);
                log.info(msg);
                res = 1;
            } else {
                msg = "인증 실패 : 코드가 일치하지 않습니다.";
                log.info(msg);
                res = 0;
            }
        } catch (Exception e) {
            msg = "인증 코드 검증 중 예외 발생 : " + e;
            log.info(msg);
            res = 0;
        }

        dto = MsgDTO.builder().result(res).msg(msg).build();
        log.info("verifyAuthCode end!");

        return dto;
    }
}

