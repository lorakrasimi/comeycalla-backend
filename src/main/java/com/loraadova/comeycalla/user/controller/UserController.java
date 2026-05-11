package com.loraadova.comeycalla.user.controller;

import com.loraadova.comeycalla.user.dto.UpdateUserProfileRequestDto;
import com.loraadova.comeycalla.user.dto.UserProfileResponseDto;
import com.loraadova.comeycalla.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> getProfile() {
        UserProfileResponseDto profile = this.userService.getProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponseDto> updateProfile(
            @Valid @RequestPart("profile") UpdateUserProfileRequestDto request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        UserProfileResponseDto updatedProfile = userService.updateProfile(request, avatar);
        return ResponseEntity.ok(updatedProfile);
    }

}