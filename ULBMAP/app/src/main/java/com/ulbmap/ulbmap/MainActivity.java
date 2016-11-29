package com.ulbmap.ulbmap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
public class MainActivity extends AppCompatActivity implements OnTouchListener,OnClickListener {

    Zoom zoom = new Zoom();
    ImageView imageView;
    Button changePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.plan);
        imageView.setOnTouchListener(this);
        changePlan = (Button) findViewById(R.id.changePlan);
        changePlan.setOnClickListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        zoom.start(v,event);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.Search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String s) {
                System.out.println("Menu 1 : "+s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                System.out.println("Menu 2 : "+s);
                return false;
            }
        });
        return true;
    }

    public void onClick(View v){
        dialog();
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
                }

            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }
}
