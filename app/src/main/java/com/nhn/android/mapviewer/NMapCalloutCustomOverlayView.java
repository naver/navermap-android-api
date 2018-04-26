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
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.overlay.NMapPOIitem;

public class NMapCalloutCustomOverlayView extends NMapCalloutOverlayView {

	private View mCalloutView;
	private TextView mCalloutText;
	private View mRightArrow;

	public NMapCalloutCustomOverlayView(Context context, NMapOverlay itemOverlay, NMapOverlayItem item, Rect itemBounds) {
		super(context, itemOverlay, item, itemBounds);

		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.callout_overlay_view, this, true);

		mCalloutView = findViewById(R.id.callout_overlay);
		mCalloutText = (TextView)mCalloutView.findViewById(R.id.callout_text);
		mRightArrow = findViewById(R.id.callout_rightArrow);

		mCalloutView.setOnClickListener(callOutClickListener);

		mCalloutText.setText(item.getTitle());

		if (item instanceof NMapPOIitem) {
			if (((NMapPOIitem)item).hasRightAccessory() == false) {
				mRightArrow.setVisibility(View.GONE);
			}
		}
	}

	private final OnClickListener callOutClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (mOnClickListener != null) {
				mOnClickListener.onClick(null, mItemOverlay, mOverlayItem);
			}
		}
	};

}
