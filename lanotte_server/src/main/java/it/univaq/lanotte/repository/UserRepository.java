package it.univaq.lanotte.repository;

import it.univaq.lanotte.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByLoginId(String appleID);
    Optional<User> findByName(String name);
}
