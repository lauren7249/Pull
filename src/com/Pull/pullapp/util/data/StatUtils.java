package com.Pull.pullapp.util.data;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.Telephony.TextBasedSmsColumns;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.Pull.pullapp.model.InitiatingData;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.util.Constants;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

public class StatUtils {
	public static boolean isInitiating(String thread_id, String message_id, Context context, 
			Long date, UserInfoStore store, SMSMessage message) {
		if(message_id==null || message_id.isEmpty()) return false;
		if(thread_id==null || thread_id.isEmpty()) return false;
		DatabaseHandler dh = new DatabaseHandler(context);
		InitiatingData initiatingData = dh
				.getInitiatingRecord(thread_id,message_id, context, date, store, message);
		dh.close();
		return initiatingData.isInitiating();

	}		
	public static boolean isInitiating(long date, int type, String body,
			long previous_date, int previous_type, String previous_body) {
		boolean initiating = false, retexting=false;
 		if(previous_type==type) retexting = true;
 		long milliseconds_elapsed = (long)(date-previous_date);
 		long seconds_elapsed = (long) (milliseconds_elapsed*0.001);
 		long minutes_elapsed = (long) (seconds_elapsed*0.016666666667);
 		float hours_elapsed = (float) (minutes_elapsed*0.016666666667);
 		if(retexting && hours_elapsed > 0.167) initiating=true;
 		else if(!retexting) {
 			if (hours_elapsed > 24) initiating=true;
 			else if(hours_elapsed > 8 && !previous_body.contains("?")) initiating = true;
 		}
 		return initiating;
	}


	public static HashMap<String, TreeMap<Long, Float>> getDataSeries(
			Cursor messages_cursor, Context context) {
		boolean initiating = false;
		int previous_type = 0;
		long previous_date = 0;
		long my_previous_initiation_date =0, their_previous_initiation_date=0;
		String previous_body = null;
		int me=1, them=1, responses_mine=0, responses_theirs=0, my_characters=0, their_characters=0;
		float me_freq =0 , them_freq = 0, start_date = 0,
				my_total_minutes = 0, their_total_minutes = 0;
		int initiation_count=0;			
		long date;
		float balance, minutes_elapsed, my_average_response_time = 0, their_average_response_time = 0;
		int num = messages_cursor.getCount();
		HashMap<String,TreeMap<Long,Float>> data = new HashMap<String,TreeMap<Long,Float>>();
		
		TreeMap<Long,Float> initiation_me = new TreeMap<Long,Float>();
		TreeMap<Long,Float> initiation_them = new TreeMap<Long,Float>();
		TreeMap<Long,Float> initiation_ratio = new TreeMap<Long,Float>();	
		
		TreeMap<Long,Float> response_times_me = new TreeMap<Long,Float>();
		TreeMap<Long,Float> response_times_them = new TreeMap<Long,Float>();
		TreeMap<Long,Float> response_time_ratio = new TreeMap<Long,Float>();

		TreeMap<Long,Float> volume_me = new TreeMap<Long,Float>();
		TreeMap<Long,Float> volume_them = new TreeMap<Long,Float>();
		TreeMap<Long,Float> volume_ratio = new TreeMap<Long,Float>();
		for (int i=0; i<num; i++) {
			messages_cursor.moveToPosition(i);
			date = messages_cursor.getLong(6);
			if(i==0) {
				start_date = date;
			}
			String body = messages_cursor.getString(2).toString();
			String message_id = messages_cursor.getString(3).toString();
			int type = Integer.parseInt(messages_cursor.getString(1).toString());
			if(previous_body!=null && previous_date>0) {

		  		initiating = isInitiating(date, type, body, previous_date, previous_type, previous_body);
				/*	message = new SMSMessage(date, body, address, store.getName(address), 
		    			type, store, ParseUser.getCurrentUser().getUsername());
				 * initiating = ContentUtils.isInitiating(thread_id, message_id, context, 
						date, store, message);*/
			} 
			long previous_initiation_date = 0;
			int previous_me = 0;
			int previous_them = 0;
			if(initiating) {
				if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
					me++;
				}
				else {
					them++;
				}
			initiation_count++;
			
			} 
			else if(previous_type != type && previous_date>0) { // not a retext and not an initiation, therefore a response
	     		long milliseconds_elapsed = (long)(date-previous_date);
	     		long seconds_elapsed = (long) (milliseconds_elapsed*0.001);
	     		minutes_elapsed = (long) (seconds_elapsed*0.016666666667);
			  if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
				  my_total_minutes = (float) my_total_minutes + minutes_elapsed;
				  responses_mine++;
				  my_average_response_time = my_total_minutes/(float)responses_mine;
				  if(my_average_response_time>0) response_times_me.put(date, my_average_response_time);					  
			  }
			  else {
				their_total_minutes = (float) their_total_minutes + minutes_elapsed;
				responses_theirs++;
				their_average_response_time = their_total_minutes/(float)responses_theirs;
				if(their_average_response_time>0) response_times_them.put(date, their_average_response_time);					
			  }
			  if(their_average_response_time>0 && my_average_response_time>0)
			  response_time_ratio.put(date, (float) their_average_response_time/(float) my_average_response_time);
		  }
		  if(initiation_count>2) {

			  if(them>me) balance=((float)them/(float)me)-1;
			  else balance=-(((float)me/(float)them)-1);
			  
			  float their_period;
			  float my_period;
			  if(their_previous_initiation_date>0) {
				  their_period = date-their_previous_initiation_date;
			  } else their_period = date-start_date;

			  if(my_previous_initiation_date>0) {
				  my_period = date-my_previous_initiation_date;
			  } else my_period = date-start_date;
			
			  me_freq = (float)(me-1)/(date-start_date);
			  them_freq = (float)(them-1)/(date-start_date);

			  initiation_me.put(date, me_freq);
			  initiation_them.put(date, them_freq);
			  initiation_ratio.put(date, balance);

		  }		
		  if(initiating) {
			  if(type==TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
				  my_previous_initiation_date = date;
				  previous_me = me;
			  }
			  else {
				  their_previous_initiation_date = date;
				  previous_them = them;
			  }
			  initiation_count++;
		  }
		  			  
