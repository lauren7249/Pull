package com.Pull.pullapp.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TableLayout.LayoutParams;

import com.Pull.pullapp.R;
import com.Pull.pullapp.util.contacts.RecipientsAdapter;
import com.Pull.pullapp.util.contacts.RecipientsEditor;

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
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = LayoutParams.MATCH_PARENT;
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.x = 200;
        p.y = 20;
        p.gravity = Gravity.TOP;
        dialog.getWindow().setAttributes(p);    	
        View v = inflater.inflate(R.layout.share_popup_layout, container, false);
		mConfidantesEditor = (RecipientsEditor)v.findViewById(R.id.confidantes_editor);
		mRecipientsAdapter = new RecipientsAdapter(getActivity().getApplicationContext());
		mConfidantesEditor.setAdapter(mRecipientsAdapter);

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
