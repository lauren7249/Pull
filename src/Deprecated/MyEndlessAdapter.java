package Deprecated;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.Pull.pullapp.R;
import com.commonsware.cwac.endless.EndlessAdapter;

public class MyEndlessAdapter extends EndlessAdapter {
  private RotateAnimation rotate=null;
  private View pendingView=null;

  public MyEndlessAdapter(ThreadItemsListAdapter adapter) {

    super(adapter);
    rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                                0.5f, Animation.RELATIVE_TO_SELF,
                                0.5f);
    rotate.setDuration(600);
    rotate.setRepeatMode(Animation.RESTART);
    rotate.setRepeatCount(Animation.INFINITE);
  }
  
  @Override
  protected View getPendingView(ViewGroup parent) {
    View row=LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_thread, null);
    
    pendingView=row.findViewById(R.id.txt_title);
    pendingView.setVisibility(View.GONE);
    //pendingView=row.findViewById(R.id.throbber);
    pendingView.setVisibility(View.VISIBLE);
    startProgressAnimation();
    
    return(row);
  }
  
  @Override
  protected boolean cacheInBackground() {
    return(getWrappedAdapter().getCount()<75);
  }
  
  @Override
  protected void appendCachedData() {
    if (getWrappedAdapter().getCount()<75) {
      @SuppressWarnings("unchecked")
      ThreadItemsListAdapter a=(ThreadItemsListAdapter)getWrappedAdapter();
      
      //for (int i=0;i<25;i++) { a.addItem((a.getCount()); }
    }
  }
  
  void startProgressAnimation() {
    if (pendingView!=null) {
      pendingView.startAnimation(rotate);
    }
  }
}