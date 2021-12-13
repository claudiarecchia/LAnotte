package it.univaq.lanotte.repository;

import it.univaq.lanotte.model.Product;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.function.Function;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Product findByName(String name);
    // List<Product> findAll();
}
