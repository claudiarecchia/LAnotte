package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Base64;

@Getter
@Setter
@ToString

@Document(collection = "products")
public class Product {
    @Id
    private ObjectId id;
    private String name;
    private ArrayList<String> ingredients;
    // private byte[] image;
    private String image;
    private String category;
    private ArrayList<String> stamps;
    private Double price;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("ingredients", ingredients);
        json.put("category", category);
        json.put("stamps", stamps);
        json.put("price", price);
        json.put("image", image);

//        if(image != null) {
//            json.put("image", Base64.getEncoder().encodeToString(image));
//        }
        return json;
    }

}
