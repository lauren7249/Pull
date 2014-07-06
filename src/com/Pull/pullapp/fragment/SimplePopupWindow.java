package com.Pull.pullapp.fragment;

import com.Pull.pullapp.R;
import com.Pull.pullapp.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class SimplePopupWindow extends BetterPopupWindow implements OnClickListener {
	private String message;
	private TextView b;
	
	public SimplePopupWindow(View anchor) {
		super(anchor);
	}

	@Override
	protected void onCreate() {
		// inflate layout
		LayoutInflater inflater =
				(LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.popup_layout, null);
		b = (TextView) root.getChildAt(0);
		
		// set the inflated view as what we want to display
		this.setContentView(root);
		
		
	}

	@Override
	public void onClick(View v) {
		this.dismiss();
	}
	public void setMessage(String message) {
		this.message = message;
		b.setText(message);
	}
}
