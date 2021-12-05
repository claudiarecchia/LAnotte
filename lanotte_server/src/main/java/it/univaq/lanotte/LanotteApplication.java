package it.univaq.lanotte;

import it.univaq.lanotte.model.Business;
import it.univaq.lanotte.model.Product;
import it.univaq.lanotte.model.User;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.ProductRepository;
import it.univaq.lanotte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import java.util.List;

@SpringBootApplication
@RestController
public class LanotteApplication {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BusinessRepository businessRepository;

    public static void main(String[] args) {
        SpringApplication.run(LanotteApplication.class, args);
    }

    @GetMapping("/hello")
    public String sayHello() {
        User user = userRepository.findByEmail("claudiarecchia97@gmail.com");
        return user.toString();
    }

    @GetMapping("/drink")
    @ResponseBody
    public String drink() {
        JSONObject obj = new JSONObject();
        Product p = productRepository.findByName("moscow mule");
        obj = p.toJSON();
        return obj.toString();
    }

    @GetMapping("/allBusiness")
    public String businesses() {
        List<Business> business_list = businessRepository.findAll();
        return business_list.toString();
    }

}
