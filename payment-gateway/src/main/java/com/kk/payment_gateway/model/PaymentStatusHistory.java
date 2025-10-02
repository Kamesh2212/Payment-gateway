package com.kk.payment_gateway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String paymentRef;
    private String oldStatus;
    @Column(nullable = false) private String newStatus;
    private String reason;
    @Column(nullable = false) private LocalDateTime changedAt;

    @PrePersist void pre() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }
}
