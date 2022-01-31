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

import java.io.File;
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
    @Field("salt_value")
    private String saltValue;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray prod_list = new JSONArray();
        json.put("id", id);
        json.put("business_name", businessName);
        json.put("VAT_number", VATNumber);
        json.put("description", description);

        if(image != null) {
            json.put("image", Base64.getEncoder().encodeToString(image));
        }

        json.put("location", location);
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
        this.numberRatings = 0;
        this.ratingSum = 0;

        List<String> passValues = EncryptPassword(password);
        this.saltValue = passValues.get(0);
        this.password = passValues.get(1);

    }

    public Business(){}

    public String getImageBase64() {
        String value = null;
        if (image != null) {
            value = Base64.getEncoder().encodeToString(image);
        }
        return value;
    }

    private List<String> EncryptPassword(String password){
        /* generates the Salt value. It can be stored in a database. */
        String saltvalue = Encryption.getSaltvalue(30);

        /* generates an encrypted password. It can be stored in a database.*/
        String encryptedpassword = Encryption.generateSecurePassword(password, saltvalue);

        List<String> returnValues = new ArrayList<>();
        returnValues.add(saltvalue);
        returnValues.add(encryptedpassword);

        return returnValues;
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

    public void setOpeningHour(String day, String hour){
        this.openingHoures.get(day).set(0, hour);

    }

    public void setClosingHour(String day, String hour){
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

    public void setNewOpeningClosingHoures(ArrayList<String> lun, ArrayList<String> mar, ArrayList<String> mer,
                                           ArrayList<String> gio, ArrayList<String> ven, ArrayList<String> sab,
                                           ArrayList<String> dom){
        this.setOpeningHoures(new HashMap<>());
        this.initOpeningHoures();
        this.setOpeningHour("0", lun.get(0));
        this.setClosingHour("0", lun.get(1));

        this.setOpeningHour("1", mar.get(0));
        this.setClosingHour("1", mar.get(1));

        this.setOpeningHour("2", mer.get(0));
        this.setClosingHour("2", mer.get(1));

        this.setOpeningHour("3", gio.get(0));
        this.setClosingHour("3", gio.get(1));

        this.setOpeningHour("4", ven.get(0));
        this.setClosingHour("4", ven.get(1));

        this.setOpeningHour("5", sab.get(0));
        this.setClosingHour("5", sab.get(1));

        this.setOpeningHour("6", dom.get(0));
        this.setClosingHour("6", dom.get(1));
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
