package com.loraadova.comeycalla.auth.mapper;

import com.loraadova.comeycalla.auth.dto.AuthResponse;
import com.loraadova.comeycalla.auth.dto.RegisterRequest;
import com.loraadova.comeycalla.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    UserEntity toEntity(RegisterRequest request);

    default AuthResponse toAuthResponse(UserEntity user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}