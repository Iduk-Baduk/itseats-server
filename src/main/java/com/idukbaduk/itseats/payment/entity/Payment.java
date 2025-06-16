package com.idukbaduk.itseats.payment.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.payment.entity.enums.DeliveryType;
import com.idukbaduk.itseats.payment.entity.enums.PaymentMethod;
import com.idukbaduk.itseats.payment.entity.enums.PaymentStatus;
import com.idukbaduk.itseats.payment.entity.enums.RiderRequestOption;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType;

    @Column(name = "discount_value", nullable = false)
    private int discountValue;

    @Column(name = "total_cost", nullable = false)
    private int totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "store_request")
    private String storeRequest;

    @Column(name = "store_request_option", nullable = false)
    private boolean storeRequestOption;

    @Column(name = "rider_request")
    private String riderRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "rider_request_option")
    private RiderRequestOption riderRequestOption;
}
