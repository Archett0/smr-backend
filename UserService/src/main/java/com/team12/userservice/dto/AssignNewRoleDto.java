package com.team12.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AssignNewRoleDto {
    @JsonProperty("sub")
    private String sub;
}
