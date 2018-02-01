package com.rmoncayo.localnotes.data;

import android.net.Uri;

import de.triplet.simpleprovider.AbstractProvider;
import de.triplet.simpleprovider.Column;
import de.triplet.simpleprovider.Table;

/**
 * Created by monca on 1/28/2018.
 * ContentProvider for the Notes database which is implemented using SimpleProvider library
 */

public class NotesProvider extends AbstractProvider {
    public static final String authority = "com.rmoncayo.localnotes.data";
    public static final Uri notesContentUri = Uri.parse("content://" + authority + "/notes");

    @Override
    protected String getAuthority() {
        return authority;
    }

    @Override
    protected int getSchemaVersion() {
        return 2;
    }

    @Table
    public class Note {
        @Column(value = Column.FieldType.INTEGER, primaryKey = true)
        public static final String KEY_ID = "_id";
        @Column(Column.FieldType.TEXT)
        public static final String KEY_TITLE = "title";
        @Column(Column.FieldType.TEXT)
        public static final String KEY_AUDIO_PATH = "audioPath";
        @Column(Column.FieldType.TEXT)
        public static final String KEY_IMAGE_PATH = "imagePath";
        @Column(Column.FieldType.TEXT)
        public static final String KEY_BODY = "noteBody";
        @Column(Column.FieldType.FLOAT)
        public static final String KEY_LAT = "latitude";
        @Column(Column.FieldType.FLOAT)
        public static final String KEY_LONG = "longitude";

        @Column(value = Column.FieldType.INTEGER, since = 2)
        public static final String KEY_SHOULD_POLL = "shouldPollBoolean";


    }
}
