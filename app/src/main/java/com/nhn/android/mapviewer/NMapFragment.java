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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapView;

/** 
 * NMapFragment 클래스는 NMapActivity를 상속하지 않고 NMapView만 사용하고자 하는 경우에 NMapContext를 이용한 예제임.
 * NMapView 사용시 필요한 초기화 및 리스너 등록은 NMapActivity 사용시와 동일함.
 */
public class NMapFragment extends Fragment {

	private NMapContext mMapContext;
	
	/**
	 * Fragment에 포함된 NMapView 객체를 반환함
	 */
	private NMapView findMapView(View v) {

	    if (!(v instanceof ViewGroup)) {
	        return null;
	    }

	    ViewGroup vg = (ViewGroup)v;
	    if (vg instanceof NMapView) {
	        return (NMapView)vg;
	    }
	    
	    for (int i = 0; i < vg.getChildCount(); i++) {

	        View child = vg.getChildAt(i);
		    if (!(child instanceof ViewGroup)) {
		        continue;
		    }
		    
		    NMapView mapView = findMapView(child);
		    if (mapView != null) {
		    	return mapView;
		    }
	    }
	    return null;
	}

	/* Fragment 라이프사이클에 따라서 NMapContext의 해당 API를 호출함 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    mMapContext =  new NMapContext(super.getActivity()); 
	    
	    mMapContext.onCreate();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		throw new IllegalArgumentException("onCreateView should be implemented in the subclass of NMapFragment.");
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    
	    // Fragment에 포함된 NMapView 객체 찾기
	    NMapView mapView = findMapView(super.getView());
	    if (mapView == null) {
	    	throw new IllegalArgumentException("NMapFragment dose not have an instance of NMapView.");
	    }
	    
	    // NMapActivity를 상속하지 않는 경우에는 NMapView 객체 생성후 반드시 setupMapView()를 호출해야함.
	    mMapContext.setupMapView(mapView);
	}
	
	@Override
	public void onStart(){
	    super.onStart();
	    
	    mMapContext.onStart();
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    mMapContext.onResume();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    
	    mMapContext.onPause();
	}
	
	@Override
	public void onStop() {
		
		mMapContext.onStop();
		
	    super.onStop();
	}
	
	@Override
	public void onDestroyView() {
	    super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		mMapContext.onDestroy();
		
	    super.onDestroy();
	}
}
