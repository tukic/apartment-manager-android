package com.ukic.app.appmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
    private int fontSize = 20;
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

        // change to initScreen2()
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
                    //Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.btn, null);


                    Drawable gd = new Drawable() {
                        @Override
                        public void draw(@NonNull Canvas canvas) {
                            int n = text.getWidth()/cellWidth;
                            for(int i = 0; i < n; i++) {
                                float startX = cellWidth*i;
                                float endX = cellWidth*(i+1);
                                Paint paint = new Paint();
                                paint.setColor(Color.RED);
                                paint.setStrokeWidth(3);
                                if(i%2==1) paint.setAlpha(paint.getAlpha()-50);
                                if(i==0) {
                                    canvas.drawRect(startX+5, cellHeight-5, endX, 5, paint);
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setColor(Color.rgb(200, 20, 20));
                                    canvas.drawRect(startX+5, cellHeight-5, endX, 5, paint);
                                }
                                else if(i+1 >= n) {
                                    canvas.drawRect(startX, cellHeight-5, endX-5, 5, paint);
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setColor(Color.rgb(200, 20, 20));
                                    canvas.drawRect(startX, cellHeight-5, endX-5, 5, paint);
                                }
                                else {
                                    canvas.drawRect(startX, cellHeight-5, endX, 5, paint);
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setColor(Color.rgb(200, 20, 20));
                                    canvas.drawRect(startX, cellHeight-5, endX, 5, paint);
                                }

                                if(endX >= text.getWidth()) break;
                            }
                        }

                        @Override
                        public void setAlpha(int i) {

                        }

                        @Override
                        public void setColorFilter(@Nullable ColorFilter colorFilter) {

                        }

                        @Override
                        public int getOpacity() {
                            return 0;
                        }
                    };
                    //gd.setColor(Color.RED);
                    /*
                    Canvas canvas = new Canvas();
                    Paint paint = new Paint();
                    paint.setStrokeWidth(15);
                    paint.setColor(Color.BLACK);
                    canvas.drawLine(0.5f, 0.0f, 0.5f, 1.0f, paint);
                    gd.setBounds(0,100,100,0);
                    canvas.drawPaint(paint);


                    System.out.println("alpha="+gd.getAlpha());
                    System.out.println("bounds="+gd.getBounds());
                    System.out.println("top="+text.getPaddingLeft()+text.getPaddingTop()+text.getPaddingRight()+text.getPaddingTop());

                    int[] colors = {Color.RED, Color.WHITE};
                    //gd.setColors(colors);

                    //gd.setCornerRadius(15);
                    //gd.setStroke(5, Color.BLACK);
                    */
                    text.setBackground(gd);
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

        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.rgb(191,98,98));
        textView.setId(textView.generateViewId());
        textView.setWidth(cellWidth*columnSpan);
        textView.setHeight(cellHeight);

        ConstraintLayout layout = new ConstraintLayout(this);
        layout.addView(textView);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);

        constraintSet.connect(layout.getId(), constraintSet.TOP, textView.getId(), ConstraintSet.TOP,0);
        //constraintSet.connect(textView,ConstraintSet.TOP,R.id.check_answer1,ConstraintSet.TOP,0);
        constraintSet.applyTo(layout);

        TextView textView2 = new TextView(this);
        textView2.setBackgroundColor(Color.rgb(50,0,0));
        textView2.setId(textView2.generateViewId());
        textView2.setWidth(cellWidth*columnSpan);
        textView2.setHeight(cellHeight-20);


        layout.addView(textView2);
        constraintSet.clone(layout);
        constraintSet.connect(layout.getId(), constraintSet.TOP, textView2.getId(), ConstraintSet.TOP,10);
        //constraintSet.connect(textView,ConstraintSet.TOP,R.id.check_answer1,ConstraintSet.TOP,0);
        constraintSet.applyTo(layout);


        /*
        //ConstraintLayout.LayoutParams params;
        params.width = cellWidth*columnSpan-42;
        //params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = cellHeight-42;

        layout.addView(view, params);

        params.width=cellWidth*columnSpan;
        params.height=cellHeight;

        layout.addView(view, params);

         */

        GridLayout.LayoutParams paramsGrid = new GridLayout.LayoutParams();
        paramsGrid.columnSpec = GridLayout.spec(column, columnSpan);
        paramsGrid.rowSpec = GridLayout.spec(row, rowSpan);
        paramsGrid.height=cellHeight;
        paramsGrid.width=cellWidth*columnSpan;
        gridLayout.addView(view, paramsGrid);
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

    class LineDrawable extends Drawable {
        private Paint mPaint;

        public LineDrawable() {
            mPaint = new Paint();
            mPaint.setStrokeWidth(5);
            mPaint.setColor(Color.BLACK);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawLine(50, 0, 50, 100, mPaint);
        }

        @Override
        protected boolean onLevelChange(int level) {
            invalidateSelf();
            return true;
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
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

    /*

        -----

     */

    void initScreen2() {

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
                    monthYearChanged2(month, year);
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
                    monthYearChanged2(month, year);
                }
                firstInit = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        monthYearChanged2(month, year);

    }

    private void monthYearChanged2(int month, int year) {
        int lengthOfMonth = YearMonth.of(year, month).lengthOfMonth();
        gridLayout.setColumnCount(lengthOfMonth + 1);
        gridLayout.setRowCount(apartments.size() + 1);


        int firstCellWidth = 500;
        int cellWidth = 150;
        int firstCellHeight = 80;
        int cellHeight = 200;

        for (int i = 1; i <= YearMonth.of(year, month).lengthOfMonth(); i++) {
            TextView textView = new TextView(this);
            if(i%2==1) textView.setBackgroundColor(Color.WHITE);
            else textView.setBackgroundColor(Color.CYAN);
            textView.setText(new String(""+i));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(fontSize);
            addViewToGridLayout2(textView, 0, i, 1, 1
                    , cellWidth, firstCellHeight);
        }

        int apartmentIndex = 0;

        for (Apartment apartment : apartments) {

            TextView apartmentNameView = new TextView(this);
            apartmentNameView.setText(apartment.getApartmentName());
            apartmentNameView.setBackgroundColor(Color.MAGENTA);
            //apartmentNameView.setTextSize(fontSize);
            addViewToGridLayout2(apartmentNameView, apartmentIndex+1
                    , 0, 1, 1, firstCellWidth, cellHeight);

            for (int i = 0; i < lengthOfMonth; i++) {

                //Button text = new Button(this);
                TextView text = new TextView(this);
                LocalDate date = LocalDate.of(year, month, i+1);
                Reservation reservation = apartment.getApartmentReservations().get(date);

                String s;

                Drawable imgW = ResourcesCompat.getDrawable(getResources(), R.drawable.date_white, null);
                Drawable imgG = ResourcesCompat.getDrawable(getResources(), R.drawable.date_gray, null);
                if(i%2==1) text.setBackground(imgG);
                else text.setBackground(imgW);

                /*if(i%2==1) text.setBackgroundColor(Color.CYAN);
                else text.setBackgroundColor(Color.WHITE);
                 */

                int fin = i;

                text.setOnClickListener(l -> {
                    Intent intent = new Intent(this, ShowReservation.class);
                    intent.putExtra("reservation", reservation);
                    startActivity(intent);
                });


                addViewToGridLayout2(text, apartmentIndex+1, i+1, 1
                        , 1, cellWidth, cellHeight);

            }
            apartmentIndex++;
        }

        TextView vt = new TextView(this);
        //vt.setText("dljGI");
        vt.setWidth(6*cellWidth-200);
        vt.setHeight(cellHeight-25);
        vt.setBackgroundColor(Color.MAGENTA);

        GridLayout.LayoutParams paramsGrid = new GridLayout.LayoutParams();
        paramsGrid.columnSpec = GridLayout.spec(5, 6);
        paramsGrid.rowSpec = GridLayout.spec(2, 1);
        /*paramsGrid.height=cellHeight-10;
        paramsGrid.width=cellWidth-10;*/
        paramsGrid.setGravity(Gravity.CENTER);
        gridLayout.addView(vt, paramsGrid);


        //
        TextView vt2 = new TextView(this);
        //vt.setText("dljGI");
        vt2.setWidth(2*cellWidth-200);
        vt2.setHeight(cellHeight-25);
        vt2.setBackgroundColor(Color.GREEN);

        GridLayout.LayoutParams paramsGrid2 = new GridLayout.LayoutParams();
        paramsGrid2.columnSpec = GridLayout.spec(10, 2);
        paramsGrid2.rowSpec = GridLayout.spec(2, 1);
        /*paramsGrid.height=cellHeight-10;
        paramsGrid.width=cellWidth-10;*/
        paramsGrid2.setGravity(Gravity.CENTER);
        gridLayout.addView(vt2, paramsGrid2);
    }

    private void addViewToGridLayout2(View view, int row, int column, int rowSpan
            , int columnSpan, int cellWidth, int cellHeight) {

        /*
        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.rgb(191,98,98));
        textView.setId(textView.generateViewId());
        textView.setWidth(cellWidth*columnSpan);
        textView.setHeight(cellHeight);

        ConstraintLayout layout = new ConstraintLayout(this);
        layout.addView(textView);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);

        constraintSet.connect(layout.getId(), constraintSet.TOP, textView.getId(), ConstraintSet.TOP,0);
        //constraintSet.connect(textView,ConstraintSet.TOP,R.id.check_answer1,ConstraintSet.TOP,0);
        constraintSet.applyTo(layout);

        TextView textView2 = new TextView(this);
        textView2.setBackgroundColor(Color.rgb(50,0,0));
        textView2.setId(textView2.generateViewId());
        textView2.setWidth(cellWidth*columnSpan);
        textView2.setHeight(cellHeight-20);


        layout.addView(textView2);
        constraintSet.clone(layout);
        constraintSet.connect(layout.getId(), constraintSet.TOP, textView2.getId(), ConstraintSet.TOP,10);
        //constraintSet.connect(textView,ConstraintSet.TOP,R.id.check_answer1,ConstraintSet.TOP,0);
        constraintSet.applyTo(layout);
        */

        /*
        //ConstraintLayout.LayoutParams params;
        params.width = cellWidth*columnSpan-42;
        //params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = cellHeight-42;

        layout.addView(view, params);

        params.width=cellWidth*columnSpan;
        params.height=cellHeight;

        layout.addView(view, params);

         */

        GridLayout.LayoutParams paramsGrid = new GridLayout.LayoutParams();
        paramsGrid.columnSpec = GridLayout.spec(column, columnSpan);
        paramsGrid.rowSpec = GridLayout.spec(row, rowSpan);
        paramsGrid.height=cellHeight;
        paramsGrid.width=cellWidth*columnSpan;
        gridLayout.addView(view, paramsGrid);
    }

}
