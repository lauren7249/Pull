/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.Pull.pullapp.util.phone;

import android.app.AppOpsManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.provider.Telephony.CanonicalAddressesColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Mms.Addr;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.Mms.Rate;
import android.text.TextUtils;
import android.util.Log;


import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.util_alt.DownloadDrmHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The class to provide base facility to access MMS related content,
 * which is stored in a SQLite database and in the file system.
 */
public class MmsProvider extends ContentProvider {
    static final String TABLE_PDU  = "pdu";
    static final String TABLE_ADDR = "addr";
    static final String TABLE_PART = "part";
    static final String TABLE_RATE = "rate";
    static final String TABLE_DRM  = "drm";
    static final String TABLE_WORDS = "words";


    @Override
    public boolean onCreate() {
      //  setAppOps(AppOpsManager.OP_READ_SMS, AppOpsManager.OP_WRITE_SMS);
        mOpenHelper = MmsSmsDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query.
        int match = sURLMatcher.match(uri);
        if (LOCAL_LOGV) {
            Log.v(TAG, "Query uri=" + uri + ", match=" + match);
        }

        switch (match) {
            case MMS_ALL:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_ALL);
                break;
            case MMS_INBOX:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_INBOX);
                break;
            case MMS_SENT:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_SENT);
                break;
            case MMS_DRAFTS:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_DRAFTS);
                break;
            case MMS_OUTBOX:
                constructQueryForBox(qb, Mms.MESSAGE_BOX_OUTBOX);
                break;
            case MMS_ALL_ID:
                qb.setTables(TABLE_PDU);
                qb.appendWhere(Mms._ID + "=" + uri.getPathSegments().get(0));
                break;
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                qb.setTables(TABLE_PDU);
                qb.appendWhere(Mms._ID + "=" + uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + Mms.MESSAGE_BOX + "="
                        + getMessageBoxByMatch(match));
                break;
            case MMS_ALL_PART:
                qb.setTables(TABLE_PART);
                break;
            case MMS_MSG_PART:
                qb.setTables(TABLE_PART);
                qb.appendWhere(Part.MSG_ID + "=" + uri.getPathSegments().get(0));
                break;
            case MMS_PART_ID:
                qb.setTables(TABLE_PART);
                qb.appendWhere(Part._ID + "=" + uri.getPathSegments().get(1));
                break;
            case MMS_MSG_ADDR:
                qb.setTables(TABLE_ADDR);
                qb.appendWhere(Addr.MSG_ID + "=" + uri.getPathSegments().get(0));
                break;
            case MMS_REPORT_STATUS:
                /*
                   SELECT DISTINCT address,
                                   T.delivery_status AS delivery_status,
                                   T.read_status AS read_status
                   FROM addr
                   INNER JOIN (SELECT P1._id AS id1, P2._id AS id2, P3._id AS id3,
                                      ifnull(P2.st, 0) AS delivery_status,
                                      ifnull(P3.read_status, 0) AS read_status
                               FROM pdu P1
                               INNER JOIN pdu P2
                               ON P1.m_id = P2.m_id AND P2.m_type = 134
                               LEFT JOIN pdu P3
                               ON P1.m_id = P3.m_id AND P3.m_type = 136
                               UNION
                               SELECT P1._id AS id1, P2._id AS id2, P3._id AS id3,
                                      ifnull(P2.st, 0) AS delivery_status,
                                      ifnull(P3.read_status, 0) AS read_status
                               FROM pdu P1
                               INNER JOIN pdu P3
                               ON P1.m_id = P3.m_id AND P3.m_type = 136
                               LEFT JOIN pdu P2
                               ON P1.m_id = P2.m_id AND P2.m_type = 134) T
                   ON (msg_id = id2 AND type = 151)
                   OR (msg_id = id3 AND type = 137)
                   WHERE T.id1 = ?;
                 */
                qb.setTables("addr INNER JOIN (SELECT P1._id AS id1, P2._id" +
                             " AS id2, P3._id AS id3, ifnull(P2.st, 0) AS" +
                             " delivery_status, ifnull(P3.read_status, 0) AS" +
                             " read_status FROM pdu P1 INNER JOIN pdu P2 ON" +
                             " P1.m_id=P2.m_id AND P2.m_type=134 LEFT JOIN" +
                             " pdu P3 ON P1.m_id=P3.m_id AND P3.m_type=136" +
                             " UNION SELECT P1._id AS id1, P2._id AS id2, P3._id" +
                             " AS id3, ifnull(P2.st, 0) AS delivery_status," +
                             " ifnull(P3.read_status, 0) AS read_status FROM" +
                             " pdu P1 INNER JOIN pdu P3 ON P1.m_id=P3.m_id AND" +
                             " P3.m_type=136 LEFT JOIN pdu P2 ON P1.m_id=P2.m_id" +
                             " AND P2.m_type=134) T ON (msg_id=id2 AND type=151)" +
                             " OR (msg_id=id3 AND type=137)");
                qb.appendWhere("T.id1 = " + uri.getLastPathSegment());
                qb.setDistinct(true);
                break;
            case MMS_REPORT_REQUEST:
                /*
                   SELECT address, d_rpt, rr
                   FROM addr join pdu on pdu._id = addr.msg_id
                   WHERE pdu._id = messageId AND addr.type = 151
                 */
                qb.setTables(TABLE_ADDR + " join " +
                        TABLE_PDU + " on pdu._id = addr.msg_id");
                qb.appendWhere("pdu._id = " + uri.getLastPathSegment());
                qb.appendWhere(" AND " + "addr.type = " + PduHeaders.TO);
                break;
            case MMS_SENDING_RATE:
                qb.setTables(TABLE_RATE);
                break;
            case MMS_DRM_STORAGE_ID:
                qb.setTables(TABLE_DRM);
                qb.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
                break;
            case MMS_THREADS:
                qb.setTables("pdu group by thread_id");
                break;
            default:
                Log.e(TAG, "query: invalid request: " + uri);
                return null;
        }

        String finalSortOrder = null;
        if (TextUtils.isEmpty(sortOrder)) {
            if (qb.getTables().equals(TABLE_PDU)) {
                finalSortOrder = Mms.DATE + " DESC";
            } else if (qb.getTables().equals(TABLE_PART)) {
                finalSortOrder = Part.SEQ;
            }
        } else {
            finalSortOrder = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projection, selection,
                selectionArgs, null, null, finalSortOrder);

        // TODO: Does this need to be a URI for this provider.
        ret.setNotificationUri(getContext().getContentResolver(), uri);
        return ret;
    }

    private void constructQueryForBox(SQLiteQueryBuilder qb, int msgBox) {
        qb.setTables(TABLE_PDU);

        if (msgBox != Mms.MESSAGE_BOX_ALL) {
            qb.appendWhere(Mms.MESSAGE_BOX + "=" + msgBox);
        }
    }

    @Override
    public String getType(Uri uri) {
        int match = sURLMatcher.match(uri);
        switch (match) {
            case MMS_ALL:
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_DRAFTS:
            case MMS_OUTBOX:
                return VND_ANDROID_DIR_MMS;
            case MMS_ALL_ID:
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                return VND_ANDROID_MMS;
            case MMS_PART_ID: {
                Cursor cursor = mOpenHelper.getReadableDatabase().query(
                        TABLE_PART, new String[] { Part.CONTENT_TYPE },
                        Part._ID + " = ?", new String[] { uri.getLastPathSegment() },
                        null, null, null);
                if (cursor != null) {
                    try {
                        if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                            return cursor.getString(0);
                        } else {
                            Log.e(TAG, "cursor.count() != 1: " + uri);
                        }
                    } finally {
                        cursor.close();
                    }
                } else {
                    Log.e(TAG, "cursor == null: " + uri);
                }
                return "*/*";
            }
            case MMS_ALL_PART:
            case MMS_MSG_PART:
            case MMS_MSG_ADDR:
            default:
                return "*/*";
        }
    }


    private int getMessageBoxByMatch(int match) {
        switch (match) {
            case MMS_INBOX_ID:
            case MMS_INBOX:
                return Mms.MESSAGE_BOX_INBOX;
            case MMS_SENT_ID:
            case MMS_SENT:
                return Mms.MESSAGE_BOX_SENT;
            case MMS_DRAFTS_ID:
            case MMS_DRAFTS:
                return Mms.MESSAGE_BOX_DRAFTS;
            case MMS_OUTBOX_ID:
            case MMS_OUTBOX:
                return Mms.MESSAGE_BOX_OUTBOX;
            default:
                throw new IllegalArgumentException("bad Arg: " + match);
        }
    }

    @Override
    public int delete(Uri uri, String selection,
            String[] selectionArgs) {
        int match = sURLMatcher.match(uri);
        if (LOCAL_LOGV) {
            Log.v(TAG, "Delete uri=" + uri + ", match=" + match);
        }

        String table, extraSelection = null;
        boolean notify = false;

        switch (match) {
            case MMS_ALL_ID:
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                notify = true;
                table = TABLE_PDU;
                extraSelection = Mms._ID + "=" + uri.getLastPathSegment();
                break;
            case MMS_ALL:
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_DRAFTS:
            case MMS_OUTBOX:
                notify = true;
                table = TABLE_PDU;
                if (match != MMS_ALL) {
                    int msgBox = getMessageBoxByMatch(match);
                    extraSelection = Mms.MESSAGE_BOX + "=" + msgBox;
                }
                break;
            case MMS_ALL_PART:
                table = TABLE_PART;
                break;
            case MMS_MSG_PART:
                table = TABLE_PART;
                extraSelection = Part.MSG_ID + "=" + uri.getPathSegments().get(0);
                break;
            case MMS_PART_ID:
                table = TABLE_PART;
                extraSelection = Part._ID + "=" + uri.getPathSegments().get(1);
                break;
            case MMS_MSG_ADDR:
                table = TABLE_ADDR;
                extraSelection = Addr.MSG_ID + "=" + uri.getPathSegments().get(0);
                break;
            case MMS_DRM_STORAGE:
                table = TABLE_DRM;
                break;
            default:
                Log.w(TAG, "No match for URI '" + uri + "'");
                return 0;
        }

        String finalSelection = concatSelections(selection, extraSelection);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deletedRows = 0;

        if (TABLE_PDU.equals(table)) {
            deletedRows = deleteMessages(getContext(), db, finalSelection,
                                         selectionArgs, uri);
        } else if (TABLE_PART.equals(table)) {
            deletedRows = deleteParts(db, finalSelection, selectionArgs);
        } else if (TABLE_DRM.equals(table)) {
            deletedRows = deleteTempDrmData(db, finalSelection, selectionArgs);
        } else {
            deletedRows = db.delete(table, finalSelection, selectionArgs);
        }

        if ((deletedRows > 0) && notify) {
            notifyChange();
        }
        return deletedRows;
    }

    static int deleteMessages(Context context, SQLiteDatabase db,
            String selection, String[] selectionArgs, Uri uri) {
        Cursor cursor = db.query(TABLE_PDU, new String[] { Mms._ID },
                selection, selectionArgs, null, null, null);
        if (cursor == null) {
            return 0;
        }

        try {
            if (cursor.getCount() == 0) {
                return 0;
            }

            while (cursor.moveToNext()) {
                deleteParts(db, Part.MSG_ID + " = ?",
                        new String[] { String.valueOf(cursor.getLong(0)) });
            }
        } finally {
            cursor.close();
        }

        int count = db.delete(TABLE_PDU, selection, selectionArgs);
        if (count > 0) {
            Intent intent = new Intent(Mms.Intents.CONTENT_CHANGED_ACTION);
            intent.putExtra(Mms.Intents.DELETED_CONTENTS, uri);
            if (LOCAL_LOGV) {
                Log.v(TAG, "Broadcasting intent: " + intent);
            }
            context.sendBroadcast(intent);
        }
        return count;
    }

    private static int deleteParts(SQLiteDatabase db, String selection,
            String[] selectionArgs) {
        return deleteDataRows(db, TABLE_PART, selection, selectionArgs);
    }

    private static int deleteTempDrmData(SQLiteDatabase db, String selection,
            String[] selectionArgs) {
        return deleteDataRows(db, TABLE_DRM, selection, selectionArgs);
    }

    private static int deleteDataRows(SQLiteDatabase db, String table,
            String selection, String[] selectionArgs) {
        Cursor cursor = db.query(table, new String[] { "_data" },
                selection, selectionArgs, null, null, null);
        if (cursor == null) {
            // FIXME: This might be an error, ignore it may cause
            // unpredictable result.
            return 0;
        }

        try {
            if (cursor.getCount() == 0) {
                return 0;
            }

            while (cursor.moveToNext()) {
                try {
                    // Delete the associated files saved on file-system.
                    String path = cursor.getString(0);
                    if (path != null) {
                        new File(path).delete();
                    }
                } catch (Throwable ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                }
            }
        } finally {
            cursor.close();
        }

        return db.delete(table, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        // Don't let anyone update the _data column
        if (values != null && values.containsKey(Part._DATA)) {
            return 0;
        }
        int match = sURLMatcher.match(uri);
        if (LOCAL_LOGV) {
            Log.v(TAG, "Update uri=" + uri + ", match=" + match);
        }

        boolean notify = false;
        String msgId = null;
        String table;

        switch (match) {
            case MMS_ALL_ID:
            case MMS_INBOX_ID:
            case MMS_SENT_ID:
            case MMS_DRAFTS_ID:
            case MMS_OUTBOX_ID:
                msgId = uri.getLastPathSegment();
            // fall-through
            case MMS_ALL:
            case MMS_INBOX:
            case MMS_SENT:
            case MMS_DRAFTS:
            case MMS_OUTBOX:
                notify = true;
                table = TABLE_PDU;
                break;

            case MMS_MSG_PART:
            case MMS_PART_ID:
                table = TABLE_PART;
                break;

            case MMS_PART_RESET_FILE_PERMISSION:
                String path = getContext().getDir("parts", 0).getPath() + '/' +
                        uri.getPathSegments().get(1);
                // Reset the file permission back to read for everyone but me.

                return 0;

            default:
                Log.w(TAG, "Update operation for '" + uri + "' not implemented.");
                return 0;
        }

        String extraSelection = null;
        ContentValues finalValues;
        if (table.equals(TABLE_PDU)) {

            finalValues = new ContentValues(values);

            if (msgId != null) {
                extraSelection = Mms._ID + "=" + msgId;
            }
        } else if (table.equals(TABLE_PART)) {
            finalValues = new ContentValues(values);

            switch (match) {
                case MMS_MSG_PART:
                    extraSelection = Part.MSG_ID + "=" + uri.getPathSegments().get(0);
                    break;
                case MMS_PART_ID:
                    extraSelection = Part._ID + "=" + uri.getPathSegments().get(1);
                    break;
                default:
                    break;
            }
        } else {
            return 0;
        }

        String finalSelection = concatSelections(selection, extraSelection);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(table, finalValues, finalSelection, selectionArgs);
        if (notify && (count > 0)) {
            notifyChange();
        }
        return count;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int match = sURLMatcher.match(uri);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.d(TAG, "openFile: uri=" + uri + ", mode=" + mode + ", match=" + match);
        }

        if (match != MMS_PART_ID) {
            return null;
        }

        // Verify that the _data path points to mms data
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        int count = (c != null) ? c.getCount() : 0;
        if (count != 1) {
            // If there is not exactly one result, throw an appropriate
            // exception.
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }

        c.moveToFirst();
        int i = c.getColumnIndex("_data");
        String path = (i >= 0 ? c.getString(i) : null);
        c.close();

        if (path == null) {
            return null;
        }
        try {
            File filePath = new File(path);
            if (!filePath.getCanonicalPath()
                    .startsWith(getContext().getApplicationInfo().dataDir + "/app_parts/")) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return openFileHelper(uri, mode);
    }



    private void notifyChange() {
        getContext().getContentResolver().notifyChange(
                MmsSms.CONTENT_URI, null);
    }

    private final static String TAG = "MmsProvider";
    private final static String VND_ANDROID_MMS = "vnd.android/mms";
    private final static String VND_ANDROID_DIR_MMS = "vnd.android-dir/mms";
    private final static boolean DEBUG = false;
    private final static boolean LOCAL_LOGV = false;

    private static final int MMS_ALL                      = 0;
    private static final int MMS_ALL_ID                   = 1;
    private static final int MMS_INBOX                    = 2;
    private static final int MMS_INBOX_ID                 = 3;
    private static final int MMS_SENT                     = 4;
    private static final int MMS_SENT_ID                  = 5;
    private static final int MMS_DRAFTS                   = 6;
    private static final int MMS_DRAFTS_ID                = 7;
    private static final int MMS_OUTBOX                   = 8;
    private static final int MMS_OUTBOX_ID                = 9;
    private static final int MMS_ALL_PART                 = 10;
    private static final int MMS_MSG_PART                 = 11;
    private static final int MMS_PART_ID                  = 12;
    private static final int MMS_MSG_ADDR                 = 13;
    private static final int MMS_SENDING_RATE             = 14;
    private static final int MMS_REPORT_STATUS            = 15;
    private static final int MMS_REPORT_REQUEST           = 16;
    private static final int MMS_DRM_STORAGE              = 17;
    private static final int MMS_DRM_STORAGE_ID           = 18;
    private static final int MMS_THREADS                  = 19;
    private static final int MMS_PART_RESET_FILE_PERMISSION = 20;

    private static final UriMatcher
            sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("mms", null,         MMS_ALL);
        sURLMatcher.addURI("mms", "#",          MMS_ALL_ID);
        sURLMatcher.addURI("mms", "inbox",      MMS_INBOX);
        sURLMatcher.addURI("mms", "inbox/#",    MMS_INBOX_ID);
        sURLMatcher.addURI("mms", "sent",       MMS_SENT);
        sURLMatcher.addURI("mms", "sent/#",     MMS_SENT_ID);
        sURLMatcher.addURI("mms", "drafts",     MMS_DRAFTS);
        sURLMatcher.addURI("mms", "drafts/#",   MMS_DRAFTS_ID);
        sURLMatcher.addURI("mms", "outbox",     MMS_OUTBOX);
        sURLMatcher.addURI("mms", "outbox/#",   MMS_OUTBOX_ID);
        sURLMatcher.addURI("mms", "part",       MMS_ALL_PART);
        sURLMatcher.addURI("mms", "#/part",     MMS_MSG_PART);
        sURLMatcher.addURI("mms", "part/#",     MMS_PART_ID);
        sURLMatcher.addURI("mms", "#/addr",     MMS_MSG_ADDR);
        sURLMatcher.addURI("mms", "rate",       MMS_SENDING_RATE);
        sURLMatcher.addURI("mms", "report-status/#",  MMS_REPORT_STATUS);
        sURLMatcher.addURI("mms", "report-request/#", MMS_REPORT_REQUEST);
        sURLMatcher.addURI("mms", "drm",        MMS_DRM_STORAGE);
        sURLMatcher.addURI("mms", "drm/#",      MMS_DRM_STORAGE_ID);
        sURLMatcher.addURI("mms", "threads",    MMS_THREADS);
        sURLMatcher.addURI("mms", "resetFilePerm/*",    MMS_PART_RESET_FILE_PERMISSION);
    }

    private SQLiteOpenHelper mOpenHelper;

    private static String concatSelections(String selection1, String selection2) {
        if (TextUtils.isEmpty(selection1)) {
            return selection2;
        } else if (TextUtils.isEmpty(selection2)) {
            return selection1;
        } else {
            return selection1 + " AND " + selection2;
        }
    }

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}
}

