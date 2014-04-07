
package com.Pull.pullapp.util;

import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Telephony.TextBasedSmsColumns;

public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "pullDB";
 
    // Contacts table name
    private static final String TABLE_SHARED_CONVERSATIONS = "sharedConversations";
    private static final String TABLE_OUTBOX = "outbox";
    private static final String TABLE_SHARED_CONVERSATION_SMS = "sharedConversationsSMSs";
    private static final String TABLE_SHARED_CONVERSATION_COMMENTS = "sharedConversationsComments";
    
    // Columns for shared convos
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_SHARED_WITH = "number";
    private static final String KEY_CONVERSATION_FROM = "orig_number";
    private static final String KEY_HASHTAG_ID = "hashtagID";
    
    
    private SQLiteDatabase db;
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARED_CONVERSATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE + " TEXT,"
        		+ KEY_SHARED_WITH + " TEXT,"
        		+ KEY_CONVERSATION_FROM + " TEXT" + ")";    
        String CREATE_OUTBOX_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_OUTBOX + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
        		+ TextBasedSmsColumns.DATE_SENT + " TEXT,"
        		+ TextBasedSmsColumns.DATE + " TEXT,"
        		+ TextBasedSmsColumns.BODY + " TEXT,"
                + TextBasedSmsColumns.ADDRESS + " TEXT" + ")";       
        String CREATE_SHARED_SMS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARED_CONVERSATION_SMS + "("
                + KEY_ID + " INTEGER," 
        		+ TextBasedSmsColumns.DATE + " TEXT,"
        		+ TextBasedSmsColumns.BODY + " TEXT,"
        		+ TextBasedSmsColumns.TYPE + " TEXT)";         
        String CREATE_SHARED_COMMENTS = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARED_CONVERSATION_COMMENTS + "("
                + KEY_ID + " INTEGER," 
        		+ TextBasedSmsColumns.BODY + " TEXT,"
        		+ TextBasedSmsColumns.ADDRESS + " TEXT,"
        		+ TextBasedSmsColumns.DATE + " TEXT,"
        		+ KEY_HASHTAG_ID + " TEXT)";          
        db.execSQL(CREATE_OUTBOX_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
        db.execSQL(CREATE_SHARED_SMS_TABLE);
        db.execSQL(CREATE_SHARED_COMMENTS);
        
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OUTBOX);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_CONVERSATION_SMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_CONVERSATION_COMMENTS);
        // Create tables again
        onCreate(db);
    }
 

    public int addSharedConversation(SharedConversation shared) {
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, shared.getDate());
        values.put(KEY_SHARED_WITH, shared.getConfidante());
        values.put(KEY_CONVERSATION_FROM, shared.getOriginalRecipient());
        // Inserting Row
        db.insert(TABLE_SHARED_CONVERSATIONS, null, values);
        int id = this.getSharedCount();
        for(SMSMessage m : shared.getMessages()) {
        	addSharedMessage(id, m);
        }
        for(Comment c : shared.getComments()) {
        	addComment(id, c);
        }        
        return id;
    }
    public void addSharedMessage(int id, SMSMessage msg) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(TextBasedSmsColumns.DATE, msg.getDate());
        values.put(TextBasedSmsColumns.BODY, msg.getMessage());
        values.put(TextBasedSmsColumns.TYPE, msg.sentByMe);
        db.insert(TABLE_SHARED_CONVERSATION_SMS, null, values);
    }
    
    public void addComment(int id, Comment c) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(TextBasedSmsColumns.DATE, c.getDate());
        values.put(TextBasedSmsColumns.BODY, c.getMessage());
        values.put(TextBasedSmsColumns.ADDRESS, c.getSender());
        values.put(KEY_HASHTAG_ID, c.getHashtagID());
        db.insert(TABLE_SHARED_CONVERSATION_COMMENTS, null, values);
    }    
    public int addToOutbox(String recipient, String message,
			long timeScheduled, long scheduledFor) {
	    ContentValues outboxSms = new ContentValues();
	    outboxSms.put(TextBasedSmsColumns.DATE_SENT, Long.toString(timeScheduled));
	    outboxSms.put(TextBasedSmsColumns.DATE, Long.toString(scheduledFor));
	    outboxSms.put(TextBasedSmsColumns.BODY, message);
	    outboxSms.put(TextBasedSmsColumns.ADDRESS, recipient);
        // Inserting Row
        db.insert(TABLE_OUTBOX, null, outboxSms);
        int count = this.getSharedCount();
        return count;
    }    
 
    public SharedConversation getSharedConversation(int id) {
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATIONS, new String[] { KEY_ID, KEY_DATE,
                KEY_SHARED_WITH, KEY_CONVERSATION_FROM}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        SharedConversation shared = new SharedConversation(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3));

        return shared;
    }
    

    public ArrayList<Comment> getComments(int id) {
    	ArrayList<Comment> comments = new ArrayList<Comment>();
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATION_COMMENTS, null, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        // looping through all rows and adding to list
        if (cursor.moveToLast()) {
            do {
            	Comment c = new Comment(cursor.getString(1), cursor.getString(2), cursor.getString(3),
            			Integer.parseInt(cursor.getString(4)));
                comments.add(c);
            } while (cursor.moveToPrevious());
        }

        return comments;
    }    
    
    private ArrayList<SMSMessage> getMessages(int id) {
    	ArrayList<SMSMessage> messages = new ArrayList<SMSMessage>();
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATION_SMS, null, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        // looping through all rows and adding to list
        if (cursor.moveToLast()) {
            do {
            	SMSMessage m = new SMSMessage();
            	m.setDate(cursor.getLong(1));
            	m.setMessage(cursor.getString(2));
            	m.sentByMe = Boolean.parseBoolean(cursor.getString(3));
            	messages.add(m);
            } while (cursor.moveToPrevious());
        }

        return messages;
	}    
    
    // Getting single shared conversation by id
    public int deleteFromOutbox(long launchedOn) {
		String where = TextBasedSmsColumns.DATE_SENT + "=" + launchedOn;
        int rows_deleted = db.delete(TABLE_OUTBOX, where, null);
        return rows_deleted;
    }    
     
    // Getting all messages
    public List<SharedConversation> getAllSharedConversation() {
        List<SharedConversation> sharedList = new ArrayList<SharedConversation>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SHARED_CONVERSATIONS;
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToLast()) {
            do {
            	SharedConversation shared = new SharedConversation();
            	int id = Integer.parseInt(cursor.getString(0));
            	shared.setId(id);
            	shared.setDate(cursor.getString(1));
            	shared.setConfidante(cursor.getString(2));
            	shared.setOriginalRecipient(cursor.getString(3));
            	shared.setMessages(getMessages(id));
                shared.setComments(getComments(id));
                sharedList.add(shared);
            } while (cursor.moveToPrevious());
        }
        return sharedList;
    }


	public void deleteShared(SharedConversation shared) {
        db.delete(TABLE_SHARED_CONVERSATIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(shared.getId()) });
        db.delete(TABLE_SHARED_CONVERSATION_COMMENTS, KEY_ID + " = ?",
                new String[] { String.valueOf(shared.getId()) });        
    }
    

    
    public void close() {
    	if(db.isOpen()) db.close();
    }
 

    public int getSharedCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SHARED_CONVERSATIONS;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
