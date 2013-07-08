package org.utsouthwestern;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

import com.leapmotion.leap.*;

@SuppressWarnings("unused")
public class LeapListener extends Listener {
	Robot mouse;
	Screen screen;
	boolean buttonUp; //testing only, currently.
	boolean canPinch;
	int timer;
	Vector prevPos;
	
	final float SHAKE_BOUND = 12.0f;
	final int TIMER_REQ = 60;
	
	public void onInit(Controller leap){
		try {
			mouse = new Robot();
			screen = leap.locatedScreens().get(0);
			leap.enableGesture(Gesture.Type.TYPE_SWIPE);
		    //leap.enableGesture(Gesture.Type.TYPE_CIRCLE);
		    leap.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		    //leap.enableGesture(Gesture.Type.TYPE_KEY_TAP);
		    buttonUp = true;
		    prevPos = new Vector(0,0,0);
		} catch (AWTException e) {
			//break
		}
	}
	
	public void onFrame(Controller leap){
		Frame cFrame = leap.frame();
    	Pointable pointer = cFrame.pointables().get(0);
    	
    	//Instructions dependent on presence of pointables
    	if(pointer.isValid()){
    		Vector v = mouseSet(pointer);
    		readGestures(cFrame.gestures(),pointer);
    		if(pointClick(v)){
    			
    		}
    		//funge(cFrame.pointables());
    	}
	}
	
	private void readGestures(GestureList gestures, Pointable main){
		for(int i=0;i<gestures.count();i++){
			Gesture current = gestures.get(i);
			switch(current.type()){
			case TYPE_SCREEN_TAP:
				mouse.mousePress(InputEvent.BUTTON1_MASK);
				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
				System.out.println("touch");
				break;
			case TYPE_SWIPE:
				if(buttonUp){
					mouse.mousePress(InputEvent.BUTTON1_MASK);
					buttonUp = false;
					System.out.println("swipe");
				}
				break;
			default:
				if(!buttonUp){
					mouse.mouseRelease(InputEvent.BUTTON1_MASK);
				}
				break;
			}
		}
		if(gestures.count() == 0 && !buttonUp){
			mouse.mouseRelease(InputEvent.BUTTON1_MASK);
			buttonUp = true;
		}
	}
	
	private Vector mouseSet(Pointable pointer){
		Vector v = screen.intersect(pointer, true);
		int x = (int) (v.getX() * screen.widthPixels());
		int y = 1080 - (int) (v.getY() * screen.heightPixels());
		mouse.mouseMove(x, y);
		Vector i = new Vector(x,y,0);
		return i;
	}
	
	private void funge(PointableList purnt){
		if(purnt.count()>1 && !buttonUp){
			mouse.mousePress(InputEvent.BUTTON1_MASK);
			buttonUp = true;
		}else if(purnt.count()<=1 && buttonUp){
			mouse.mouseRelease(InputEvent.BUTTON1_MASK);
			buttonUp = false;
		}
	}
	
	private boolean pinchability(float distPrev){
		
		return false;
	}
	
	private boolean pointClick(Vector current){
		if(current.distanceTo(prevPos) > SHAKE_BOUND){
			timer = 0;
			prevPos = current;
			return false;
		}else{
			prevPos = current.plus(prevPos).divide(2);
			timer++;
			if(timer > TIMER_REQ){
				return true;
			}
			return false;
		}
	}
}