package info.pramoth.hw5.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import info.androidhive.materialtabs.R;

public class MainActivity extends AppCompatActivity  {

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startActivity(new Intent(MainActivity.this, SimpleTabsActivity.class));
//        startActivity(new Intent(MainActivity.this, SimpleTabsActivity.class,MapsActivity.class));


    }


}
