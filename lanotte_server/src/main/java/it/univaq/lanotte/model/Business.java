package it.univaq.lanotte.model;

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
    //private byte[] image;
    private String image;
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
        json.put("VAT_number", VATNumber);
        json.put("description", description);
        json.put("location", location);
        json.put("rating", rating);
        for (Product p : products)
            prod_list.put(p.toJSON());
        json.put("products", prod_list);
        json.put("opening_houres", openingHoures);
        json.put("image", image);

//        if(image != null) {
//            json.put("image", Base64.getEncoder().encodeToString(image));
//        }
        return json;
    }


    public Business(String businessName, String VATNumber, String city, String CAP, String location, String password){
        this.businessName = businessName;
        this.VATNumber = VATNumber;
        this.city = city;
        this.CAP = CAP;
        this.location = location;
        this.password = password;
        this.description = "";
        this.rating = 0.0;
        this.products = new ArrayList<>();
        this.openingHoures = new HashMap<>();
        this.numberRatings = 0;
        this.ratingSum = 0;
    }

    public Business(){}

}
