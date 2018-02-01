package com.rmoncayo.localnotes;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rmoncayo.localnotes.data.NotesProvider;
import com.squareup.picasso.Picasso;

import java.io.File;

import static com.rmoncayo.localnotes.AddNoteActivity.ID_NOTE_KEY;
import static com.rmoncayo.localnotes.data.NotesProvider.Note.KEY_AUDIO_PATH;
import static com.rmoncayo.localnotes.data.NotesProvider.Note.KEY_BODY;
import static com.rmoncayo.localnotes.data.NotesProvider.Note.KEY_ID;
import static com.rmoncayo.localnotes.data.NotesProvider.Note.KEY_IMAGE_PATH;
import static com.rmoncayo.localnotes.data.NotesProvider.Note.KEY_LAT;
import static com.rmoncayo.localnotes.data.NotesProvider.Note.KEY_LONG;
import static com.rmoncayo.localnotes.data.NotesProvider.Note.KEY_TITLE;

public class ViewNoteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private Double mLat = null;
    private Double mLong = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        Intent callingIntent = getIntent();
        long id = callingIntent.getLongExtra(ID_NOTE_KEY, -1);
        if (id == -1) {
            finish();
        }
        String[] projection = new String[]{KEY_TITLE,
                KEY_BODY,
                KEY_AUDIO_PATH,
                KEY_IMAGE_PATH,
                KEY_LAT,
                KEY_LONG};
        String selection = KEY_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor data = getContentResolver().query(NotesProvider.notesContentUri,
                projection,
                selection,
                selectionArgs,
                null);
        assert data != null;
        data.moveToFirst();

        int titleIndex = data.getColumnIndex(KEY_TITLE);
        int bodyIndex = data.getColumnIndex(KEY_BODY);
        int audioIndex = data.getColumnIndex(KEY_AUDIO_PATH);
        int imageIndex = data.getColumnIndex(KEY_IMAGE_PATH);
        int latIndex = data.getColumnIndex(KEY_LAT);
        int longIndex = data.getColumnIndex(KEY_LONG);

        String title = data.getString(titleIndex);
        String body = data.getString(bodyIndex);
        String audioPath = data.getString(audioIndex);
        String imagePath = data.getString(imageIndex);
        mLat = data.getDouble(latIndex);
        mLong = data.getDouble(longIndex);
        if (mGoogleMap != null) {
            LatLng latLng = new LatLng(mLat, mLong);
            addMarkerAndMoveCamera(latLng, mGoogleMap);
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.view_note_map);
        mapFragment.getMapAsync(this);

        TextView bodyTextView = findViewById(R.id.view_note_body);
        ImageView imageView = findViewById(R.id.view_note_image);
        if (imagePath != null && !imagePath.equals("")) {
            File imageFile = new File(imagePath);
            Picasso.with(this).load(imageFile).into(imageView);
        } else {
            imageView.setVisibility(ImageView.GONE);
        }

        setTitle(title);
        bodyTextView.setText(body);


        data.close();
    }
    private void addMarkerAndMoveCamera(LatLng latLng, GoogleMap googleMap) {
        if (googleMap != null) {
            if (latLng != null) {
                mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mLat != null && mLong != null) {
            LatLng latLng = new LatLng(mLat, mLong);
            addMarkerAndMoveCamera(latLng, googleMap);
        }
    }
}
