
package com.Pull.pullapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Hashtag")
public class Hashtag extends ParseObject {
	
	private int count;
	private String name;
	
    // Constructors
    public Hashtag() {
    	
    }

}
