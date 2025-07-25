package com.team12.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserInfoUpdateDto {
    private String oidcSub;
    private String email;
    private String phoneNumber;
}
