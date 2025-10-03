package com.kk.payment_gateway.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String paymentRef;
	private String oldStatus;
	@Column(nullable = false)
	private String newStatus;
	private String reason;
	@Column(nullable = false)
	private LocalDateTime changedAt;
	@Column(nullable = false)
	private BigDecimal deltaAmount; // use for refund deltas

	@PrePersist
	void pre() {
		if (changedAt == null)
			changedAt = LocalDateTime.now();
		if (deltaAmount == null)
			deltaAmount = java.math.BigDecimal.ZERO; // <-- add this

	}

}
