package com.Pull.pullapp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.Pull.pullapp.R;
import com.android.mms.MmsConfig;


public class AttachmentTypeSelectorAdapter extends IconListAdapter {
    public final static int MODE_WITH_SLIDESHOW    = 0;
    public final static int MODE_WITHOUT_SLIDESHOW = 1;

    public final static int ADD_IMAGE               = 0;
    public final static int TAKE_PICTURE            = 1;
    public final static int ADD_VIDEO               = 2;
    public final static int RECORD_VIDEO            = 3;
    public final static int ADD_SOUND               = 4;
    public final static int RECORD_SOUND            = 5;
    public final static int ADD_SLIDESHOW           = 6;

    public AttachmentTypeSelectorAdapter(Context context, int mode) {
        super(context, getData(mode, context));
    }

    public int buttonToCommand(int whichButton) {
        AttachmentListItem item = (AttachmentListItem)getItem(whichButton);
        return item.getCommand();
    }

    protected static List<IconListItem> getData(int mode, Context context) {
        List<IconListItem> data = new ArrayList<IconListItem>(7);
        addItem(data, "Attach image",
                R.drawable.ic_launcher_gallery, ADD_IMAGE);

        addItem(data, "Take photo",
                R.drawable.ic_launcher_camera, TAKE_PICTURE);

      /**  addItem(data, "Attach video",
                R.drawable.ic_launcher_video_player, ADD_VIDEO);

        addItem(data, "Record video",
                R.drawable.ic_launcher_camera_record, RECORD_VIDEO);

        if (false) {
            addItem(data, "Attach audio",
                    R.drawable.ic_launcher_musicplayer_2, ADD_SOUND);
        }

        addItem(data, "Record audio",
                R.drawable.ic_launcher_record_audio, RECORD_SOUND);

        if (mode == MODE_WITH_SLIDESHOW) {
            addItem(data, "Attach slideshow",
                    R.drawable.ic_launcher_slideshow_add_sms, ADD_SLIDESHOW);
        }*/

        return data;
    }

    protected static void addItem(List<IconListItem> data, String title,
            int resource, int command) {
        AttachmentListItem temp = new AttachmentListItem(title, resource, command);
        data.add(temp);
    }

    public static class AttachmentListItem extends IconListAdapter.IconListItem {
        private int mCommand;

        public AttachmentListItem(String title, int resource, int command) {
            super(title, resource);

            mCommand = command;
        }

        public int getCommand() {
            return mCommand;
        }
    }
}

