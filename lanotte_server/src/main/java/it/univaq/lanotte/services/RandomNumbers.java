package it.univaq.lanotte.services;

import org.springframework.stereotype.Service;

import java.util.Random;

public class RandomNumbers {

//    private final RandomNumbers rand;
//
//    public RandomNumbers(RandomNumbers rand) {
//        this.rand = rand;
//    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int firstNumber = rnd.nextInt(999);
        int secondNumber = rnd.nextInt(999);

        // this will convert any number sequence into 6 character.
        return String.format("%03d", firstNumber) + "-" + String.format("%03d", secondNumber);
    }



}
