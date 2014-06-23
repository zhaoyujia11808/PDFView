package com.artifex.mupdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class PDFView extends RelativeLayout {

	private ProgressBar mBusyIndicator;
	private static final int BACKGROUND_COLOR = 0xFFFFFFFF;
	private Context mContext;
	protected int mPageNumber = 0;

	protected float mSourceScale;
	private ImageView mEntire; // Image rendered at minimum zoom
	private Bitmap mEntireBm, mnewBm;
	private AsyncTask<Void, Void, Void> mDrawEntire;
	private LinkInfo mLinks[];
	int seconds = 0;
	int width, height;

	public PDFView(Context c) {
		super(c);
		mContext = c;

		setBackgroundColor(BACKGROUND_COLOR);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mEntire = new OpaqueImageView(mContext);
		mEntire.setScaleType(ImageView.ScaleType.FIT_CENTER);
		addView(mEntire, params);

		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params2.addRule(RelativeLayout.CENTER_IN_PARENT);

		mBusyIndicator = new ProgressBar(mContext);
		mBusyIndicator.setIndeterminate(true);
		// addView(mBusyIndicator);
		addView(mBusyIndicator, params2);

	}

	public PDFView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		mContext = context;

		setBackgroundColor(BACKGROUND_COLOR);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mEntire = new OpaqueImageView(mContext);
		mEntire.setScaleType(ImageView.ScaleType.FIT_CENTER);
		addView(mEntire, params);

		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params2.addRule(RelativeLayout.CENTER_IN_PARENT);

		mBusyIndicator = new ProgressBar(mContext);
		mBusyIndicator.setIndeterminate(true);
		// addView(mBusyIndicator);
		addView(mBusyIndicator, params2);

	}

	protected void drawPage(Bitmap bm, int sizeX, int sizeY, int patchX,
			int patchY, int patchWidth, int patchHeight) {
		if (MuPDFCore.core == null)
			return;
		else
			MuPDFCore.core.drawPage(mPageNumber, bm, sizeX, sizeY, patchX,
					patchY, patchWidth, patchHeight);
	}

	protected LinkInfo[] getLinkInfo() {
		if (MuPDFCore.core == null)
			return null;
		else
			return MuPDFCore.core.getPageLinks(mPageNumber);
	}

	public int getCount() {
		if (MuPDFCore.core == null)
			return 0;
		else
			return MuPDFCore.core.countPages();
	}

	public void NextPage() {
		if (getCount() > 0) {
			mPageNumber++;
			mPageNumber = mPageNumber % getCount();
			setPage(mPageNumber);
		}
	}

	public void realse() {
		// Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}

		mPageNumber = 0;

		if (mEntire != null)
			mEntire.setImageBitmap(null);
		mEntireBm = null;
	}

	public void setPage(int page) {
		// Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}
		mPageNumber = page;

		// Calculate scaled size that fits within the screen limits
		// This is the size at minimum zoom

		if (mBusyIndicator != null && mEntireBm == null)
			mBusyIndicator.setVisibility(VISIBLE);

		mnewBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		// Render the page in the background
		mDrawEntire = new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... v) {
				drawPage(mnewBm, width, height, 0, 0, width, height);
				return null;
			}

			protected void onPostExecute(Void aa) {
				if (MuPDFCore.core == null)
					return;
				mBusyIndicator.setVisibility(View.GONE);
				mEntire.setImageBitmap(mnewBm);
				if (mEntireBm != null && !mEntireBm.isRecycled())
					mEntireBm.recycle();
				mEntireBm = mnewBm;
				invalidate();
				if (listen != null) {
					listen.onComplete();
				}

			}
		};
		mDrawEntire.execute();

	}

	interface OnCompletelisten {
		abstract void onComplete();
	}

	OnCompletelisten listen;

	public OnCompletelisten getListen() {
		return listen;
	}

	public void setListen(OnCompletelisten listen) {
		this.listen = listen;
	}

	public int getPage() {
		return mPageNumber;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public boolean OpenFile(String path, int x, int y) {
		if (x <= 0 || y <= 0)
			return false;
		width = x;
		height = y;

		System.out.println("Trying to open " + path);
		try {
			MuPDFCore.core = new MuPDFCore(path);
			// New file: drop the old outline data

		} catch (Exception e) {
			System.out.println(e);
		}

		if (MuPDFCore.core == null)
			return false;
		if (MuPDFCore.core != null && MuPDFCore.core.needsPassword()) {
			return false;
		}

		setListen(new OnCompletelisten() {

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				System.out.println("oncompletion");
				removeCallbacks(post);
				if (seconds > 0)
					postDelayed(post, seconds * 1000);
			}
		});

		setPage(0);
		return true;
	}

	Runnable post = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			NextPage();

		}
	};

	public void onDestroy() {

		removeCallbacks(post);
		realse();
		if (MuPDFCore.core != null)
			MuPDFCore.core.onDestroy();
		MuPDFCore.core = null;
	}

	// @Override
	// protected void onLayout(boolean changed, int left, int top, int right,
	// int bottom) {
	// super.onLayout(changed, left, top, right, bottom);
	//
	// }

}

// Make our ImageViews opaque to optimize redraw
class OpaqueImageView extends ImageView {

	public OpaqueImageView(Context context) {
		super(context);
	}

}
