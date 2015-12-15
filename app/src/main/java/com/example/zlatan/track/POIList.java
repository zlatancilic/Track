package com.example.zlatan.track;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class POIList extends AppCompatActivity {

    List<String> primjerListe = new ArrayList<String>();
    String[] commandList = {"Close windows","Stop vehicle","Lock vehicle"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poilist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //primjerListe = new ArrayList<String>();
        primjerListe.add("Vozilo 1");
        primjerListe.add("Vozilo 2");
        primjerListe.add("Vozilo 3");
        primjerListe.add("Vozilo 4");

        ListView lw = (ListView) findViewById(R.id.poi_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, primjerListe);
        lw.setAdapter(adapter);
        registerForContextMenu(lw);

        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("poi_id",Integer.toString(position));
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });


    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.poi_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Send command to " + primjerListe.get(info.position));
            String[] menuItems = commandList;
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("poi_id", Integer.toString(info.position));
        returnIntent.putExtra("command_id", Integer.toString(menuItemIndex));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        return true;
    }

}
