package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<Order, Long> {

}