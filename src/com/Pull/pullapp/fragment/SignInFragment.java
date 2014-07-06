package com.Pull.pullapp.fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public final class SignInFragment extends Fragment {
    private static final String KEY_CONTENT = "SignInFragment:Content";

    public static SignInFragment newInstance(String content, int res) {
        SignInFragment fragment = new SignInFragment();

        fragment.mContent = content;
        fragment.mDrawable = res;
        
        return fragment;
    }

    private String mContent = "???";
	private int mDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView text = new TextView(getActivity());
        text.setGravity(Gravity.CENTER);
        text.setText(mContent);
        text.setTextSize(20);
        text.setTextColor(Color.WHITE);
        text.setPadding(15, 15, 15, 15);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.setGravity(Gravity.TOP | Gravity.CENTER);
        layout.addView(text);
        layout.setBackgroundResource(mDrawable);
        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}