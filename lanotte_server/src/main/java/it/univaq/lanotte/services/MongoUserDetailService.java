package it.univaq.lanotte.services;

import it.univaq.lanotte.model.Business;
import it.univaq.lanotte.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class MongoUserDetailService implements UserDetailsService {

@Autowired
private BusinessRepository businessRepository;

    @Override
    public UserDetails loadUserByUsername(String businessName) throws UsernameNotFoundException {

        Optional<Business> business = businessRepository.findByBusinessName(businessName);

        System.out.println("NAME: " + businessName);
        System.out.println("Business found " + business.isPresent());

        if(business == null) {
            throw new UsernameNotFoundException("Business not found");
        }
        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("business"));
        return new User(business.get().getBusinessName(), business.get().getPassword(), authorities);
    }
}
