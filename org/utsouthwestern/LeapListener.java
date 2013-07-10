package org.utsouthwestern;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

import com.leapmotion.leap.*;

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
	final float PINCH_BOUND = 5.0f;
	
	public void onInit(Controller leap){
		try {
			mouse = new Robot();
			screen = leap.locatedScreens().get(0);
			leap.enableGesture(Gesture.Type.TYPE_SWIPE);
		    buttonUp = true;
		    prevPos = new Vector(0,0,0);
		} catch (AWTException e) {
			//break
		}
	}
	
	public void onFrame(Controller leap){
		Frame cFrame = leap.frame();
    	Pointable pointer = cFrame.pointables().frontmost();
    	
    	//Instructions dependent on presence of pointables
    	if(pointer.isValid()){
    		Vector v = mouseFind(pointer);
    		readGestures(cFrame.gestures(),pointer);
    		
    		//Click if the user has hovered long enough.
    		pointClick2(pointClick(v));
    		
    		pinch(cFrame);
    	}
	}
	
	
	private void readGestures(GestureList gestures, Pointable main){
		for(int i=0;i<gestures.count();i++){
			Gesture current = gestures.get(i);
			switch(current.type()){
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
	
	private Vector mouseFind(Pointable pointer){
		Vector v = screen.intersect(pointer, true);
		int x = (int) (v.getX() * screen.widthPixels());
		int y = 1080 - (int) (v.getY() * screen.heightPixels());
		Vector i = new Vector(x,y,0);
		return i;
	}
	
	void pinch(Frame frame){
		PointableList pointables = frame.pointables();
		Pointable pointer = pointables.frontmost();
		if (pointables.count() == 2){
			Pointable other = null;
			for (int i = 0; i < pointables.count(); i++){
				if(pointables.get(i) != pointer){
					other = pointables.get(i);
					break;
				}
			}
			if(pointer.stabilizedTipPosition().distanceTo(other.stabilizedTipPosition()) < PINCH_BOUND){
				canPinch = true;
			}else{
				canPinch = false;
			}
		}else if(pointables.count() == 1){
			if(canPinch){
				mouse.mousePress(InputEvent.BUTTON1_MASK);
			}else{
				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
			}
		}else{
			canPinch = false;
		}
	}
	
	
	
	// the hoverClicking methods.
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
	
	private void pointClick2(boolean yay){
		if (yay){
			mouse.mouseMove((int) prevPos.getX(), (int) prevPos.getY());
			mouse.mousePress(InputEvent.BUTTON1_MASK);
			mouse.mouseRelease(InputEvent.BUTTON1_MASK);
			timer = 0;
		}
	}
}