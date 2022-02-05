package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum Stamps {
    vegan,
    gluten_free,
    alcoholic
//    vegan("vegan"),
//    gluten_free("gluten free"),
//    alcoholic("alcoholic");
//
//    private final String value;
//
//    Stamps(String s) {
//        this.value = s;
//    }
//
//    public String getValue(){
//        return value;
//    }
}
