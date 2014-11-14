package com.Pull.pullapp.adapter;

import java.util.List;

import com.Pull.pullapp.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

public class PicturesAdapter extends ArrayAdapter<Bitmap> {

	private List<Bitmap> pictures;
	private Context mContext;

	public PicturesAdapter(Context context, int resource, List<Bitmap> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.pictures = objects;
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
		Bitmap i = pictures.get(position);

		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			ImageView tt = (ImageView) v.findViewById(R.id.img);

			// check to see if each individual textview is null.
			// if not, assign some text!
			if (tt != null){
				tt.setImageBitmap(i);
			}
		}

		// the view must be returned to our activity
		return v;

	}


}
