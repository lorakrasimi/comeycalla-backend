package com.loraadova.comeycalla.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileResponseDto {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private UserStatsDto stats;
}
