package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Order
 * Order -> Member
 * Order -> Delivery
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

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
}
