package com.example.zlatan.track;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class EditCreateListPOI extends AppCompatActivity {

    List<String> primjerListe = new ArrayList<String>();
    static final int ADD_POI_REQUEST = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_create_list_poi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPoiIntent = new Intent(getApplicationContext(), AddPOI.class);
                startActivityForResult(pickPoiIntent, ADD_POI_REQUEST);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        primjerListe.add("Vozilo 1");
        primjerListe.add("Vozilo 2");
        primjerListe.add("Vozilo 3");
        primjerListe.add("Vozilo 4");

        ListView lw = (ListView) findViewById(R.id.edit_create_poi_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, primjerListe);
        lw.setAdapter(adapter);

        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("poi_id", Integer.toString(position));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}
