package com.team12.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserRegisterDto {
    @JsonProperty("nickname")
    private String username;

    @JsonProperty("picture")
    private String picture;

    @JsonProperty("email")
    private String email;

    @JsonProperty("sub")
    private String sub;
}
