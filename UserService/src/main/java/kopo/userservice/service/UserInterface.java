package kopo.userservice.service;

import kopo.userservice.dto.MsgDTO;
import kopo.userservice.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserInterface extends UserDetailsService {

    // 회원 가입
    MsgDTO registerUser(UserDTO userDTO);

    // 아이디 찾기
    MsgDTO findId(String userName, String userEmail);

    // 비밀번호 재설정
    MsgDTO resetPw(String userId, String userEmail);

    // 회원 정보 조회
    UserDTO getUserInfo(String userId);

    // 회원 정보 수정
    MsgDTO updateUserInfo(UserDTO userDTO);

    // 비밀번호 변경
    MsgDTO updatePassword(UserDTO userDTO);

    // 회원 탈퇴
    MsgDTO deleteUser(String userId);

    // 아이디 중복체크
    boolean existUser(String userId);

    // 이메일 중복체크
    boolean existEmail(String email);
}
