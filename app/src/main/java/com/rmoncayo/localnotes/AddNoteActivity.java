package com.rmoncayo.localnotes;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.rmoncayo.localnotes.data.NotesProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AddNoteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mAudioFileName = null;

    private MediaRecorder mRecorder = null;
    //private Visualizer mVisualizer = null;

    private String mImagePath = "";
    private String mAudioPath = "";
    private String mTitleString = "";
    private String mBodyString = "";
    private Double mLat = null;
    private Double mLong = null;
    private GoogleMap mGoogleMap = null;
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final float GEOFENCE_RADIUS_IN_METERS = 50;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        mAudioFileName = getFilesDir().getAbsolutePath();
//        DateFormat timeStampFormat = getDateTimeInstance();
//        Date myDate = new Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mAudioFileName += "/" + timeStamp + ".3gp";

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(NotesProvider.Note.KEY_AUDIO_PATH)) {
                mAudioPath = savedInstanceState.getString(NotesProvider.Note.KEY_AUDIO_PATH);
            }
            if (savedInstanceState.containsKey(NotesProvider.Note.KEY_IMAGE_PATH)) {
                mImagePath = savedInstanceState.getString(NotesProvider.Note.KEY_IMAGE_PATH);
            }
            if (savedInstanceState.containsKey(NotesProvider.Note.KEY_TITLE)) {
                mTitleString = savedInstanceState.getString(NotesProvider.Note.KEY_TITLE);
            }
            if (savedInstanceState.containsKey(NotesProvider.Note.KEY_BODY)) {
                mBodyString = savedInstanceState.getString(NotesProvider.Note.KEY_BODY);
            }
            if (savedInstanceState.containsKey(NotesProvider.Note.KEY_LAT)) {
                mLat = savedInstanceState.getDouble(NotesProvider.Note.KEY_LAT);
            }
            if (savedInstanceState.containsKey(NotesProvider.Note.KEY_LONG)) {
                mLong = savedInstanceState.getDouble(NotesProvider.Note.KEY_LONG);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        } else {
            setContentView(R.layout.activity_add_note);
            LinearLayout recordButtonHolder = findViewById(R.id.record_audio_button_holder);
            RecordButton mRecordButton = new RecordButton(this);
            recordButtonHolder.addView(mRecordButton);
            Button pictureButton = findViewById(R.id.take_picture_button);
            pictureButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            });
            ((EditText) findViewById(R.id.note_name_edit_text))
                    .getText()
                    .append(mTitleString);
            ((EditText) findViewById(R.id.note_body_edit_text))
                    .getText()
                    .append(mBodyString);

            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.add_note_map);
            mapFragment.getMapAsync(this);

            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    // TODO: Get info about the selected place.
                    LatLng latLng = place.getLatLng();
                    mLat = latLng.latitude;
                    mLong = latLng.longitude;
                    if (mGoogleMap != null) {
                        mGoogleMap.clear();
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(latLng))
                                .setTitle(String.valueOf(place.getAddress()));
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i(LOG_TAG, "An error occurred: " + status);
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_note_menu, menu);
        return true;
    }

    //TODO probably deal with recording audio while rotating, stop recording ondestroy etc
    private void onSaveButtonClicked(MenuItem item) {
        EditText editText = findViewById(R.id.note_name_edit_text);
        if (editText.getText().toString().trim().equalsIgnoreCase("")) {
            editText.setError(getString(R.string.note_title_empty_error_string));
        } else if (mLat == null || mLong == null) {
            Toast.makeText(this, R.string.no_location_selected_string_add_note, Toast.LENGTH_LONG).show();
        } else {
            editText.setError(null);
            EditText bodyEditText = findViewById(R.id.note_body_edit_text);

            GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_LONG).show();
                finish();
                return;
            }


            ContentValues contentValues = new ContentValues();
            contentValues.put(NotesProvider.Note.KEY_TITLE, editText.getText().toString().trim());
            contentValues.put(NotesProvider.Note.KEY_IMAGE_PATH, mImagePath);
            contentValues.put(NotesProvider.Note.KEY_AUDIO_PATH, mAudioPath);
            contentValues.put(NotesProvider.Note.KEY_BODY, bodyEditText.getText().toString().trim());
            contentValues.put(NotesProvider.Note.KEY_LAT, mLat);
            contentValues.put(NotesProvider.Note.KEY_LONG, mLong);
            Uri insertedUri = getContentResolver().insert(NotesProvider.notesContentUri, contentValues);
            Intent viewNoteIntent = new Intent(this, ViewNoteActivity.class);
            long rowId = ContentUris.parseId(insertedUri);
            viewNoteIntent.putExtra(NotesProvider.Note.KEY_ID, rowId);

            List<Geofence> geofenceList = new ArrayList<>();
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(String.valueOf(rowId))
                    .setCircularRegion(mLat, mLong, GEOFENCE_RADIUS_IN_METERS)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
            GeofencingRequest geofencingRequest = getGeofencingRequest(geofenceList);
            geofencingClient.addGeofences(geofencingRequest,
                    createGeofencePendingIntent())
                    .addOnSuccessListener(this,
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("ADDNOTE", String.valueOf(mLat) + " " + String.valueOf(mLong) + "onSuccess");
                                }
                            });

            startActivity(viewNoteIntent);


        }
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //initialTrigger is 0 so that this won't trigger when added to location the user is at
        builder.setInitialTrigger(0);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent createGeofencePendingIntent() {
        Log.d(LOG_TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        EditText editText = findViewById(R.id.note_name_edit_text);
        EditText bodyEditText = findViewById(R.id.note_body_edit_text);
        outState.putString(NotesProvider.Note.KEY_TITLE, editText.getText().toString().trim());
        outState.putString(NotesProvider.Note.KEY_IMAGE_PATH, mImagePath);
        outState.putString(NotesProvider.Note.KEY_AUDIO_PATH, mAudioPath);
        outState.putString(NotesProvider.Note.KEY_BODY, bodyEditText.getText().toString());
        if (mLat != null) {
            outState.putDouble(NotesProvider.Note.KEY_LAT, mLat);
        }
        if (mLong != null) {
            outState.putDouble(NotesProvider.Note.KEY_LONG, mLong);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                //TODO fix java.lang.IllegalArgumentException: Missing android.support.FILE_PROVIDER_PATHS meta-data V

                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.rmoncayo.localnotes",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();
        } else {
            mImagePath = "";
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mImagePath = image.getAbsolutePath();
        return image;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note_save_menu_button:
                onSaveButtonClicked(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }


    public class RecordButton extends AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText(R.string.stop_recording_button_string);
                } else {
                    setText(R.string.start_recording_button_string);
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText(R.string.start_recording_button_string);
            setOnClickListener(clicker);
        }
    }

    private void onRecord(boolean start) {
        if (start) {
            try {
                startRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*if (mVisualizer != null) {
                mVisualizer.startListening();
            }*/
        } else {
            stopRecording();
            /*if (mVisualizer != null) {
                mVisualizer.stopListening();
            }*/
        }
    }

    //TODO 1/27 think about handling recording and then losing focus/stopping
    private void startRecording() throws IOException {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //TODO 1/27 maybe add ability to change audio recording quality in a settings menu
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mAudioFileName);

        mRecorder.prepare();
        mRecorder.start();
    }

    private void stopRecording() {
        mAudioPath = mAudioFileName;
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
    }
}
