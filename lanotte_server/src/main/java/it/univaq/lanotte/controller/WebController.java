package it.univaq.lanotte.controller;


import it.univaq.lanotte.Encryption;
import it.univaq.lanotte.model.*;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.OrderRepository;
import it.univaq.lanotte.repository.ProductRepository;
import it.univaq.lanotte.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

//    @RequestMapping({"home", "/", "dashboard"})
//    public String home(Model m, HttpSession session) {
//        Business business = (Business) session.getAttribute("session");
//        m.addAttribute("business", business);
//
//        String toReturn = "index";
//
//        return toReturn;
//    }

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
        // System.out.println(existingBusiness.toString());

        Optional<Business> existingBusinessByVATNumber = businessRepository.findByVATNumber(business.getVATNumber());
        // System.out.println(existingBusinessByVATNumber.toString());

        // if in db there not exists a business with the same name
        // then the Business can be created
        if (existingBusiness.isEmpty() && existingBusinessByVATNumber.isEmpty()){

            Business new_business = new Business(business.getBusinessName(),
                    business.getVATNumber(), business.getCity(), business.getCAP(),
                    business.getLocation(), business.getPassword());

            // set default image
            new_business.setImage(Files.readAllBytes(Paths.get("src/main/resources/static/img/pub_birreria.jpg")));

            businessRepository.save(new_business);

            session.setAttribute("business", new_business);
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
    public String login(@ModelAttribute Business business, Model m, HttpSession session) throws Exception {

        String pageToReturn = "redirect:/index";

        Optional<Business> business_opt = businessRepository.findByBusinessName(business.getBusinessName());
        if (business_opt.isPresent()){
            /* verify the original password and encrypted password */
            Boolean status = Encryption.verifyPassword(business.getPassword(), business_opt.get().getPassword(), business_opt.get().getSaltValue());
            if (status){
                // login successful -- save business in session
                session.setAttribute("business", business_opt.get());

                // System.out.println(session.getAttribute("business"));
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

    @RequestMapping({"index", "/"})
    public String index(Model m, HttpSession session) {
        Business business = (Business) session.getAttribute("business");

        m.addAttribute("businessName", business.getBusinessName());
        m.addAttribute("ratingsAvg", business.getRating());
        m.addAttribute("numberRating", business.getNumberRatings());
        m.addAttribute("business", business);

        Optional<List<Order>> order_list = orderRepository.findByBusiness(business);

        if (order_list.isPresent()) {
            m.addAttribute("numberOfOrders", order_list.get().size());
        } else {
            m.addAttribute("numberOfOrders", 0);
        }

        ArrayList<Order> placedOrders = new ArrayList<>();
        ArrayList<Order> preparingOrders = new ArrayList<>();
        ArrayList<Order> preparedOrders = new ArrayList<>();

        if (order_list.isPresent()) {
            for (Order o : order_list.get()) {
                switch (o.getStatus()) {
                    case placed -> {
                        placedOrders.add(o);
                        o.groupByProductName();
                    }
                    case preparing -> {
                        preparingOrders.add(o);
                        o.groupByProductName();
                    }
                    case prepared -> {
                        preparedOrders.add(o);
                        o.groupByProductName();
                    }
                }
            }
        }

        m.addAttribute("placedOrders", placedOrders);
        m.addAttribute("preparingOrders", preparingOrders);
        m.addAttribute("preparedOrders", preparedOrders);

        return "index";
    }

    @RequestMapping({"businessLogout"})
    public String logout(Model m, HttpSession session) {

        session.removeAttribute("business");

        return "redirect:/businessLogin";
    }

    @RequestMapping(value = "addProduct", method = RequestMethod.GET)
    public String addProduct(Model m, HttpSession session) {

        String pageToReturn = "addProduct";

        // check session value
        if (session.getAttribute("business") != null) {

            Business business = (Business) session.getAttribute("business");
            m.addAttribute("businessName", business.getBusinessName());

            m.addAttribute("product", new Product());
            m.addAttribute("product_name_error", false);
        }
        // not logged in --> login
        else{
            pageToReturn = "redirect:/login";
        }
        return pageToReturn;
    }


    @RequestMapping(value = "addProduct", method = RequestMethod.POST)
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("file") MultipartFile image,
                             @RequestParam("ingredient") ArrayList<String> ingredients,
                             Model m, HttpSession session) throws Exception {
        String pageToReturn = "menu";

        // selected an image
        if (!(image.getOriginalFilename().contentEquals(""))) {
            product.setImage(image.getBytes());
        }
        // no image selected --> default image
        else {
            product.setImage(Files.readAllBytes(Paths.get("src/main/resources/static/img/mule-mug-rame.jpg")));
        }

        product.setIngredients(ingredients);
        Business business = (Business) session.getAttribute("business");

        // check if already exists a product with the same name
        Product alreadyExistingProduct = business.searchProductByName(product);
        if (alreadyExistingProduct != null){
            m.addAttribute("product_name_error", true);
            m.addAttribute("product", product);
            pageToReturn = "addProduct";
        }
        else {

            Product savedProduct = productRepository.save(product);

            // save orders for "old" business
            Optional<List<Order>> order_list = orderRepository.findByBusiness(business);
            business.addProductToMenu(savedProduct);
            // update business
            businessRepository.save(business);

            if (order_list.isPresent()){
                for (Order o : order_list.get()){
                    o.setBusiness(business);
                    orderRepository.save(o);
                }
            }

            // update stored value in session
            session.setAttribute("business", business);

            m.addAttribute("businessName", business.getBusinessName());
            List<Product> product_list = business.getProducts();
            m.addAttribute("products", product_list);
        }
        return pageToReturn;
    }

    @RequestMapping(value = "menu", method = RequestMethod.GET)
    public String menu(Model m, HttpSession session) {

        String pageToReturn = "menu";

        // check session value
        if (session.getAttribute("business") != null) {

            Business business = (Business) session.getAttribute("business");
            m.addAttribute("businessName", business.getBusinessName());

            List<Product> product_list = business.getProducts();
            m.addAttribute("products", product_list);
        }
        // not logged in --> login
        else{
            pageToReturn = "redirect:/login";
        }
        return pageToReturn;
    }

    @RequestMapping(value = "/deleteProduct/{id}")
    public String deleteProduct(@PathVariable String id, Model m, HttpSession session) {

        String pageToReturn = "redirect:/menu";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        else{
            Business business = (Business) session.getAttribute("business");

            Optional<Product> productToBeRemoved = productRepository.findById(id);

            // save orders for "old" business
            Optional<List<Order>> order_list = orderRepository.findByBusiness(business);

            // update products' list in business object
            business.removeProductFromMenu(productToBeRemoved.get());
            productRepository.deleteById(id);
            // update business in db
            businessRepository.save(business);

            // update fav for each user
            List<User> user_list = userRepository.findAll();
            for (User u : user_list){
                u.removeProductFromFavourites(productToBeRemoved.get(), business);
                userRepository.save(u);
            }

            // update old orders for business
            if (order_list.isPresent()){
                for (Order o : order_list.get()){
                    o.setBusiness(business);
                    orderRepository.save(o);
                }
            }

            m.addAttribute("businessName", business.getBusinessName());
            m.addAttribute("products", business.getProducts());
        }



        return pageToReturn;
    }

    @RequestMapping(value = "/modifyProduct", method = RequestMethod.POST)
    public String modifyProduct(@RequestParam(value="modify") String id,
                                Model m, HttpSession session) {

        String pageToReturn = "modProduct";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");
        Optional<Product> productToModify = productRepository.findById(id);

        if (productToModify.isPresent()) {
            Product prod = productToModify.get();

            m.addAttribute("businessName", business.getBusinessName());
            m.addAttribute("product", prod);
            m.addAttribute("new_ingredients", new ArrayList<String>());
            m.addAttribute("product_name_error", false);

        }

        return pageToReturn;
    }

    @RequestMapping(value = "/applyChangesProduct", method = RequestMethod.POST)
    public String applyChangesProduct(@ModelAttribute(value="product") Product product,
                                      @RequestParam(value="ingredient") ArrayList<String> new_ingredients,
                                      @RequestParam("file") MultipartFile image,
                                      Model m, HttpSession session) throws IOException {

        String pageToReturn = "menu";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");
        // there is another product with the same name in business' menu
        Product alreadyExistingProduct = business.searchProductByName(product);

        if (alreadyExistingProduct != null){
            m.addAttribute("product_name_error", true);
            pageToReturn = "modProduct";
        }
        else{
            // System.out.println("NEW INGREDIENTS" + new_ingredients);
            while (new_ingredients.contains("")){
                new_ingredients.remove("");
            }
            product.setIngredients(new_ingredients);

            if (!(image.getOriginalFilename().contentEquals(""))) {
                product.setImage(image.getBytes());
            }

            // modify business' product list
            business.replaceProductInMenu(product);

            // update business in db
            businessRepository.save(business);

            // update products' list in db
            productRepository.save(product);

            // update fav for each user
            List<User> user_list = userRepository.findAll();
            for (User u : user_list){
                u.updateValuesFavouriteProduct(product, business);
                userRepository.save(u);
            }

            // update in each order
            List<Order> order_list = orderRepository.findAll();
            for (Order o : order_list){
                o.updateValuesProduct(product);
                orderRepository.save(o);
            }

            m.addAttribute("businessName", business.getBusinessName());

            m.addAttribute("products", business.getProducts());
        }

        return pageToReturn;
    }

    @RequestMapping(value = "/businessProfile")
    public String deleteProduct(Model m, HttpSession session) {

        String pageToReturn = "businessProfile";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }
        else{
            Business business = (Business) session.getAttribute("business");
            m.addAttribute("business", business);
            m.addAttribute("business_name_error", false);
            m.addAttribute("VATNumber_error", false);
        }

        return pageToReturn;
    }


    @RequestMapping(value = "/applyChangesBusiness", method = RequestMethod.POST)
    public String applyChangesBusiness(@ModelAttribute(value="business") Business modifiedBusiness,
                                      @RequestParam("file") MultipartFile image,
                                       @RequestParam("lun") ArrayList<String> lun,
                                       @RequestParam("mar") ArrayList<String> mar,
                                       @RequestParam("mer") ArrayList<String> mer,
                                       @RequestParam("gio") ArrayList<String> gio,
                                       @RequestParam("ven") ArrayList<String> ven,
                                       @RequestParam("sab") ArrayList<String> sab,
                                       @RequestParam("dom") ArrayList<String> dom,
                                      Model m, HttpSession session) throws IOException {

        String pageToReturn = "businessProfile";
        // also change value stored in session
        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }
        else {
            Business business = (Business) session.getAttribute("business");

            // check error in name (already existing business)
            String modifiedBusinessName = modifiedBusiness.getBusinessName();
            if (!Objects.equals(modifiedBusinessName, business.getBusinessName())) {
                Optional<Business> existingBusiness = businessRepository.findByBusinessName(modifiedBusinessName);
                if (existingBusiness.isPresent()) {
                    m.addAttribute("business_name_error", true);
                    m.addAttribute("business", business);
                }
            } else {
                // check VATNumber
                String modifiedVATNumber = modifiedBusiness.getVATNumber();
                if (!Objects.equals(modifiedVATNumber, business.getVATNumber())) {
                    Optional<Business> existingBusiness = businessRepository.findByVATNumber(modifiedVATNumber);
                    if (existingBusiness.isPresent()) {
                        m.addAttribute("VATNumber_error", true);
                        m.addAttribute("business", business);
                    }
                }
                // no errors
                else{

                    // save business' orders
                    Optional<List<Order>> orderList = orderRepository.findByBusiness(business);

                    modifiedBusiness.setOpeningHoures(new HashMap<>());
                    modifiedBusiness.initOpeningHoures();
                    modifiedBusiness.setOpeningHour("0", lun.get(0));
                    modifiedBusiness.setClosingHour("0", lun.get(1));

                    modifiedBusiness.setOpeningHour("1", mar.get(0));
                    modifiedBusiness.setClosingHour("1", mar.get(1));

                    modifiedBusiness.setOpeningHour("2", mer.get(0));
                    modifiedBusiness.setClosingHour("2", mer.get(1));

                    modifiedBusiness.setOpeningHour("3", gio.get(0));
                    modifiedBusiness.setClosingHour("3", gio.get(1));

                    modifiedBusiness.setOpeningHour("4", ven.get(0));
                    modifiedBusiness.setClosingHour("4", ven.get(1));

                    modifiedBusiness.setOpeningHour("5", sab.get(0));
                    modifiedBusiness.setClosingHour("5", sab.get(1));

                    modifiedBusiness.setOpeningHour("6", dom.get(0));
                    modifiedBusiness.setClosingHour("6", dom.get(1));


                    // update business in session and on db
                    business.setBusinessName(modifiedBusiness.getBusinessName());
                    business.setVATNumber(modifiedVATNumber);
                    business.setCity(modifiedBusiness.getCity());
                    business.setCAP(modifiedBusiness.getCAP());
                    business.setLocation(modifiedBusiness.getLocation());
                    business.setOpeningHoures(modifiedBusiness.getOpeningHoures());
                    if (!(image.getOriginalFilename().contentEquals(""))) {
                        business.setImage(image.getBytes());
                    }


                    businessRepository.save(business);
                    session.removeAttribute("business");
                    session.setAttribute("business", business);

                    // update business' orders as well
                    if (orderList.isPresent()){
                        for (Order o : orderList.get()){
                            o.setBusiness(business);
                            orderRepository.save(o);
                        }
                    }

                    m.addAttribute("business", business);
                }
            }
        }
        return pageToReturn;
    }

    @RequestMapping(value = "/signAsPrepared", method = RequestMethod.POST)
    public String signAsPrepared(@RequestParam(value="prepared") String id,
                                Model m, HttpSession session) {

        String pageToReturn = "redirect:/index";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");

        Optional<Order> preparedOrder = orderRepository.findById(id);
        if (preparedOrder.isPresent()){
            preparedOrder.get().setStatus(OrderStatus.prepared);
            orderRepository.save(preparedOrder.get());
        }

        return pageToReturn;
    }

    @RequestMapping(value = "/signAsToPrepare", method = RequestMethod.POST)
    public String signAsToPrepare(@RequestParam(value="toPrepare") String id,
                                Model m, HttpSession session) {

        String pageToReturn = "redirect:/index";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");

        Optional<Order> orderToPrepare = orderRepository.findById(id);
        if (orderToPrepare.isPresent()){
            orderToPrepare.get().setStatus(OrderStatus.preparing);
            orderRepository.save(orderToPrepare.get());
        }

        return pageToReturn;
    }

    @RequestMapping(value = "/signAsCollected", method = RequestMethod.POST)
    public String signAsCollected(@RequestParam(value="toCollect") String id,
                                Model m, HttpSession session) {

        String pageToReturn = "redirect:/index";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");

        Optional<Order> orderToPrepare = orderRepository.findById(id);
        if (orderToPrepare.isPresent()){
            orderToPrepare.get().setStatus(OrderStatus.collected);
            orderRepository.save(orderToPrepare.get());
        }

        return pageToReturn;
    }

    @RequestMapping({"orders"})
    public String orders(Model m, HttpSession session) {

        String pageToReturn = "orders";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");

        Optional<List<Order>> orderList = orderRepository.findByBusiness(business);

        if (orderList.isPresent()){
            m.addAttribute("orders", orderList.get());
        }
        m.addAttribute("businessName", business.getBusinessName());
        return pageToReturn;
    }


}
