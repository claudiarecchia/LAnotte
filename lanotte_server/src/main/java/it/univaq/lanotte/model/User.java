package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Getter
@Setter
@ToString

@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;
    private String password;
    @Field("favourite_products")
    private Map<String, ArrayList<Product>> favouriteProducts;
    private Map<String, Integer> ratings;
    @Field("login_id")
    private String loginId;

    // only for admin
    private String name;

    @Transient
    private Map<String, ArrayList<JSONObject>> favouriteProductsToJSON = new HashMap<>();

    public User(String loginId){
        this.favouriteProducts = new HashMap<>();
        this.ratings = new HashMap<>();
        this.loginId = loginId;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);

        for (String key : favouriteProducts.keySet()){
            favouriteProductsToJSON.put(key, new ArrayList<>());
        }

        for(String key : favouriteProducts.keySet()){
            for (Product p : favouriteProducts.get(key)){
                favouriteProductsToJSON.get(key).add(p.toJSON());
            }
        }
        json.put("favourite_products", favouriteProductsToJSON);
        json.put("ratings", ratings);
        json.put("login_id", loginId);
        return json;
    }

    public void updateValuesFavouriteProduct(Product product, Business business){
        if (favouriteProducts.containsKey(business.getBusinessName())){
            for (Product p : favouriteProducts.get(business.getBusinessName())){
                if (p.getId().equals(product.getId())){
                    favouriteProducts.get(business.getBusinessName()).remove(p);
                    favouriteProducts.get(business.getBusinessName()).add(product);
                }
            }
        }
    }

    public void removeProductFromFavourites(Product product, Business business){
        if (favouriteProducts.containsKey(business.getBusinessName())){
            favouriteProducts.get(business.getBusinessName()).removeIf(p -> p.getId().equals(product.getId()));
        }
    }
}
