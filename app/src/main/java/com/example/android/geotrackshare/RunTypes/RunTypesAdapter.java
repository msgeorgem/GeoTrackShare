package com.example.android.geotrackshare.RunTypes;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.android.geotrackshare.R;

import java.util.ArrayList;

/**
 * Created by Marcin on 2017-05-08.
 */

public class RunTypesAdapter extends ArrayAdapter<RunType> {

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the list is the data we want
     * to populate into the lists.
     *
     * @param context   The current context. Used to inflate the layout file.
     * @param listItems A List of AndroidFlavor objects to display in a list
     */
    public RunTypesAdapter(Activity context, int textViewResourceId, ArrayList<RunType> listItems) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, textViewResourceId, listItems);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The position in the list of data that should be displayed in the
     *                    list item view.
     * @param convertView The recycled view to populate.
     * @param parent      The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {

        // Check if the existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_run_type, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the {@link WItem} object located at this position in the list
        RunType currentItem = getItem(position);

        viewHolder.titleTextView.setText(currentItem.getTitle());

        // Get the image resource ID from the current AndroidFlavor object and
        // set the image to iconView
        // Check if an image is provided for this word or not
        if (currentItem.hasImage()) {
            // If an image is available, display the provided image based on the resource ID
            viewHolder.iconView.setImageResource(currentItem.getPicture());
            // Make sure the view is visible
            viewHolder.iconView.setVisibility(View.VISIBLE);
        } else {
            // Otherwise hide the ImageView (set visibility to GONE)
            viewHolder.iconView.setVisibility(View.GONE);
        }
        return convertView;

    }

    class ViewHolder {
        private TextView titleTextView;
        private ImageView iconView;

        public ViewHolder(@NonNull View view) {
            this.titleTextView = view
                    .findViewById(R.id.run_type_text_view);
            this.iconView = view
                    .findViewById(R.id.icon);
        }
    }
}
