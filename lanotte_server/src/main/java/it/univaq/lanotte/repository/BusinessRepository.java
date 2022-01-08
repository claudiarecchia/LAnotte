package it.univaq.lanotte.repository;

import it.univaq.lanotte.model.Business;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends MongoRepository<Business, String> {
    Optional<Business> findByBusinessName(String businessName);
}
