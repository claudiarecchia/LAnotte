package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Getter
@Setter
@ToString

@Document(collection = "orders")
public class Order {
    @Id
    private ObjectId id;
    @Field("date_time")
    private String dateTime;
    private ArrayList<Product> products;
    private Business business;
    private User user;
    @Field("order_status")
    private OrderStatus status;
    @Field("device_token")
    private String deviceToken;
    @Field("code_to_collect")
    private String codeToCollect;

    @Transient
    private Map<Product, Integer> productsNameAndQuantity = new HashMap<>();


    public Order(Business business, ArrayList<Product> products, String deviceToken,
                 User user, String dateTime){
        this.setBusiness(business);
        this.setProducts(products);
        this.setStatus(OrderStatus.placed);
        this.setDeviceToken(deviceToken);
        this.setCodeToCollect("");
        this.setUser(user);
        this.setDateTime(dateTime);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray prod_list = new JSONArray();
        json.put("id", id);
        json.put("date_time", dateTime);
        for (Product p : products)
            prod_list.put(p.toJSON());
        json.put("products", prod_list);
        json.put("business", business.toJSON());
        json.put("user", user.toJSON());
        json.put("code_to_collect", codeToCollect);
        json.put("order_status", status);

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

    public void groupByProductName(){
        ArrayList<String> names = new ArrayList<>();
        Integer counter = 1;
        for (int i=0; i<this.products.size(); i++){
            if (!names.contains(this.products.get(i).getName())) {
                names.add(this.products.get(i).getName());
                for (int j=i+1; j<this.products.size(); j++){
                    if (this.products.get(j).getId().equals(this.products.get(i).getId())){
                        counter += 1;
                    }
                }
                this.productsNameAndQuantity.put(this.products.get(i), counter);
                counter = 1;
            }
        }

    }

    public Double getTotal(){
        Double total = 0.0;

        for (Product p : this.products){
            total += p.getPrice();
        }
        return total;
    }
}
