package it.univaq.lanotte.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
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

import java.util.*;

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
            System.out.println(body);

            JSONObject business1 = body.getJSONObject("business");
            Optional<Business> business = businessRepository.findByBusinessName(business1.getString("business_name"));
            order.setBusiness(business.get());

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
            JSONObject user1 = body.getJSONObject("user");
            if ((user1.has("apple_id") && !user1.isNull("apple_id"))){

                Optional<User> user = userRepository.findByAppleId(user1.getString("apple_id"));
                // System.out.println(user);
                System.out.println("utente presente");
                order.setUser(user.get());
                System.out.println(order.getUser().getId());
            }

//            else {
//                System.out.println("utente NON presente");
//                User new_guest_user = userRepository.save(new User(""));
//                order.setUser(new_guest_user);
//            }

            order.setDateTime(body.getString("date_time"));
            // System.out.println(order.getDateTime());

        } catch (JSONException e) { }

        Order saved_order = orderRepository.save(order);
        j_arr.put(saved_order.toJSON());

        return j_arr.toString();
    }


    @PostMapping("/archive")
    @ResponseBody
    public String archive(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);
            System.out.println("BODY " + body);

            Optional<User> user_opt = userRepository.findByAppleId(body.getString("apple_id"));
            User user = user_opt.get();
            System.out.println(user);
            List<Order> orders = orderRepository.findAllByUserOrderByIdDesc(user);

            for (Order o : orders)
                j_arr.put(o.toJSON());
        }
        catch (JSONException e) { }
        System.out.println(j_arr);
        return j_arr.toString();
    }


//    @PostMapping("/lastOrder")
//    @ResponseBody
//    public String lastProductOrderedToday(@RequestBody String requestBody) {
//        JSONArray j_arr = new JSONArray();
//
//        try {
//            JSONObject body = new JSONObject(requestBody);
//            System.out.println(body);
//            Optional<User> user_opt = userRepository.findById(body.getString("id"));
//            User user = user_opt.get();
//
//            Order order = orderRepository.findFirstByUserOrderByIdDesc(user);
//            System.out.println(order);
//            j_arr.put(order.toJSON());
//        }
//        catch (JSONException e) { }
//
//        return j_arr.toString();
//    }

    @PostMapping("/getUser")
    @ResponseBody
    public String favouriteProducts(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);
            System.out.println(body);
            Optional<User> user_opt = userRepository.findByAppleId(body.getString("apple_id"));

            // already present user in the db
            if (user_opt.isPresent()) {
                User user = user_opt.get();
                j_arr.put(user.toJSON());
            }

            // is a new user -> creating a new one with the received apple_id
            else{
                User new_user = new User(body.getString("apple_id"));
                userRepository.save(new_user);
                j_arr.put(new_user.toJSON());
            }
        }
        catch (JSONException e) { }

        return j_arr.toString();
    }


    @PostMapping("/saveFavourites")
    @ResponseBody
    public String saveFavouriteProducts(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);
            // System.out.println("BODY: " + body);

            Optional<User> user_opt = userRepository.findByAppleId(body.getString("apple_id"));
            User user = user_opt.get();

            // save user's orders
            List<Order> user_orders = orderRepository.findByUser(user);

            // modify fav products
            JSONObject obj = body.getJSONObject("favourite_products");
            Map<String, ArrayList<Product>> result = new ObjectMapper().readValue(obj.toString(), new TypeReference<Map<String, ArrayList<Product>>>(){});

            // update favs
            user.setFavouriteProducts(result);

            userRepository.save(user);

            // update all user's orders
            for (Order o : user_orders){
                o.setUser(user);
                orderRepository.save(o);
            }

            j_arr.put(user.toJSON());
        }
        catch (JSONException e) { } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return j_arr.toString();
    }

    @PostMapping("/saveRatings")
    @ResponseBody
    public String saveRatings(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);
            // System.out.println("BODY: " + body);

            Optional<User> user_opt = userRepository.findByAppleId(body.getString("apple_id"));
            User user = user_opt.get();

            // save user's orders
            List<Order> user_orders = orderRepository.findByUser(user);

            // modify ratings
            JSONObject obj = body.getJSONObject("ratings");
            Map<String, Integer> result = new ObjectMapper().readValue(obj.toString(), new TypeReference<Map<String, Integer>>(){});

            // get old ratings (to compare with new to update business attributes)
            Map<String, Integer> old_ratings = user.getRatings();
            MapDifference<String, Integer> diff = Maps.difference(old_ratings, result);
            Set<String> keysOnlyInSource = diff.entriesOnlyOnLeft().keySet();
            Set<String> keysOnlyInTarget = diff.entriesOnlyOnRight().keySet();
            Map<String, MapDifference.ValueDifference<Integer>> entriesDiffering = diff.entriesDiffering();

            System.out.println("ELEMENTI DIFFERENTI:");
            System.out.println(keysOnlyInSource);
            System.out.println(keysOnlyInTarget);
            System.out.println(entriesDiffering);

            // rating for new business -> add rating
            if (keysOnlyInTarget.toString() != "[]") {
                Optional<Business> business_opt = businessRepository.findByBusinessName(keysOnlyInTarget.iterator().next());
                Business business = business_opt.get();
                business.setNumberRatings(business.getNumberRatings() + 1);
                System.out.println(result.get(keysOnlyInTarget.iterator().next()));
                business.setRatingSum(business.getRatingSum() + result.get(keysOnlyInTarget.iterator().next()));
                business.setRating((double) (business.getRatingSum() / business.getNumberRatings()));
                businessRepository.save(business);
            }

            // changed rating for an already reviewed business -> change rating
            if (entriesDiffering.toString() != "{}") {
                Optional<Business> business_opt = businessRepository.findByBusinessName(entriesDiffering.keySet().iterator().next());
                Business business = business_opt.get();

                // compute the difference
                MapDifference.ValueDifference<Integer> list_difference = entriesDiffering.values().iterator().next();
                Integer difference = list_difference.rightValue() - list_difference.leftValue();
                business.setRatingSum(business.getRatingSum() + difference);
                business.setRating((double) (business.getRatingSum() / business.getNumberRatings()));
                businessRepository.save(business);
            }


            // update ratings
            user.setRatings(result);

            userRepository.save(user);

            // update all user's orders
            for (Order o : user_orders){
                o.setUser(user);
                orderRepository.save(o);
            }

            j_arr.put(user.toJSON());
        }
        catch (JSONException e) { } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return j_arr.toString();
    }
}
