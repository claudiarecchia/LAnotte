package it.univaq.lanotte.controller;

import it.univaq.lanotte.model.Business;
import it.univaq.lanotte.model.Product;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.ProductRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api")
public class MobileAPI {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BusinessRepository businessRepository;


    @GetMapping("/drink")
    @ResponseBody
    public String drink() {
        JSONArray j_arr = new JSONArray();
        // JSONObject obj = new JSONObject();
        Product p = productRepository.findByName("moscow mule");
        // obj = p.toJSON();
        j_arr.put(p.toJSON());
        j_arr.put(p.toJSON());
        j_arr.put(p.toJSON());
        return j_arr.toString();
    }

    @GetMapping("/allProducts")
    @ResponseBody
    public String products() {
        JSONArray j_arr = new JSONArray();
        List<Product> product_list = productRepository.findAll();

        for(Product prod: product_list)
            j_arr.put(prod.toJSON());

        return j_arr.toString();
    }

    @GetMapping("/allBusinesses")
    @ResponseBody
    public String businesses() {
        JSONArray j_arr = new JSONArray();
        List<Business> business_list = businessRepository.findAll();
        for(Business bus: business_list)
            j_arr.put(bus.toJSON());

        return j_arr.toString();
    }
}
