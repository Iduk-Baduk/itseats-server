package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.entity.enums.MemberType;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderQueryServiceTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StoreCategoryRepository storeCategoryRepository;

    private Member member;
    private StoreCategory storeCategory;
    private double riderLng;
    private double riderLat;
    private double userLng;
    private double userLat;
    int deliveryFee;

    @BeforeEach
    void setUp() {
        // 라이더 위치: 서울 종로구
        riderLng = 126.9829;
        riderLat = 37.5704;

        userLng = 126.9851;
        userLat = 37.5631;

        deliveryFee = 6000;

        member = Member.builder()
                .password("Password123!")
                .email("example@email.com")
                .username("testuser")
                .nickname("닉넴")
                .name("한상희")
                .phone("010-1234-1234")
                .memberType(MemberType.CUSTOMER)
                .build();

        memberRepository.save(member);

        storeCategory = StoreCategory.builder()
                .categoryCode("카테고리 코드")
                .categoryName("테스트 카테고리")
                .build();

        storeCategoryRepository.save(storeCategory);
    }

    private Store createStore(String address, double lng, double lat, String name) {
        Store store = Store.builder()
                .member(member)
                .storePhone("010-1234-1234")
                .storeCategory(storeCategory)
                .storeAddress(address)
                .orderable(true)
                .businessStatus(BusinessStatus.OPEN)
                .defaultDeliveryFee(deliveryFee)
                .location(GeoUtil.toPoint(lng, lat))
                .description("가게입니다.")
                .storeName(name)
                .build();

        storeRepository.save(store);

        return store;
    }

    private Order createOrder(
      String orderNumber,
      double lng,
      double lat,
      OrderStatus orderStatus,
      int price,
      String address
    ) {
        Order order = Order.builder()
                .deliveryFee(deliveryFee)
                .orderNumber(orderNumber)
                .storeLocation(GeoUtil.toPoint(lng, lat))
                .deliveryType(DeliveryType.DEFAULT)
                .orderStatus(orderStatus)
                .orderPrice(price)
                .deliveryAddress(address)
                .member(member)
                .destinationLocation(GeoUtil.toPoint(userLng, userLat))
                .build();

        orderRepository.save(order);

        return order;
    }

}