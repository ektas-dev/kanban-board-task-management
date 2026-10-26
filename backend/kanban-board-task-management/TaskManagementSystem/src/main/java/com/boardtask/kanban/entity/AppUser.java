package com.boardtask.kanban.entity;

import java.util.List;

import com.boardtask.kanban.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = "email") })
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name cannot be empty.")
	@Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters.")
	@Column(nullable = false)
	private String name;

	@NotBlank(message = "Email cannot be empty.")
	@Email(message = "Invalid email format.")
	@Column(nullable = false, unique = true)
	private String email;

	@NotBlank(message = "Password cannot be empty.")
	@Size(min = 6, message = "Password must be at least 6 characters.")
	@Column(nullable = false)
	private String password;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role = Role.USER;

	@OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Task> tasks;

}
