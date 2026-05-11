package com.loraadova.comeycalla.user.mapper;

import com.loraadova.comeycalla.user.dto.UserProfileResponseDto;
import com.loraadova.comeycalla.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponseDto toDto(UserEntity user);
}