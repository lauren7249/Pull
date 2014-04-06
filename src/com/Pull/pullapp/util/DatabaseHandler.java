
package com.Pull.pullapp.util;

import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "pullDB";
 
    // Contacts table name
    private static final String TABLE_SHARED_CONVERSATIONS = "sharedConversations";
 
    // Columns
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_SHARED_WITH = "number";
    private static final String KEY_CONVERSATION_FROM = "orig_number";
    private static final String KEY_HASHTAG = "hashtags";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARED_CONVERSATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE + " TEXT,"
        		+ KEY_SHARED_WITH + " TEXT,"
        		+ KEY_CONVERSATION_FROM + " TEXT,"
                + KEY_HASHTAG + " TEXT" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_CONVERSATIONS);
 
        // Create tables again
        onCreate(db);
    }
 

    public void addSharedMessage(SharedConversation shared, int limit) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, shared.getDate());
        values.put(KEY_SHARED_WITH, shared.getConfidante());
        values.put(KEY_CONVERSATION_FROM, shared.getOriginalRecipient());
        values.put(KEY_HASHTAG, shared.getHashtags());
 
        // Inserting Row
        db.insert(TABLE_SHARED_CONVERSATIONS, null, values);
        
        if ((limit > 0) && (this.getSharedCount() > limit)) {
        	// TODO: delete oldest messages
        }
        
        db.close(); // Closing database connection
    }
 
    // Getting single shared conversation by id
    public SharedConversation getSharedConversation(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATIONS, new String[] { KEY_ID, KEY_DATE,
                KEY_SHARED_WITH, KEY_CONVERSATION_FROM, KEY_HASHTAG }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        SharedConversation shared = new SharedConversation(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        // return message
        return shared;
    }
     
    // Getting all messages
    public List<SharedConversation> getAllSharedConversation() {
        List<SharedConversation> sharedList = new ArrayList<SharedConversation>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SHARED_CONVERSATIONS;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToLast()) {
            do {
            	SharedConversation shared = new SharedConversation();
            	shared.setId(Integer.parseInt(cursor.getString(0)));
            	shared.setDate(cursor.getString(1));
            	shared.setConfidante(cursor.getString(2));
            	shared.setOriginalRecipient(cursor.getString(3));
            	shared.setHashtags(cursor.getString(4));
                // Adding contact to list
                sharedList.add(shared);
            } while (cursor.moveToPrevious());
        }
 
        // return contact list
        return sharedList;
    }
 
    // Deleting single message
    public void deleteShared(SharedConversation shared) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SHARED_CONVERSATIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(shared.getId()) });
        db.close();
    }
    

    public void deleteAllSharedConversations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_CONVERSATIONS);
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_SHARED_CONVERSATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE + " TEXT,"
        		+ KEY_SHARED_WITH + " TEXT,"
        		+ KEY_CONVERSATION_FROM + " TEXT,"
                + KEY_HASHTAG + " TEXT" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
        db.close();
    }
 
    // Getting contacts Count
    public int getSharedCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SHARED_CONVERSATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }
}
