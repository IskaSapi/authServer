package com.auth.server.authServer.security.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private int statusCode;
    private String status;
}
