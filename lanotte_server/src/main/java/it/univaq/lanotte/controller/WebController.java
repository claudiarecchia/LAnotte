package it.univaq.lanotte.controller;


import it.univaq.lanotte.model.Business;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.OrderRepository;
import it.univaq.lanotte.repository.ProductRepository;
import it.univaq.lanotte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping({""})
public class WebController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @RequestMapping({"home", "/", "dashboard"})
    public String home(Model m, HttpSession session) {

        String toReturn = "index";

        return toReturn;
    }

    @RequestMapping({"businessRegistration"})
    public String businessRegistration(Model m, HttpSession session) {
        m.addAttribute("business", new Business());
        m.addAttribute("businessName");
        m.addAttribute("VATNumber");
        m.addAttribute("location");
        m.addAttribute("city");
        m.addAttribute("CAP");
        m.addAttribute("password");
        m.addAttribute("business_name_error", false);
        m.addAttribute("VAT_error", false);

        return "register";
    }

    @RequestMapping(value = "businessRegistration", method = RequestMethod.POST)
    public String businessRegistration(@ModelAttribute Business business,
                        Model m,
                        HttpSession session) throws Exception {

        String pageToReturn = "index";

        Optional<Business> existingBusiness = businessRepository.findByBusinessName(business.getBusinessName());
        System.out.println(existingBusiness.toString());

        Optional<Business> existingBusinessByVATNumber = businessRepository.findByVATNumber(business.getVATNumber());
        System.out.println(existingBusinessByVATNumber.toString());

        // if in db there not exists a business with the same name
        // then the Business can be created
        if (existingBusiness.isEmpty() && existingBusinessByVATNumber.isEmpty()){

            Business new_business = new Business(business.getBusinessName(),
                    business.getVATNumber(), business.getCity(), business.getCAP(),
                    business.getLocation(), business.getPassword());

            businessRepository.save(new_business);
        }

        // error messages
        else{
            if (existingBusiness.isPresent()) {
                m.addAttribute("business_name_error", true);
            }
            if (existingBusinessByVATNumber.isPresent()){
                m.addAttribute("VAT_error", true);
            }
            pageToReturn = "register";
        }
        return pageToReturn;
    }

    @RequestMapping({"businessLogin"})
    public String login(Model m, HttpSession session) {
        m.addAttribute("business", new Business());
        m.addAttribute("businessName");
        m.addAttribute("password");
        m.addAttribute("business_name_error", false);
        m.addAttribute("password_error", false);

        return "login";
    }

    @RequestMapping(value = "businessLogin", method = RequestMethod.POST)
    public String login(@ModelAttribute Business business,
                        Model m,
                        HttpSession session) throws Exception {

        String pageToReturn = "redirect:/index";

        Optional<Business> business_opt = businessRepository.findByBusinessName(business.getBusinessName());
        if (business_opt.isPresent()){
            if (Objects.equals(business_opt.get().getPassword(), business.getPassword())){
                // login successful -- save business in session
                session.setAttribute("business", business_opt.get());

                System.out.println(session.getAttribute("business"));
            }
            // name is present but password does not match
            else{
                m.addAttribute("password_error", true);
                pageToReturn = "login";
            }
        }
        // business name is not present
        else{
            m.addAttribute("business_name_error", true);
            pageToReturn = "login";
        }

        return pageToReturn;
    }

    @RequestMapping({"index"})
    public String index(Model m, HttpSession session) {
        Business business = (Business) session.getAttribute("business");
        System.out.println("index: " + business);
        m.addAttribute("businessName", business.getBusinessName());

        return "index";
    }

    @RequestMapping({"logout"})
    public String logout(Model m, HttpSession session) {

        session.removeAttribute("business");

        return "redirect:/businessLogin";
    }

}
