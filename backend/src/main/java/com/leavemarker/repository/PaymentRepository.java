package com.leavemarker.repository;

import com.leavemarker.entity.Company;
import com.leavemarker.entity.Payment;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCompany(Company company);
    List<Payment> findBySubscription(Subscription subscription);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    List<Payment> findByStatus(PaymentStatus status);
}
