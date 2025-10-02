package com.kk.payment_gateway.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor   // generates constructor with all fields
@Getter
@Setter
@NoArgsConstructor    // generates empty constructor (needed for Kafka deserialization)
public class PaymentEvent {
   
	private String paymentRef;
    private String accountNumber;
    private BigDecimal amount;
    
   
}
