package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
