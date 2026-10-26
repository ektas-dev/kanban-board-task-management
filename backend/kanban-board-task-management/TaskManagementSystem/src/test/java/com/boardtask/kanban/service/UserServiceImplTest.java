package com.boardtask.kanban.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.boardtask.kanban.dto.user.ChangePasswordRequestDto;
import com.boardtask.kanban.dto.user.UpdateProfileRequestDto;
import com.boardtask.kanban.dto.user.UserResponseDto;
import com.boardtask.kanban.entity.AppUser;
import com.boardtask.kanban.exception.BadRequestException;
import com.boardtask.kanban.mapper.UserMapper;
import com.boardtask.kanban.repository.AppUserRepository;
import com.boardtask.kanban.security.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        currentUser = mock(AppUser.class);
    }

    // ---------------------------------------------------------
    // getProfile()
    // ---------------------------------------------------------

    @Test
    void getProfile_authenticatedUserExists_returnsUserResponse() {
        UserResponseDto expectedDto = mock(UserResponseDto.class);

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);

        try (MockedStatic<UserMapper> mapper =
                     org.mockito.Mockito.mockStatic(UserMapper.class)) {

            mapper.when(() -> UserMapper.toResponseDto(currentUser))
                    .thenReturn(expectedDto);

            UserResponseDto result = userService.getProfile();

            assertNotNull(result);
            assertEquals(expectedDto, result);

            mapper.verify(
                    () -> UserMapper.toResponseDto(currentUser));
        }

        verify(securityUtil).getCurrentUser();
    }

    // ---------------------------------------------------------
    // updateProfile()
    // ---------------------------------------------------------

    @Test
    void updateProfile_validName_updatesAndSavesUser() {
        UpdateProfileRequestDto request =
                mock(UpdateProfileRequestDto.class);
        UserResponseDto expectedDto = mock(UserResponseDto.class);

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);
        when(request.getName()).thenReturn("Updated Name");

        try (MockedStatic<UserMapper> mapper =
                     org.mockito.Mockito.mockStatic(UserMapper.class)) {

            mapper.when(() -> UserMapper.toResponseDto(currentUser))
                    .thenReturn(expectedDto);

            UserResponseDto result = userService.updateProfile(request);

            assertEquals(expectedDto, result);

            verify(currentUser).setName("Updated Name");
            verify(appUserRepository).save(currentUser);

            mapper.verify(
                    () -> UserMapper.toResponseDto(currentUser));
        }

        verify(securityUtil).getCurrentUser();
    }

    @Test
    void updateProfile_emptyName_savesProvidedName() {
        UpdateProfileRequestDto request =
                mock(UpdateProfileRequestDto.class);

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);
        when(request.getName()).thenReturn("");

        try (MockedStatic<UserMapper> mapper =
                     org.mockito.Mockito.mockStatic(UserMapper.class)) {

            mapper.when(() -> UserMapper.toResponseDto(currentUser))
                    .thenReturn(mock(UserResponseDto.class));

            userService.updateProfile(request);

            verify(currentUser).setName("");
            verify(appUserRepository).save(currentUser);
        }
    }

    // ---------------------------------------------------------
    // changePassword()
    // ---------------------------------------------------------

    @Test
    void changePassword_validPasswords_updatesAndSavesPassword() {
        ChangePasswordRequestDto request =
                mock(ChangePasswordRequestDto.class);

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);
        when(request.getCurrentPassword()).thenReturn("oldPassword");
        when(request.getNewPassword()).thenReturn("newPassword");
        when(currentUser.getPassword()).thenReturn("encodedOldPassword");

        when(passwordEncoder.matches(
                "oldPassword", "encodedOldPassword"))
                .thenReturn(true);

        when(passwordEncoder.matches(
                "newPassword", "encodedOldPassword"))
                .thenReturn(false);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        userService.changePassword(request);

        verify(passwordEncoder).matches(
                "oldPassword", "encodedOldPassword");
        verify(passwordEncoder).matches(
                "newPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword");

        verify(currentUser).setPassword("encodedNewPassword");
        verify(appUserRepository).save(currentUser);
    }

    @Test
    void changePassword_incorrectCurrentPassword_throwsBadRequestException() {
        ChangePasswordRequestDto request =
                mock(ChangePasswordRequestDto.class);

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);
        when(request.getCurrentPassword()).thenReturn("wrongPassword");
        when(currentUser.getPassword()).thenReturn("encodedOldPassword");

        when(passwordEncoder.matches(
                "wrongPassword", "encodedOldPassword"))
                .thenReturn(false);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.changePassword(request));

        assertEquals(
                "Current password is incorrect",
                exception.getMessage());

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }

    @Test
    void changePassword_newPasswordSameAsCurrent_throwsBadRequestException() {
        ChangePasswordRequestDto request =
                mock(ChangePasswordRequestDto.class);

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);
        when(request.getCurrentPassword()).thenReturn("oldPassword");
        when(request.getNewPassword()).thenReturn("oldPassword");
        when(currentUser.getPassword()).thenReturn("encodedOldPassword");

        when(passwordEncoder.matches(
                "oldPassword", "encodedOldPassword"))
                .thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.changePassword(request));

        assertEquals(
                "New password must be different from the current password.",
                exception.getMessage());

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }

    @Test
    void changePassword_passwordEncodingFails_propagatesException() {
        ChangePasswordRequestDto request =
                mock(ChangePasswordRequestDto.class);

        when(securityUtil.getCurrentUser()).thenReturn(currentUser);
        when(request.getCurrentPassword()).thenReturn("oldPassword");
        when(request.getNewPassword()).thenReturn("newPassword");
        when(currentUser.getPassword()).thenReturn("encodedOldPassword");

        when(passwordEncoder.matches(
                "oldPassword", "encodedOldPassword"))
                .thenReturn(true);

        when(passwordEncoder.matches(
                "newPassword", "encodedOldPassword"))
                .thenReturn(false);

        when(passwordEncoder.encode("newPassword"))
                .thenThrow(new IllegalStateException("Encoding failed"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.changePassword(request));

        assertEquals("Encoding failed", exception.getMessage());

        verify(appUserRepository, never()).save(any(AppUser.class));
    }
}