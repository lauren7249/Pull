package com.Pull.pullapp.adapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.Pull.pullapp.mms.ItemLoadedCallback;
import com.Pull.pullapp.mms.ThumbnailManager;
import com.Pull.pullapp.mms.ThumbnailManager.ImageLoaded;
import com.Pull.pullapp.R;

public class PicturesAdapter extends ArrayAdapter<Uri> {

	private List<Uri> pictures;
	private Context mContext;
	private ThumbnailManager mThumbnailManager;
	
	public PicturesAdapter(Context context, int resource, ArrayList<Uri> arrayList) {
		super(context, resource, arrayList);
		this.mContext = context;
		this.pictures = arrayList;
		this.mThumbnailManager = new ThumbnailManager(context);
	}

	/*
	 * we are overriding the getView method here - this is what defines how each
	 * list item will look.
	 */
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.picture_item, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Uri i = pictures.get(position);

		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			final ImageView tt = (ImageView) v.findViewById(R.id.img);
			mThumbnailManager.getThumbnail(i, new ItemLoadedCallback<ImageLoaded> (){

				@Override
				public void onItemLoaded(ImageLoaded result, Throwable exception) {
					if(exception!=null) {
						exception.printStackTrace();
						return;
					}
					if(result!=null) {
						tt.setImageBitmap(result.mBitmap);
						notifyDataSetChanged();
					}
					
				}
				
			});
				
		}

		// the view must be returned to our activity
		return v;

	}


}
