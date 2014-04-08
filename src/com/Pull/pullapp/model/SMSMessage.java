/*
 * This file is part of No Stranger SMS.
 *
 * No Stranger SMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * No Stranger SMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with No Stranger SMS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.Pull.pullapp.model;

import android.provider.Telephony.TextBasedSmsColumns;

public class SMSMessage {
	private int smsId;
	long smsDate;
	private String smsSender, smsMessage, smsRecipient;
    public boolean sentByMe, box, isDelayed;
	public long futureSendTime;
	public long launchedOn;
	private int hashtagID = -1;
    
    // Constructors
    public SMSMessage() {
    }

    public SMSMessage(String message, boolean sentByMe) {
        this.smsMessage = message;
        this.sentByMe = sentByMe;
    }
    
    public SMSMessage(String message, String sender, boolean sentByMe) {
        this.smsMessage = message;
        this.smsSender = sender;
        this.sentByMe = sentByMe;
    }    
    public SMSMessage(String message, String sender, int type) {
        this.smsMessage = message;
        this.smsSender = sender;
        this.sentByMe = (type != TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
        this.isDelayed = (type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);
    }  
    
    public SMSMessage(int id, long date, String sender, String recipient, String message) {
        this.smsId = id;
        this.smsDate = date;
        this.smsSender = sender;
        this.smsRecipient = recipient;
        this.smsMessage = message;
    }

    public SMSMessage(long date, String message, String sender, int type) {
    	this.smsDate = date;
        this.smsSender = sender;
        this.smsMessage = message;
        this.sentByMe = (type != TextBasedSmsColumns.MESSAGE_TYPE_INBOX);
        this.isDelayed = (type == TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);
    }

    // Id
    public int getId() {
        return this.smsId;
    }

    public void setId(int id) {
        this.smsId = id;
    }

    // Date
    public long getDate() {
    	return this.smsDate;
    }

    public void setDate(long date) {
    	this.smsDate = date;
	}

    // Sender
    public String getSender() {
        return this.smsSender;
    }
     
    public void setSender(String sender){
        this.smsSender = sender;
    }
    // Sender
    public String getRecipient() {
        return this.smsRecipient;
    }
     
    public void setRecipient(String recipient){
        this.smsRecipient = recipient;
    }
    // Message text
    public String getMessage(){
        return this.smsMessage;
    }

    public void setMessage(String message){
        this.smsMessage = message;
    }
    
	public int getHashtagID() {
		return this.hashtagID;
	}

	public void setHashtagID(int indexOf) {
		this.hashtagID = indexOf;
		
	}
}
