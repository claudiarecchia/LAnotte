package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Base64;

@Getter
@Setter
@ToString

@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;
    private String email;
    private String password;

    public User(){ }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("email", email);
        json.put("password", password);
        return json;
    }

}
