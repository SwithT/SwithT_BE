package com.tweety.SwithT.payment.repository;

import com.tweety.SwithT.payment.domain.Payments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments, Long> {
    Optional<Payments> findByImpUid(String impUid);
    Page<Payments> findByMemberId(Pageable pageable, Long tuteeId);
}
