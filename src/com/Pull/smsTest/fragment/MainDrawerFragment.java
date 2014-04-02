package com.Pull.smsTest.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.Pull.smsTest.R;


public class MainDrawerFragment extends SherlockFragment implements
		OnItemClickListener {

	private View mRootView;


	ListView lv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_drawer_main, container,
				false);
		
		lv = (ListView) mRootView.findViewById(android.R.id.list);
		
		TextView header = new TextView(getActivity());
		header.setText(R.string.app_name);
		header.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
		header.setTextColor(getResources().getColor(android.R.color.white));
		header.setTextSize(TypedValue.COMPLEX_UNIT_SP , 18);
		
		lv.addHeaderView(header);
		
		
		lv.setOnItemClickListener(this);
		return mRootView;
	}
	
	public int dpToPx(int dp){
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		return (int)((dp * displayMetrics.density) + 0.5);
	}


	@Override
	public void onItemClick(AdapterView<?> adaper, View v, int location,
			long arg3) {
		
		location-=1;
			
	}
}
