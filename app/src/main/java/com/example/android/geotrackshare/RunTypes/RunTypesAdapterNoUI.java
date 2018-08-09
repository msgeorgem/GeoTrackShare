package com.example.android.geotrackshare.RunTypes;


import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Marcin on 2017-05-08.
 */

public class RunTypesAdapterNoUI extends ArrayAdapter<RunType> {

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the list is the data we want
     * to populate into the lists.
     *
     * @param context   The current context. Used to inflate the layout file.
     * @param listItems A List of AndroidFlavor objects to display in a list
     */
    public RunTypesAdapterNoUI(Context context, ArrayList<RunType> listItems) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, listItems);
    }

}
