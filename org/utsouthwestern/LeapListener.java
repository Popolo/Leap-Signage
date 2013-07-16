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
	boolean canGrab;
	boolean grabbing;
	int timer;
	Vector current;
	Vector currentPos;
	float prevDist;
	
	//final float SHAKE_BOUND = 12.0f;
	final int TIMER_REQ = 300;
	final float PINCH_BOUND = 50.0f;
	final float CONVERSION = 30.0f;
	
	public void onInit(Controller leap){
		leap.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		try {
			mouse = new Robot();
			screen = leap.locatedScreens().get(0);
		    buttonUp = true;
		    currentPos = new Vector(0,0,0);
		} catch (AWTException e) {
			//break
		}
	}
	
	public void onFrame(Controller leap){
		Frame cFrame = leap.frame();
    	Pointable pointer = cFrame.pointables().frontmost();
    	
    	//Instructions dependent on presence of pointables
    	if(pointer.isValid()){
    	//	pinch(cFrame);
    	}
    	grab(cFrame);
	}
	
	void pinch(Frame frame){
		PointableList pointables = frame.pointables();
		Pointable pointer = pointables.frontmost();
		
		if (pointables.count() == 2){
			Pointable other = null;
			float dist = 900.0f;
			for (int i = 0; i < pointables.count(); i++){
				if(!pointables.get(i).equals(pointer) && pointables.count() > 1){
					other = pointables.get(i);
					dist = pointer.stabilizedTipPosition().distanceTo(other.stabilizedTipPosition());
					break;
				}
			}
			if( dist < PINCH_BOUND){
				canPinch = true;
				System.out.println("it's ture!" + dist);
			}else{
				canPinch = false;
			}
			if(!buttonUp){
				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
				System.out.print("not olives...");
				buttonUp = true;
			}
		}else if(pointables.count() == 1 && canPinch){
			if(buttonUp){
				buttonUp = false;
				mouse.mousePress(InputEvent.BUTTON1_MASK);
				System.out.print(" Olives.");
			}
		}else{
			if(!buttonUp){
				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
				System.out.print("not olives...");
				buttonUp = true;
				canPinch = false;
			}
		}
	}
	
	public void grab(Frame frame){
		PointableList pointables = frame.pointables();
		if(timer>0){timer -= 1;}
		if(pointables.count() >= 4){
			canGrab = true;
			timer = 30;
		}else if (pointables.count() == 0 && timer > 0 && canGrab){
			grabbing = true;
			canGrab = false;
		}
		if(pointables.count() > 0 && grabbing){
			grabbing = false;
		}
		if (timer == 0 && canGrab){
			canGrab = false;
		}
		if(grabbing && buttonUp){
			mouse.mousePress(InputEvent.BUTTON1_MASK);
			buttonUp = false;
		}else if(!grabbing && !buttonUp){
			mouse.mouseRelease(InputEvent.BUTTON1_MASK);
			buttonUp = true;
		}
		
		if (grabbing){
			accelerometer(currentPos,frame.hands().frontmost());
		}
	}
	
	public void accelerometer(Vector posCurrent, Hand pointer){
		Vector n = posCurrent.plus(pointer.palmVelocity());
		currentPos = n;
		System.out.println(n);
		mouse.mouseMove((int) n.getX(), (int) n.getY()); 
	}

}