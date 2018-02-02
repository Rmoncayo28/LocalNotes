package com.rmoncayo.localnotes;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rmoncayo.localnotes.data.NotesProvider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private MediaPlayer mMediaPlayer = null;
    private Long mId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        Intent callingIntent = getIntent();
        if (callingIntent == null) {
            finish();
        }
        long id = callingIntent.getLongExtra(NotesProvider.Note.KEY_ID, -1);
        mId = id;
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
        final Button audioButton = findViewById(R.id.view_note_play_button);
        //TODO 2/1 make mMediaPlayer static and check if it == null before creating it
        if (audioPath != null && !audioPath.equals("")) {
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(audioPath);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        audioButton.setText(R.string.view_note_audio_play_text);
                    }
                });
                audioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                            audioButton.setText(R.string.view_note_audio_play_text);
                        } else {
                            mMediaPlayer.start();
                            audioButton.setText(R.string.view_note_audio_pause_text);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                audioButton.setVisibility(Button.GONE);
            }
        } else {
            audioButton.setVisibility(Button.GONE);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_note_menu_button:
                GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);
                List<String> requestIds = new ArrayList<>();
                requestIds.add(String.valueOf(mId));
                String where = NotesProvider.Note.KEY_ID + "=?";
                String[] whereArgs = new String[]{String.valueOf(mId)};
                int numDeleted =
                        getContentResolver().delete(NotesProvider.notesContentUri, where, whereArgs);
                if (numDeleted == 0) {
                    Toast.makeText(this,
                            "Unable to delete note",
                            Toast.LENGTH_LONG).show();
                } else {
                    geofencingClient.removeGeofences(requestIds);
                    Intent goToMain = new Intent(this, MainActivity.class);
                    startActivity(goToMain);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
