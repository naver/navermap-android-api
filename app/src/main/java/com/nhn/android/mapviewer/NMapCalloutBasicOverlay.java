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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;

import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;

/**
 * Basic callout overlay
 * 
 * @author kyjkim 
 */
public class NMapCalloutBasicOverlay extends NMapCalloutOverlay {

	private static final int CALLOUT_TEXT_PADDING_X = 10;
	private static final int CALLOUT_TEXT_PADDING_Y = 10;
	private static final int CALLOUT_TAG_WIDTH = 10;
	private static final int CALLOUT_TAG_HEIGHT = 10;
	private static final int CALLOUT_ROUND_RADIUS_X = 5;
	private static final int CALLOUT_ROUND_RADIUS_Y = 5;

	private final Paint mInnerPaint, mBorderPaint, mTextPaint;
	private final Path mPath;
	private float mOffsetX, mOffsetY;

	public NMapCalloutBasicOverlay(NMapOverlay itemOverlay, NMapOverlayItem item, Rect itemBounds) {
		super(itemOverlay, item, itemBounds);

		mInnerPaint = new Paint();
		mInnerPaint.setARGB(225, 75, 75, 75); //gray
		mInnerPaint.setAntiAlias(true);

		mBorderPaint = new Paint();
		mBorderPaint.setARGB(255, 255, 255, 255);
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setStyle(Style.STROKE);
		mBorderPaint.setStrokeWidth(2);

		mTextPaint = new Paint();
		mTextPaint.setARGB(255, 255, 255, 255);
		mTextPaint.setAntiAlias(true);

		mPath = new Path();
		mPath.setFillType(Path.FillType.WINDING);
	}

	@Override
	protected boolean isTitleTruncated() {
		return false;
	}

	@Override
	protected int getMarginX() {
		return 10;
	}

	@Override
	protected Rect getBounds(NMapView mapView) {

		adjustTextBounds(mapView);

		mTempRect.set((int)mTempRectF.left, (int)mTempRectF.top, (int)mTempRectF.right, (int)mTempRectF.bottom);
		mTempRect.union(mTempPoint.x, mTempPoint.y);

		return mTempRect;
	}

	@Override
	protected PointF getSclaingPivot() {
		PointF pivot = new PointF();

		pivot.x = mTempRectF.centerX();
		pivot.y = mTempRectF.bottom + CALLOUT_TAG_HEIGHT;

		return pivot;
	}

	@Override
	protected void drawCallout(Canvas canvas, NMapView mapView, boolean shadow, long when) {

		adjustTextBounds(mapView);

		stepAnimations(canvas, mapView, when);

		// Draw inner info window
		canvas.drawRoundRect(mTempRectF, CALLOUT_ROUND_RADIUS_X, CALLOUT_ROUND_RADIUS_Y, mInnerPaint);

		// Draw border for info window
		canvas.drawRoundRect(mTempRectF, CALLOUT_ROUND_RADIUS_X, CALLOUT_ROUND_RADIUS_Y, mBorderPaint);

		// Draw bottom tag
		if (CALLOUT_TAG_WIDTH > 0 && CALLOUT_TAG_HEIGHT > 0) {
			float x = mTempRectF.centerX();
			float y = mTempRectF.bottom;

			Path path = mPath;
			path.reset();

			path.moveTo(x - CALLOUT_TAG_WIDTH, y);
			path.lineTo(x, y + CALLOUT_TAG_HEIGHT);
			path.lineTo(x + CALLOUT_TAG_WIDTH, y);
			path.close();

			canvas.drawPath(path, mInnerPaint);
			canvas.drawPath(path, mBorderPaint);
		}

		//  Draw title
		canvas.drawText(mOverlayItem.getTitle(), mOffsetX, mOffsetY, mTextPaint);
	}

	/* Internal Functions */

	private void adjustTextBounds(NMapView mapView) {

		//  Determine the screen coordinates of the selected MapLocation
		mapView.getMapProjection().toPixels(mOverlayItem.getPointInUtmk(), mTempPoint);

		final String title = mOverlayItem.getTitle();
		mTextPaint.getTextBounds(title, 0, title.length(), mTempRect);

		//  Setup the callout with the appropriate size & location
		mTempRectF.set(mTempRect);
		mTempRectF.inset(-CALLOUT_TEXT_PADDING_X, -CALLOUT_TEXT_PADDING_Y);
		mOffsetX = mTempPoint.x - mTempRect.width() / 2;
		mOffsetY = mTempPoint.y - mTempRect.height() - mItemBounds.height() - CALLOUT_TEXT_PADDING_Y;
		mTempRectF.offset(mOffsetX, mOffsetY);
	}

	@Override
	protected Drawable getDrawable(int rank, int itemState) {
		
		return null;
	}
}
