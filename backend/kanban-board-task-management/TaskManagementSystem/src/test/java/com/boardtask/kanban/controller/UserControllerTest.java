package com.boardtask.kanban.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardtask.kanban.controller.UserController;
import com.boardtask.kanban.dto.user.ChangePasswordRequestDto;
import com.boardtask.kanban.dto.user.UpdateProfileRequestDto;
import com.boardtask.kanban.dto.user.UserResponseDto;
import com.boardtask.kanban.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private org.springframework.test.web.servlet.request
            .RequestPostProcessor authenticatedUser() {
        return user("testuser").authorities(
                new SimpleGrantedAuthority("ROLE_USER"));
    }

    private UserResponseDto createUserResponse() {
        UserResponseDto dto = org.mockito.Mockito.mock(
                UserResponseDto.class);

        when(dto.getId()).thenReturn(1L);
        when(dto.getName()).thenReturn("John Doe");
        when(dto.getEmail()).thenReturn("john@example.com");

        return dto;
    }

    // ---------------------------------------------------------
    // GET /api/users/profile-details
    // ---------------------------------------------------------

    @Test
    void getProfile_authenticatedUser_returnsOk() throws Exception {
        UserResponseDto response = createUserResponse();

        when(userService.getProfile()).thenReturn(response);

        mockMvc.perform(get("/api/users/profile-details")
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email")
                        .value("john@example.com"));

        verify(userService).getProfile();
    }

    // ---------------------------------------------------------
    // PUT /api/users/update-profile
    // ---------------------------------------------------------

    @Test
    void updateProfile_validRequest_returnsOk() throws Exception {
        UpdateProfileRequestDto request =
                new UpdateProfileRequestDto();
        request.setName("Updated Name");

        UserResponseDto response = createUserResponse();
        when(response.getName()).thenReturn("Updated Name");

        when(userService.updateProfile(
                any(UpdateProfileRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/users/update-profile")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(userService).updateProfile(
                any(UpdateProfileRequestDto.class));
    }

    // ---------------------------------------------------------
    // PUT /api/users/change-password
    // ---------------------------------------------------------

    @Test
    void changePassword_validRequest_returnsNoContent()
            throws Exception {

        ChangePasswordRequestDto request =
                new ChangePasswordRequestDto();
        request.setCurrentPassword("OldPassword123!");
        request.setNewPassword("NewPassword123!");

        mockMvc.perform(put("/api/users/change-password")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(
                any(ChangePasswordRequestDto.class));
    }

    // ---------------------------------------------------------
    // Malformed JSON
    // ---------------------------------------------------------

    @Test
    void updateProfile_malformedJson_returnsBadRequest()
            throws Exception {

        mockMvc.perform(put("/api/users/update-profile")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void changePassword_malformedJson_returnsBadRequest()
            throws Exception {

        mockMvc.perform(put("/api/users/change-password")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
