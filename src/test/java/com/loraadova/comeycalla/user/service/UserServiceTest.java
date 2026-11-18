package com.loraadova.comeycalla.user.service;

import com.loraadova.comeycalla.auth.security.CurrentUserService;
import com.loraadova.comeycalla.common.CloudinaryService;
import com.loraadova.comeycalla.mealplan.service.MealPlanService;
import com.loraadova.comeycalla.recipe.service.RecipeService;
import com.loraadova.comeycalla.user.dto.UpdateUserProfileRequestDto;
import com.loraadova.comeycalla.user.dto.UserProfileResponseDto;
import com.loraadova.comeycalla.user.entity.UserEntity;
import com.loraadova.comeycalla.user.mapper.UserMapper;
import com.loraadova.comeycalla.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de usuario")
class UserServiceTest {
    @Captor
    private ArgumentCaptor<UserEntity> captor;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private UserService userService;

    @Nested
    class GetUserProfileTests {

        @Test
        @DisplayName("Get profile test successfully")
        void getProfileSuccessfully() {
            //given
            Long userId = 1L;
            UserEntity user = new UserEntity();
            user.setId(userId);
            UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto();

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toDto(user)).thenReturn(userProfileResponseDto);

            //when
            UserProfileResponseDto result = userService.getProfile();

            //Debe ser assertSame(userProfileResponseDto, result). Solo afecta al mensaje de error, pero se nota.
            assertSame(userProfileResponseDto, result);

            verify(currentUserService).getCurrentUserId();
            verify(userRepository).findById(userId);
            verify(userMapper, times(1)).toDto(user); //El times 1 es el valor por defecto
        }

        @Test
        @DisplayName("Get profile not found exception")
        void getProfileNotFoundException() {
            // Given
            Long userId = 1L;

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.getProfile()
            );

            // Then
            assertEquals("User not found", exception.getMessage());
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    class UpdateProfileTests {

        @Test
        @DisplayName("Update profile test with avatar uploads image and saves url")
        void updateProfileWithAvatarSuccessfully() {
            //given
            Long userId = 1L;
            UserEntity user = new UserEntity();
            user.setId(userId);
            UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto();

            final UpdateUserProfileRequestDto request = UpdateUserProfileRequestDto.builder()
                    .email("new email")
                    .username("new user name")
                    .build();

            MockMultipartFile avatar = new MockMultipartFile(
                    "avatar",
                    "foto.png",
                    "image/png",
                    "contenido-falso".getBytes());
            String imageUrl = "https://cloudinary.com/foto.png";


            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(cloudinaryService.uploadImage(avatar)).thenReturn(imageUrl);
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(userProfileResponseDto);

            //When
            UserProfileResponseDto result = userService.updateProfile(request, avatar);

            //Then
            assertSame(userProfileResponseDto, result);
            verify(userRepository).save(captor.capture());
            UserEntity savedUser = captor.getValue();

            assertEquals(imageUrl, captor.getValue().getAvatar());
            assertEquals("new email", savedUser.getEmail());
            assertEquals("new user name", savedUser.getUsername());

            verify(currentUserService).getCurrentUserId();
            verify(cloudinaryService).uploadImage(avatar);
            verify(userMapper).toDto(user);

        }

        @Test
        @DisplayName("Update profile test with avatar uploads NULL and saves url")
        void updateProfileWithoutAvatarSuccessfully() {
            //given
            Long userId = 1L;
            UserEntity user = new UserEntity();
            user.setId(userId);
            UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto();

            final UpdateUserProfileRequestDto request = UpdateUserProfileRequestDto.builder()
                    .email("new email")
                    .username("new user name")
                    .build();

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(userProfileResponseDto);

            //When
            UserProfileResponseDto result = userService.updateProfile(request, null);

            //Then
            assertSame(userProfileResponseDto, result);
            verify(userRepository).save(captor.capture());
            UserEntity savedUser = captor.getValue();

            assertNull(captor.getValue().getAvatar());
            assertEquals("new email", savedUser.getEmail());
            assertEquals("new user name", savedUser.getUsername());

            verify(currentUserService).getCurrentUserId();
            verifyNoInteractions(cloudinaryService);
            verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("Update profile not found exception")
        void updateProfileNotFoundException() {
            Long userId = 1L;
            UserEntity user = new UserEntity();
            user.setId(userId);

            UpdateUserProfileRequestDto dto = new UpdateUserProfileRequestDto();

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.updateProfile(dto, null));
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        @DisplayName("Not possible to update profile because of existing email")
        void updateProfileExistingEmail() {
            Long userId = 1L;
            UserEntity user = new UserEntity();
            user.setId(userId);

            final UpdateUserProfileRequestDto request = UpdateUserProfileRequestDto.builder()
                    .email("new email")
                    .username("new user name")
                    .build();

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmailAndIdNot("new email", userId)).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(request,null));
            assertEquals("El email ya está registrado", exception.getMessage());

            verify(userRepository, never()).save(any());
            verifyNoInteractions(cloudinaryService);
        }

        @Test
        @DisplayName("Not possible to update profile because of existing username")
        void updateProfileExistingUsername() {
            Long userId = 1L;
            UserEntity user = new UserEntity();
            user.setId(userId);

            final UpdateUserProfileRequestDto request = UpdateUserProfileRequestDto.builder()
                    .email("new email")
                    .username("new user name")
                    .build();

            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByUsernameAndIdNot("new user name", userId)).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(request,null));
            assertEquals("El nombre de usuario ya existe", exception.getMessage());

            verify(userRepository, never()).save(any());
            verifyNoInteractions(cloudinaryService);
        }
    }
}