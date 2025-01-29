package org.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WsUser {
    private int userId;
    private String username;
}
