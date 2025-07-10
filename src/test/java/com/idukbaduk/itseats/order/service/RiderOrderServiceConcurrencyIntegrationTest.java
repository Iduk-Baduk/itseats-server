package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.entity.enums.MemberType;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.entity.RiderAssignment;
import com.idukbaduk.itseats.rider.entity.enums.AssignmentStatus;
import com.idukbaduk.itseats.rider.entity.enums.DeliveryMethod;
import com.idukbaduk.itseats.rider.repository.RiderAssignmentRepository;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreCategory;
import com.idukbaduk.itseats.store.entity.enums.BusinessStatus;
import com.idukbaduk.itseats.store.entity.enums.StoreStatus;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreCategoryRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RiderOrderServiceConcurrencyIntegrationTest {

    @Autowired
    private RiderOrderService riderOrderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StoreCategoryRepository storeCategoryRepository;
    @Autowired
    private RiderRepository riderRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RiderAssignmentRepository riderAssignmentRepository;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private Order testOrder;
    private List<Rider> testRiders;
    private final int NUMBER_OF_CONCURRENT_RIDERS = 10;

    @BeforeEach
    void setUp() {
        riderAssignmentRepository.deleteAll();
        orderRepository.deleteAll();
        riderRepository.deleteAll();
        storeRepository.deleteAll();
        storeCategoryRepository.deleteAll();
        memberRepository.deleteAll();

        Member orderMember = Member.builder()
                .password("password")
                .email("order@example.com")
                .username("orderUser")
                .nickname("orderNick")
                .name("Order User")
                .phone("010-1111-2222")
                .memberType(MemberType.CUSTOMER)
                .build();
        memberRepository.save(orderMember);

        Member storeMember = Member.builder()
                .password("password12")
                .email("store@example.com")
                .username("storeUser")
                .nickname("storeNick")
                .name("Store User")
                .phone("010-6666-1234")
                .memberType(MemberType.OWNER)
                .build();
        memberRepository.save(storeMember);

        Member findOrderMember = memberRepository.findByUsername("orderUser")
                .orElseThrow(() -> (new MemberException(MemberErrorCode.INVALID_USERNAME)));

        Member findStoreMember = memberRepository.findByUsername("storeUser")
                .orElseThrow(() -> (new MemberException(MemberErrorCode.INVALID_USERNAME)));

        StoreCategory storeCategory = StoreCategory.builder()
                .categoryCode("테스트 카테고리 코드")
                .categoryName("테스트 카테고리 이름")
                .build();

        storeCategoryRepository.save(storeCategory);

        StoreCategory findStoreCategory = storeCategoryRepository.findByCategoryCode("테스트 카테고리 코드")
                .orElseThrow(() -> new StoreException(StoreErrorCode.CATEGORY_NOT_FOUND));

        Store testStore = Store.builder()
                .storeName("테스트 매장")
                .storeAddress("테스트 주소")
                .location(GeoUtil.toPoint(0, 0))
                .businessStatus(BusinessStatus.OPEN)
                .member(findStoreMember)
                .orderable(true)
                .storeCategory(findStoreCategory)
                .storePhone("010-1234-5678")
                .storeStatus(StoreStatus.ACCEPTED)
                .build();
        storeRepository.save(testStore);

        testOrder = Order.builder()
                .orderNumber("CONCURRENCY_TEST_ORDER")
                .orderPrice(10000)
                .orderStatus(OrderStatus.COOKED)
                .deliveryFee(3000)
                .deliveryAddress("Test Address")
                .member(findOrderMember)
                .deliveryEta(LocalDateTime.now().plusHours(1))
                .destinationLocation(GeoUtil.toPoint(0 ,0))
                .storeLocation(GeoUtil.toPoint(0 ,0))
                .deliveryType(DeliveryType.DEFAULT)
                .store(testStore)
                .build();
        orderRepository.save(testOrder);

        testRiders = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CONCURRENT_RIDERS; i++) {
            Member riderMember = Member.builder()
                    .password("password")
                    .email("rider" + i + "@example.com")
                    .username("riderUser" + i)
                    .nickname("riderNick" + i)
                    .name("Rider " + i)
                    .phone("010-3333-444" + i)
                    .memberType(MemberType.RIDER)
                    .build();
            memberRepository.save(riderMember);


            Rider rider = Rider.builder()
                    .member(riderMember)
                    .deliveryMethod(DeliveryMethod.BICYCLE)
                    .isWorking(true)
                    .preferredArea("종로")
                    .build();
            riderRepository.save(rider);
            testRiders.add(rider);

            RiderAssignment assignment = RiderAssignment.builder()
                    .rider(rider)
                    .order(testOrder)
                    .assignmentStatus(AssignmentStatus.PENDING)
                    .build();
            riderAssignmentRepository.save(assignment);
        }
    }

    @AfterEach
    void deleteData() {
        riderAssignmentRepository.deleteAll();
        orderRepository.deleteAll();
        riderRepository.deleteAll();
        storeRepository.deleteAll();
        storeCategoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("여러 라이더가 동시에 주문을 수락할 때 한 명만 성공해야 한다")
    void acceptOrder_concurrently_onlyOneSucceeds() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_RIDERS); // 10개의 쓰레드 풀을 생성
        CountDownLatch readyLatch = new CountDownLatch(NUMBER_OF_CONCURRENT_RIDERS); // 모든 쓰레드 준비 대기
        CountDownLatch startLatch = new CountDownLatch(1); // 시작 신호용
        CountDownLatch doneLatch = new CountDownLatch(NUMBER_OF_CONCURRENT_RIDERS); // 모든 작업 완료 대기

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        for (int i = 0; i < NUMBER_OF_CONCURRENT_RIDERS; i++) {
            final int index = i;
            // 쓰레드 풀 시작
            executorService.submit(() -> {
                try {
                    String riderName = testRiders.get(index).getMember().getUsername();

                    readyLatch.countDown();
                    startLatch.await();

                    try {
                        riderOrderService.acceptDelivery(riderName, testOrder.getOrderId());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("주문 수락 실패: rider={}, error={}", riderName, e.getMessage());
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        
        executorService.shutdown();

        assertThat(failCount.get()).isEqualTo(9);
        assertThat(successCount.get()).isEqualTo(1);
    }
}
