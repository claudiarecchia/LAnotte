package it.univaq.lanotte.controller;


import it.univaq.lanotte.Encryption;
import it.univaq.lanotte.model.Business;
import it.univaq.lanotte.model.Order;
import it.univaq.lanotte.model.Product;
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
import java.util.ArrayList;
import java.util.List;
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

        String pageToReturn = "redirect:/index";

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

    @RequestMapping(value = "addProduct", method = RequestMethod.GET)
    public String addProduct(Model m, HttpSession session) {

        String pageToReturn = "addProduct";

        // check session value
        if (session.getAttribute("business") != null) {

            Business business = (Business) session.getAttribute("business");
            m.addAttribute("businessName", business.getBusinessName());

            m.addAttribute("product", new Product());
            m.addAttribute("name");
            m.addAttribute("product_name_error", false);
            m.addAttribute("category");
            // m.addAttribute("image");
            m.addAttribute("stamps", new ArrayList<String>());
            m.addAttribute("ingredients", new ArrayList<String>());
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
                             Model m, HttpSession session) throws Exception {
        String pageToReturn = "addProduct";
        //String pageToReturn = "redirect:/index";
        System.out.println(product.getName());

        if (!(image.getOriginalFilename().contentEquals(""))) {
            System.out.println(image);
            System.out.println(image.getBytes());

            product.setImage(image.getBytes());
        }
        Business business = (Business) session.getAttribute("business");

        Product alreadyExistingProduct = business.searchProductByName(product);
        if (alreadyExistingProduct != null){
            m.addAttribute("product_name_error", true);
            m.addAttribute("product", product);
        }

        // save orders for "old" business
        Optional<List<Order>> order_list = orderRepository.findByBusiness(business);

        business.addProductToMenu(product);

        if (order_list.isPresent()){
            for (Order o : order_list.get()){
                o.setBusiness(business);
                orderRepository.save(o);
            }
        }

        productRepository.save(product);

        // update business
        businessRepository.save(business);

        m.addAttribute("product", new Product());

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

        Business business = (Business) session.getAttribute("business");

        Optional<Product> productToBeRemoved = productRepository.findById(id);

        // update products' list in business object
        business.removeProductFromMenu(productToBeRemoved.get());
        productRepository.deleteById(id);
        // update business in db
        businessRepository.save(business);

        m.addAttribute("businessName", business.getBusinessName());
        m.addAttribute("products", business.getProducts());

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

            System.out.println(prod);

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

        System.out.println("NEW INGREDIENTS" + new_ingredients);
        product.setIngredients(new_ingredients);

        if (!(image.getOriginalFilename().contentEquals(""))) {
            System.out.println(image);
            System.out.println(image.getBytes());

            product.setImage(image.getBytes());
        }

        // modify business' product list
        business.replaceProductInMenu(product);

        // update business in db
        businessRepository.save(business);

        // update products' list in db
        productRepository.save(product);

        m.addAttribute("businessName", business.getBusinessName());

        m.addAttribute("products", business.getProducts());
        return pageToReturn;
    }

}
