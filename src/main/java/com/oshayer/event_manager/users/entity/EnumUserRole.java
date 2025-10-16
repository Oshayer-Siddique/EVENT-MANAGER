package com.oshayer.event_manager.users.entity;

public enum EnumUserRole {
    ROLE_USER("801", "User"),
    ROLE_ORG_ADMIN("802", "Organization Admin"),
    ROLE_EVENT_MANAGER("803", "Event Manager"),
    ROLE_OPERATOR("804", "Operator"),
    ROLE_EVENT_CHECKER("805", "Event Checker"); // ✅ new role added

    private final String code;
    private final String displayName;

    EnumUserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EnumUserRole fromCode(String code) {
        for (EnumUserRole role : EnumUserRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}
