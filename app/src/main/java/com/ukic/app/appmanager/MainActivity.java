package com.ukic.app.appmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import enumerations.ReservationStatus;
import model.Apartment;
import model.Reservation;
import model.Tourists;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static String reservationsUrl = "https://apartment-manager-demo.herokuapp.com/demo/all-reservations";
    private static String apartmentsUrl = "https://apartment-manager-demo.herokuapp.com/demo/all-apartments";

    //private static String reservationsUrl = "http://localhost:8080/demo/all-reservations";
    //private static String apartmentsUrl = "http://localhost:8080/demo/all-apartments";

    private Set<Apartment> apartments = new HashSet<>();
    TextView loadingReservationsTextView;

    ProgressBar progressBar;

    private DrawerLayout drawer;
    Toolbar toolbar;

    GridLayout gridLayout;
    Spinner selectMonthSpinner;
    Spinner selectYearSpinner;
    private String[] monthsString = {"Lipanj", "Srpanj", "Kolovoz", "Rujan"};
    private String[] yearsString = {"2018", "2019", "2020", "2021", "2022"};
    private int month = 8;
    private int year = 2019;
    private int fontSize = 20;

    boolean firstInit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingReservationsTextView = findViewById(R.id.loadingReservationsTxtViewId);
        loadingReservationsTextView.setText(R.string.loadingReservationsStr);

        progressBar = findViewById(R.id.progressBar2);

        if(firstInit) {
            progressBar.setProgress(0);
            new FetchApartments(this).execute(apartmentsUrl);
            firstInit = false;
        }

    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.new_reservation_opt:
                Intent intent = new Intent(this, NewReservation.class);
                Bundle bundle = new Bundle();
                int i = 0;
                for(Apartment apartment : apartments) {
                    bundle.putSerializable("apartment" + i, apartment);
                    i++;
                }
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            case R.id.refresh:
                progressBar.setProgress(0);
                apartments.clear();
                new FetchApartments(this).execute("https://apartment-manager-demo.herokuapp.com/demo/all-apartments");
                firstInit = false;
                break;

            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
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
            progressBar.setProgress(40);
            try {
                new JsonTask(activity).execute(reservationsUrl);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }
    }

    private class JsonTask extends AsyncTask<String, Integer, String> {

        Activity activity;

        public JsonTask(Activity activity) { this.activity = activity; }

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

            Intent intent = new Intent(activity, ShowReservations.class);
            Bundle bundle = new Bundle();
            int i = 0;
            for(Apartment apartment : apartments) {
                bundle.putSerializable("apartment" + i, apartment);
                i++;
            }
            intent.putExtras(bundle);
            //startActivity(intent);

            loadingFinished(activity);
            progressBar.setProgress(100);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(40+values[0]);
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

    public void loadingFinished(Activity activity) {
        activity.setContentView(R.layout.calendar);
        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        final ActionBar actionBar = getSupportActionBar();

        initReservationsScreen(activity);
    }

    public void initReservationsScreen(Activity activity) {
        gridLayout = (GridLayout) findViewById(R.id.table);
        selectMonthSpinner = (Spinner) findViewById(R.id.spinnerMonth);

        ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, monthsString);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthSpinner.setAdapter(adapterMonth);
        selectMonthSpinner.setSelection(month-6);

        selectYearSpinner = (Spinner) findViewById(R.id.spinnerYear);
        ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, yearsString);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectYearSpinner.setAdapter(adapterYear);
        selectYearSpinner.setSelection(year-2018);

        progressBar = findViewById(R.id.progressBar);
        if(firstInit) {
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }
        progressBar.setVisibility(View.VISIBLE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_bar);
        navigationView.setNavigationItemSelectedListener(this);

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

    public void monthYearChanged(int month, int year) {
        int lengthOfMonth = YearMonth.of(year, month).lengthOfMonth();
        gridLayout.setColumnCount(lengthOfMonth + 1);
        gridLayout.setRowCount(apartments.size() + 1);


        int firstCellWidth = 500;
        int cellWidth = 150;
        int cellWidthCut = 90;
        int firstCellHeight = 80;
        int cellHeight = 200;
        int cellHeightCut = 50;

        for (int i = 1; i <= YearMonth.of(year, month).lengthOfMonth(); i++) {
            TextView textView = new TextView(this);
            if(i%2==1) textView.setBackgroundColor(Color.WHITE);
            else textView.setBackgroundColor(Color.CYAN);
            textView.setText(new String(""+i));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(fontSize);
                            /*text.setOnClickListener(l -> {
                    Intent intent = new Intent(this, ShowReservation.class);
                    intent.putExtra("reservation", reservation);
                    startActivity(intent);
                });*/
            addViewToGridLayout(textView, 0, i, 1, 1
                    , cellWidth, firstCellHeight);
        }

        int apartmentIndex = 0;

        for (Apartment apartment : apartments) {


            // TODO:solve problems with calendar view
            // reservations start and final date are not displayed correctly

            TextView apartmentNameView = new TextView(this);
            apartmentNameView.setText(apartment.getApartmentName());
            apartmentNameView.setBackgroundColor(Color.MAGENTA);

            addViewToGridLayout(apartmentNameView, apartmentIndex+1
                    , 0, 1, 1, firstCellWidth, cellHeight);

            for (int i = 0; i < lengthOfMonth; i++) {

                //Button text = new Button(this);
                TextView text = new TextView(this);
                LocalDate date = LocalDate.of(year, month, i + 1);
                Reservation reservation = apartment.getApartmentReservations().get(date);

                Drawable imgW = ResourcesCompat.getDrawable(getResources(), R.drawable.date_white, null);
                Drawable imgG = ResourcesCompat.getDrawable(getResources(), R.drawable.date_gray, null);
                if (i % 2 == 1) text.setBackground(imgG);
                else text.setBackground(imgW);

                addViewToGridLayout(text, apartmentIndex + 1, i + 1, 1
                        , 1, cellWidth, cellHeight);
            }

            for (int i = 0; i < lengthOfMonth; i++) {
                /*if(i%2==1) text.setBackgroundColor(Color.CYAN);
                else text.setBackgroundColor(Color.WHITE);
                 */

                LocalDate date = LocalDate.of(year, month, i + 1);
                Reservation reservation = apartment.getApartmentReservations().get(date);
                String s = "";
                if(reservation != null) s = reservation.getTourists().getName();

                TextView reservationTextView = new TextView(this);

                if(i == 0) {
                    Reservation lastRes = apartment.getApartmentReservations().get(date.minusDays(1));
                    if(lastRes != null) {

                        int reservationSpan = 1;
                        if(reservation != null) {
                            LocalDate tmpDate = date;
                            Reservation tmpRes = reservation;
                            for(; tmpRes != null && tmpRes.equals(lastRes)
                                    && reservationSpan <= YearMonth.of(year, month).lengthOfMonth(); reservationSpan++) {
                                int day = reservationSpan;
                                tmpRes = apartment.getApartmentReservations().get(LocalDate.of(year, month, day));

                            }

                        }

                        s = lastRes.getTourists().getName();

                        reservationTextView.setWidth(reservationSpan*cellWidth-cellWidthCut);
                        reservationTextView.setHeight(cellHeight-cellHeightCut);
                        reservationTextView.setBackgroundColor(Color.YELLOW);
                        if(3*reservationSpan > s.length()) {
                            reservationTextView.setText(s);
                        }

                        GridLayout.LayoutParams paramsGrid = new GridLayout.LayoutParams();
                        paramsGrid.columnSpec = GridLayout.spec(1, reservationSpan);
                        paramsGrid.rowSpec = GridLayout.spec(apartmentIndex+1, 1);
                        paramsGrid.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                        gridLayout.addView(reservationTextView, paramsGrid);

                        reservationTextView.setOnClickListener(l -> {
                            Intent intent = new Intent(this, ShowReservation.class);
                            intent.putExtra("reservation", lastRes);
                            startActivity(intent);
                        });
                        reservationTextView.setGravity(Gravity.CENTER);

                        i += reservationSpan-1;
                        continue;
                    }
                }

                else {
                    if(reservation != null) {
                        int reservationSpan = 0;
                        while (reservation.equals(apartment.getApartmentReservations().get(date))) {
                            date = date.plusDays(1);
                            reservationSpan++;
                            if(i+reservationSpan >= lengthOfMonth+1) {
                                reservationSpan = lengthOfMonth-i+1;
                                break;
                            }
                        }

                        if(i+reservationSpan >= lengthOfMonth+1) {
                            reservationTextView = new TextView(this);

                            reservationTextView.setWidth(reservationSpan * cellWidth - cellWidthCut);
                            reservationTextView.setHeight(cellHeight - cellHeightCut);
                            reservationTextView.setBackgroundColor(Color.YELLOW);
                            if(3*reservationSpan > s.length()) {
                                reservationTextView.setText(s);
                            }

                            GridLayout.LayoutParams paramsGrid = new GridLayout.LayoutParams();
                            paramsGrid.columnSpec = GridLayout.spec(i, reservationSpan);
                            paramsGrid.rowSpec = GridLayout.spec(apartmentIndex + 1, 1);
                            paramsGrid.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

                            reservationTextView.setOnClickListener(l -> {
                                Intent intent = new Intent(this, ShowReservation.class);
                                intent.putExtra("reservation", reservation);
                                startActivity(intent);
                            });
                            reservationTextView.setGravity(Gravity.CENTER);

                            gridLayout.addView(reservationTextView, paramsGrid);

                            break;

                        } else {

                            reservationTextView = new TextView(this);

                            reservationTextView.setWidth((reservationSpan + 1) * cellWidth - 2 * cellWidthCut);
                            reservationTextView.setHeight(cellHeight - cellHeightCut);
                            reservationTextView.setBackgroundColor(Color.YELLOW);
                            if(3*reservationSpan > s.length()) {
                                reservationTextView.setText(s);
                            }

                            GridLayout.LayoutParams paramsGrid = new GridLayout.LayoutParams();
                            paramsGrid.columnSpec = GridLayout.spec(i, reservationSpan + 1);
                            paramsGrid.rowSpec = GridLayout.spec(apartmentIndex + 1, 1);
                            paramsGrid.setGravity(Gravity.CENTER);

                            reservationTextView.setOnClickListener(l -> {
                                Intent intent = new Intent(this, ShowReservation.class);
                                intent.putExtra("reservation", reservation);
                                startActivity(intent);
                            });
                            reservationTextView.setGravity(Gravity.CENTER);

                            gridLayout.addView(reservationTextView, paramsGrid);

                            i += reservationSpan - 1;
                            continue;
                        }


                    }
                }

            }
            apartmentIndex++;
        }
    }

    public void addViewToGridLayout(View view, int row, int column, int rowSpan
            , int columnSpan, int cellWidth, int cellHeight) {

        GridLayout.LayoutParams paramsGrid = new GridLayout.LayoutParams();
        paramsGrid.columnSpec = GridLayout.spec(column, columnSpan);
        paramsGrid.rowSpec = GridLayout.spec(row, rowSpan);
        paramsGrid.height=cellHeight;
        paramsGrid.width=cellWidth*columnSpan;
        gridLayout.addView(view, paramsGrid);

    }

}


