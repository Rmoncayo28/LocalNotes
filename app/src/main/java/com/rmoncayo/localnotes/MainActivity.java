package com.rmoncayo.localnotes;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rmoncayo.localnotes.data.NotesProvider;
import com.rmoncayo.localnotes.recycler.NotesAdapter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private final int cursorLoaderId = 1;
    private RecyclerView mRecyclerView = null;
    private GoogleMap mGoogleMap;
    private final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        mapFragment.getView().getLayoutParams().height = height / 2;
        FloatingActionButton fab = findViewById(R.id.main_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivity(intent);
            }
        });
        mRecyclerView = findViewById(R.id.main_notes_recycler);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,
                        false));
        getLoaderManager().initLoader(cursorLoaderId, null, this);
        ActivityCompat.requestPermissions(this, permissions, MY_LOCATION_REQUEST_CODE);

    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{NotesProvider.Note.KEY_ID,
                NotesProvider.Note.KEY_TITLE,
                NotesProvider.Note.KEY_LAT,
                NotesProvider.Note.KEY_LONG};
        return new CursorLoader(this,
                NotesProvider.notesContentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        mRecyclerView.setAdapter(new NotesAdapter(this, data));

    }


    @Override
    public void onLoaderReset(Loader loader) {

    }

    private static final int MY_LOCATION_REQUEST_CODE = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 2 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                if (mGoogleMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mGoogleMap.setMyLocationEnabled(true);
                }

            } else {

                Toast.makeText(this,
                        "This app requires location permission to function",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }
}
