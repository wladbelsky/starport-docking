package com.wladbelsky.starportdocking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class RequestLP extends AppCompatActivity{

    private Button startButton, endButton;
    private Spinner locSelect;


    private Calendar start = Calendar.getInstance(),end = Calendar.getInstance();
    private List<String> locations =  new ArrayList<>();
    private ArrayAdapter<String> locAdapter;

    public static final int REQUEST_PAD_CODE = 1956;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_lp);
        startButton = findViewById(R.id.start_date_button);
        endButton = findViewById(R.id.end_date_button);
        locSelect = findViewById(R.id.location_spinner);
        final DatePickerDialog startDatePickerDialog = new DatePickerDialog(
                this, new StartListener(), start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
        final DatePickerDialog endDatePickerDialog = new DatePickerDialog(
                this, new EndListener(), start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
        setTextDate(startButton, start);
        setTextDate(endButton, end);
        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startDatePickerDialog.show();
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                endDatePickerDialog.show();
            }
        });
        GetPlaces getPlaces = new GetPlaces();
        getPlaces.execute();
    }

    public void requestClick(View v)
    {
        if(start.getTime().compareTo(end.getTime()) > 0) {
            startButton.setError("");
            endButton.setError("");
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.arrival_greater_that_departure), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        else {
            startButton.setError(null);
            endButton.setError(null);
            PostOrderTask pot = new PostOrderTask(SplashActivity.getToken(), start, end);
            pot.execute();
        }
    }

    private class GetPlaces extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONArray jObject = new JSONArray(SplashActivity.getJsonFromServer(SplashActivity.serverIP + "/get_locations.php"));
                Log.i("oracle", jObject.toString());

                Log.i("oracle", jObject.length()+"");
                for(int i = 0; i < jObject.length(); i++)
                {
                    locations.add(jObject.getJSONObject(i).getString("name"));
                }
            } catch (Exception e) {
                Log.e("oracle", "Err when connect \n" + e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                locAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, locations);
                locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                locSelect.setAdapter(locAdapter);
            }
        }
    }


    private class PostOrderTask extends AsyncTask<Void, Void, Boolean> {


        private final String mToken;
        private final Calendar mStart;
        private final Calendar mEnd;
        private int pad_id;
        PostOrderTask(String token, Calendar start, Calendar end) {
            mToken = token;
            mStart = start;
            mEnd = end;
        }

        private String toSqlDate(Calendar c)
        {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
            return format.format(c.getTime());
        }

        @Override
        protected Boolean doInBackground(Void... params) {



            try {
                JSONObject jObject = new JSONObject(SplashActivity.getJsonFromServer(SplashActivity.serverIP + "/post_order.php?token="+mToken+
                        "&start="+toSqlDate(mStart)+"&end="+toSqlDate(mEnd)+"&location="+locSelect.getSelectedItem()));
                if(jObject.getBoolean("success")) {
                    pad_id = jObject.getInt("pad_num");
                    return true;
                }
                else
                    return false;

            } catch (Exception e) {
                Log.e("oracle", "Err when connect \n" + e.getMessage());
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                Intent intent = new Intent();
                intent.putExtra("pad", pad_id);
                setResult(RESULT_OK, intent);
                finish();
            }
            else
            {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.pad_reserve_denied), Snackbar.LENGTH_LONG)
                       .setAction("Action", null).show();
            }
        }
    }
    /*
    Intent intent = new Intent();
    intent.putExtra("name", etName.getText().toString());
    setResult(RESULT_OK, intent);
    */


    private void setTextDate(Button button, Calendar calendar)
    {
        button.setText(String.format("%1$tA %1$tb %1$td %1$tY", calendar));
    }

    public class StartListener implements DatePickerDialog.OnDateSetListener
    {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            start.set(year,month,day);
            setTextDate(startButton, start);

        }
    }

    public class EndListener implements DatePickerDialog.OnDateSetListener
    {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            end.set(year,month,day);
            setTextDate(endButton,end);
        }
    }

}
