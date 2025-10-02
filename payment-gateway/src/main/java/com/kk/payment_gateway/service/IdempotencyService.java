package com.kk.payment_gateway.service;


import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kk.payment_gateway.model.IdempotencyEntry;
import com.kk.payment_gateway.repository.IdempotencyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRepository repo;

    @Transactional(readOnly = true)
    public Optional<String> findPaymentRef(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return repo.findByKey(key).map(IdempotencyEntry::getPaymentRef);
    }

    @Transactional
    public void remember(String key, String paymentRef) {
        if (key == null || key.isBlank()) return;
        repo.save(IdempotencyEntry.builder()
                .key(key).paymentRef(paymentRef).createdAt(LocalDateTime.now()).build());
    }
}

