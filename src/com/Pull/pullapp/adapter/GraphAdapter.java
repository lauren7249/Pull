package com.Pull.pullapp.adapter;

import java.util.HashMap;
import java.util.List;
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
	private static final int NUM_SERIES_PER_PLOT = 5;
	private static final int NUM_POINTS_PER_SERIES = 10;
	private HashMap<String, TreeMap<Long, Float>> series;
	private Context context;
	private String original_name;
	private Activity activity;
        public GraphAdapter(Context context, int resId, List<View> views, HashMap<String, 
        		TreeMap<Long, Float>> data, String name, Activity activity) {
            super(context, resId, views);
            this.series = data;
            this.context = context;
            this.original_name = name;
            this.activity = activity;
        }

        @Override
        public int getCount() {
            return series.size()/3;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = convertView;
            if (v == null) {
                v = inf.inflate(R.layout.graph_item, parent, false);
          
	            LinearLayout mGraphView = (LinearLayout) v.findViewById(R.id.xyplot);
	            TextView header = (TextView) v.findViewById(R.id.header);
	        	String[] graphs;
				String graph_title;
				Log.i("position","pos"+pos);
				switch(pos){
				case(0):
					graphs = new String[]{Constants.GRAPH_CONTACT_INIT_FREQ_THEM,
						Constants.GRAPH_CONTACT_INIT_FREQ_ME,
						Constants.GRAPH_CONTACT_INIT_FREQ_RATIO};
					graph_title = "How often you each text first";
					header.setText(graph_title);
					ContentUtils.addGraph(activity, mGraphView,  original_name, series, graphs, "");
					break;
				case(1):
					
					graphs = new String[]{Constants.GRAPH_RESPONSE_TIME_THEM,
						Constants.GRAPH_RESPONSE_TIME_ME,
						Constants.GRAPH_RESPONSE_TIME_RATIO};
				
					graph_title = "How long you each take to respond";
					header.setText(graph_title);
					ContentUtils.addGraph(activity, mGraphView,  original_name, series, graphs, "");
					break;
				}
					
            }
            return v;
        }
    
}
