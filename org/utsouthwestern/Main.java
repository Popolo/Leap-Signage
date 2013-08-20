
package org.utsouthwestern;

import java.io.IOException;

import com.leapmotion.leap.Controller;
/*
 * Creates a Listener, and appends it to the leap. The other object does all the work.
 */

public class Main {	
	public static void main(String[] args){
		
		Controller leap = new Controller();
	    LeapListener listener = new LeapListener();
	    leap.addListener(listener);
	    
	    try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	   leap.removeListener(listener);
	    
	}
}