package it.univaq.lanotte.services;

import it.univaq.lanotte.model.Business;

import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Autowired
private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String businessName) throws UsernameNotFoundException {

        List<SimpleGrantedAuthority> auth = Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        User user = new User("username", "password", auth);

        Optional<Business> business = businessRepository.findByBusinessName(businessName);
        Optional<it.univaq.lanotte.model.User> admin = userRepository.findByName(businessName);

        System.out.println("NAME: " + businessName);
        System.out.println("Business found " + business.isPresent());
        System.out.println("Is ADMIN " + admin.isPresent());

        if (business == null && admin == null) {
            throw new UsernameNotFoundException("USER not found");
        }
        if (business.isPresent()) {
            List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_BUSINESS"));
            user = new User(business.get().getBusinessName(), business.get().getPassword(), authorities);
        }
        if (admin.isPresent()) {
            List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            user = new User(admin.get().getName(), admin.get().getPassword(), authorities);
        }
        return user;
    }
}
