package com.ukic.app.appmanager;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;

import model.Apartment;

/**
 * Creates and shows activity with new reservation form which allows inserting new reservation in
 * the system.
 */
public class NewReservation extends AppCompatActivity {

    private EditText touristsNameView;
    private EditText checkInDateView, checkOutDateView;
    private EditText pricePerNightView, totalPriceView;

    // clickable text view, when clicked shows additional form fields
    private TextView additionalInfoClickable;

    // date pattern
    private final String datePattern = "dd.MM.yyyy";

    private ViewGroup newReservationLayout;

    // additional fields shown
    private boolean additionalFields = false;

    // additional views
    private View additionalInfoViews;

    // additional view fields
    private CheckBox advancedPaymentCheckBox, pets;
    private Spinner apartmentSpinner, advancedPaymentCurrencySpinner;
    private EditText advancedPaymentAmount, numberOfPersons, numberOfAdults, numberOfChildren;
    private EditText country, city, phone, email, notesView;

    private static String[] currencies = {"EUR", "HRK"};

    private Button saveBtn, discardBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_new);

        touristsNameView = findViewById(R.id.tourists_name);
        checkInDateView = findViewById(R.id.check_in_date);
        checkOutDateView = findViewById(R.id.check_out_date);
        apartmentSpinner = findViewById(R.id.apartment);

        // getAllApartments from parent activity and store its in the list
        List<Apartment> apartments = new LinkedList<>();
        Bundle bundle = this.getIntent().getExtras();
        for(int i = 0; ;i++) {
            Apartment apartment = (Apartment) bundle.getSerializable("apartment" + i);
            if(apartment == null) break;
            apartments.add(apartment);
        }

        // set apartment spinner adapter
        List<String> apartmentNames = new ArrayList<>();
        apartments.forEach(apartment -> apartmentNames.add(apartment.getApartmentName()));
        ArrayAdapter<String> apartmentSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, apartmentNames);
        apartmentSpinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_item);
        apartmentSpinner.setAdapter(apartmentSpinnerArrayAdapter);

        // date pickers shows calendar picker dialogs after clicked
        checkInDateView.setOnClickListener(l -> {
            final Calendar calendar = Calendar.getInstance();
            // date picker dialog
            DatePickerDialog checkInPicker = new DatePickerDialog(NewReservation.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                        String date = LocalDate.of(year, monthOfYear+1, dayOfMonth)
                                .format(DateTimeFormatter.ofPattern(datePattern));
                        checkInDateView.setText(date);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            checkInPicker.show();
        });

        checkOutDateView.setOnClickListener(l -> {
            final Calendar calendar = Calendar.getInstance();
            // date picker dialog
            DatePickerDialog checkOutPicker = new DatePickerDialog(NewReservation.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String date = LocalDate.of(year, monthOfYear+1, dayOfMonth)
                            .format(DateTimeFormatter.ofPattern(datePattern));
                    checkOutDateView.setText(date);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            checkOutPicker.show();
        });


        // after checkInDate, checkOutDate or pricePerNight date fields are changed calculate
        // totalPrice if possible and set its price
        checkInDateView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    try {
                        LocalDate checkInDate = LocalDate.parse(editable
                                , DateTimeFormatter.ofPattern(datePattern));
                        LocalDate checkOutDate = LocalDate.parse(checkOutDateView.getText()
                                , DateTimeFormatter.ofPattern(datePattern));
                        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                        BigDecimal pricePerNight = new BigDecimal(pricePerNightView.getText()
                                .toString().replace(',', '.'));
                        BigDecimal totalPrice = pricePerNight.multiply(new BigDecimal(days))
                                .setScale(2, BigDecimal.ROUND_HALF_UP);
                        totalPriceView.setText(String.valueOf(totalPrice).replace('.', ','));
                    } catch (NumberFormatException | DateTimeParseException ignore) {
                        // ignore
                    }
                }
            }
        });

        checkOutDateView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    try {
                        LocalDate checkInDate = LocalDate.parse(checkInDateView.getText()
                                , DateTimeFormatter.ofPattern(datePattern));
                        LocalDate checkOutDate = LocalDate.parse(editable.toString()
                                , DateTimeFormatter.ofPattern(datePattern));
                        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                        BigDecimal pricePerNight = new BigDecimal(pricePerNightView.getText()
                                .toString().replace(',', '.'));
                        BigDecimal totalPrice = pricePerNight.multiply(new BigDecimal(days))
                                .setScale(2, BigDecimal.ROUND_HALF_UP);
                        totalPriceView.setText(String.valueOf(totalPrice).replace('.', ','));
                    } catch (NumberFormatException | DateTimeParseException ignore) {
                        // ignore
                    }
                }
            }
        });


        pricePerNightView = findViewById(R.id.price_per_day);
        totalPriceView = findViewById(R.id.total_price);
        pricePerNightView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    try {
                        LocalDate checkInDate = LocalDate.parse(checkInDateView.getText()
                                , DateTimeFormatter.ofPattern(datePattern));
                        LocalDate checkOutDate = LocalDate.parse(checkOutDateView.getText()
                                , DateTimeFormatter.ofPattern(datePattern));
                        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                        BigDecimal pricePerNight = new BigDecimal(editable.toString().replace(',', '.'));
                        BigDecimal totalPrice = pricePerNight.multiply(new BigDecimal(days)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        totalPriceView.setText(String.valueOf(totalPrice).replace('.', ','));
                    } catch (NumberFormatException | DateTimeParseException ignore) {
                        // ignore
                    }
                }
            }
        });


        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        additionalInfoViews = vi.inflate(R.layout.additional_info, null);

        newReservationLayout = findViewById(R.id.new_reservation_layout);

        // after additionalInfo textView is clicked show extra fields or if extra fields are already
        // shown close them
        additionalInfoClickable = findViewById(R.id.additional_info);
        additionalInfoClickable.setOnClickListener(l -> {

            if(!additionalFields) {
                // insert into main view -1 from child count becouse of buttons layout
                newReservationLayout.addView(additionalInfoViews
                        , newReservationLayout.getChildCount()-1, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                additionalInfoClickable.setCompoundDrawablesWithIntrinsicBounds(R.drawable
                        .ic_remove_black_24dp, 0, 0, 0);
                additionalFields = true;
            } else {
                newReservationLayout.removeView(additionalInfoViews);
                additionalInfoClickable.setCompoundDrawablesWithIntrinsicBounds(R.drawable
                        .ic_add_black_24dp, 0, 0, 0);
                additionalFields = false;
            }
        });

        // additional information fields
        advancedPaymentCheckBox = additionalInfoViews.findViewById(R.id.advanced_payment_check_box);
        pets = additionalInfoViews.findViewById(R.id.pets);
        advancedPaymentCurrencySpinner = additionalInfoViews.findViewById(R.id.advanced_payment_currency);
        advancedPaymentCurrencySpinner.setEnabled(false);
        advancedPaymentAmount = additionalInfoViews.findViewById(R.id.advanced_payment_amount);
        advancedPaymentAmount.setEnabled(false);
        numberOfPersons = additionalInfoViews.findViewById(R.id.number_of_persons);
        numberOfAdults = additionalInfoViews.findViewById(R.id.number_of_adults);
        numberOfChildren = additionalInfoViews.findViewById(R.id.number_of_children);
        country = additionalInfoViews.findViewById(R.id.country);
        city = additionalInfoViews.findViewById(R.id.city);
        phone = additionalInfoViews.findViewById(R.id.phone_number);
        email = additionalInfoViews.findViewById(R.id.email);
        notesView = additionalInfoViews.findViewById(R.id.notes);

        ArrayAdapter<String> advancedPaymentSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, currencies);
        advancedPaymentSpinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_item);
        advancedPaymentCurrencySpinner.setAdapter(advancedPaymentSpinnerArrayAdapter);

        // if checkBox is checked enable advancedPayment fields
        // otherwise disabled it
        advancedPaymentCheckBox.setOnClickListener(l -> {
            if(advancedPaymentCheckBox.isChecked()) {
                advancedPaymentAmount.setEnabled(true);
                advancedPaymentCurrencySpinner.setEnabled(true);
            } else {
                advancedPaymentAmount.setEnabled(false);
                advancedPaymentCurrencySpinner.setEnabled(false);
            }
        });

        // buttons
        saveBtn = findViewById(R.id.save);
        discardBtn = findViewById(R.id.discard);

        // if save button is clicked call async task with post request
        saveBtn.setOnClickListener(l ->
        {
            String apartmentName = (String) apartmentSpinner.getSelectedItem();
            int apartmentId = -1;
            for(Apartment apartment : apartments) {
                if(apartment.getApartmentName().equals(apartmentName))
                    apartmentId = apartment.getApartmentId();
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);

            String[] params = Util.prepareReservation(touristsNameView.getText().toString()
                    , LocalDate.parse(checkInDateView.getText().toString(), dateFormatter)
                    , LocalDate.parse(checkOutDateView.getText().toString(), dateFormatter)
                    , apartmentId, pricePerNightView.getText().toString()
                    , totalPriceView.getText().toString(), advancedPaymentCheckBox.isChecked()
                    , advancedPaymentCurrencySpinner.getSelectedItem().toString()
                    , advancedPaymentAmount.getText().toString()
                    , numberOfPersons.getText().toString(), numberOfAdults.getText().toString()
                    , numberOfChildren.getText().toString(), country.getText().toString()
                    , city.getText().toString(), phone.getText().toString()
                    , email.getText().toString(), pets.isChecked(), notesView.getText().toString());

            new SaveReservation(NewReservation.this).execute(params);
        });

        // if discard button is clicked finish activity
        discardBtn.setOnClickListener(l -> finish());
    }

    private class SaveReservation extends AsyncTask<String, Integer, String> {

        private boolean successfull = false;
        Activity activity;

        public SaveReservation(Activity activity) {
            this.activity = activity;
        }

        // parameters to post request
        @Override
        protected String doInBackground(String... params) {
            System.out.println("url=" + params[0]);
            System.out.println("query=" + params[1]);
            String urlString = params[0]; // URL to call
            String data = params[1]; //data to post
            OutputStream out = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setDoOutput(true);
                out = new BufferedOutputStream(urlConnection.getOutputStream());

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                writer.write(data);
                writer.flush();
                writer.close();

                out.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }
                String text = sb.toString();
                System.out.println("response=" + text);
                reader.close();

                urlConnection.connect();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                successfull = false;
                return null;
            }

            successfull = true;
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(successfull) {
                Toast toast = Toast.makeText(activity
                        , R.string.saving_reservation_successful, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(activity
                        , R.string.saving_reservation_not_successful, Toast.LENGTH_SHORT);
                toast.show();
            }
        }


    }

}
