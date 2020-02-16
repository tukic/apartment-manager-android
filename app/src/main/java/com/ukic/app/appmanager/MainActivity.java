package com.ukic.app.appmanager;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import enumerations.ReservationStatus;
import model.Apartment;
import model.Reservation;
import model.Tourists;

public class MainActivity extends AppCompatActivity {


    Set<Apartment> apartments = new HashSet<>();

    Button btnHit;
    TextView txtJson;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnHit = (Button) findViewById(R.id.btnHit);
        txtJson = (TextView) findViewById(R.id.tvJsonItem);
        txtJson.setMovementMethod(new ScrollingMovementMethod());

        new FetchApartments().execute("https://apartment-manager-demo.herokuapp.com/demo/all-apartments");

        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JsonTask().execute("https://apartment-manager-demo.herokuapp.com/demo/all-reservations");
            }
        });
    }


    private class FetchApartments extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                StringBuffer important = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }


                try {
                    JSONArray array = new JSONArray(buffer.toString());
                    int n = array.length();

                    for(int i = 0; i < n; i++) {

                        JSONObject obj = array.getJSONObject(i);

                        Integer apartmentId = obj.getInt("apartmentId");
                        int baseCapacity = obj.getInt("baseCapacity");
                        int additionalCapacity = obj.getInt("additionalCapacity");
                        String apartmentName = obj.getString("apartmentName");
                        String internalName = obj.getString("internalName");
                        String apartmentNote = obj.getString("apartmentNote");

                        apartments.add(new Apartment(apartmentId, baseCapacity, additionalCapacity
                                , apartmentName, internalName, apartmentNote, null));

                    }

                } catch (JSONException exp) {
                    exp.printStackTrace();
                }

                System.out.println(apartments);
                return important.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson.setText(result);
        }

    }

    private class JsonTask extends AsyncTask<String, String, String> {


        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                StringBuffer important = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }


                try {
                    JSONArray array = new JSONArray(buffer.toString());

                    for(int i = 0; i < array.length(); i++) {

                        JSONObject obj = array.getJSONObject(i);

                        int apartmentId = obj.getJSONObject("apartment").getInt("apartmentId");
                        Apartment apartment = findApartmentById(apartments, apartmentId);

                        JSONObject touristsObj = obj.getJSONObject("tourists");
                        Long touristsId = touristsObj.getLong("touristsId");
                        String name = touristsObj.getString("name");
                        String country = touristsObj.getString("country");
                        String city = touristsObj.getString("city");
                        Integer numberOfPersons = touristsObj.getInt("numberOfPersons");
                        Integer numberOfAdults = touristsObj.getInt("numberOfAdults");
                        Integer numberOfChildren = touristsObj.getInt("numberOfChildren");
                        String email = touristsObj.getString("email");
                        String phoneNumber = touristsObj.getString("phoneNumber");
                        Boolean pets = touristsObj.getBoolean("pets");
                        String touristsNote = touristsObj.getString("touristsNote");

                        Tourists tourists = new Tourists(touristsId, name, country, city
                                , numberOfAdults, numberOfChildren, numberOfPersons
                                , email, phoneNumber, pets, touristsNote);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                        Long reservationId = obj.getLong("reservationId");
                        LocalDate checkInDate = LocalDate.parse(obj.getString("checkInDate"));
                        LocalDate checkOutDate = LocalDate.parse(obj.getString("checkInDate"));
                        BigDecimal pricePerNight = new BigDecimal(obj.getDouble("pricePerNight"));
                        BigDecimal totalPrice = new BigDecimal(obj.getDouble("totalPrice"));
                        ReservationStatus confirmed = ReservationStatus.reservation;
                        BigDecimal advancedPayment = new BigDecimal(obj.getDouble("advancedPayment"));
                        String advPayCurrency = obj.getString("advPayCurrency");

                        Reservation reservation = new Reservation(reservationId, tourists
                                , apartment, checkInDate, checkOutDate, pricePerNight, totalPrice
                                , confirmed, advancedPayment, advPayCurrency);

                        //important.append("reservationId=").append(obj.getString("reservationId"));
                        //important.append("checkInDate=").append(obj.getString("checkInDate"));

                        important.append(reservation);
                    }

                } catch (JSONException exp) {
                    exp.printStackTrace();
                }

                return important.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson.setText(result);
        }
    }


    Apartment findApartmentById(Set<Apartment> apartments, int apartmentId) {
        for(Apartment apartment : apartments) {
            if (apartment.getApartmentId().equals(apartmentId)) {
                return apartment;
            }
        }
        return null;
    }

}
