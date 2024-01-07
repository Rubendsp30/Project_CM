package com.example.project_cm.utils;


import java.security.SecureRandom;

public class ClientNameUtil {

    static String clientName;

    public static String getClientName() {
        return clientName;
    }

    //TODO Add Salt to more complex encryption
    public static void generateClientName() {
        String DATA_FOR_RANDOM_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(18);
        for (int i = 0; i < 18; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
            sb.append(rndChar);
        }
         clientName = sb.toString();
         //return sb.toString();
    }

}
