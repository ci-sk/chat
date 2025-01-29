package org.example.entity.vo.request;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String email;
}
