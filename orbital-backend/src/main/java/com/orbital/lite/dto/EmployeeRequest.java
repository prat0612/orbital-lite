package com.orbital.lite.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be 120 characters or fewer")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 180, message = "Email must be 180 characters or fewer")
        String email,

        @Size(max = 120, message = "Role must be 120 characters or fewer")
        String role,

        @Size(max = 120, message = "Department must be 120 characters or fewer")
        String department
) {
}
