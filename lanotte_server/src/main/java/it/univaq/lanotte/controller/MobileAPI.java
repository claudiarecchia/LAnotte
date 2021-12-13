package it.univaq.lanotte.controller;

import it.univaq.lanotte.model.Business;
import it.univaq.lanotte.model.Order;
import it.univaq.lanotte.model.Product;
import it.univaq.lanotte.model.User;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.OrderRepository;
import it.univaq.lanotte.repository.ProductRepository;
import it.univaq.lanotte.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("api")
public class MobileAPI {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;


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

    @PostMapping("/placeOrder")
    @ResponseBody
    public String placeOrder(@RequestBody String requestBody){
        JSONArray j_arr = new JSONArray();
        ArrayList<Product> product_list = new ArrayList<>();
        Order order = new Order();

        try{
            JSONObject body = new JSONObject(requestBody);
            //System.out.println(body);

            JSONObject business1 = body.getJSONObject("business");
            Business business = businessRepository.findByBusinessName(business1.getString("business_name"));
            order.setBusiness(business);

            JSONArray products = body.getJSONArray("products");
            // System.out.println(products);
            for (int i=0; i< products.length(); i++){
                JSONObject product = products.getJSONObject(i);
                product_list.add(productRepository.findById(product.getString("id")).get());
                // System.out.println(product);
            }
            order.setProducts(product_list);

            // is there is no user listed, then the user is a guest
            // so, we save a new empty user in the db and we get back the generated User
            if (body.has("user")){
                System.out.println("utente presente");
                JSONObject user1 = body.getJSONObject("user");
                Optional<User> user = userRepository.findById(user1.getString("id"));
                order.setUser(user.get());
                System.out.println(order.getUser().getId());
            }
            else {
                User new_guest_user = userRepository.save(new User());
                order.setUser(new_guest_user);
            }

            order.setDateTime(body.getString("date_time"));

        } catch (JSONException e) { }

        Order saved_order = orderRepository.save(order);
        //JSONObject return_order = new JSONObject(saved_order.toJSON());
        j_arr.put(saved_order.toJSON());
        // j_arr.put(saved_order.toJSON());
        //System.out.println(j_arr);
        return j_arr.toString();
    }


    @PostMapping("/archive")
    @ResponseBody
    public String archive(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);
            System.out.println(body);

            Optional<User> user_opt = userRepository.findById(body.getString("id"));
            User user = user_opt.get();

            List<Order> orders = orderRepository.findByUser(user);

            for (Order o : orders)
                j_arr.put(o.toJSON());
        }
        catch (JSONException e) { }

        return j_arr.toString();
    }
}
