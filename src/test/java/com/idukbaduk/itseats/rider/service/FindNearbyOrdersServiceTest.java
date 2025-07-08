package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.entity.enums.MemberType;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.rider.dto.NearByOrderRequest;
import com.idukbaduk.itseats.rider.dto.ReadyOrderResponse;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.entity.enums.StoreStatus;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FindNearbyOrdersServiceTest {

    @Autowired
    private RiderService riderService;

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

    @Test
    @DisplayName("라이더 위치 10km 반경 내에 있는 'COOKED' 상태의 배달만 조회한다.")
    void findNearbyOrdersTest() {
        // given

        // 0.7km
        Store storeA = createStore("서울특별시 종로구 관철동 13-22", 126.9858, 37.5693, "종로점");
        createOrder(
                "ORDER_A001",
                storeA.getLocation().getX(),
                storeA.getLocation().getY(),
                OrderStatus.WAITING,
                21000,
                storeA.getStoreAddress(),
                storeA
        );

        // 0.8km
        Store storeB = createStore("서울 중구 을지로2가 185", 126.9863, 37.5659, "을지로점");
        createOrder(
                "ORDER_B001",
                storeB.getLocation().getX(),
                storeB.getLocation().getY(),
                OrderStatus.COOKED,
                18000,
                storeB.getStoreAddress(),
                storeB
        );

        // 9.0km
        Store storeC = createStore("서울 강남구 역삼동 825-2", 127.0276, 37.4979, "강남점");
        createOrder(
                "ORDER_C001",
                storeC.getLocation().getX(),
                storeC.getLocation().getY(),
                OrderStatus.WAITING,
                35000,
                storeC.getStoreAddress(),
                storeC
        );


        // 1.4km
        Store storeD = createStore("서울 종로구 인사동 157", 126.9845, 37.5728, "인사동점");
        createOrder(
                "ORDER_D001",
                storeD.getLocation().getX(),
                storeD.getLocation().getY(),
                OrderStatus.COOKED,
                22000,
                storeD.getStoreAddress(),
                storeD
        );

        // when
        // 3km(3000m) 이내 배달 조회
        NearByOrderRequest request = new NearByOrderRequest(riderLat, riderLng);
        List<ReadyOrderResponse> nearbyOrders = riderService.findNearbyOrders(request);

        // then
        assertThat(nearbyOrders).hasSize(2);

        assertThat(nearbyOrders)
                .extracting("storeName")
                .containsExactlyInAnyOrder("을지로점", "인사동점");

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
                .storeStatus(StoreStatus.ACCEPTED)
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
            String address,
            Store store
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
                .deliveryEta(LocalDateTime.now())
                .store(store)
                .build();

        orderRepository.save(order);

        return order;
    }

}