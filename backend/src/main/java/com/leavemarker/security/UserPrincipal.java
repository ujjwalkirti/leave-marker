package com.leavemarker.security;

import com.leavemarker.entity.Employee;
import com.leavemarker.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String fullName;
    private String password;
    private Role role;
    private Long companyId;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(Employee employee) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())
        );

        return new UserPrincipal(
                employee.getId(),
                employee.getEmail(),
                employee.getFullName(),
                employee.getPassword(),
                employee.getRole(),
                employee.getCompany().getId(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
