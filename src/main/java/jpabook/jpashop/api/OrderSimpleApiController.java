package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order
 * Order -> Member
 * Order -> Delivery
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // API 스펙 변경의 문제뿐만아니라 성능상으로도 문제가 발생한다.
    // FORCE_LAZY_LOADING 때문에 필요하지 않은 데이터들도 모두 가져오기 때문에 불필요한 쿼리들이 발생한다.
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        // Order의 Member -> Member의 List<Order> -> ... 무한 루프 발생.
        // Entity 직접 노출 시 한쪽에서 @JsonIgnore를 통해 끊어줘야 한다.
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        // jpql이 날아가기 때문에 order만 조회된다. 즉시 로딩으로 설정하면 지연로딩과 똑같이 단건 조회를 하기 때문에 N+1 문제가 발생한다.
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
            // 지연로딩을 즉시로딩으로 바꾸면 불필요한 경우에도 데이터를 조회해서 성능 문제가 발생할 수 있다. 그리고 성능 최적화의 여지가 사라진다.
            // 항상 지연로딩을 기본으로 설정하고, 성능 최적화가 필요한 경우 fetch join 을 사용한다.
        }
        // 반환된 데이터의 구조가 너무 복잡하다. 특히 Entity가 변경되는 경우 그리고 이미 사용되는 경우 문제가 많아진다.
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        // api 스펙에 딱 맞는 형태 v1보다 구조가 더 간단하다.
        // ORDER 2개
        // 1 + 회원 N + 배송 N (최악의 경우)
        // 지연로딩은 영속성 컨텍스트를 먼저 조회하고 없으면 쿼리를 통해 조회하기 때문에 모든 경우에 쿼리 발생하는 것은 아니다.
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                //.map(o -> new SimpleOrderDto(o))
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
        // v1과 마찬가지로 LAZY 로딩으로 인한 많은 쿼리 발생. 각 루프별로 member, delivery 조회 쿼리 발생
        // Order -> SQL 1번 -> 결과 주문 수 2개
        // Order 하나마다 지연 로딩으로 인한 여러개의 쿼리 발생 -> N + 1(1 + N) 문제
        // EAGER 로 바꿔도 해결이 되지 않는다.
        // 원래는 이대로 반환하면 안됨. data로 감싸줘야 한다.
        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        // fetch join 으로 이미 조회된 상태이기 때문에 지연 로딩이 발생되지 않는다.
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        List<OrderSimpleQueryDto> orderDtos = orderSimpleQueryRepository.findOrderDtos();
        return orderDtos;
    }
    // v3와 v4는 우열을 가리기 어렵다.
    // v3는 모두 가져오기 때문에 여러 API에서 재사용할 수 있다.
    // 하지만 v4는 해당 DTO에 fit하게 만들어졌기 때문에 재사용성이 낮다. 그리고 DTO로 조회한 것은 내용을 변경할 수 없다.
    // v4는 코드가 좀 더 지저분하다. API 스펙에 맞춰진다. -> 논리적으로 계층이 깨진다. trade off
    // 대부분의 리소스는 where 절에서 소모되기 때문에 대부분의 경우 성능차이가 미미하다. select 필드가 클 때 고려해야한다.
    // Repository -> Entity의 조회에 사용한다.

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        // DTO에서 Entity를 바로 받는 것은 문제가 안된다.
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 영속성 컨텍스트에서 탐색, 없으면 쿼리로 조회
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
