package com.artifex.mupdf;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class MuPDFActivity extends Activity {
	PDFView aa;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RelativeLayout main = new RelativeLayout(this);
		setContentView(main);
		aa = new PDFView(this);

		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
				100, 200);

		params3.addRule(RelativeLayout.CENTER_IN_PARENT);
		aa.setLayoutParams(params3);
		main.addView(aa);

		aa.OpenFile("/mnt/sdcard/aa.pdf",100,200);
		aa.setSeconds(3);
	}

	public void onDestroy() {

		super.onDestroy();
	}

}
