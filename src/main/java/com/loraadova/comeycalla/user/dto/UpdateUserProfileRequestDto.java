package com.loraadova.comeycalla.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UpdateUserProfileRequestDto {
    private String username;
    private String email;
}
