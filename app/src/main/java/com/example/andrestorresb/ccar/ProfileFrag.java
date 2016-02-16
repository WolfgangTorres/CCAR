package com.example.andrestorresb.ccar;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by andrestorres on 2/15/16.
 */
public class ProfileFrag extends Fragment {
    public static final String ARG_EXA_NUMBER = "example_number";

    public ProfileFrag(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_profile, container, false);
        int i = getArguments().getInt(ARG_EXA_NUMBER);
        return rootView;
    }
}
