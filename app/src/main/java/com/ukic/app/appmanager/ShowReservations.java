package com.ukic.app.appmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
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
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import enumerations.ReservationStatus;
import model.Apartment;
import model.Reservation;
import model.Tourists;

public class ShowReservations extends AppCompatActivity {

    ProgressDialog pd;

    GridLayout gridLayout;
    private int month = 8;
    private int year = 2019;
    Set<Apartment> apartments = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        gridLayout = (GridLayout) findViewById(R.id.table);

        try {
            new FetchApartments().execute("https://apartment-manager-demo.herokuapp.com/demo/all-apartments").get();
            new JsonTask().execute("https://apartment-manager-demo.herokuapp.com/demo/all-reservations").get();
        } catch (InterruptedException | ExecutionException exp) {
            exp.getStackTrace();
        }


        int lengthOfMonth = YearMonth.of(year, month).lengthOfMonth();
        gridLayout.setColumnCount(lengthOfMonth);
        gridLayout.setRowCount(apartments.size()+1);

        for (int i = 1; i <= YearMonth.of(year, month).lengthOfMonth(); i++) {
            TextView textView = new TextView(this);
            textView.setBackgroundColor(Color.CYAN);
            textView.setText(new String(""+i));
            textView.setGravity(Gravity.CENTER);
            addViewToGridLayout(textView, 0, i-1, 1, 1);
        }

        int apartmentIndex = 0;

        for (Apartment apartment : apartments) {
            for (int i = 0; i < lengthOfMonth; i++) {

                Button text = new Button(this);

                LocalDate date = LocalDate.of(year, month, i+1);
                Reservation reservation = apartment.getApartmentReservations().get(date);

                String s;
                if(reservation != null) {
                    s = reservation.getTourists().getName();
                    text.setBackgroundColor(Color.RED);
                    int fin = i;
                    text.setOnClickListener(l -> {
                        Intent intent = new Intent(this, ShowReservation.class);
                        intent.putExtra("reservation", reservation);
                        startActivity(intent);
                    });
                } else {
                    s = "prazno";
                    text.setBackgroundColor(Color.GREEN);
                    text.setEnabled(false);
                }

                int reservationSpan = 0;
                if(reservation != null) {
                    while (reservation.equals(apartment.getApartmentReservations().get(date))) {
                        date = date.plusDays(1);
                        reservationSpan++;
                    }
                } else {
                    while (apartment.getApartmentReservations().get(date)==null) {
                        date = date.plusDays(1);
                        reservationSpan++;
                    }
                }

                if(i+reservationSpan > lengthOfMonth) {
                    reservationSpan = lengthOfMonth - i;
                }

                if(reservationSpan > s.length()) {
                    text.setText(s);
                }
                addViewToGridLayout(text, apartmentIndex+1, i, 1, reservationSpan);
                i += reservationSpan-1;

                /*

                tableRow.addView(text);
                TextView c1 = new TextView(this);
                TextView c2 = new TextView(this);
                c1.setText("column1");
                c2.setText("column1");
                tableRow.addView(c1)
                tableRow.addView(c2);
                tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                 */
            }
            apartmentIndex++;
        }





    }

    private void addViewToGridLayout(View view, int row, int column, int rowSpan, int columnSpan) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 50*columnSpan;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(column, columnSpan);
        params.rowSpec = GridLayout.spec(row, rowSpan);

        gridLayout.addView(view, params);
    }

    private class FetchApartments extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ShowReservations.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            //pd.show();
        }

        @Override
        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                System.out.println("ispis1");
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

        }

    }

    private class JsonTask extends AsyncTask<String, String, String> {


        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(ShowReservations.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            //pd.show();
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
                        Integer numberOfPersons = touristsObj.optInt("numberOfPersons");
                        Integer numberOfAdults = touristsObj.optInt("numberOfAdults");
                        Integer numberOfChildren = touristsObj.optInt("numberOfChildren");
                        String email = touristsObj.getString("email");
                        String phoneNumber = touristsObj.getString("phoneNumber");
                        Boolean pets = touristsObj.optBoolean("pets");
                        String touristsNote = touristsObj.getString("touristsNote");

                        Tourists tourists = new Tourists(touristsId, name, country, city
                                , numberOfAdults, numberOfChildren, numberOfPersons
                                , email, phoneNumber, pets, touristsNote);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                        Long reservationId = obj.getLong("reservationId");
                        LocalDate checkInDate = LocalDate.parse(obj.getString("checkInDate"));
                        LocalDate checkOutDate = LocalDate.parse(obj.getString("checkOutDate"));
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

                        System.out.println("id: " + reservationId);
                        apartment.putReservationInApartment(reservation);

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

    public Set<Apartment> getApartments() {
        return apartments;
    }


}
