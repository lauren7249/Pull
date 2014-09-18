package com.Pull.pullapp.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Pull.pullapp.R;
import com.Pull.pullapp.util.Constants;
import com.Pull.pullapp.util.ContentUtils;

public class GraphAdapter extends ArrayAdapter<View> {

	private Context context;
	private TreeMap<String, ArrayList<View>> graph_sections;

        public GraphAdapter(Context context, int resId, List<View> views,
				TreeMap<String, ArrayList<View>> graph_sections) {
        	 super(context, resId, views);
        	 this.context = context;
        	 this.graph_sections = graph_sections;
		}

		@Override
        public int getCount() {
            return graph_sections.size();
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Map.Entry<String, ArrayList<View>> entry = (Entry<String, ArrayList<View>>) graph_sections.entrySet().toArray()[pos];
            
            

            View v = convertView;
            if(v == null) {
                v = inf.inflate(R.layout.graph_item, parent, false);
                LinearLayout mGraphView = (LinearLayout) v.findViewById(R.id.xyplot);
                ArrayList<View> views = entry.getValue();
                for(View k : views) {
                	mGraphView.addView(k);
                }                
            }

            
            TextView header = (TextView) v.findViewById(R.id.header);
            header.setText(entry.getKey());
            

            return v;
        }
    
}
