package jpabook.jpashop.repository.order.simplequery;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class OrderSimpleQueryRepository {

    private final EntityManager em;
    public OrderSimpleQueryRepository(EntityManager em) {
        this.em = em;
    }

    // 기존의 Repository는 Entity 조회만을 위해 사용한다.
    // query service, query repository로 뽑아낸다.
    // 화면에 dependency 한 것을 애매하게 Repository 에 넣지 않는다.

    public List<OrderSimpleQueryDto> findOrderDtos() {
        // 서로 다른 형태이기 때문에 기본적으로 Dto에 매핑될 수 없다. new를 통해 해결한다.
        // jpql의 결과를 DTO로 직접 변환
        // join 까지는 동일하지만 select 절에서 필요한 부분만을 가져오기 때문에 더 효율적이다.
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, o.delivery.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderSimpleQueryDto.class).
                getResultList();
    }
}
