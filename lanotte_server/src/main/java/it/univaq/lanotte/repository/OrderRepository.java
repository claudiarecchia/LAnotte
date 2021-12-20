package it.univaq.lanotte.repository;

import it.univaq.lanotte.model.Order;
import it.univaq.lanotte.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUser(User user);
    Order findFirstByOrderByIdDesc();

    Order findFirstByUserOrderByIdDesc(User user);
    List<Order> findAllByUserOrderByIdDesc(User user);
}
