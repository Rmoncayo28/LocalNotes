package com.rmoncayo.localnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout mapHolder = findViewById(R.id.main_map_holder);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);
        FloatingActionButton fab = findViewById(R.id.main_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    class FABClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //TODO implement marker adding here after
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }
}
