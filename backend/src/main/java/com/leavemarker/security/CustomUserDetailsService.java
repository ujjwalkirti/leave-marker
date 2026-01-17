package com.leavemarker.security;

import com.leavemarker.entity.Employee;
import com.leavemarker.enums.EmployeeStatus;
import com.leavemarker.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmailAndStatusAndDeletedFalse(email, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or account is inactive"));

        return UserPrincipal.create(employee);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(employee);
    }
}
