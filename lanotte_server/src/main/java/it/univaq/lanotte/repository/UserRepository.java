package it.univaq.lanotte.repository;

import it.univaq.lanotte.model.Business;
import it.univaq.lanotte.model.Product;
import it.univaq.lanotte.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Repository
public interface UserRepository extends MongoRepository<User, String>{
    User findByEmail(String email);
}
