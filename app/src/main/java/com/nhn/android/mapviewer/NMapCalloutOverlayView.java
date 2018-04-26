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
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

public abstract class NMapCalloutOverlayView extends FrameLayout implements
	NMapOverlayManager.CalloutOverlayViewInterface {

	protected NMapOverlayItem mOverlayItem;
	protected Rect mItemBounds;
	protected NMapCalloutOverlay.OnClickListener mOnClickListener;
	protected NMapOverlay mItemOverlay;

	private static final long SCALE_DURATION_MILLS = 200L;
	private static final float CALLOUT_MARGIN_X = 13.33F;
	private final float mMarginX;

	protected final Point mTempPoint = new Point();
	protected final Rect mTempRect = new Rect();

	public NMapCalloutOverlayView(Context context, NMapOverlay itemOverlay, NMapOverlayItem item, Rect itemBounds) {
		super(context);

		mOverlayItem = item;
		mItemBounds = itemBounds;
		mOnClickListener = null;
		mItemOverlay = itemOverlay;

		int px = 0;
		int py = (int)(mItemBounds.height() * mOverlayItem.getAnchorYRatio());
		NMapView.LayoutParams lp = new NMapView.LayoutParams(NMapView.LayoutParams.WRAP_CONTENT,
			NMapView.LayoutParams.WRAP_CONTENT, mOverlayItem.getPoint(), px, -py,
			NMapView.LayoutParams.BOTTOM_CENTER);
		this.setLayoutParams(lp);

		mMarginX = NMapResourceProvider.toPixelFromDIP(CALLOUT_MARGIN_X);

	}

	/** 
	 * @return bounds of this callout overlay in the map view.
	 */
	private Rect getBounds(NMapView mapView) {

		//  First determine the screen coordinates of the selected MapLocation
		mapView.getMapProjection().toPixels(mOverlayItem.getPointInUtmk(), mTempPoint);

		mTempRect.left = this.getLeft();
		mTempRect.top = this.getTop();
		mTempRect.right = this.getRight();
		mTempRect.bottom = this.getBottom();

		mTempRect.union(mTempPoint.x, mTempPoint.y);

		return mTempRect;
	}

	/** 
	 * check if callout overlay is in the visible bounds of map view.
	 */
	@Override
	public boolean isCalloutViewInVisibleBounds(NMapView mapView) {

		if (this.getVisibility() == View.VISIBLE) {
			Rect boundsVisible = mapView.getMapController().getBoundsVisible();
			Rect bounds = getBounds(mapView);

			return Rect.intersects(boundsVisible, bounds);
		}

		return false;
	}

	private int getMarginX() {
		return (int)(mMarginX);
	}

	/** 
	 * Attempts to adjust location of this callout overlay within in the map view.
	 */
	@Override
	public void adjustBounds(NMapView mapView, boolean animate, boolean adjustToCenter) {

		if (adjustToCenter) {
			NGeoPoint pt = mOverlayItem.getPoint();
			if (animate) {
				mapView.getMapController().animateTo(pt, true);
			} else {
				mapView.getMapController().setMapCenter(pt);
			}

		} else {
			Rect boundsVisible = mapView.getMapController().getBoundsVisible();
			Rect bounds = getBounds(mapView);

			int marginX = getMarginX();

			if (!boundsVisible.contains(bounds)) {

				int centerX = 0;
				if (bounds.width() >= boundsVisible.width()) {
					centerX = bounds.centerX();
				} else {
					if (bounds.left < boundsVisible.left) {
						centerX = boundsVisible.left - bounds.left + marginX;
					} else if (bounds.right > boundsVisible.right) {
						centerX = boundsVisible.right - bounds.right - marginX;
					}
					centerX = boundsVisible.centerX() - centerX;
				}

				int centerY = 0;
				if (bounds.top < boundsVisible.top) {
					centerY = boundsVisible.top - bounds.top + marginX;
				} else if (bounds.bottom > boundsVisible.bottom) {
					centerY = boundsVisible.bottom - bounds.bottom - marginX;
				}
				centerY = boundsVisible.centerY() - centerY;

				NGeoPoint pt = mapView.getMapProjection().fromPixels(centerX, centerY);
				if (animate) {
					mapView.getMapController().animateTo(pt, true);
				} else {
					mapView.getMapController().setMapCenter(pt);
				}
			}
		}

		if (animate) {
			animateCallout();
		}
	}

	private void animateCallout() {

		// Create a scale animation
		ScaleAnimation animation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
			ScaleAnimation.RELATIVE_TO_SELF, 1.0f);

		animation.setDuration(SCALE_DURATION_MILLS);

		this.startAnimation(animation);
	}

	/**
	 * Register a callback to be invoked when this callout is clicked
	 * 
	 * @param l the click listener to attach to this callout overlay
	 */
	@Override
	public void setOnClickListener(NMapCalloutOverlay.OnClickListener listener) {
		mOnClickListener = listener;
	}

}
