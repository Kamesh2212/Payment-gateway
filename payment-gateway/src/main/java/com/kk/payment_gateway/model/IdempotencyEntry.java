package com.kk.payment_gateway.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "idempotency_keys", indexes = {
        @Index(name="ux_idem_key", columnList = "idem_key", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdempotencyEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="key_value", nullable = false, unique = true, length = 200)
    private String key;
    
//    @Column(name = "key_value", nullable = false)   // <— map it
//    private String keyValue;

    @Column(nullable = false)
    private String paymentRef;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist void pre() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

