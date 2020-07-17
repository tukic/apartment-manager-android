package com.ukic.app.appmanager;

import android.app.DatePickerDialog;
import android.content.Context;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Console;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

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

    private EditText notesView;
    private ViewGroup newReservationLayout;

    // additional fields shown
    private boolean additionalFields = false;

    // additional views
    private View additionalInfoViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_new);

        touristsNameView = findViewById(R.id.tourists_name);
        checkInDateView = findViewById(R.id.check_in_date);
        checkOutDateView = findViewById(R.id.check_out_date);

        // date pickers shows calendar picker dialogs after clicked
        checkInDateView.setOnClickListener(l -> {
            final Calendar calendar = Calendar.getInstance();
            // date picker dialog
            DatePickerDialog checkInPicker = new DatePickerDialog(NewReservation.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                        String date = LocalDate.of(year, monthOfYear, dayOfMonth)
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
                    String date = LocalDate.of(year, monthOfYear, dayOfMonth)
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

        // fill in any details dynamically here
        notesView = additionalInfoViews.findViewById(R.id.notes);

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



    }

}
