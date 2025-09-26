package com.oshayer.event_manager.organizations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_org_code", columnNames = "org_code"),
                @UniqueConstraint(name = "uk_org_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_org_code_name", columnList = "org_code, name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationEntity {

    @Id
    @GeneratedValue
    private UUID id;

    // ===================== Core Info =====================
    @Column(name = "org_code", nullable = false, length = 50)
    private String orgCode;

    @Column(nullable = false)
    private String name;

    @Column
    private String address;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column
    private String phone;

    @Column
    private String website;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "owner_name")
    private String ownerName;

    // ===================== Business / Legal =====================
    @Column(name = "transac_currency", nullable = false, length = 10)
    private String transacCurrency = "AUD";   // default value

    @Column(name = "biz_license_no")
    private String bizLicenseNo;

    @Column(name = "biz_license_issue_date")
    private LocalDate bizLicenseIssueDate;

    @Column(name = "biz_license_exp_date")
    private LocalDate bizLicenseExpDate;

    // ===================== Flexible Metadata =====================
    @Column(columnDefinition = "jsonb")
    private String meta;

    // ===================== Audit =====================
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
