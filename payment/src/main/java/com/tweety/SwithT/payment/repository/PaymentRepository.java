package com.tweety.SwithT.payment.repository;

import com.tweety.SwithT.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Long, Payment> {
    Optional<Payment> findByImpUid(String impUid);
}
