package com.ukic.app.appmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import enumerations.ReservationStatus;
import model.Apartment;
import model.Reservation;
import model.Tourists;

public class ShowReservations extends AppCompatActivity {

    ProgressBar pb;


    GridLayout gridLayout;
    Spinner selectMonthSpinner;
    Spinner selectYearSpinner;
    private String[] monthsString = {"Lipanj", "Srpanj", "Kolovoz", "Rujan"};
    private String[] yearsString = {"2018", "2019", "2020", "2021", "2022"};
    private int month = 8;
    private int year = 2019;
    private int fontSize = 25;
    Set<Apartment> apartments = new HashSet<>();

    boolean firstInit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        /*
        try {
            new FetchApartments(this).execute("https://apartment-manager-demo.herokuapp.com/demo/all-apartments");//.get();
            //new JsonTask().execute("https://apartment-manager-demo.herokuapp.com/demo/all-reservations").get();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
         */

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        for (int i = 0; ; i++) {
            Apartment apartment = (Apartment) bundle.getSerializable("apartment"+i);
            if(apartment == null) break;
            apartments.add(apartment);
        }

        initScreen();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setContentView(R.layout.calendar);
        initScreen();
    }

    void initScreen() {

        setContentView(R.layout.calendar);
        gridLayout = (GridLayout) findViewById(R.id.table);
        selectMonthSpinner = (Spinner) findViewById(R.id.spinnerMonth);

        ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, monthsString);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthSpinner.setAdapter(adapterMonth);
        selectMonthSpinner.setSelection(month-6);

        selectYearSpinner = (Spinner) findViewById(R.id.spinnerYear);
        ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, yearsString);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectYearSpinner.setAdapter(adapterYear);
        selectYearSpinner.setSelection(year-2018);

        pb = findViewById(R.id.progressBar);
        if(firstInit) {
            pb.setMax(100);
            pb.setProgress(0);
        }
        pb.setVisibility(View.VISIBLE);

        selectMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                month = selectMonthSpinner.getSelectedItemPosition() + 6;
                if(!firstInit)
                {
                    gridLayout.removeAllViews();
                    monthYearChanged(month, year);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        selectYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year = selectYearSpinner.getSelectedItemPosition() + 2018;
                if(!firstInit)
                {
                    gridLayout.removeAllViews();
                    monthYearChanged(month, year);
                }
                firstInit = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        monthYearChanged(month, year);

    }

    private void monthYearChanged(int month, int year) {
        int lengthOfMonth = YearMonth.of(year, month).lengthOfMonth();
        gridLayout.setColumnCount(lengthOfMonth + 1);
        gridLayout.setRowCount(apartments.size() + 1);

        int firstCellWidth = 500;
        int cellWidth = 150;
        int firstCellHeight = 100;
        int cellHeight = 250;

        for (int i = 1; i <= YearMonth.of(year, month).lengthOfMonth(); i++) {
            TextView textView = new TextView(this);
            textView.setBackgroundColor(Color.CYAN);
            textView.setText(new String(""+i));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(fontSize);
            addViewToGridLayout(textView, 0, i, 1, 1
                    , cellWidth, firstCellHeight);
        }

        int apartmentIndex = 0;

        for (Apartment apartment : apartments) {

            Button apartmentNameView = new Button(this);
            apartmentNameView.setText(apartment.getApartmentName());
            apartmentNameView.setBackgroundColor(Color.MAGENTA);
            apartmentNameView.setTextSize(fontSize);
            addViewToGridLayout(apartmentNameView, apartmentIndex+1
                    , 0, 1, 1, firstCellWidth, cellHeight);

            for (int i = 0; i < lengthOfMonth; i++) {

                Button text = new Button(this);

                LocalDate date = LocalDate.of(year, month, i+1);
                Reservation reservation = apartment.getApartmentReservations().get(date);

                String s;
                if(reservation != null) {
                    s = reservation.getTourists().getName();
                    //text.setBackgroundColor(Color.RED);
                    Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.button_date_reserved, null);
                    //Drawable img = this.getResources().getDrawable( R.drawable.button_date_reserved );
                    //text.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
                    text.setBackground(img);
                    int fin = i;
                    text.setOnClickListener(l -> {
                        Intent intent = new Intent(this, ShowReservation.class);
                        intent.putExtra("reservation", reservation);
                        startActivity(intent);
                    });
                } else {
                    s = "prazno";
                    Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.button_date_available, null);
                    text.setBackground(img);
                    //text.setBackgroundColor(Color.GREEN);
                    text.setEnabled(false);
                }
                text.setTextSize(fontSize);

                int reservationSpan = 0;
                if(reservation != null) {
                    while (reservation.equals(apartment.getApartmentReservations().get(date))) {
                        date = date.plusDays(1);
                        reservationSpan++;
                        if(i+reservationSpan > lengthOfMonth) {
                            reservationSpan = lengthOfMonth - i;
                            break;
                        }
                    }
                } else {
                    while (apartment.getApartmentReservations().get(date)==null) {
                        date = date.plusDays(1);
                        reservationSpan++;
                        if(i+reservationSpan > lengthOfMonth) {
                            reservationSpan = lengthOfMonth - i;
                            break;
                        }
                    }
                }

                if(3*reservationSpan > s.length()) {
                    text.setText(s);
                }
                addViewToGridLayout(text, apartmentIndex+1, i+1, 1
                        , reservationSpan, cellWidth, cellHeight);
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

    private void addViewToGridLayout(View view, int row, int column, int rowSpan
            , int columnSpan, int cellWidth, int cellHeight) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = cellWidth*columnSpan;
        //params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = cellHeight;
        params.columnSpec = GridLayout.spec(column, columnSpan);
        params.rowSpec = GridLayout.spec(row, rowSpan);

        gridLayout.addView(view, params);
    }

    private class FetchApartments extends AsyncTask<String, Integer, String> {

        Activity activity;

        public FetchApartments(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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
                        publishProgress(40*i/n);
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
            pb.setProgress(40);
            try {
                new JsonTask().execute("https://apartment-manager-demo.herokuapp.com/demo/all-reservations");
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int prog = pb.getProgress();
            prog++;
            pb.setProgress(values[0]);
        }
    }

    private class JsonTask extends AsyncTask<String, Integer, String> {


        protected void onPreExecute() {
            super.onPreExecute();

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
                    int n = array.length();

                    for(int i = 0; i < n; i++) {
                        publishProgress(i*60/n);

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
            pb.setProgress(100);
            initScreen();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int prog = pb.getProgress();
            prog++;
            pb.setProgress(40+values[0]);
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
