package com.vibemusic.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }

}
