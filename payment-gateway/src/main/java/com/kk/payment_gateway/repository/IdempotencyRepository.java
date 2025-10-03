package com.kk.payment_gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kk.payment_gateway.model.IdempotencyEntry;

public interface IdempotencyRepository extends JpaRepository<IdempotencyEntry, Long> {
    Optional<IdempotencyEntry> findByKey(String key);

}

