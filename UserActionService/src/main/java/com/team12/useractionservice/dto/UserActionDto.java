package com.team12.useractionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserActionDto {
    private Long id;
    private Long userId;
    private Long listingId;
    private int actionValue;
}
