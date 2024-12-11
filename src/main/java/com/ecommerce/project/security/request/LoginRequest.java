package com.ecommerce.project.security.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "username field must not be blank")
    private String username;

    @NotBlank(message = "Password field must not be blank")
    private String password;
}
