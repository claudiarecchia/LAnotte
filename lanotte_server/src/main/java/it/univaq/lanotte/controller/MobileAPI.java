package it.univaq.lanotte.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import it.univaq.lanotte.model.*;
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

/**
 * class which permits mobile devices to ask for L'Anotte resources;
 * reachable by adding "/api/" to requests.
 */
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

    /**
     *
     * @return a stringified json array containing all the products found in the db
     */
    @GetMapping("/allProducts")
    @ResponseBody
    public String products() {
        JSONArray j_arr = new JSONArray();
        List<Product> product_list = productRepository.findAll();

        for(Product prod: product_list)
            j_arr.put(prod.toJSON());

        return j_arr.toString();
    }

    /**
     *
     * @return a stringified json array containing all the businesses found in the db
     */
    @GetMapping("/allBusinesses")
    @ResponseBody
    public String businesses() {
        JSONArray j_arr = new JSONArray();
        List<Business> business_list = businessRepository.findAll();

        for(Business bus: business_list)
            j_arr.put(bus.toJSON());

        return j_arr.toString();
    }

    /**
     *
     * @param requestBody contains:
     *      the Order object, which contains:
     *              - the Business object (name: business)
     *              - the products list in which each element is a Product object (name: products)
     *              - the User object (name: user)
     *              - the String date_time already in the form "gg/mm/aaaa, HH:mm" (name: date_time)
     *
     * @param token added in the "authorization" field of the request. It is the code
     *              representing the device to which will be sent the notification when the order changes its status
     *
     * @return the Order object, into a stringified jsonarray
     */
    @PostMapping("/placeOrder")
    @ResponseBody
    public String placeOrder(@RequestBody String requestBody, @RequestHeader("authorization") String token) {
        JSONArray j_arr = new JSONArray();
        ArrayList<Product> product_list = new ArrayList<>();
        Order order = null;
        System.out.println("TOKEN: " + token);

        try {
            JSONObject body = new JSONObject(requestBody);

            JSONObject business1 = body.getJSONObject("business");
            Optional<Business> business = businessRepository.findByBusinessName(business1.getString("business_name"));
            JSONArray products = body.getJSONArray("products");
            // System.out.println(products);
            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                product_list.add(productRepository.findById(product.getString("id")).get());
            }

            JSONObject user1 = body.getJSONObject("user");
            Optional<User> user = userRepository.findByLoginId(user1.getString("login_id"));

            order = new Order(business.get(), product_list, token, user.get(), body.getString("date_time"));

        } catch (JSONException e) {
        }

        Order saved_order = orderRepository.save(order);
        j_arr.put(saved_order.toJSON());

        return j_arr.toString();
    }

    /**
     *
     * @param requestBody containing the User object (and his "login_id" as a String)
     * @return all the orders made by the User, in a stringified jsonarray
     */
    @PostMapping("/archive")
    @ResponseBody
    public String archive(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);

            Optional<User> user_opt = userRepository.findByLoginId(body.getString("login_id"));
            User user = user_opt.get();
            List<Order> orders = orderRepository.findAllByUserOrderByIdDesc(user);

            for (Order o : orders)
                j_arr.put(o.toJSON());
        }
        catch (JSONException e) { }
        return j_arr.toString();
    }

    /**
     *
     * @param requestBody containing the User object (and his "login_id" as a String)
     * @return the User object, in a stringified jsonarray
     */
    @PostMapping("/getUser")
    @ResponseBody
    public String getUserInfos(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);
            Optional<User> user_opt = userRepository.findByLoginId(body.getString("login_id"));

            if (user_opt.isPresent()){
                User user = user_opt.get();
                j_arr.put(user.toJSON());
            }
            // new user
            else {
                User newUser = new User(body.getString("login_id"));
                userRepository.save(newUser);
                j_arr.put(newUser.toJSON());
            }
        }
        catch (JSONException e) { }

        return j_arr.toString();
    }

    /**
     *
     * @param requestBody containing the User object, which contains
     *                    - his "login_id" as a String (name: login_id)
     *                    - his favourite_products as a List of Product (name: favourite_products)
     *
     * @return the updated User object, in a stringified jsonarray
     */
    @PostMapping("/saveFavourites")
    @ResponseBody
    public String saveFavouriteProducts(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);

            Optional<User> user_opt = userRepository.findByLoginId(body.getString("login_id"));
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

    /**
     *
     * @param requestBody containing the User object, which contains
     *                    - his login_id as a String (name: login_id)
     *                    - his ratings as an array of [String, Int] where String is the name of the business
     *                      and Int is the rating of the user for that business. (name: ratings)
     *
     * @return the updated User object, in a stringified jsonarray
     */
    @PostMapping("/saveRatings")
    @ResponseBody
    public String saveRatings(@RequestBody String requestBody) {
        JSONArray j_arr = new JSONArray();

        try {
            JSONObject body = new JSONObject(requestBody);
            // System.out.println("BODY: " + body);

            Optional<User> user_opt = userRepository.findByLoginId(body.getString("login_id"));
            User user = user_opt.get();


            // modify ratings
            JSONObject obj = body.getJSONObject("ratings");
            Map<String, Integer> result = new ObjectMapper().readValue(obj.toString(), new TypeReference<Map<String, Integer>>(){});

            // get old ratings (to compare with new to update business attributes)
            Map<String, Integer> old_ratings = user.getRatings();
            MapDifference<String, Integer> diff = Maps.difference(old_ratings, result);
            Set<String> keysOnlyInTarget = diff.entriesOnlyOnRight().keySet();
            Map<String, MapDifference.ValueDifference<Integer>> entriesDiffering = diff.entriesDiffering();

            // rating for new business -> add rating
            if (keysOnlyInTarget.toString() != "[]") {
                Optional<Business> business_opt = businessRepository.findByBusinessName(keysOnlyInTarget.iterator().next());
                Business business = business_opt.get();

                // save all orders with the new computed ratings for the business (before modifying)
                Optional<List<Order>> order_list = orderRepository.findByBusiness(business);

                // apply mod
                business.setNumberRatings(business.getNumberRatings() + 1);
                business.setRatingSum(business.getRatingSum() + result.get(keysOnlyInTarget.iterator().next()));
                business.setRating((double) (business.getRatingSum() / business.getNumberRatings()));

                businessRepository.save(business);

                // modify in orders
                if (order_list.isPresent()){
                    for (Order o : order_list.get()){
                        if (o.getBusiness().getId().equals(business.getId())){
                            o.updateValuesBusiness(business);
                            orderRepository.save(o);
                        }
                    }
                }
            }

            // changed rating for an already reviewed business -> change rating
            else if (entriesDiffering.toString() != "{}") {
                Optional<Business> business_opt = businessRepository.findByBusinessName(entriesDiffering.keySet().iterator().next());
                Business business = business_opt.get();

                // save all orders with the new computed ratings for the business (before modifying)
                Optional<List<Order>> order_list = orderRepository.findByBusiness(business);

                // compute the difference
                MapDifference.ValueDifference<Integer> list_difference = entriesDiffering.values().iterator().next();
                Integer difference = list_difference.rightValue() - list_difference.leftValue();
                business.setRatingSum(business.getRatingSum() + difference);
                business.setRating((double) (business.getRatingSum() / business.getNumberRatings()));
                businessRepository.save(business);

                // modify in orders
                if (order_list.isPresent()){
                    for (Order o : order_list.get()){
                        if (o.getBusiness().getId().equals(business.getId())){
                            o.updateValuesBusiness(business);
                            orderRepository.save(o);
                        }
                    }
                }
            }

            // all user's orders
            List<Order> user_orders = orderRepository.findByUser(user);

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
