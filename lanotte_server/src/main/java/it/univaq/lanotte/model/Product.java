package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Getter
@Setter
@ToString

@Document(collection = "products")
public class Product {
    @Id
    private ObjectId id;
    private String name;
    private ArrayList<String> ingredients;
    private byte[] image;
    private String category;
    private ArrayList<String> stamps;
}
