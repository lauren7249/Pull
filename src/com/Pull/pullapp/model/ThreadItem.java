package com.Pull.pullapp.model;

public class ThreadItem {

	public String ID;
	public String displayName;
	public boolean read;
	public String number, snippet;
	
	public ThreadItem(String id, String name, String numbers, String snippet, boolean read) {
		this.ID = id;
		this.displayName = name;
		this.number = numbers;
		this.read = read;
		this.snippet = snippet;
	}

}
