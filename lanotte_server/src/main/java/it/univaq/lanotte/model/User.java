package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.data.mongodb.core.mapping.Field;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Base64;
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

    public User(){ }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        Gson gson = new Gson();
        JSONArray fav_prod_list = new JSONArray();
        json.put("id", id);
        json.put("email", email);
        json.put("password", password);

//        Type gsonType = new TypeToken<Map>(){}.getType();
//        json.put("favourite_products", gson.toJson(favouriteProducts, gsonType));
//        for (Map<String, Product> p : favouriteProducts) {
//            fav_prod_list.put(p);
//        }
        json.put("favourite_products", favouriteProducts);
        //json.put("favourite_products", fav_prod_list);

        System.out.println(json);
        return json;
    }

}
