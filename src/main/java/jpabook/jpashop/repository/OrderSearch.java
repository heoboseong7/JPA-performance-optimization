package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderSearch extends JpaRepository<Order, Long> {

}