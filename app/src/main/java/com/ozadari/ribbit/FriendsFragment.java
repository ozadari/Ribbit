package com.ozadari.ribbit;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Oz Adari on 18/05/2014.
 */
public class FriendsFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("FriendsFragment", "start");
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        Log.i("FriendsFragment", "ends");
        return rootView;
    }
}
