package com.boardtask.kanban.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
	
	@NotBlank(message = "Name is required")
	@Size(min = 3, max = 50)
	private String name;
	
	@NotBlank(message = "Email is required")
	@Email(message ="Invalid email format")
	private String email;
	
	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must contain at least 8 characters")
	private String password;

}
