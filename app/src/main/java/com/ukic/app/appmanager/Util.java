package com.ukic.app.appmanager;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Util {

    private static String url = "https://apartment-manager-demo.herokuapp.com/demo/reservations/new";
    // private static String url = "http://localhost:8080/demo/reservations/new";
    private static int paramsCount = 2;
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String[] prepareReservation(String touristsName, LocalDate checkInDate
            , LocalDate checkOutDate, int apartmentId, String pricePerNight
            , String totalPrice, boolean advancedPaymentPaid, String advancedPaymentCurrency
            , String advancedPaymentAmount, String numberOfPersons, String numberOfAdults
            , String numberOfChildren, String country, String city, String phone, String email
            , boolean pets, String notes) {

        String[] params = new String[paramsCount];
        params[0] = url;
        StringBuilder data = new StringBuilder();
        data.append("name=").append(touristsName).append("&")
                .append("apartmentId=").append(apartmentId).append("&")
                .append("checkInDate=").append(checkInDate.format(dateFormatter)).append("&")
                .append("persons=").append(numberOfPersons).append("&")
                .append("checkOutDate=").append(checkOutDate.format(dateFormatter)).append("&")
                .append("adults=").append(numberOfAdults).append("&")
                .append("pricePerNight=").append(pricePerNight).append("&")
                .append("children=").append(numberOfChildren).append("&")
                .append("advancedPaymentPaid=").append(advancedPaymentPaid).append("&")
                .append("advancedPaymentCurrency=").append(advancedPaymentCurrency).append("&")
                .append("city=").append(city).append("&")
                .append("advancedPaymentAmount=").append(advancedPaymentAmount).append("&")
                .append("country=").append(country).append("&")
                .append("totalPrice=").append(totalPrice).append("&")
                .append("email=").append(email).append("&")
                .append("phone=").append(phone).append("&")
                .append("pets=").append(pets).append("&")
                .append("notes=").append(notes);
        params[1] = data.toString();

        return params;
    }
}
