package kopo.userservice.auth;

import kopo.userservice.dto.UserDTO;
import kopo.userservice.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Slf4j
public record AuthInfo(UserDTO userDTO) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> pSet = new HashSet<>();

        String roles = CmmUtil.nvl(userDTO.roles());

        log.info("getAuthorities / roles : {}", roles);
        if (!roles.isEmpty()) {
            for (String role : roles.split(",")) {
                pSet.add(new SimpleGrantedAuthority(role));
            }
        }

        return pSet;
    }

    // 사용자의 패스워드를 반환
    @Override
    public String getPassword() {
        return CmmUtil.nvl(userDTO.userPw());
    }

    // 사용자의 id를 반환(유니크한 값)
    @Override
    public String getUsername() {
        return CmmUtil.nvl(userDTO.userId());
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료되지 않았음
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        return true; // 잠금되지 않았음
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        return true; // 사용 가능
    }
}
