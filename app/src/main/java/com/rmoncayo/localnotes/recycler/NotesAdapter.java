package com.rmoncayo.localnotes.recycler;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rmoncayo.localnotes.R;
import com.rmoncayo.localnotes.ViewNoteActivity;
import com.rmoncayo.localnotes.data.NotesProvider;

/**
 * Created by monca on 2/1/2018.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private DataSetObserver mDataSetObserver;

    public NotesAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_layout, parent, false);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.view.setTag(position);
        ((TextView)holder.view.findViewById(R.id.recycler_note_title_text_view))
                .setText(mCursor.getString(mCursor.getColumnIndex(NotesProvider.Note.KEY_TITLE)));

    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout view;
        ViewHolder(View itemView) {
            super(itemView);
            this.view = (LinearLayout) itemView;
            this.view.findViewById(R.id.recycler_note_title_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int postition = (int) view.getTag();
                    mCursor.moveToPosition((Integer) view.getTag());
                    long id = mCursor.getLong(mCursor.getColumnIndex(NotesProvider.Note.KEY_ID));
                    Intent intent = new Intent(mContext, ViewNoteActivity.class);
                    intent.putExtra(NotesProvider.Note.KEY_ID, id);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
    