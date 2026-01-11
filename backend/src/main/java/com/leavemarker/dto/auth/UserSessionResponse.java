package com.leavemarker.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionResponse {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private Long companyId;
}
