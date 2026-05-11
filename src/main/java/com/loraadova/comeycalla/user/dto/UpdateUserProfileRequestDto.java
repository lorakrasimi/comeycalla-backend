package com.loraadova.comeycalla.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateUserProfileRequestDto {
    private String username;
    private String email;
}
