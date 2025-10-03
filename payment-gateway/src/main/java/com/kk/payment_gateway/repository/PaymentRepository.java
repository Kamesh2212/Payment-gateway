package com.kk.payment_gateway.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kk.payment_gateway.model.Payment;

import jakarta.persistence.LockModeType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentRef(String paymentRef);
    
//    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
//    @Query("select p from Payment p where p.id = :id")
//    Optional<Payment> findAndLockById(@Param("id") UUID id);
}
