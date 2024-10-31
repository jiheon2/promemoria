package kopo.userservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.userservice.auth.AuthInfo;
import kopo.userservice.controller.response.Result;
import kopo.userservice.dto.MsgDTO;
import kopo.userservice.dto.UserDTO;
import kopo.userservice.repository.UserRepository;
import kopo.userservice.repository.entity.UserEntity;
import kopo.userservice.service.UserInterface;
import kopo.userservice.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements UserInterface {

    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    @Override
    public MsgDTO registerUser(UserDTO userDTO) {

        log.info("회원가입 실행 : {}", this.getClass().getName());

        // 사용자 정보 로깅
        log.info("userDTO : {}", userDTO);

        // 중복 사용자 확인
        if (userRepository.existsByUserId(userDTO.userId())) {
            return MsgDTO.builder().msg("이미 가입되었습니다").result(Result.FAIL.getCode()).build();
        } else {
            // 회원 가입 Entity 생성
            UserEntity registerEntity = UserEntity.builder()
                    .userId(userDTO.userId())
                    .userName(userDTO.userName())
                    .userAge(userDTO.userAge())
                    .userEmail(userDTO.userEmail())
                    .userPw(bCryptPasswordEncoder.encode(userDTO.userPw()))
                    .userAddress1(userDTO.userAddress1())
                    .userAddress2(userDTO.userAddress2())
                    .userGender(userDTO.userGender())
                    .phoneNumber(userDTO.phoneNumber())
                    .postNumber(userDTO.postNumber())
                    .roles("USER")
                    .build();

            try {
                // 회원가입
                userRepository.save(registerEntity);
            } catch (Exception e) {
                log.error("회원 가입 중 에러 발생 : {}", e.getMessage());
                return MsgDTO.builder().msg("회원 가입 중 에러가 발생하였습니다.").result(Result.ERROR.getCode()).build();
            }
            return MsgDTO.builder().msg("회원 가입을 축하드립니다.").result(Result.SUCCESS.getCode()).build();
        }
    }

    @Override
    public MsgDTO findId(String userName, String userEmail) {

        log.info("아이디찾기 실행 : {}", this.getClass().getName());

        // 사용자 정보 로깅
        log.info("userName : {}", userName);
        log.info("userEmail : {}", userEmail);

        try {
            // 회원 정보 조회
            UserEntity userEntity = userRepository.findByUserNameAndUserEmail(userName, userEmail);

            if (userEntity != null) {
                String userId = userEntity.getUserId();
                return MsgDTO.builder()
                        .msg("회원님의 ID는 " + userId + "입니다.")
                        .result(Result.SUCCESS.getCode())
                        .build();
            } else {
                return MsgDTO.builder()
                        .msg("일치하는 회원 정보가 없습니다.")
                        .result(Result.FAIL.getCode())
                        .build();
            }
        } catch (Exception e) {
            log.error("오류 발생: {}", e.getMessage());
            return MsgDTO.builder()
                    .msg("시스템 오류가 발생하였습니다. 잠시 후 다시 시도해주세요.")
                    .result(Result.ERROR.getCode())
                    .build();
        }
    }

    @Override
    public MsgDTO resetPw(String userId, String userEmail) {

        // 메일 발송 시스템으로 구현하기

        log.info("비밀번호 재설정 실행 : {}", this.getClass().getName());

        // 사용자 정보 로깅
        log.info("userId : {}", userId);
        log.info("userEmail : {}", userEmail);

        try {
            Optional<UserEntity> userEntity = Optional.ofNullable(userRepository.findByUserNameAndUserEmail(userId, userEmail));

            if (userEntity.isPresent()) {
                String password = "0000";
                String encodedPassword = bCryptPasswordEncoder.encode(password);
                UserEntity passwordEntity = UserEntity.builder().userId(userId).userPw(encodedPassword).build();
                userRepository.save(passwordEntity);

                return MsgDTO.builder()
                        .msg("회원님의 비밀번호를 " + password + "로 초기화했습니다. 변경해주시길 바랍니다.")
                        .result(Result.SUCCESS.getCode())
                        .build();
            } else {
                return MsgDTO.builder()
                        .msg("일치하는 회원 정보가 없습니다.")
                        .result(Result.FAIL.getCode())
                        .build();
            }
        } catch (Exception e) {
            log.error("오류 발생: {}", e.getMessage());
            return MsgDTO.builder()
                    .msg("시스템 오류가 발생하였습니다. 잠시 후 다시 시도해주세요.")
                    .result(Result.ERROR.getCode())
                    .build();
        }
    }

    @Override
    public UserDTO getUserInfo(String userId) {

        log.info("회원정보 조회 실행 : {}", this.getClass().getName());

        log.info("userId : {}", userId);

        UserDTO userDTO = null;

        // 회원 정보 조회
        UserEntity userEntity = userRepository.findByUserId(userId);

        // Entity to DTO
        if (userEntity != null) {
            userDTO = new ObjectMapper().convertValue(userEntity,
                    new TypeReference<UserDTO>() {
                    });
        }
        return userDTO;
    }

    @Override
    @Transactional
    public MsgDTO updateUserInfo(UserDTO userDTO) {

        log.info("회원정보 수정 실행 : {}", this.getClass().getName());

        log.info("userDTO : {}", userDTO);

        try {
            String userId = userDTO.userId();

            // 회원 정보 조회
            UserEntity userEntity = userRepository.findByUserId(userId);

            // 수정
            UserEntity updateEntity = UserEntity.builder()
                    .userId(userEntity.getUserId())
                    .userName(userDTO.userName())
                    .userAge(userDTO.userAge())
                    .userEmail(userDTO.userEmail())
                    .userPw(userEntity.getUserPw())
                    .userAddress1(userDTO.userAddress1())
                    .userAddress2(userDTO.userAddress2())
                    .userGender(userEntity.getUserGender())
                    .phoneNumber(userDTO.phoneNumber())
                    .postNumber(userDTO.postNumber())
                    .roles(userEntity.getRoles())
                    .build();
            userRepository.save(updateEntity);

            // 회원 정보 조회
            userEntity = userRepository.findByUserId(userId);

            log.info("수정된 값 확인");
            log.info("userName : {}", userEntity.getUserName());
            log.info("userAge : {}", userEntity.getUserAge());
            log.info("userEmail : {}", userEntity.getUserEmail());
            log.info("userAddress1 : {}", userEntity.getUserAddress1());
            log.info("userAddress2 : {}", userEntity.getUserAddress2());
            log.info("phoneNumber : {}", userEntity.getPhoneNumber());
            log.info("postNumber : {}", userEntity.getPostNumber());

            if (userRepository.existsByUserEmail(userEntity.getUserEmail())) {
                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly(); // 롤백
                return MsgDTO.builder().msg("중복된 이메일이 존재합니다.").result(Result.FAIL.getCode()).build();
            } else {
                return MsgDTO.builder().msg("회원정보가 수정되었습니다.").result(Result.SUCCESS.getCode()).build();
            }
        } catch (Exception e) {
            log.error("에러 발생 : {}", e.getMessage());
            return MsgDTO.builder().msg("에러가 발생하였습니다.").result(Result.ERROR.getCode()).build();
        }
    }

    @Override
    @Transactional
    public MsgDTO updatePassword(UserDTO userDTO) {

        log.info("비밀번호 수정 실행 : {}", this.getClass().getName());

        try {
            String userId = CmmUtil.nvl(userDTO.userId());
            String password = CmmUtil.nvl(userDTO.userPw());

            UserEntity userEntity = userRepository.findByUserId(userId);

            if (userEntity != null) {
                password = bCryptPasswordEncoder.encode(password);
                userEntity.setUserPw(password);

                userRepository.save(userEntity);

                return MsgDTO.builder().msg("비밀번호가 수정되었습니다.").result(Result.SUCCESS.getCode()).build();
            } else {
                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly(); // 롤백
                return MsgDTO.builder().msg("비밀번호 수정에 실패하였습니다.").result(Result.FAIL.getCode()).build();
            }
        } catch (Exception e) {
            log.error("에러 발생 : {}", e.getMessage());
            return MsgDTO.builder().msg("에러가 발생하였습니다.").result(Result.ERROR.getCode()).build();
        }
    }

    @Override
    @Transactional
    public MsgDTO deleteUser(String userId) {

        log.info("회원탈퇴 실행");

        log.info("userId : {}", userId);

        try {
            // 회원 탈퇴
            userRepository.deleteByUserId(userId);

            // 탈퇴 되었는지 확인
            if (userRepository.existsByUserId(userId)) {
                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
                return MsgDTO.builder().msg("회원 탈퇴에 실패하였습니다.").result(Result.FAIL.getCode()).build();
            } else {
                return MsgDTO.builder().msg("회원 탈퇴에 성공하셨습니다.").result(Result.SUCCESS.getCode()).build();
            }
        } catch (Exception e) {
            log.error("에러 발생 : {}", e.getMessage());
            return MsgDTO.builder().msg("에러가 발생하였습니다.").result(Result.ERROR.getCode()).build();
        }
    }

    @Override
    public boolean existUser(String userId) {

        log.info("아이디 중복조회 실행 : {}", this.getClass().getName());

        return userRepository.existsByUserId(userId);
    }

    @Override
    public boolean existEmail(String email) {

        log.info("이메일 중복조회 실행 : {}", this.getClass().getName());

        return userRepository.existsByUserEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        log.info("loadUserByUsername 실행 : {}", this.getClass().getName());

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException(userId);
        } else {
            // Entity to DTO
            UserDTO userDTO = new ObjectMapper().convertValue(userEntity,
                    new TypeReference<UserDTO>() {
                    });
            return new AuthInfo(userDTO);
        }
    }
}

