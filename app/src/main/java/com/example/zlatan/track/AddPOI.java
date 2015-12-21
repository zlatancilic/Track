package com.example.zlatan.track;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class AddPOI extends AppCompatActivity {

    List<String> poi_types_array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        poi_types_array = new ArrayList<String>();
        poi_types_array.add("Vehicle");
        poi_types_array.add("Cellphone");
        poi_types_array.add("Buliding");


        Spinner spinner = (Spinner) findViewById(R.id.poi_type_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                R.layout.spinner_item, poi_types_array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select Type");
    }

}
