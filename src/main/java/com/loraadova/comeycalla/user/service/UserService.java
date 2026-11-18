package com.loraadova.comeycalla.user.service;

import com.loraadova.comeycalla.auth.security.CurrentUserService;
import com.loraadova.comeycalla.common.CloudinaryService;
import com.loraadova.comeycalla.mealplan.service.MealPlanService;
import com.loraadova.comeycalla.recipe.service.RecipeService;
import com.loraadova.comeycalla.user.dto.UpdateUserProfileRequestDto;
import com.loraadova.comeycalla.user.dto.UserProfileResponseDto;
import com.loraadova.comeycalla.user.dto.UserStatsDto;
import com.loraadova.comeycalla.user.entity.UserEntity;
import com.loraadova.comeycalla.user.mapper.UserMapper;
import com.loraadova.comeycalla.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private MealPlanService mealPlanService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private CloudinaryService cloudinaryService;

    public UserProfileResponseDto getProfile() {
        UserEntity user = userRepository.findById(this.currentUserService.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfileResponseDto dto = userMapper.toDto(user);
        dto.setStats(mockStats(user.getId()));
        return dto;
    }

    public UserProfileResponseDto updateProfile(UpdateUserProfileRequestDto request, MultipartFile avatar) {
        UserEntity user = userRepository.findById(this.currentUserService.getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), user.getId())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        } else user.setUsername(request.getUsername());

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
            throw new IllegalArgumentException("El email ya está registrado");
        } else user.setEmail(request.getEmail());

        if (avatar == null) {
            // Borra imagen
            user.setAvatar(null);
        } else {
            // Nueva imagen
            String imageUrl = this.cloudinaryService.uploadImage(avatar);
            user.setAvatar(imageUrl);
        }

        UserEntity updated = userRepository.save(user);

        UserProfileResponseDto dto = userMapper.toDto(updated);
        dto.setStats(mockStats(user.getId()));

        return dto;
    }

    private UserStatsDto mockStats(Long id) {
        return new UserStatsDto(
                this.recipeService.getRecipesCountByUser(id),
                this.mealPlanService.getRecipesCountByUser(id),
                156
        );
    }
}
