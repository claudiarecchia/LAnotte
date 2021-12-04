package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;

@Getter
@Setter
@ToString

@Document(collection = "orders")
public class Order {
    @Id
    private ObjectId id;
    private String hour;
    private String date;
    @Field("estimated_hour")
    private String estimatedHour;
    private String state;
    private ArrayList<Product> products;
    private Business business;
    private User user;
}
