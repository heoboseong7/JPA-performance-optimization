package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class OrderRepository {

    private final EntityManager em;

    public OrderRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll() {
        return em.createQuery("select o from Order o", Order.class)
                .getResultList();
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {

            String jpql = "select o from Order o join o.member m";
            boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    // fetch join을 이용한 N+1 문제 해결
    // Order, Member, Delivery를 join해서 한번에 가져온다.
    // fetch 는 JPA만 있는 문법이다.
    // fetch join 은 자주 사용되기 때문에 깊이있게 이해하는 것이 중요하다.
    // 성능 문제의 90%는 이 문제에 해당된다.
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();

    }


    public List<Order> findAllWithThem() {
        return em.createQuery(
                        // 1. 쿼리에 distinct 키워드 추가 2. 루트 엔티티가 중복인 경우 중복을 걸러서 컬렉션이 담아준다.
                        // 데이터베이스의 distinct 는 모든 값이 같아야 중복 제거가 가능하다. 따라서 전송된 쿼리를 돌려보면 4개가 나온다.
                        // JPA 에서 자체적으로 Order 가 같은 id 값이면 중복을 제거해준다.
                        // 페치 조인을 하게되면 페이징이 불가능하다는 치명적 단점이 있다. 쿼리에서가 아닌 메모리에서 페이징 처리를 한다. - 경고 발생
                        // 1만개가 있다면 일단 1만개를 메모리로 가져온 뒤 페이징 처리를 한다.
                        // Hibernate 가 이러한 선택을 한 이유:
                        // 데이터베이스에서는 일대다 관계에서 다 기준으로 데이터가 뻥튀기 되기 때문에 OrderItem 기준으로 페이징 되버린다.
                        // 일대다 조인을 한 순간 Order 의 기준 자체가 틀어진다.
                        // 컬렉션 페치 조인은 1개만 사용할 수 있다. 둘 이상 사용할 경우 일대 다의 다 -> N*M으로 매우 복잡해진다. ROW도 많아진다.
                        // 개수가 안맞거나 정합성이 깨질 수 있다.
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery" + // 기존과 똑같다.
                        " join fetch o.orderItems oi" +
                        // order 2개, orderItems 4개 결국 Order 가 4개가 된다.
                        // 가져올 데이터가 2배가 된다.
                        // 1:N에서 N만큼 데이터가 증가한다. 이게 발생한지는 Hibernate 입장에서 모른다. 중복된 데이터 발생.
                        " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        // ToOne 관계는 페치 조인으로 잡는 것이 좋다.
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}

