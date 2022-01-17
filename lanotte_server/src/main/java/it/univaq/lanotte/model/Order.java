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
import java.util.Base64;
import java.util.Optional;

@Getter
@Setter
@ToString

@Document(collection = "orders")
public class Order {
    @Id
    private ObjectId id;
    @Field("date_time")
    private String dateTime;
    @Field("estimated_hour")
    private String estimatedHour;
    // private String state;
    private ArrayList<Product> products;
    private Business business;
    private User user;
    @Field("order_status")
    private OrderStatus status;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray prod_list = new JSONArray();
        json.put("id", id);
        json.put("date_time", dateTime);
//        json.put("estimated_hour", estimatedHour);
//        json.put("state", state);
        for (Product p : products)
            prod_list.put(p.toJSON());
        json.put("products", prod_list);
        json.put("business", business.toJSON());
        json.put("user", user.toJSON());
        return json;
    }


    public void updateValuesProduct(Product product){

        for (int i=0; i<products.size(); i++){
            if (products.get(i).getId().equals(product.getId())){
                products.remove(i);
                products.add(product);
                break;
            }
        }
    }

    public void updateValuesBusiness(Business business){
        this.business.setNumberRatings(business.getNumberRatings());
        this.business.setRatingSum(business.getRatingSum());
        this.business.setRating(business.getRating());
    }

}
