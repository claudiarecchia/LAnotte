package it.univaq.lanotte.controller;


import com.google.firebase.messaging.FirebaseMessagingException;
import it.univaq.lanotte.model.*;
import it.univaq.lanotte.repository.BusinessRepository;
import it.univaq.lanotte.repository.OrderRepository;
import it.univaq.lanotte.repository.ProductRepository;
import it.univaq.lanotte.repository.UserRepository;
import it.univaq.lanotte.services.FirebaseMessagingService;
import it.univaq.lanotte.services.RandomNumbers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
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

    @Autowired
    protected AuthenticationManager authenticationManager;


    public Business getLoggedBusinessUser(){
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUser = auth.getName();
        Optional<Business> business_opt = businessRepository.findByBusinessName(loggedUser);
        return business_opt.get();
    }

    public void businessLoginAfterRegistration(Business business, HttpServletRequest request){
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(business.getBusinessName(), business.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create a new session and add the security context.
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    }

    @RequestMapping("businessRegistration")
    public String businessRegistration(Model m) {
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
                                       HttpServletRequest request) throws Exception {
        String pageToReturn = "forward:businessDashboard";

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

            this.businessLoginAfterRegistration(business, request);
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

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping({"businessDashboard", "/"})
    public String index(Model m) {

        String pageToReturn = "index";

        Business business = getLoggedBusinessUser();

        ArrayList<Order> placedOrders;
        ArrayList<Order> preparingOrders;
        ArrayList<Order> preparedOrders;

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

        m.addAttribute("placedOrders", placedOrders);
        m.addAttribute("preparingOrders", preparingOrders);
        m.addAttribute("preparedOrders", preparedOrders);

        return pageToReturn;
    }

    @RequestMapping({"businessLogout"})
    public String logout() {
        return "redirect:/businessLogin";
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "businessAddProduct", method = RequestMethod.GET)
    public String addProduct(Model m) {

        String pageToReturn = "addProduct";
        Business business = getLoggedBusinessUser();

        m.addAttribute("business", business);
        m.addAttribute("product", new Product());
        m.addAttribute("product_name_error", false);

        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "businessAddProduct", method = RequestMethod.POST)
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("file") MultipartFile image,
                             @RequestParam("ingredient") ArrayList<String> ingredients,
                             Model m) throws Exception {
        String pageToReturn = "menu";

        Business business = getLoggedBusinessUser();

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

        // check alcohol content
        Boolean alcoholic = product.getStamps().contains(Stamps.alcoholic);
        if (alcoholic && product.getAlcoholContent() == null) {
                m.addAttribute("alcohol_content_error", true);
                pageToReturn = "addProduct";
        }
        else if (product.getAlcoholContent() != null && !alcoholic) {
            m.addAttribute("alcohol_checkbox_error", true);
            pageToReturn = "addProduct";
        }
        // check name product
        else if (alreadyExistingProduct != null) {
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
    public String menu(Model m) {

        String pageToReturn = "menu";

        Business business = getLoggedBusinessUser();
        m.addAttribute("businessName", business.getBusinessName());
        m.addAttribute("business", business);

        List<Product> product_list = business.getProducts();
        m.addAttribute("products", product_list);

        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessDeleteProduct/{id}")
    public String deleteProduct(@PathVariable String id, Model m) {

        String pageToReturn = "redirect:/businessMenu";

        Business business = getLoggedBusinessUser();

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

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessModifyProduct", method = RequestMethod.POST)
    public String modifyProduct(@RequestParam(value="modify") String id,
                                Model m) {

        String pageToReturn = "modProduct";

        Business business = getLoggedBusinessUser();

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
                                      Model m) throws IOException {

        String pageToReturn = "menu";

        Business business = getLoggedBusinessUser();

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

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessProfile")
    public String deleteProduct(Model m, HttpSession session) {

        String pageToReturn = "businessProfile";
        Business business = getLoggedBusinessUser();

        m.addAttribute("business", business);
        m.addAttribute("business_name_error", false);
        m.addAttribute("VATNumber_error", false);

        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
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

        Business business = getLoggedBusinessUser();

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
                ArrayList<ArrayList<String>> week = new ArrayList<>(Arrays.asList(lun, mar, mer, gio, ven, sab, dom));
                modifiedBusiness.setNewOpeningClosingTimes(week);

                // update business on db
                business.updateRemainingValuesBusiness(modifiedBusiness);

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
    @RequestMapping(value = "/businessSignAsPrepared", method = RequestMethod.POST)
    public String signAsPrepared(@RequestParam(value="prepared") String id) throws FirebaseMessagingException {

        String pageToReturn = "redirect:businessDashboard";

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
    @RequestMapping(value = "/businessSignAsToPrepare", method = RequestMethod.POST)
    public String signAsToPrepare(@RequestParam(value="toPrepare") String id) {

        String pageToReturn = "redirect:businessDashboard";

        // Business business = getLoggedBusinessUser();

        Optional<Order> orderToPrepare = orderRepository.findById(id);
        if (orderToPrepare.isPresent()){
            orderToPrepare.get().setStatus(OrderStatus.preparing);
            orderRepository.save(orderToPrepare.get());
        }

        return pageToReturn;
    }

    @PreAuthorize("hasRole('BUSINESS')")
    @RequestMapping(value = "/businessSignAsCollected", method = RequestMethod.POST)
    public String signAsCollected(@RequestParam(value="toCollect") String id) throws FirebaseMessagingException {

        String pageToReturn = "redirect:businessDashboard";
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
    public String orders(Model m) {

        String pageToReturn = "orders";

       Business business = getLoggedBusinessUser();

            Optional<List<Order>> orderList = orderRepository.findByBusiness(business);

            if (orderList.isPresent()){
                m.addAttribute("orders", orderList.get());
            }

            m.addAttribute("business", business);

        return pageToReturn;
    }


    @RequestMapping("/pageError")
    public String handleError(HttpServletRequest request) {
        String pageErrorToReturn = "";
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                System.out.println("NOT FOUND");
                pageErrorToReturn = "404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                System.out.println("INTERNAL SERVER ERROR");
                pageErrorToReturn = "500";
            }
            else if(statusCode == HttpStatus.FORBIDDEN.value()) {
                System.out.println("FORBIDDEN");
                pageErrorToReturn = "403";
            }
        }
        return pageErrorToReturn;
    }


}
