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
	final float PINCH_BOUND = 50.0f;
	
	public void onInit(Controller leap){
		leap.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		try {
			mouse = new Robot();
			screen = leap.locatedScreens().get(0);
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
    		//Vector v = mouseFind(pointer);
    		//Click if the user has hovered long enough.
    		//pointClick2(pointClick(v));
    		
    		pinch(cFrame);
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
	
	public void grab(){
		
	}
}