package com.oshayer.event_manager.users.entity;

import com.oshayer.event_manager.organizations.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        },
        indexes = {
                @Index(name = "idx_users_name", columnList = "full_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    // ===================== Role =====================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumUserRole role = EnumUserRole.ROLE_USER;

    @Column(name = "role_code", nullable = false, length = 10)
    private String roleCode;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    // ===================== Organization =====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_user_org"))
    private OrganizationEntity organization;

    // ===================== Personal Info =====================
    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column
    private String address;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    // ===================== Authentication =====================
    @Column(name = "password_hash")
    private String passwordHash;

    // ===================== Verification =====================
    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_expiry")
    private Instant emailVerificationExpiry;

    @Column(name = "is_mobile_verified", nullable = false)
    private boolean mobileVerified = false;

    @Column(name = "mobile_verified_at")
    private OffsetDateTime mobileVerifiedAt;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private Instant resetPasswordExpiry;

    // ===================== Activity / Audit =====================
    @Column(name = "signup_date", nullable = false)
    private Instant signupDate = Instant.now();

    @Column(name = "signup_at")
    private OffsetDateTime signupAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "last_logout_at")
    private OffsetDateTime lastLogoutAt;

    // ===================== Ticketing Behavior =====================
    @Column(name = "total_ticket_count", nullable = false)
    private Integer totalTicketCount = 0;

    @Column(name = "total_ticket_price", nullable = false)
    private Float totalTicketPrice = 0.0f;

    @Column(name = "tickets_used", nullable = false)
    private Integer ticketsUsed = 0;

    @Column(name = "tickets_at_hand", nullable = false)
    private Integer ticketsAtHand = 0;

    // ===================== Validation =====================
    @Column(name = "data_digest")
    private String dataDigest;

    @Column(name = "user_consent")
    private String userConsent;

    // ===================== System Timestamps =====================
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ===================== Sync role fields =====================
    @PrePersist
    @PreUpdate
    private void syncRoleMetadata() {
        if (role != null) {
            this.roleCode = role.getCode();
            this.roleName = role.getDisplayName();
        }
    }
}
