package it.univaq.lanotte.controller;


import com.google.firebase.messaging.FirebaseMessagingException;
import it.univaq.lanotte.Encryption;
import it.univaq.lanotte.model.*;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.OrderRepository;
import it.univaq.lanotte.repository.ProductRepository;
import it.univaq.lanotte.repository.UserRepository;
import it.univaq.lanotte.services.FirebaseMessagingService;
import it.univaq.lanotte.services.RandomNumbers;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("")
public class WebController implements ErrorController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;


    public Business getLoggedUser(){
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUser = auth.getName();
        Optional<Business> business_opt = businessRepository.findByBusinessName(loggedUser);
        return business_opt.get();
    }

    @RequestMapping("businessRegistration")
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
        Optional<Business> existingBusinessByVATNumber = businessRepository.findByVATNumber(business.getVATNumber());

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
    public String login(Model m) {
        m.addAttribute("business", new Business());
        m.addAttribute("businessName");
        m.addAttribute("password");
        m.addAttribute("business_name_error", false);
        m.addAttribute("password_error", false);

        return "login";
    }

//    @RequestMapping(value = "businessLogin", method = RequestMethod.POST)
//    public String login(@ModelAttribute Business business, Model m, HttpSession session) throws Exception {
//
//        String pageToReturn = "redirect:/index";
//
//        Optional<Business> business_opt = businessRepository.findByBusinessName(business.getBusinessName());
//        if (business_opt.isPresent()){
//            /* verify the original password and encrypted password */
//            Boolean status = Encryption.verifyPassword(business.getPassword(), business_opt.get().getPassword(), business_opt.get().getSaltValue());
//            if (status){
//                // login successful -- save business in session
//                session.setAttribute("business", business_opt.get());
//            }
//            // name is present but password does not match
//            else{
//                m.addAttribute("password_error", true);
//                pageToReturn = "login";
//            }
//        }
//        // business name is not present
//        else{
//            m.addAttribute("business_name_error", true);
//            pageToReturn = "login";
//        }
//        return pageToReturn;
//    }

    // @PreAuthorize("hasRole('BUSINESS')")
    @Secured({"ROLE_BUSINESS"})
    @RequestMapping({"businessDashboard", "/"})
    public String index(Model m, HttpSession session) {

        String pageToReturn = "index";

        // if (SecurityContextHolder.getContext().getAuthentication() != null) {
            System.out.println("PRINCIPAL: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            // System.out.println("PRINCIPAL: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails );
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String loggedUser = auth.getName();
            String role = String.valueOf(auth.getAuthorities());
            System.out.println("ROLES: " + role);
            System.out.println("LOGGED USER: " + loggedUser);

            Optional<Business> business_opt = businessRepository.findByBusinessName(loggedUser);

            // Business business = (Business) session.getAttribute("business");
            ArrayList<Order> placedOrders = null;
            ArrayList<Order> preparingOrders = null;
            ArrayList<Order> preparedOrders = null;
            if (business_opt.isPresent()) {
                Business business = business_opt.get();

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

                placedOrders = new ArrayList<>();
                preparingOrders = new ArrayList<>();
                preparedOrders = new ArrayList<>();

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
            }


            m.addAttribute("placedOrders", placedOrders);
            m.addAttribute("preparingOrders", preparingOrders);
            m.addAttribute("preparedOrders", preparedOrders);

//        }
//
//        // else not logged user
//        else{
//            pageToReturn = "error403";
//        }

        return pageToReturn;
    }

    @RequestMapping({"businessLogout"})
    public String logout(Model m, HttpSession session) {

        // session.removeAttribute("business");

        return "redirect:/businessLogin";
    }

    // @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "businessAddProduct", method = RequestMethod.GET)
    public String addProduct(Model m, HttpSession session) {

        String pageToReturn = "addProduct";

        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUser = auth.getName();
        Optional<Business> business_opt = businessRepository.findByBusinessName(loggedUser);
        if (business_opt.isPresent()){
            Business business = business_opt.get();

            m.addAttribute("businessName", business.getBusinessName());
            m.addAttribute("business", business);

            m.addAttribute("product", new Product());
            m.addAttribute("product_name_error", false);

        }

        return pageToReturn;
    }

    // @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "businessAddProduct", method = RequestMethod.POST)
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("file") MultipartFile image,
                             @RequestParam("ingredient") ArrayList<String> ingredients,
                             Model m) throws Exception {
        String pageToReturn = "menu";

        Business business = getLoggedUser();

        // selected an image
        if (!(image.getOriginalFilename().contentEquals(""))) {
            product.setImage(image.getBytes());
        }
        // no image selected --> default image
        else {
            product.setImage(Files.readAllBytes(Paths.get("src/main/resources/static/img/mule-mug-rame.jpg")));
        }

        product.setIngredients(ingredients);

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

            List<Product> product_list = business.getProducts();
            m.addAttribute("products", product_list);
        }
        m.addAttribute("business", business);
        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
   @RequestMapping(value = "businessMenu", method = RequestMethod.GET)
    public String menu(Model m, HttpSession session) {

        String pageToReturn = "menu";

            System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String loggedUser = auth.getName();
            Optional<Business> business_opt = businessRepository.findByBusinessName(loggedUser);
            if (business_opt.isPresent()){
                Business business = business_opt.get();
                m.addAttribute("businessName", business.getBusinessName());
                m.addAttribute("business", business);

                List<Product> product_list = business.getProducts();
                m.addAttribute("products", product_list);
            }
        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessDeleteProduct/{id}")
    public String deleteProduct(@PathVariable String id, Model m, HttpSession session) {

        String pageToReturn = "redirect:/businessMenu";

       Business business = getLoggedUser();

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

        m.addAttribute("business", business);
        m.addAttribute("products", business.getProducts());

        return pageToReturn;
    }

    // @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessModifyProduct", method = RequestMethod.POST)
    public String modifyProduct(@RequestParam(value="modify") String id,
                                Model m, HttpSession session) {

        String pageToReturn = "modProduct";

        Business business = getLoggedUser();

        Optional<Product> productToModify = productRepository.findById(id);

        if (productToModify.isPresent()) {
            Product prod = productToModify.get();

            m.addAttribute("business", business);
            m.addAttribute("product", prod);
            m.addAttribute("new_ingredients", new ArrayList<String>());
            m.addAttribute("product_name_error", false);

        }

        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessApplyChangesProduct", method = RequestMethod.POST)
    public String applyChangesProduct(@ModelAttribute(value="product") Product product,
                                      @RequestParam(value="ingredient") ArrayList<String> new_ingredients,
                                      @RequestParam("file") MultipartFile image,
                                      Model m, HttpSession session) throws IOException {

        String pageToReturn = "menu";

        Business business = getLoggedUser();

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
            m.addAttribute("products", business.getProducts());
        }
        m.addAttribute("business", business);
        return pageToReturn;
    }

    // @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessProfile")
    public String deleteProduct(Model m, HttpSession session) {

        String pageToReturn = "businessProfile";
        Business business = getLoggedUser();

        m.addAttribute("business", business);
        m.addAttribute("business_name_error", false);
        m.addAttribute("VATNumber_error", false);

        return pageToReturn;
    }

    // @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessApplyChanges", method = RequestMethod.POST)
    public String applyChangesBusiness(@ModelAttribute(value="business") Business modifiedBusiness,
                                      @RequestParam("file") MultipartFile image,
                                       @RequestParam("lun") ArrayList<String> lun,
                                       @RequestParam("mar") ArrayList<String> mar,
                                       @RequestParam("mer") ArrayList<String> mer,
                                       @RequestParam("gio") ArrayList<String> gio,
                                       @RequestParam("ven") ArrayList<String> ven,
                                       @RequestParam("sab") ArrayList<String> sab,
                                       @RequestParam("dom") ArrayList<String> dom,
                                      Model m) throws IOException {

        String pageToReturn = "businessProfile";
        // also change value stored in session

        Business business = getLoggedUser();

        // check error in name (already existing business)
        String modifiedBusinessName = modifiedBusiness.getBusinessName();
        if (!Objects.equals(modifiedBusinessName, business.getBusinessName())) {
            Optional<Business> existingBusiness = businessRepository.findByBusinessName(modifiedBusinessName);
            if (existingBusiness.isPresent()) {
                m.addAttribute("business_name_error", true);
            }
        } else {
            // check VATNumber
            String modifiedVATNumber = modifiedBusiness.getVATNumber();
            if (!Objects.equals(modifiedVATNumber, business.getVATNumber())) {
                Optional<Business> existingBusiness = businessRepository.findByVATNumber(modifiedVATNumber);
                if (existingBusiness.isPresent()) {
                    m.addAttribute("VATNumber_error", true);
                }
            }
            // no errors
            else{

                // save business' orders
                Optional<List<Order>> orderList = orderRepository.findByBusiness(business);
                modifiedBusiness.setNewOpeningClosingHoures(lun, mar, mer, gio, ven, sab, dom);
//                modifiedBusiness.setOpeningHoures(new HashMap<>());
//                modifiedBusiness.initOpeningHoures();
//                modifiedBusiness.setOpeningHour("0", lun.get(0));
//                modifiedBusiness.setClosingHour("0", lun.get(1));
//
//                modifiedBusiness.setOpeningHour("1", mar.get(0));
//                modifiedBusiness.setClosingHour("1", mar.get(1));
//
//                modifiedBusiness.setOpeningHour("2", mer.get(0));
//                modifiedBusiness.setClosingHour("2", mer.get(1));
//
//                modifiedBusiness.setOpeningHour("3", gio.get(0));
//                modifiedBusiness.setClosingHour("3", gio.get(1));
//
//                modifiedBusiness.setOpeningHour("4", ven.get(0));
//                modifiedBusiness.setClosingHour("4", ven.get(1));
//
//                modifiedBusiness.setOpeningHour("5", sab.get(0));
//                modifiedBusiness.setClosingHour("5", sab.get(1));
//
//                modifiedBusiness.setOpeningHour("6", dom.get(0));
//                modifiedBusiness.setClosingHour("6", dom.get(1));


                // update business on db
                business.updateRemainingValuesBusiness(modifiedBusiness);

//                business.setBusinessName(modifiedBusiness.getBusinessName());
//                business.setVATNumber(modifiedVATNumber);
//                business.setCity(modifiedBusiness.getCity());
//                business.setCAP(modifiedBusiness.getCAP());
//                business.setLocation(modifiedBusiness.getLocation());
//                business.setOpeningHoures(modifiedBusiness.getOpeningHoures());

                if (!(image.getOriginalFilename().contentEquals(""))) {
                    business.setImage(image.getBytes());
                }

                businessRepository.save(business);

                // update business' orders as well
                if (orderList.isPresent()){
                    for (Order o : orderList.get()){
                        o.setBusiness(business);
                        orderRepository.save(o);
                    }
                }
            }
        }
        m.addAttribute("business", business);
        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/signAsPrepared", method = RequestMethod.POST)
    public String signAsPrepared(@RequestParam(value="prepared") String id,
                                Model m, HttpSession session) throws FirebaseMessagingException {

        String pageToReturn = "redirect:/index";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");

        Optional<Order> preparedOrder = orderRepository.findById(id);
        if (preparedOrder.isPresent()){
            preparedOrder.get().setStatus(OrderStatus.prepared);

            // generate number to collect the order
            String randomValue = RandomNumbers.getRandomNumberString();
            preparedOrder.get().setCodeToCollect(randomValue);
            orderRepository.save(preparedOrder.get());

            // notify to the user
            firebaseMessagingService.sendPreparedOrderNotification(preparedOrder.get().getDeviceToken(),
                    randomValue,
                    preparedOrder.get().getBusiness().getBusinessName());
        }

        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
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

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/signAsCollected", method = RequestMethod.POST)
    public String signAsCollected(@RequestParam(value="toCollect") String id,
                                Model m, HttpSession session) throws FirebaseMessagingException {

        String pageToReturn = "redirect:/index";

        if (session.getAttribute("business") == null) {
            pageToReturn = "redirect:/login";
        }

        Business business = (Business) session.getAttribute("business");

        Optional<Order> collectedOrder = orderRepository.findById(id);
        if (collectedOrder.isPresent()){
            collectedOrder.get().setStatus(OrderStatus.collected);
            orderRepository.save(collectedOrder.get());

            // notify to the user
            firebaseMessagingService.sendCollectedOrderNotification(collectedOrder.get().getDeviceToken(),
                    collectedOrder.get().getBusiness().getBusinessName());

        }

        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping({"businessOrders"})
    public String orders(Model m, HttpSession session) {

        String pageToReturn = "orders";

        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUser = auth.getName();
        Optional<Business> business_opt = businessRepository.findByBusinessName(loggedUser);

        if (business_opt.isPresent()){
            Business business = business_opt.get();

            Optional<List<Order>> orderList = orderRepository.findByBusiness(business);

            if (orderList.isPresent()){
                m.addAttribute("orders", orderList.get());
            }
            m.addAttribute("businessName", business.getBusinessName());
            m.addAttribute("business", business);
        }

        return pageToReturn;
    }


    @RequestMapping("/pageError")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                System.out.println("NOT FOUND");
                return "404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                System.out.println("INTERNAL SERVER ERROR");
                return "500";
            }
        }
        return "404";
    }


}
