package com.Pull.smsTest.model;

public class ThreadItem {

	public String ID;
	public String displayName;
	public boolean read;
	public String number;
	
	public ThreadItem(String id, String name, String numbers, boolean read) {
		this.ID = id;
		this.displayName = name;
		this.number = numbers;
		this.read = read;
	}

}
