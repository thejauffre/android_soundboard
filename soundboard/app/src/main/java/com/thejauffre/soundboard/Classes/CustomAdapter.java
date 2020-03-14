package com.thejauffre.soundboard.Classes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.thejauffre.soundboard.R;

import java.util.List;

import static com.thejauffre.soundboard.Classes.Common.LOGTAG;
import static com.thejauffre.soundboard.Classes.Common.playFile;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public CustomAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.filename = mData.get(position);
        holder.myTextView.setText(holder.filename.substring(0, holder.filename.lastIndexOf('.')));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        Button imgButton;
        String filename;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.header_name);
            imgButton = itemView.findViewById(R.id.imageButton);
            imgButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d(LOGTAG, "Pressed element: " + myTextView.getText());
            playFile(filename);
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}