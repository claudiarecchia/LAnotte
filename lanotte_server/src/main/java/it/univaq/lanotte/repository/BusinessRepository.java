package it.univaq.lanotte.repository;

import it.univaq.lanotte.model.Business;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends MongoRepository<Business, String> {
    Business findByBusinessName(String businessName);
}
