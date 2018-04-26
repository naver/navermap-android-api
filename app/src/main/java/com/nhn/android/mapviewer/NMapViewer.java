/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nhn.android.mapviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapCircleData;
import com.nhn.android.maps.overlay.NMapCircleStyle;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapCalloutCustomOverlay;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

/**
 * Sample class for map viewer library.
 * 
 * @author kyjkim
 */
public class NMapViewer extends NMapActivity {
	private static final String LOG_TAG = "NMapViewer";
	private static final boolean DEBUG = false;

	// set your Client ID which is registered for NMapViewer library.
	private static final String CLIENT_ID = "";

	private MapContainerView mMapContainerView;

	private NMapView mMapView;
	private NMapController mMapController;

	private static final NGeoPoint NMAP_LOCATION_DEFAULT = new NGeoPoint(126.978371, 37.5666091);
	private static final int NMAP_ZOOMLEVEL_DEFAULT = 11;
	private static final int NMAP_VIEW_MODE_DEFAULT = NMapView.VIEW_MODE_VECTOR;
	private static final boolean NMAP_TRAFFIC_MODE_DEFAULT = false;
	private static final boolean NMAP_BICYCLE_MODE_DEFAULT = false;

	private static final String KEY_ZOOM_LEVEL = "NMapViewer.zoomLevel";
	private static final String KEY_CENTER_LONGITUDE = "NMapViewer.centerLongitudeE6";
	private static final String KEY_CENTER_LATITUDE = "NMapViewer.centerLatitudeE6";
	private static final String KEY_VIEW_MODE = "NMapViewer.viewMode";
	private static final String KEY_TRAFFIC_MODE = "NMapViewer.trafficMode";
	private static final String KEY_BICYCLE_MODE = "NMapViewer.bicycleMode";

	private SharedPreferences mPreferences;

	private NMapOverlayManager mOverlayManager;

	private NMapMyLocationOverlay mMyLocationOverlay;
	private NMapLocationManager mMapLocationManager;
	private NMapCompassManager mMapCompassManager;

	private NMapViewerResourceProvider mMapViewerResourceProvider;

	private NMapPOIdataOverlay mFloatingPOIdataOverlay;
	private NMapPOIitem mFloatingPOIitem;

	private static boolean USE_XML_LAYOUT = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (USE_XML_LAYOUT) {
			setContentView(R.layout.main);

			mMapView = (NMapView)findViewById(R.id.mapView);
		} else {
			// create map view
			mMapView = new NMapView(this);

			// create parent view to rotate map view
			mMapContainerView = new MapContainerView(this);
			mMapContainerView.addView(mMapView);

			// set the activity content to the parent view
			setContentView(mMapContainerView);
		}

		// set a registered Client Id for Open MapViewer Library
		mMapView.setNcpClientId(CLIENT_ID);

		// initialize map view
		mMapView.setClickable(true);
		mMapView.setEnabled(true);
		mMapView.setFocusable(true);
		mMapView.setFocusableInTouchMode(true);
		mMapView.requestFocus();

		// register listener for map state changes
		mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
		mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
		mMapView.setOnMapViewDelegate(onMapViewTouchDelegate);

		// use map controller to zoom in/out, pan and set map center, zoom level etc.
		mMapController = mMapView.getMapController();

		// use built in zoom controls
		NMapView.LayoutParams lp = new NMapView.LayoutParams(LayoutParams.WRAP_CONTENT,
			LayoutParams.WRAP_CONTENT, NMapView.LayoutParams.BOTTOM_RIGHT);
		mMapView.setBuiltInZoomControls(true, lp);

		// create resource provider
		mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

		// set data provider listener
		super.setMapDataProviderListener(onDataProviderListener);

		// create overlay manager
		mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
		// register callout overlay listener to customize it.
		mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
		// register callout overlay view listener to customize it.
		mOverlayManager.setOnCalloutOverlayViewListener(onCalloutOverlayViewListener);

		// location manager
		mMapLocationManager = new NMapLocationManager(this);
		mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

		// compass manager
		mMapCompassManager = new NMapCompassManager(this);

		// create my location overlay
		mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {

		stopMyLocation();

		super.onStop();
	}

	@Override
	protected void onDestroy() {

		// save map view state such as map center position and zoom level.
		saveInstanceState();

		super.onDestroy();
	}

	/* Test Functions */

