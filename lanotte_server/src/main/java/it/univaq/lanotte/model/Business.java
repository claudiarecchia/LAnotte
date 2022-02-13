package it.univaq.lanotte.model;

import it.univaq.lanotte.Encryption;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Getter
@Setter
@ToString

@Document(collection = "business")
public class Business {
    @Id
    private ObjectId id;
    @Field("business_name")
    private String businessName;
    @Field("VAT_number")
    private String VATNumber;
    private String description;
    private byte[] image;
    private String location;
    private Double rating;
    private ArrayList<Product> products;
    @Field("opening_houres")
    private Map<String, ArrayList<String>> openingHoures;
    @Field("number_ratings")
    private Integer numberRatings;
    @Field("rating_sum")
    private Integer ratingSum;

    private String city;
    private String CAP;
    private String password;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray prod_list = new JSONArray();
        json.put("id", id);
        json.put("business_name", businessName);
        json.put("description", description);

        if(image != null) {
            json.put("image", Base64.getEncoder().encodeToString(image));
        }
        json.put("rating", rating);
        for (Product p : products)
            prod_list.put(p.toJSON());
        json.put("products", prod_list);
        json.put("opening_houres", openingHoures);

        return json;
    }


    public Business(String businessName, String VATNumber, String city, String CAP, String location, String password){
        this.businessName = businessName;
        this.VATNumber = VATNumber;
        this.city = city;
        this.CAP = CAP;
        this.location = location;
        this.description = "";
        this.rating = 0.0;
        this.products = new ArrayList<>();
        this.openingHoures = new HashMap<>();
        this.initOpeningHoures();
        this.numberRatings = 0;
        this.ratingSum = 0;
        this.password = Encryption.passwordEncoder.encode(password);
    }

    public Business(){}

    public String getImageBase64() {
        String value = null;
        if (image != null) {
            value = Base64.getEncoder().encodeToString(image);
        }
        return value;
    }

    public void addProductToMenu(Product product){
        List<Product> product_list = this.getProducts();
        product_list.add(product);
    }

    public void removeProductFromMenu(Product product){
        for (int i=0; i< products.size(); i++){
            if (Objects.equals(products.get(i).getId(), product.getId())){
                products.remove(products.get(i));
            }
        }
    }

    public void replaceProductInMenu(Product product){
        removeProductFromMenu(product);  // works only with id value
        addProductToMenu(product);  // adds the entire object
    }

    public Product searchProductByName(Product product){
        for (Product p : products){
            if (Objects.equals(p.getName(), product.getName()) &&
                    !Objects.equals(p.getId(), product.getId())){
                return p;
            }
        }
        return null;
    }

    public String getOpeningHour(String day){
        return this.openingHoures.get(day).get(0);
    }

    public String getClosingHour(String day){
        return this.openingHoures.get(day).get(1);
    }

    public void setOpeningTime(String day, String hour){
        this.openingHoures.get(day).set(0, hour);

    }

    public void setClosingTime(String day, String hour){
        this.openingHoures.get(day).set(1, hour);
    }

    public void initOpeningHoures(){
        ArrayList<String> dayList = new ArrayList<>(
                Arrays.asList("0", "1", "2", "3", "4", "5", "6")
        );
        for (int i=0; i<7; i++){
            this.openingHoures.put(dayList.get(i), new ArrayList<>(
                    Arrays.asList("", "")
            ));
        }
    }

    public void setNewOpeningClosingTimes(ArrayList<ArrayList<String>> week){
        this.setOpeningHoures(new HashMap<>());
        this.initOpeningHoures();
        // lun
        this.setOpeningTime("0", week.get(0).get(0));
        this.setClosingTime("0", week.get(0).get(1));
        // mar
        this.setOpeningTime("1", week.get(1).get(0));
        this.setClosingTime("1", week.get(1).get(1));
        // mer
        this.setOpeningTime("2", week.get(2).get(0));
        this.setClosingTime("2", week.get(2).get(1));
        // gio
        this.setOpeningTime("3", week.get(3).get(0));
        this.setClosingTime("3", week.get(3).get(1));
        // ven
        this.setOpeningTime("4", week.get(4).get(0));
        this.setClosingTime("4", week.get(4).get(1));
        // sab
        this.setOpeningTime("5", week.get(5).get(0));
        this.setClosingTime("5", week.get(5).get(1));
        // dom
        this.setOpeningTime("6", week.get(6).get(0));
        this.setClosingTime("6", week.get(6).get(1));
    }

    public void updateRemainingValuesBusiness(Business modifiedBusiness){
        this.setBusinessName(modifiedBusiness.getBusinessName());
        this.setVATNumber(modifiedBusiness.getVATNumber());
        this.setCity(modifiedBusiness.getCity());
        this.setCAP(modifiedBusiness.getCAP());
        this.setLocation(modifiedBusiness.getLocation());
        this.setOpeningHoures(modifiedBusiness.getOpeningHoures());
    }

}
