package com.idukbaduk.itseats.payment.service;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
