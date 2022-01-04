package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString

@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;
    private String email;
    private String password;
    @Field("favourite_products")
    private Map<String, ArrayList<Product>> favouriteProducts;
    private Map<String, Integer> ratings;

    public User(){
        this.favouriteProducts = new HashMap<>();
        this.ratings = new HashMap<>();
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("email", email);
        json.put("password", password);
        json.put("favourite_products", favouriteProducts);
        json.put("ratings", ratings);
        System.out.println(json);
        return json;
    }

}
