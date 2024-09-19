package com.tweety.SwithT.payment.repository;

import com.tweety.SwithT.payment.domain.Balance;
import com.tweety.SwithT.payment.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Long, Balance> {
    List<Balance> findByStatus(Status status);
}
