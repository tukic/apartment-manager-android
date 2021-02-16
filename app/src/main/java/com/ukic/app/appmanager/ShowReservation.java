package com.ukic.app.appmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import enumerations.ReservationStatus;
import model.Apartment;
import model.Reservation;
import model.Tourists;

public class ShowReservation extends AppCompatActivity {

    ProgressDialog pd;

    TableLayout tableLayout;
    Set<Apartment> apartments = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation);

        Reservation reservation = (Reservation) getIntent().getSerializableExtra("reservation");
        TextView touristsNameTextView = (TextView) findViewById(R.id.reservationName);
        touristsNameTextView.setText(reservation.getTourists().getName());
        TextView pricePerNightTextView = (TextView) findViewById(R.id.pricePerNightID);
        pricePerNightTextView.setText(reservation.getPricePerNight().toString());


    }
}