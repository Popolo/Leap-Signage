package org.utsouthwestern;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

import com.leapmotion.leap.*;

public class LeapListener extends Listener {
	Robot mouse;
	Screen screen;
	boolean buttonUp;
	boolean canGrab;
	boolean grabbing;
	int timer;
	float prevDist;
	Point lockPosition;
	int overload;
	Frame pFrame;
	boolean overloaded;

	final int TIMER_REQ = 300;
	final float CONVERSION = 0.081f;

	public void onInit(Controller leap) {
		leap.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		try {
			mouse = new Robot();
			screen = leap.locatedScreens().get(0);
			buttonUp = true;
			lockPosition = null;
			overload = 0;
		} catch (AWTException e) {
		}
	}

	public void onFrame(Controller leap) {
		Frame cFrame = leap.frame();
		Pointable pointer = cFrame.pointables().frontmost();
		HandList hands = cFrame.hands();
		Point currentPos = MouseInfo.getPointerInfo().getLocation();
		mouse.mouseMove(currentPos.x, currentPos.y);
	
		handZoom(cFrame);
		if (hands.count() > 0) {
			if (pointer.isValid()) {
			}
			grab(cFrame);
		}
		pFrame = cFrame;
	}

	// Limits the given vector to screen coordinates.
	private Vector limit(Vector v) {
		if (v.getX() < 0) {
			v.setX(0);
		} else if (v.getX() > screen.widthPixels()) {
			v.setX(screen.widthPixels());
		}
	
		if (v.getY() < 0) {
			v.setY(0);
		} else if (v.getY() > screen.heightPixels()) {
			v.setY(screen.heightPixels());
		}
	
		return v;
	}

	//Allows zooming using two hands
	public void handZoom(Frame cFrame) {
		if(overload > 50 || overloaded){
			overload -= 5;
			overloaded = (overload < 0);
			return;
		}
		if(cFrame.hands().count() < 2 || cFrame.pointables().count() > 2){
			lockPosition = null;
			overload -= 5;
		}else if (cFrame.hands().count() == 2 && cFrame.pointables().count() <= 2) {
			if(lockPosition == null){
				lockPosition = MouseInfo.getPointerInfo().getLocation();
			}
			HandList hands = cFrame.hands();
			float dist = hands.get(0).palmPosition().distanceTo(hands.get(1).palmPosition());
			float diff = dist - prevDist;
			diff *= -1;
			prevDist = dist;
			if(dist > 10 && ((diff > 2 && diff < 30) || (diff < -2 && diff > -30))){
				mouse.mouseMove(lockPosition.x,lockPosition.y);
				mouse.mouseWheel((int) (diff * CONVERSION));
				overload++;
				System.out.println("Zoomed: " + diff);
			}else{
				overload -= 5;
			}
		} else {
			prevDist = 0;
		}
	}
	
	

	// Exactly what it says.
	public void grab(Frame frame) {
		if(frame.hands().count() == 1){
			PointableList pointables = frame.pointables();
			if (timer > 0) {
				timer -= 1;
			}
			
			//Allow grabbing with enough fingers
			if (pointables.count() >= 4) {
				canGrab = true;
				timer = 80;
			} else if (pointables.count() <= 1 && timer > 0 && canGrab) {
				grabbing = true;
				canGrab = false;
			}
			if (pointables.count() > 1 && grabbing) {
				grabbing = false;
			}
			if (timer == 0 && canGrab) {
				canGrab = false;
			}
			if (grabbing && buttonUp) {
				mouse.mousePress(InputEvent.BUTTON1_MASK);
				buttonUp = false;
			} else if (!grabbing && !buttonUp) {
				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
				buttonUp = true;
			}
	
			if (grabbing) {
				grabMove(frame.hands().frontmost());
			}
		}
	}

	// Moves the pointer when grabbing.
	public void grabMove(Hand pointer) {
		
		Vector posCurrent = ptv(MouseInfo.getPointerInfo().getLocation());
		Vector i = pointer.palmVelocity().times(CONVERSION);
		i.setY(i.getY() * -1);
		Vector n = posCurrent.plus(i);
		n = limit(n);
		mouse.mouseMove((int) n.getX(), (int) n.getY());

		System.out.println(pointer.palmVelocity());
		System.out.print(n);
	}

	// Converts points to vectors.
	private Vector ptv(Point p) {
		return new Vector(p.x, p.y, 0);
	}
}