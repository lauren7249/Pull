package com.Pull.pullapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;

public class RecipientsPopupWindow extends BetterPopupWindow implements OnClickListener {
	private TextView b;
	private RecipientsEditor mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
	private ViewGroup root;
	private Context mContext;	
	public RecipientsPopupWindow(View anchor) {
		super(anchor);
	}

	@Override
	protected void onCreate() {
		// inflate layout
		LayoutInflater inflater =
				(LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		root = (ViewGroup) inflater.inflate(R.layout.share_popup_layout, null);
		b = (TextView) root.getChildAt(0);
		

		
		// set the inflated view as what we want to display
		this.setContentView(root);
		
		
		
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
