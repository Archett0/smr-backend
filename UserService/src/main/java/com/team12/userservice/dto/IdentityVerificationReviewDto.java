package com.team12.userservice.dto;

import com.team12.userservice.model.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class IdentityVerificationReviewDto {
    private Long id;
    private VerificationStatus status;
}
