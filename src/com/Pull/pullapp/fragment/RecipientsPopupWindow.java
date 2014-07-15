package com.Pull.pullapp.fragment;

import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.Pull.pullapp.R;
import com.Pull.pullapp.util.RecipientsAdapter;
import com.Pull.pullapp.util.RecipientsEditor;

public class RecipientsPopupWindow extends DialogFragment  {
	private RecipientsEditor mConfidantesEditor;
	private RecipientsAdapter mRecipientsAdapter;
	private ImageButton chooser;	
	private Dialog dialog;
    public RecipientsPopupWindow() {
        // Empty constructor required for DialogFragment
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	}	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	dialog = getDialog();
    	dialog.setTitle("Approver");
        View v = inflater.inflate(R.layout.share_popup_layout, container, false);
		mConfidantesEditor = (RecipientsEditor)v.findViewById(R.id.confidantes_editor);
		mRecipientsAdapter = new RecipientsAdapter(getActivity().getApplicationContext());
		mConfidantesEditor.setAdapter(mRecipientsAdapter);
		mConfidantesEditor.setTextColor(Color.BLACK);
		chooser = (ImageButton) v.findViewById(R.id.choose);
		chooser.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ApproverDialogListener activity = (ApproverDialogListener) getActivity();
				activity.onFinishEditDialog(getApproverNumber());
				dialog.dismiss();
			}
			
		});

        return v;
    }
    public String getApproverNumber(){
    	if(mConfidantesEditor.constructContactsFromInput(false).getToNumbers().length==0) return null;
    	return mConfidantesEditor.constructContactsFromInput(false).getToNumbers()[0];
    }
    
    public interface ApproverDialogListener {
        void onFinishEditDialog(String inputText);
    }    
 
}
