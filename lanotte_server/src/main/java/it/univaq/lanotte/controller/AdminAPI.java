package it.univaq.lanotte.controller;

import it.univaq.lanotte.model.*;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.OrderRepository;
import it.univaq.lanotte.repository.ProductRepository;
import it.univaq.lanotte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping({""})
public class AdminAPI {

    @Autowired
    BusinessRepository businessRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping({"adminDashboard"})
    public String index(Model m) {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUser = auth.getName();

        List<Business> businesses = businessRepository.findAll();
        m.addAttribute("businesses", businesses);

        return "adminIndex";
    }

}
