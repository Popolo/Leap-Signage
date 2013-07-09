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
	float prevDist;
	
	final float SHAKE_BOUND = 12.0f;
	final int TIMER_REQ = 300;
	
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
    		
    		//Click if the user has hovered long enough.
    		if(pointClick(v)){
    			mouse.mouseMove((int) prevPos.getX(), (int) prevPos.getY());
    			mouse.mousePress(InputEvent.BUTTON1_MASK);
				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
    		}
    		if(cFrame.pointables().count() == 1 && pinchability(prevDist, cFrame.pointables())){
    			setPinch(cFrame);
    		}
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
	//	mouse.mouseMove(x, y);
		Vector i = new Vector(x,y,0);
		return i;
	}
	
	private void pinchTest(PointableList pointables){
		if(pointables.count()>1 && !buttonUp){
			mouse.mousePress(InputEvent.BUTTON1_MASK);
			buttonUp = true;
		}else if(pointables.count()<=1 && buttonUp){
			mouse.mouseRelease(InputEvent.BUTTON1_MASK);
			buttonUp = false;
		}
	}
	
	private boolean pinchability(float distPrev, PointableList pointables){
		float distCurrent = screen.intersect(pointables.get(0), true).distanceTo(screen.intersect(pointables.get(1), true));
		prevDist = distCurrent;
		return (distCurrent < distPrev);
	}
	
	private boolean pointClick(Vector current){
		if(current.distanceTo(prevPos) > SHAKE_BOUND){
			timer = 0;
			prevPos = current;
			return false;
		}else{
			prevPos = current.plus(prevPos).divide(2);
			timer++;
			if(timer % 50 == 0){
				System.out.println("Wait " + (6 - timer / 50));
			}
			if(timer > TIMER_REQ){
				return true;
			}
			return false;
		}
	}
	
	public void setPinch(Frame f){
		if(!(f.pointables().count() == 1)){
			canPinch = false;
		}
	}
}