		  previous_date = date;
		  previous_body = body;
		  previous_type = type;
		}
		
		date = System.currentTimeMillis();
		if(them>me) balance=((float)them/(float)me)-1;
		else balance=-(((float)me/(float)them)-1);			
		me_freq = (float)(me-1)/(date-start_date);
		them_freq = (float)(them-1)/(date-start_date);			
		initiation_me.put(date, me_freq);
		initiation_them.put(date, them_freq);
		initiation_ratio.put(date, balance);			
		
		data.put(Constants.GRAPH_CONTACT_INIT_FREQ_THEM, initiation_them);
		data.put(Constants.GRAPH_CONTACT_INIT_FREQ_ME,initiation_me);
		data.put(Constants.GRAPH_CONTACT_INIT_FREQ_RATIO,initiation_ratio);
		 
		data.put(Constants.GRAPH_RESPONSE_TIME_THEM,response_times_them);
		data.put(Constants.GRAPH_RESPONSE_TIME_ME, response_times_me);
		data.put(Constants.GRAPH_RESPONSE_TIME_RATIO,response_time_ratio);	
		
		return data;
	}
	
	@Deprecated
	public static void addGraphs(Activity activity,
			LinearLayout mGraphView, String original_name,
			Cursor messages_cursor, Context mContext) {
		HashMap<String,TreeMap<Long,Float>> data = getDataSeries(messages_cursor, mContext);
		GraphView[] graphViews = new GraphView[data.size()];
		String title = "";
		for(int i=0; i<data.size(); i++) {
			if(i%3==0) title = original_name;
			else if(i%3==1) title = "You";
			else if(i%3==2) title = "";
			GraphView graphView = new LineGraphView(
				    activity
				    , title
				);
				// add data
			try {
				graphView.addSeries(new GraphViewSeries(treemapToGraphSeries(data.get(i))));
			} catch(RuntimeException e) {
				continue;
			}
			graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
				  @Override
				  public String formatLabel(double value, boolean isValueX) {
				    if (isValueX) {
				    	
				    	Date d = new Date((long) value);
				    	DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH);
				    	String dateOut = dateFormatter.format(d);
				    	//return dateOut;
				    	return "";
				    }
				    return "";
				  }
				});		
			graphView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,1f));
			graphViews[i] = graphView;
			mGraphView.addView(graphViews[i]);	 
		}
	}	
	
	private static GraphViewData[] treemapToGraphSeries(
			TreeMap<Long, Float> hashMap) {
		GraphViewData[] k = new GraphViewData[hashMap.size()];
		int i = 0;
		for(Entry<Long, Float> entry : hashMap.entrySet()) {
			k[i] = new GraphViewData(entry.getKey(),entry.getValue());
			i++;
		}
		return k;
		
	}

	public static ArrayList<View> getGraphs(Activity activity,String original_name, HashMap<String, TreeMap<Long,Float>> data,
			String[] series_names, String title) {
		
			ArrayList<View> views = new ArrayList<View>();
			GraphView graphView = new LineGraphView(
				    activity
				    , title
				);

			try {
				String ratio_name = series_names[2];
				TreeMap <Long,Float> ratios = data.get(ratio_name);
				Long lastDate = ratios.lastKey();
				Float lastRatio = ratios.get(lastDate);
				String response_Time_balance_overall;
				if(ratio_name.equals(Constants.GRAPH_RESPONSE_TIME_RATIO)) {
					//if(lastRatio<.9) response_Time_balance_overall = original_name + " generally responds to texts faster than you.";
				} else if(ratio_name.equals(Constants.GRAPH_CONTACT_INIT_FREQ_RATIO)) {
					
				}
				
				//Log.i("ratio",series_names[1] + " " + lastRatio);					
				graphView.addSeries(new GraphViewSeries(original_name,
						new GraphViewSeriesStyle(Color.rgb(251, 76, 60), 3), 
						treemapToGraphSeries(data.get(series_names[0]))));
				
				graphView.addSeries(new GraphViewSeries("You",
						new GraphViewSeriesStyle(Color.rgb(52, 185, 204), 3),
						treemapToGraphSeries(data.get(series_names[1]))));
			} catch(RuntimeException e) {
				TextView tv = new TextView(activity);
				tv.setText("Not enough messages for this graph -- check back later!");
				e.printStackTrace();
				views.add(tv);
				return views;
			}
			graphView.setShowLegend(true);
			graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
				  @Override
				  public String formatLabel(double value, boolean isValueX) {
				    if (isValueX) {
				    	
				    	Date d = new Date((long) value);
				    	DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH);
				    	String dateOut = dateFormatter.format(d); 
				    	return dateOut;
				    	//return "";
				    }
				    return "";
				  }
				});		

			graphView.getGraphViewStyle().setNumHorizontalLabels(1);
			graphView.getGraphViewStyle().setNumVerticalLabels(1);	
			graphView.getGraphViewStyle().setVerticalLabelsWidth(0);
			graphView.getGraphViewStyle().setTextSize(20);
			graphView.getGraphViewStyle().setLegendWidth(280);
			graphView.setPadding(0, 0, 20, 10);
			graphView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,1f));
			views.add(graphView);
			
			
			return views;
	}


}
