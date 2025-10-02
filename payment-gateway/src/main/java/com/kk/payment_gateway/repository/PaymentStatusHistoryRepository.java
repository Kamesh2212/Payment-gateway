package com.kk.payment_gateway.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.kk.payment_gateway.model.PaymentStatusHistory;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {}
