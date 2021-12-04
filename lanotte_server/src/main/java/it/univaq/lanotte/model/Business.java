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

@Document(collection = "business")
public class Business {
    @Id
    private ObjectId id;
    @Field("business_name")
    private String businessName;
    @Field("VAT_number")
    private int VATNumber;
    private String description;
    private byte[] image;
    private String location;
    private double rating;
    private ArrayList<Product> products;

}
