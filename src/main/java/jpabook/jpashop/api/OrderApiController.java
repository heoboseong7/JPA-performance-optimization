package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        /* 아래의 코드를 람다식으로 바꾼 형태 */
//        all.stream().forEach(order -> {
//            order.getMember().getName();
//            order.getDelivery().getAddress();
//
//            List<OrderItem> orderItems = order.getOrderItems();// 핵심!
//            orderItems.stream().forEach(o -> o.getItem().getName()); // orderItem과 그 내부의 item을 초기화
//        });

        // 프록시 강제 초기화
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();// 핵심!
            orderItems.stream().forEach(o -> o.getItem().getName()); // orderItem과 그 내부의 item을 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        // V2와 V3는 코드가 동일하다. V2에서 findAllByString 만 수정하면 V3가 된다.
        List<Order> orders = orderRepository.findAllWithThem();
        for (Order order : orders) {
            // JPA 입장에서 이 데이터를 줄여서 줘야할 지 판단하기 어렵기 때문에 모두 제공한다.
            // 레퍼런스까지 같은 값이 2개씩 생긴다.
            System.out.println("order ref= " + order + " id =" + order.getId());
        }
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());

        return result;
    }

    @Data // 보통 그냥 쓰지만 해주는게 너무 많아서 안쓰는게 좋은 경우도 있다.
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        // DTO 안에 Entity 가 있다. 결국 Entity 가 외부로 노출된다.
        // DTO 로 단순히 Lapping 하는 것이 아니라 Entity 에 대한 의존을 완전히 끊어야 한다. 많이 일어나는 실수
        // 껍데기만이 아니라, 속의 내용까지 전부 Entity 가 노출되지 않도록 해야한다. 단, ValueObject 같은 경우는 괜찮다.

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            // OrderItems 는 엔티티라서 null 이 나온다. -> 프록시 초기화
            order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // 있어야 orderItems 가 제대로 나온다.
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        // 필요한 데이터만 뽑아서. 기존보다 Depth를 하나 줄일 수 있다.
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
