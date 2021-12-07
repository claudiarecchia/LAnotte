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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        // List<JSONObject> prod_list = new ArrayList<>();
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
        if(image != null) {
            json.put("image", Base64.getEncoder().encodeToString(image));
        }
        return json;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getVATNumber() {
        return VATNumber;
    }

    public void setVATNumber(String VATNumber) {
        this.VATNumber = VATNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }
}
