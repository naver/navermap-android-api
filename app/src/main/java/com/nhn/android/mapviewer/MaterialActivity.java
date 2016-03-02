package com.nhn.android.mapviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapView;

/**
 * Created by acekim on 16. 3. 2.
 */

/**
 * @author acekim
 * This example show how to develop Material Design with Naver map API
 * you can write material design by using AppCompatDelegate class.
 * @see themes.xml / colors.xml / activity_material.xml / AndroidManifest.xml
 */

public class MaterialActivity extends NMapActivity implements AppCompatCallback {

    private final static String TAG = MaterialActivity.class.getName();

    private final static String API_KEY = "Insert your Naver map API KEY";
    private final static String CLIENT_ID = "Insert your Naver map CLIENT_ID";

    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDelegate = AppCompatDelegate.create(this, this);
        mDelegate.onCreate(savedInstanceState);
        mDelegate.setContentView(R.layout.activity_material);

        initToolbar();
        initNaverMap();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.material_toolbar);
        mDelegate.setSupportActionBar(toolbar);
    }

    private void initNaverMap() {
        NMapView mapView = (NMapView) findViewById(R.id.material_mapview);
        //mMapView.setApiKey(API_KEY);
        mapView.setClientId(CLIENT_ID);
        mapView.setEnabled(true);
        mapView.setClickable(true);
        mapView.setFocusable(true);
        mapView.setFocusableInTouchMode(true);
        mapView.requestFocus();
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        Log.i(TAG, "onSupportActionModeStarted");
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        Log.i(TAG, "onSupportActionModeFinished");
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        Log.i(TAG, "onWindowStartingSupportActionMode");
        return null;
    }
}