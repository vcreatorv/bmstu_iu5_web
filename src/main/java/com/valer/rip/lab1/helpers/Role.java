package com.valer.rip.lab1.helpers;

public enum Role {
    BUYER(1),
    MANAGER(2),
    ADMIN(3);

    private final int value;

    Role(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Role fromValue(int value) {
        for (Role role : Role.values()) {
            if (role.getValue() == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role value: " + value);
    }
}
