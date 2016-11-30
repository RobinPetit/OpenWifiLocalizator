package com.ulbmap.ulbmap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity implements OnTouchListener,OnClickListener,SearchView.OnQueryTextListener {

    Zoom zoom = new Zoom();
    ImageView imageView;
    Button changePlan;
    Button local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.plan);
        imageView.setOnTouchListener(this);
        changePlan = (Button) findViewById(R.id.changePlan);
        changePlan.setOnClickListener(this);
        local = (Button) findViewById(R.id.local);
        local.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.Search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    public boolean onQueryTextSubmit(String s){
        System.out.println("text envoyé : "+s);
        return false;
    }

    public boolean onQueryTextChange(String s){
        System.out.println("text modifié : "+s);
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        zoom.start(v,event);
        return true;
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.changePlan:
                dialog();
                break;
            case R.id.local:
                searchLocal();
                break;
        }
    }

    public void dialog(){
        String[] items = {"Plaine", "Solbosch"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                switch (item) {
                    case 0:
                        imageView.setImageResource(R.drawable.plaine);
                        break;
                    case 1:
                        imageView.setImageResource(R.drawable.solbosch);
                        break;
                }

            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void searchLocal(){
        final String[] items = {"Forum A",
                "Forum B",
                "Forum C",
                "Forum D",
                "Forum E",
                "Forum F",
                "Forum G",
                "Forum H",
                "Pof 2058",
                "Pof 2064",
                "Pof 2066",
                "Pof 2070",
                "Pof 2072",
                "Pof 2076",
                "Pof 2078",
                "Pof 2080"
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Locaux");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                System.out.println("Sélection du local : "+items[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}