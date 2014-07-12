
package com.Pull.pullapp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;

import com.Pull.pullapp.model.Comment;
import com.Pull.pullapp.model.SMSMessage;
import com.Pull.pullapp.model.SharedConversation;

public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 4;
 
    // Database Name
    public static final String DATABASE_NAME = "pullDB";
 
    // Contacts table name
    public static final String TABLE_SHARED_CONVERSATIONS = "sharedConversations";
    public static final String TABLE_OUTBOX = "outbox";
    public static final String TABLE_SHARED_CONVERSATION_SMS = "sharedConversationsSMSs";
    public static final String TABLE_SHARED_CONVERSATION_COMMENTS = "sharedConversationsComments";
    
    // Columns for shared convos
    public static final String KEY_ID = "id";
    public static final String KEY_DATE = "date";
    public static final String KEY_SHARED_WITH = "number";
    public static final String KEY_CONVERSATION_FROM = "orig_number";
    public static final String KEY_HASHTAG_ID = "hashtagID";
    public static final String KEY_SHARER = "sharer";
    public static final String KEY_PROPOSED = "isproposal";
    public static final String KEY_CONVERSATION_FROM_NAME = "orig_name";
	public static final String KEY_CONVO_TYPE = "convo_type";

	
    
    private SQLiteDatabase db;

	private String KEY_HASHCODE = "hashcode";

	private Context mContext;
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
        mContext = context;
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARED_CONVERSATIONS + "("
                + KEY_ID + " TEXT," 
        		+ KEY_DATE + " DATE,"
        		+ KEY_SHARED_WITH + " TEXT,"
        		+ KEY_CONVERSATION_FROM + " TEXT," 
        		+ KEY_CONVERSATION_FROM_NAME + " TEXT,"
        		+ KEY_CONVO_TYPE + " TEXT,"
        		+ KEY_SHARER + " TEXT)";    
        String CREATE_OUTBOX_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_OUTBOX + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
        		+ TextBasedSmsColumns.DATE_SENT + " DATE,"
        		+ TextBasedSmsColumns.DATE + " DATE,"
        		+ TextBasedSmsColumns.BODY + " TEXT,"
                + TextBasedSmsColumns.ADDRESS + " TEXT" + ")";             
        String CREATE_SHARED_SMS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARED_CONVERSATION_SMS + "("
                + KEY_ID + " TEXT," 
                + KEY_HASHCODE + " INTEGER," 
                + KEY_CONVO_TYPE + " TEXT,"
        		+ TextBasedSmsColumns.DATE + " DATE,"
        		+ TextBasedSmsColumns.BODY + " TEXT,"
        		+ TextBasedSmsColumns.TYPE + " TEXT," 
        		+ TextBasedSmsColumns.ADDRESS + " TEXT)";              
        String CREATE_SHARED_COMMENTS = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARED_CONVERSATION_COMMENTS + "("
                + KEY_ID + " TEXT," 
        		+ TextBasedSmsColumns.BODY + " TEXT,"
        		+ TextBasedSmsColumns.ADDRESS + " TEXT,"
        		+ TextBasedSmsColumns.DATE + " DATE," 
        		+ KEY_PROPOSED + " TEXT)";          
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
 
    /**returns count in table**/
    public int addSharedConversation(SharedConversation shared) {
    	String id = shared.getObjectId();
    	if(!contains(shared)) {
	        for(SMSMessage m : shared.getMessages()) {
	        	addSharedMessage(id, m, shared.getType());
	        }
	        for(Comment c : shared.getComments()) {
	        	addComment(shared.getObjectId(), c);
	        }       
    	}
        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_DATE, shared.getDate());
        values.put(KEY_SHARED_WITH, shared.getConfidante());
        values.put(KEY_CONVERSATION_FROM, shared.getOriginalRecipient());
        values.put(KEY_CONVERSATION_FROM_NAME, shared.getOriginalRecipientName());
        values.put(KEY_SHARER, shared.getSharer());
        values.put(KEY_CONVO_TYPE, shared.getType());
        // Inserting Row
        long row_id = db.insert(TABLE_SHARED_CONVERSATIONS, null, values);
        //Log.i("insert","returned " + row_id);
        int count = this.getSharedCount(shared.getType());     
        return count;
    }
    
    public int addSharedConversation(String convo_id, String confidante, String originalRecipientName, 
    		String originalRecipient, String sender, int type){
        ContentValues values = new ContentValues();
        values.put(KEY_ID, convo_id);
        values.put(KEY_DATE, System.currentTimeMillis());
        values.put(KEY_SHARED_WITH, confidante);
        values.put(KEY_CONVERSATION_FROM_NAME, originalRecipientName);
        values.put(KEY_CONVERSATION_FROM, originalRecipient);
        values.put(KEY_SHARER, sender);
        values.put(KEY_CONVO_TYPE, type);
        // Inserting Row
        if(exists(convo_id)) db.update(TABLE_SHARED_CONVERSATIONS, values, KEY_ID+"="+convo_id, null);
        else db.insert(TABLE_SHARED_CONVERSATIONS, null, values);
        int count = this.getSharedCount(type);     
        if(type == TextBasedSmsColumns.MESSAGE_TYPE_INBOX) {
		    Intent intent = new Intent(Constants.ACTION_DATABASE_UPDATE);
		    intent.putExtra(Constants.EXTRA_SHARED_CONVERSATION_ID, convo_id);
	        mContext.sendBroadcast(intent);		
        }
        return count;    	
    }
	public void updateSharedConversation(SharedConversation shared) {
    	String id = shared.getObjectId();
    	//if the conversation exists, we are updating it
        for(SMSMessage m : shared.getMessages()) {
        	addSharedMessage(id, m, shared.getType());
        }
	}    
    public void addSharedMessage(String convo_id, SMSMessage msg, int convo_type) {
    	if(msg == null) return;
    	if(msg.getMessage() == null) return;
    	if(alreadyInserted(convo_id, msg)) return;
        ContentValues values = new ContentValues();
        values.put(KEY_ID, convo_id);
        values.put(KEY_HASHCODE , msg.hashCode());
        values.put(KEY_CONVO_TYPE, convo_type);
        values.put(TextBasedSmsColumns.DATE, msg.getDate());
        values.put(TextBasedSmsColumns.BODY, msg.getMessage());
        values.put(TextBasedSmsColumns.TYPE, msg.getType());
        values.put(TextBasedSmsColumns.ADDRESS, msg.getAddress());
        //Log.i("dbhandler","inserting type " + type);
        db.insert(TABLE_SHARED_CONVERSATION_SMS, null, values);
    }   
    private boolean alreadyInserted(String convo_id, SMSMessage msg) {
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATION_SMS, new String[] {KEY_ID}, 
                KEY_ID + "=? and " + KEY_HASHCODE + "=?",
                new String[] { convo_id, Integer.toString(msg.hashCode()) }, null, null, null, null);
        if(cursor.moveToFirst()) {
        	cursor.close();
        	return true;
        }
        cursor.close();
        return false;
	}

	public void addComment(String convo_id, Comment c) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, convo_id);
        values.put(TextBasedSmsColumns.DATE, c.getDate());
        values.put(TextBasedSmsColumns.BODY, c.getMessage());
        values.put(TextBasedSmsColumns.ADDRESS, c.getSender());
        values.put(KEY_PROPOSED, Boolean.toString(c.isProposal()));
        db.insert(TABLE_SHARED_CONVERSATION_COMMENTS, null, values);
    }    
    
    public boolean exists(String convo_id) {
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATIONS, new String[] {KEY_ID}, 
                KEY_ID + "=?",
                new String[] { convo_id }, null, null, null, null);
        if(cursor.moveToFirst()) {
        	cursor.close();
        	return true;
        }
        cursor.close();
        return false;
    }    
 
    public SharedConversation getSharedConversation(String convo_id) {
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATIONS, new String[] { KEY_ID, KEY_DATE,
                KEY_SHARED_WITH, KEY_CONVERSATION_FROM, KEY_CONVERSATION_FROM_NAME, KEY_SHARER,
                KEY_CONVO_TYPE}, 
                KEY_ID + "=?",
                new String[] { convo_id }, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) return null;
        SharedConversation shared = new SharedConversation();
        cursor.moveToFirst();
    	shared.setObjectId(convo_id);
        shared.setDate(cursor.getLong(1));
    	shared.setConfidante(cursor.getString(2));
    	shared.setOriginalRecipient(cursor.getString(3));
    	shared.setOriginalRecipientName(cursor.getString(4));
    	shared.setSharer(cursor.getString(5));
    	shared.setType(Integer.parseInt(cursor.getString(6)));
    	shared.setMessages(getMessages(convo_id));
        shared.setComments(getComments(convo_id));
        
        return shared;
    }
    
    public SharedConversation getSharedConversation(String convo_id, int messageType) {
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATIONS, new String[] { KEY_ID, KEY_DATE,
                KEY_SHARED_WITH, KEY_CONVERSATION_FROM, KEY_CONVERSATION_FROM_NAME}, 
                KEY_ID + "=? and " + KEY_CONVO_TYPE + "=?",
                new String[] { convo_id , Integer.toString(messageType)}, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) return null;
        SharedConversation shared = new SharedConversation();
        cursor.moveToFirst();
    	shared.setObjectId(convo_id);
        shared.setDate(cursor.getLong(1));
    	shared.setConfidante(cursor.getString(2));
    	shared.setOriginalRecipient(cursor.getString(3));
    	shared.setOriginalRecipientName(cursor.getString(4));
    	shared.setMessages(getMessages(convo_id));
        shared.setComments(getComments(convo_id));
        return shared;
    }
    public ArrayList<Comment> getComments(String convo_id) {
    	ArrayList<Comment> comments = new ArrayList<Comment>();
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATION_COMMENTS, null, KEY_ID + "=?",
                new String[] { convo_id }, null, null, KEY_DATE + " DESC", null);
        // looping through all rows and adding to list
        if (cursor.moveToLast()) {
            do {
            	Comment c = new Comment(cursor.getString(1), cursor.getString(2), cursor.getLong(3));
                c.setProposal(cursor.getString(4).equals("true"));
            	comments.add(c);
            } while (cursor.moveToPrevious());
        }

        return comments;
    }    
    
    private TreeSet<SMSMessage> getMessages(String convo_id) {
 	
    	TreeSet<SMSMessage> messages = new TreeSet<SMSMessage>();
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATION_SMS, null, KEY_ID + "=?",
                new String[] { convo_id }, null, null, KEY_DATE , null);  
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	SMSMessage m = new SMSMessage();
            	m.setDate(cursor.getLong(1));
            	m.setMessage(cursor.getString(2));
            	m.setSentByMe(cursor.getInt(3)==TextBasedSmsColumns.MESSAGE_TYPE_SENT);
            	messages.add(m);
            } while (cursor.moveToNext());
        }

        return messages;
	}    
    
    public int addToOutbox(String recipient, String message,
			long timeScheduled, long scheduledFor) {
    	Log.i("inserting row ", "to outbox  for addres " + recipient);
	    ContentValues outboxSms = new ContentValues();
	    outboxSms.put(TextBasedSmsColumns.DATE_SENT, timeScheduled);
	    outboxSms.put(TextBasedSmsColumns.DATE, scheduledFor);
	    outboxSms.put(TextBasedSmsColumns.BODY, message);
	    outboxSms.put(TextBasedSmsColumns.ADDRESS, recipient);
        // Inserting Row
        db.insert(TABLE_OUTBOX, null, outboxSms);
        int count = this.getOutboxCount();
        return count;
    }
    
    private int getOutboxCount() {
        String countQuery = "SELECT  * FROM " + TABLE_OUTBOX;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
	}

	public Cursor getPendingMessagesCursor(String number){
        return db.query(TABLE_OUTBOX, null, 
        		TextBasedSmsColumns.ADDRESS+"=?", 
        		new String[] { number }, 
        		null, null, TextBasedSmsColumns.DATE);
    }
    
    // Getting single shared conversation by id
    public int deleteFromOutbox(long launchedOn) {
		String where = TextBasedSmsColumns.DATE_SENT + "=" + launchedOn;
        int rows_deleted = db.delete(TABLE_OUTBOX, where, null);
        return rows_deleted;
    }    
     

    public int getSharedCount(int messageType) {
        String countQuery = "SELECT  * FROM " + TABLE_SHARED_CONVERSATIONS +
        		" where " + KEY_CONVO_TYPE + "=?";
        String[] where = { Integer.toString(messageType)};
        Cursor cursor = db.rawQuery(countQuery, where);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }    
    // Getting all messages
    public List<SharedConversation> getAllSharedConversation(int messageType) {
        List<SharedConversation> sharedList = new ArrayList<SharedConversation>();
        Cursor cursor = getSharedConversationCursor(messageType);
        if (cursor.moveToLast()) {
            do {
            	SharedConversation shared = new SharedConversation();
            	String convo_id = cursor.getString(0);
            	shared.setObjectId(convo_id);
            	shared.setDate(cursor.getLong(1));
            	shared.setConfidante(cursor.getString(2));
            	shared.setOriginalRecipient(cursor.getString(3));
            	shared.setOriginalRecipientName(cursor.getString(4));
            	shared.setType(Integer.parseInt(cursor.getString(5)));
            	shared.setSharer(cursor.getString(6));
            	shared.setMessages(getMessages(convo_id));
                shared.setComments(getComments(convo_id));
                sharedList.add(shared);
            } while (cursor.moveToPrevious());
        }
        return sharedList;
    }
    // Getting all messages
    public Cursor getSharedConversationCursor(int messageType) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SHARED_CONVERSATIONS 
        		+ " where "	 + KEY_CONVO_TYPE + "=? order by " +  KEY_DATE + " desc";
        String[] where = {Integer.toString(messageType)};
        Cursor cursor = db.rawQuery(selectQuery, where);
        return cursor;
    }
    
    // Getting all messages
    public Cursor getSharedConversationCursor(int messageType, String columns) {
        // Select All Query
        String selectQuery = "SELECT " + columns + " FROM " + TABLE_SHARED_CONVERSATIONS 
        		+ " where "	 + KEY_CONVO_TYPE + "=? order by " +  KEY_DATE + " desc";
        String[] where = {Integer.toString(messageType)};
        Cursor cursor = db.rawQuery(selectQuery, where);
        return cursor;
    }    

	public void deleteShared(SharedConversation shared) {
        db.delete(TABLE_SHARED_CONVERSATIONS, KEY_ID + " = ?",
                new String[] { shared.getObjectId() });
        db.delete(TABLE_SHARED_CONVERSATION_COMMENTS, KEY_ID + " = ?",
                new String[] { shared.getObjectId() });        
    }
    

    
    public void close() {
    	if(db.isOpen()) db.close();
    }

	public boolean contains(SharedConversation shared) {
		String id = shared.getObjectId();
		return(exists(id)) ;
	}

	public void addSharedMessages(String convoID, List<SMSMessage> message_list, int type) {
		if(contains(convoID)) for(SMSMessage m : message_list) {
			addSharedMessage(convoID, m, type);
		}
	}

	public boolean contains(String convoID) {
		return(exists(convoID)) ;
	}

	public Cursor getSharedConversationCursor(String columns) {
        // Select All Query
        String selectQuery = "SELECT " + columns + " FROM " + TABLE_SHARED_CONVERSATIONS 
        		+ " order by " +  KEY_DATE + " desc";
        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor;
	}
	
	
	//DIRTY HACK TO MAKE THIS WORK WITH MESSAGECURSORADAPTER
	//HENCE THE CREATION OF 2 TABLES WITH THE SAME SHIT OMG TERRIBLE CODE, STFU IM FAILING FAST
	//convo id is a hashcode of the 2 people talking
	public Cursor getSharedMessagesCursor(String convoID) {
        String[] variables = new String[]{"'sent' as category",
        		TextBasedSmsColumns.TYPE,TextBasedSmsColumns.BODY,
        		KEY_ID + " as " + BaseColumns._ID, 
        		TextBasedSmsColumns.ADDRESS, "1 as " + TextBasedSmsColumns.READ,
        		TextBasedSmsColumns.DATE};		
        Cursor cursor = db.query(TABLE_SHARED_CONVERSATION_SMS, variables, KEY_ID + "=?",
                new String[] { convoID }, null, null, TextBasedSmsColumns.DATE , null);   
		return cursor;
	}


 

}
