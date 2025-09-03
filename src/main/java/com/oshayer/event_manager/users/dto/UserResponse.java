package com.oshayer.event_manager.users.dto;

import com.oshayer.event_manager.users.entity.EnumUserRole;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    // ===================== Identity =====================
    private UUID id;
    private String username;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String imageUrl;
    private String address;

    // ===================== Role =====================
    private EnumUserRole role;
    private String roleCode;
    private String roleName;

    // ===================== Organization =====================
    private UUID organizationId;   // just expose org id (not the whole entity)
    private String organizationName; // optional if you want to show org name

    // ===================== Verification =====================
    private boolean emailVerified;
    private OffsetDateTime emailVerifiedAt;
    private boolean mobileVerified;
    private OffsetDateTime mobileVerifiedAt;

    // ===================== Audit =====================
    private Instant signupDate;
    private OffsetDateTime signupAt;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime lastLogoutAt;

    // ===================== Ticketing =====================
    private Integer totalTicketCount;
    private Float totalTicketPrice;
    private Integer ticketsUsed;
    private Integer ticketsAtHand;

    // ===================== System Timestamps =====================
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