	private void startMyLocation() {

		if (mMyLocationOverlay != null) {
			if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
				mOverlayManager.addOverlay(mMyLocationOverlay);
			}

			if (mMapLocationManager.isMyLocationEnabled()) {

				if (!mMapView.isAutoRotateEnabled()) {
					mMyLocationOverlay.setCompassHeadingVisible(true);

					mMapCompassManager.enableCompass();

					mMapView.setAutoRotateEnabled(true, false);

					mMapContainerView.requestLayout();
				} else {
					stopMyLocation();
				}

				mMapView.postInvalidate();
			} else {
				boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
				if (!isMyLocationEnabled) {
					Toast.makeText(NMapViewer.this, "Please enable a My Location source in system settings",
						Toast.LENGTH_LONG).show();

					Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(goToSettings);

					return;
				}
			}
		}
	}

	private void stopMyLocation() {
		if (mMyLocationOverlay != null) {
			mMapLocationManager.disableMyLocation();

			if (mMapView.isAutoRotateEnabled()) {
				mMyLocationOverlay.setCompassHeadingVisible(false);

				mMapCompassManager.disableCompass();

				mMapView.setAutoRotateEnabled(false, false);

				mMapContainerView.requestLayout();
			}
		}
	}

	private void testPathDataOverlay() {

		// set path data points
		NMapPathData pathData = new NMapPathData(9);

		pathData.initPathData();
		pathData.addPathPoint(127.108099, 37.366034, NMapPathLineStyle.TYPE_SOLID);
		pathData.addPathPoint(127.108088, 37.366043, 0);
		pathData.addPathPoint(127.108079, 37.365619, 0);
		pathData.addPathPoint(127.107458, 37.365608, 0);
		pathData.addPathPoint(127.107232, 37.365608, 0);
		pathData.addPathPoint(127.106904, 37.365624, 0);
		pathData.addPathPoint(127.105933, 37.365621, NMapPathLineStyle.TYPE_DASH);
		pathData.addPathPoint(127.105929, 37.366378, 0);
		pathData.addPathPoint(127.106279, 37.366380, 0);
		pathData.endPathData();

		NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
		if (pathDataOverlay != null) {

			// add path data with polygon type
			NMapPathData pathData2 = new NMapPathData(4);
			pathData2.initPathData();
			pathData2.addPathPoint(127.106, 37.367, NMapPathLineStyle.TYPE_SOLID);
			pathData2.addPathPoint(127.107, 37.367, 0);
			pathData2.addPathPoint(127.107, 37.368, 0);
			pathData2.addPathPoint(127.106, 37.368, 0);
			pathData2.endPathData();
			pathDataOverlay.addPathData(pathData2);
			// set path line style
			NMapPathLineStyle pathLineStyle = new NMapPathLineStyle(mMapView.getContext());
			pathLineStyle.setPataDataType(NMapPathLineStyle.DATA_TYPE_POLYGON);
			pathLineStyle.setLineColor(0xA04DD2, 0xff);
			pathLineStyle.setFillColor(0xFFFFFF, 0x00);
			pathData2.setPathLineStyle(pathLineStyle);

			// add circle data
			NMapCircleData circleData = new NMapCircleData(1);
			circleData.initCircleData();
			circleData.addCirclePoint(127.1075, 37.3675, 50.0F);
			circleData.endCircleData();
			pathDataOverlay.addCircleData(circleData);
			// set circle style
			NMapCircleStyle circleStyle = new NMapCircleStyle(mMapView.getContext());
			circleStyle.setLineType(NMapPathLineStyle.TYPE_DASH);
			circleStyle.setFillColor(0x000000, 0x00);
			circleData.setCircleStyle(circleStyle);

			// show all path data
			pathDataOverlay.showAllPathData(0);
		}
	}

	private void testPathPOIdataOverlay() {

		// set POI data
		NMapPOIdata poiData = new NMapPOIdata(4, mMapViewerResourceProvider, true);
		poiData.beginPOIdata(4);
		poiData.addPOIitem(349652983, 149297368, "Pizza 124-456", NMapPOIflagType.FROM, null);
		poiData.addPOIitem(349652966, 149296906, null, NMapPOIflagType.NUMBER_BASE + 1, null);
		poiData.addPOIitem(349651062, 149296913, null, NMapPOIflagType.NUMBER_BASE + 999, null);
		poiData.addPOIitem(349651376, 149297750, "Pizza 000-999", NMapPOIflagType.TO, null);
		poiData.endPOIdata();

		// create POI data overlay
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);

		// set event listener to the overlay
		poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

	}

	private void testPOIdataOverlay() {

		// Markers for POI item
		int markerId = NMapPOIflagType.PIN;

		// set POI data
		NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
		poiData.beginPOIdata(2);
		NMapPOIitem item = poiData.addPOIitem(127.0630205, 37.5091300, "Pizza 777-111", markerId, 0);
		item.setRightAccessory(true, NMapPOIflagType.CLICKABLE_ARROW);
		poiData.addPOIitem(127.061, 37.51, "Pizza 123-456", markerId, 0);
		poiData.endPOIdata();

		// create POI data overlay
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);

		// set event listener to the overlay
		poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

		// select an item
		poiDataOverlay.selectPOIitem(0, true);

		// show all POI data
		//poiDataOverlay.showAllPOIdata(0);
	}

	private void testFloatingPOIdataOverlay() {
		// Markers for POI item
		int marker1 = NMapPOIflagType.PIN;

		// set POI data
		NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
		poiData.beginPOIdata(1);
		NMapPOIitem item = poiData.addPOIitem(null, "Touch & Drag to Move", marker1, 0);
		if (item != null) {
			// initialize location to the center of the map view.
			item.setPoint(mMapController.getMapCenter());
			// set floating mode
			item.setFloatingMode(NMapPOIitem.FLOATING_TOUCH | NMapPOIitem.FLOATING_DRAG);
			// show right button on callout
			item.setRightButton(true);

			mFloatingPOIitem = item;
		}
		poiData.endPOIdata();

		// create POI data overlay
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		if (poiDataOverlay != null) {
			poiDataOverlay.setOnFloatingItemChangeListener(onPOIdataFloatingItemChangeListener);

			// set event listener to the overlay
			poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

			poiDataOverlay.selectPOIitem(0, false);

			mFloatingPOIdataOverlay = poiDataOverlay;
		}
	}

	/* NMapDataProvider Listener */
	private final OnDataProviderListener onDataProviderListener = new OnDataProviderListener() {

		@Override
		public void onReverseGeocoderResponse(NMapPlacemark placeMark, NMapError errInfo) {

			if (DEBUG) {
				Log.i(LOG_TAG, "onReverseGeocoderResponse: placeMark="
					+ ((placeMark != null) ? placeMark.toString() : null));
			}

			if (errInfo != null) {
				Log.e(LOG_TAG, "Failed to findPlacemarkAtLocation: error=" + errInfo.toString());

				Toast.makeText(NMapViewer.this, errInfo.toString(), Toast.LENGTH_LONG).show();
				return;
			}

			if (mFloatingPOIitem != null && mFloatingPOIdataOverlay != null) {
				mFloatingPOIdataOverlay.deselectFocusedPOIitem();

				if (placeMark != null) {
					mFloatingPOIitem.setTitle(placeMark.toString());
				}
				mFloatingPOIdataOverlay.selectPOIitemBy(mFloatingPOIitem.getId(), false);
			}
		}

	};

	/* MyLocation Listener */
	private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

		@Override
		public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {

			if (mMapController != null) {
				mMapController.animateTo(myLocation);
			}

			return true;
		}

		@Override
		public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

			// stop location updating
			//			Runnable runnable = new Runnable() {
			//				public void run() {										
			//					stopMyLocation();
			//				}
			//			};
			//			runnable.run();	

			Toast.makeText(NMapViewer.this, "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

			Toast.makeText(NMapViewer.this, "Your current location is unavailable area.", Toast.LENGTH_LONG).show();

			stopMyLocation();
		}

	};

	/* MapView State Change Listener*/
	private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

		@Override
		public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

			if (errorInfo == null) { // success
				// restore map view state such as map center position and zoom level.
				restoreInstanceState();

			} else { // fail
				Log.e(LOG_TAG, "onFailedToInitializeWithError: " + errorInfo.toString());

				Toast.makeText(NMapViewer.this, errorInfo.toString(), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onAnimationStateChange(NMapView mapView, int animType, int animState) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onAnimationStateChange: animType=" + animType + ", animState=" + animState);
			}
		}

		@Override
		public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onMapCenterChange: center=" + center.toString());
			}
		}

		@Override
		public void onZoomLevelChange(NMapView mapView, int level) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onZoomLevelChange: level=" + level);
			}
		}

		@Override
		public void onMapCenterChangeFine(NMapView mapView) {

		}
	};

	private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

		@Override
		public void onLongPress(NMapView mapView, MotionEvent ev) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLongPressCanceled(NMapView mapView) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTouchDown(NMapView mapView, MotionEvent ev) {

		}

		@Override
		public void onScroll(NMapView mapView, MotionEvent e1, MotionEvent e2) {
		}

		@Override
		public void onTouchUp(NMapView mapView, MotionEvent ev) {
			// TODO Auto-generated method stub

		}

	};

	private final NMapView.OnMapViewDelegate onMapViewTouchDelegate = new NMapView.OnMapViewDelegate() {

		@Override
		public boolean isLocationTracking() {
			if (mMapLocationManager != null) {
				if (mMapLocationManager.isMyLocationEnabled()) {
					return mMapLocationManager.isMyLocationFixed();
				}
			}
			return false;
		}

	};

	/* POI data State Change Listener*/
	private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {

		@Override
		public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
			if (DEBUG) {
				Log.i(LOG_TAG, "onCalloutClick: title=" + item.getTitle());
			}

			// [[TEMP]] handle a click event of the callout
			Toast.makeText(NMapViewer.this, "onCalloutClick: " + item.getTitle(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
			if (DEBUG) {
				if (item != null) {
					Log.i(LOG_TAG, "onFocusChanged: " + item.toString());
				} else {
					Log.i(LOG_TAG, "onFocusChanged: ");
				}
			}
		}
	};

	private final NMapPOIdataOverlay.OnFloatingItemChangeListener onPOIdataFloatingItemChangeListener = new NMapPOIdataOverlay.OnFloatingItemChangeListener() {

		@Override
		public void onPointChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
			NGeoPoint point = item.getPoint();

			if (DEBUG) {
				Log.i(LOG_TAG, "onPointChanged: point=" + point.toString());
			}

			findPlacemarkAtLocation(point.longitude, point.latitude);

			item.setTitle(null);

		}
	};

	private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {

		@Override
		public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem,
			Rect itemBounds) {

			// handle overlapped items
			if (itemOverlay instanceof NMapPOIdataOverlay) {
				NMapPOIdataOverlay poiDataOverlay = (NMapPOIdataOverlay)itemOverlay;

				// check if it is selected by touch event
				if (!poiDataOverlay.isFocusedBySelectItem()) {
					int countOfOverlappedItems = 1;

					NMapPOIdata poiData = poiDataOverlay.getPOIdata();
					for (int i = 0; i < poiData.count(); i++) {
						NMapPOIitem poiItem = poiData.getPOIitem(i);

						// skip selected item
						if (poiItem == overlayItem) {
							continue;
						}

						// check if overlapped or not
						if (Rect.intersects(poiItem.getBoundsInScreen(), overlayItem.getBoundsInScreen())) {
							countOfOverlappedItems++;
						}
					}

					if (countOfOverlappedItems > 1) {
						String text = countOfOverlappedItems + " overlapped items for " + overlayItem.getTitle();
						Toast.makeText(NMapViewer.this, text, Toast.LENGTH_LONG).show();
						return null;
					}
				}
			}

			// use custom old callout overlay
			if (overlayItem instanceof NMapPOIitem) {
				NMapPOIitem poiItem = (NMapPOIitem)overlayItem;

				if (poiItem.showRightButton()) {
					return new NMapCalloutCustomOldOverlay(itemOverlay, overlayItem, itemBounds,
						mMapViewerResourceProvider);
				}
			}

			// use custom callout overlay
			return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);

			// set basic callout overlay
			//return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);			
		}

	};

	private final NMapOverlayManager.OnCalloutOverlayViewListener onCalloutOverlayViewListener = new NMapOverlayManager.OnCalloutOverlayViewListener() {

		@Override
		public View onCreateCalloutOverlayView(NMapOverlay itemOverlay, NMapOverlayItem overlayItem, Rect itemBounds) {

			if (overlayItem != null) {
				// [TEST] 말풍선 오버레이를 뷰로 설정함
				String title = overlayItem.getTitle();
				if (title != null && title.length() > 5) {
					return new NMapCalloutCustomOverlayView(NMapViewer.this, itemOverlay, overlayItem, itemBounds);
				}
			}

			// null을 반환하면 말풍선 오버레이를 표시하지 않음
			return null;
		}

	};

	/* Local Functions */
	private static boolean mIsMapEnlared = false;

	private void restoreInstanceState() {
		mPreferences = getPreferences(MODE_PRIVATE);

		int longitudeE6 = mPreferences.getInt(KEY_CENTER_LONGITUDE, NMAP_LOCATION_DEFAULT.getLongitudeE6());
		int latitudeE6 = mPreferences.getInt(KEY_CENTER_LATITUDE, NMAP_LOCATION_DEFAULT.getLatitudeE6());
		int level = mPreferences.getInt(KEY_ZOOM_LEVEL, NMAP_ZOOMLEVEL_DEFAULT);
		int viewMode = mPreferences.getInt(KEY_VIEW_MODE, NMAP_VIEW_MODE_DEFAULT);
		boolean trafficMode = mPreferences.getBoolean(KEY_TRAFFIC_MODE, NMAP_TRAFFIC_MODE_DEFAULT);
		boolean bicycleMode = mPreferences.getBoolean(KEY_BICYCLE_MODE, NMAP_BICYCLE_MODE_DEFAULT);

		mMapController.setMapViewMode(viewMode);
		mMapController.setMapViewTrafficMode(trafficMode);
		mMapController.setMapViewBicycleMode(bicycleMode);
		mMapController.setMapCenter(new NGeoPoint(longitudeE6, latitudeE6), level);

		if (mIsMapEnlared) {
			mMapView.setScalingFactor(2.0F);
		} else {
			mMapView.setScalingFactor(1.0F);
		}
	}

	private void saveInstanceState() {
		if (mPreferences == null) {
			return;
		}

		NGeoPoint center = mMapController.getMapCenter();
		int level = mMapController.getZoomLevel();
		int viewMode = mMapController.getMapViewMode();
		boolean trafficMode = mMapController.getMapViewTrafficMode();
		boolean bicycleMode = mMapController.getMapViewBicycleMode();

		SharedPreferences.Editor edit = mPreferences.edit();

		edit.putInt(KEY_CENTER_LONGITUDE, center.getLongitudeE6());
		edit.putInt(KEY_CENTER_LATITUDE, center.getLatitudeE6());
		edit.putInt(KEY_ZOOM_LEVEL, level);
		edit.putInt(KEY_VIEW_MODE, viewMode);
		edit.putBoolean(KEY_TRAFFIC_MODE, trafficMode);
		edit.putBoolean(KEY_BICYCLE_MODE, bicycleMode);

		edit.commit();

	}

	/**
	 * Invoked during init to give the Activity a chance to set up its Menu.
	 * 
	 * @param menu the Menu to which entries may be added
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int viewMode = mMapController.getMapViewMode();
		boolean isTraffic = mMapController.getMapViewTrafficMode();
		boolean isBicycle = mMapController.getMapViewBicycleMode();
        boolean isAlphaLayer = mMapController.getMapAlphaLayerMode();

		menu.findItem(R.id.action_revert).setEnabled((viewMode != NMapView.VIEW_MODE_VECTOR) || isTraffic || mOverlayManager.sizeofOverlays() > 0);
		menu.findItem(R.id.action_vector).setChecked(viewMode == NMapView.VIEW_MODE_VECTOR);
		menu.findItem(R.id.action_satellite).setChecked(viewMode == NMapView.VIEW_MODE_HYBRID);
		menu.findItem(R.id.action_traffic).setChecked(isTraffic);
		menu.findItem(R.id.action_bicycle).setChecked(isBicycle);
        menu.findItem(R.id.action_alpha_layer).setChecked(isAlphaLayer);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_revert:
				if (mMyLocationOverlay != null) {
					stopMyLocation();
					mOverlayManager.removeOverlay(mMyLocationOverlay);
				}

				mMapController.setMapViewMode(NMapView.VIEW_MODE_VECTOR);
				mMapController.setMapViewTrafficMode(false);
				mMapController.setMapViewBicycleMode(false);

				mOverlayManager.clearOverlays();
				return true;
			case R.id.action_vector:
				invalidateMenu();
				mMapController.setMapViewMode(NMapView.VIEW_MODE_VECTOR);
				return true;

			case R.id.action_satellite:
				invalidateMenu();
				mMapController.setMapViewMode(NMapView.VIEW_MODE_HYBRID);
				return true;

			case R.id.action_traffic:
				invalidateMenu();
				mMapController.setMapViewTrafficMode(!mMapController.getMapViewTrafficMode());
				return true;

			case R.id.action_bicycle:
				invalidateMenu();
				mMapController.setMapViewBicycleMode(!mMapController.getMapViewBicycleMode());
				return true;

            case R.id.action_alpha_layer:
                invalidateMenu();
                mMapController.setMapAlphaLayerMode(!mMapController.getMapAlphaLayerMode(), 0xccFFFFFF);
                return true;

			case R.id.action_zoom:
				mMapView.displayZoomControls(true);
				return true;

			case R.id.action_my_location:
				startMyLocation();
				return true;

			case R.id.action_poi_data:
				mOverlayManager.clearOverlays();

				// add POI data overlay
				testPOIdataOverlay();
				return true;

			case R.id.action_path_data:
				mOverlayManager.clearOverlays();

				// add path data overlay
				testPathDataOverlay();

				// add path POI data overlay
				testPathPOIdataOverlay();
				return true;

			case R.id.action_floating_data:
				mOverlayManager.clearOverlays();
				testFloatingPOIdataOverlay();
				return true;

			case R.id.action_new_activity:
				Intent intent = new Intent(this, FragmentMapActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_visible_bounds:
				// test visible bounds
				Rect viewFrame = mMapView.getMapController().getViewFrameVisible();
				mMapController.setBoundsVisible(0, 0, viewFrame.width(), viewFrame.height() - 200);

				// add POI data overlay
				mOverlayManager.clearOverlays();

				testPathDataOverlay();
				return true;

			case R.id.action_scale_factor:
				if (mMapView.getMapProjection().isProjectionScaled()) {
					if (mMapView.getMapProjection().isMapHD()) {
						mMapView.setScalingFactor(2.0F, false);
					} else {
						mMapView.setScalingFactor(1.0F, false);
					}
				} else {
					mMapView.setScalingFactor(2.0F, true);
				}
				mIsMapEnlared = mMapView.getMapProjection().isProjectionScaled();
				return true;

			case R.id.action_auto_rotate:
				if (mMapView.isAutoRotateEnabled()) {
					mMapView.setAutoRotateEnabled(false, false);

					mMapContainerView.requestLayout();

					mHnadler.removeCallbacks(mTestAutoRotation);
				} else {

					mMapView.setAutoRotateEnabled(true, false);

					mMapView.setRotateAngle(30);
					mHnadler.postDelayed(mTestAutoRotation, AUTO_ROTATE_INTERVAL);

					mMapContainerView.requestLayout();
				}
				return true;
			case R.id.action_navermap:
				mMapView.executeNaverMap();
				return true;

		}
		return false;
	}

	private void invalidateMenu() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			invalidateOptionsMenu();
		}
	}

	private static final long AUTO_ROTATE_INTERVAL = 2000;
	private final Handler mHnadler = new Handler();
	private final Runnable mTestAutoRotation = new Runnable() {
		@Override
		public void run() {
//        	if (mMapView.isAutoRotateEnabled()) {
//    			float degree = (float)Math.random()*360;
//    			
//    			degree = mMapView.getRoateAngle() + 30;
//
//    			mMapView.setRotateAngle(degree);	
//            	
//            	mHnadler.postDelayed(mTestAutoRotation, AUTO_ROTATE_INTERVAL);        		
//        	}
		}
	};

	/** 
	 * Container view class to rotate map view.
	 */
	private class MapContainerView extends ViewGroup {

		public MapContainerView(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			final int width = getWidth();
			final int height = getHeight();
			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);
				final int childWidth = view.getMeasuredWidth();
				final int childHeight = view.getMeasuredHeight();
				final int childLeft = (width - childWidth) / 2;
				final int childTop = (height - childHeight) / 2;
				view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
			}

			if (changed) {
				mOverlayManager.onSizeChanged(width, height);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			int sizeSpecWidth = widthMeasureSpec;
			int sizeSpecHeight = heightMeasureSpec;

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				final View view = getChildAt(i);

				if (view instanceof NMapView) {
					if (mMapView.isAutoRotateEnabled()) {
						int diag = (((int)(Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
						sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
						sizeSpecHeight = sizeSpecWidth;
					}
				}

				view.measure(sizeSpecWidth, sizeSpecHeight);
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
