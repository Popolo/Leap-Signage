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
	boolean canPinch;
	boolean canUnpinch;
	boolean canGrab;
	boolean grabbing;
	int timer;
	int prevenTimer;
	float prevDist;

	final int TIMER_REQ = 300;
	final float PINCH_BOUND = 50.0f;
	final float CONVERSION = 0.051f;
	final float LOW_SPEED = 5.0f;

	public void onInit(Controller leap) {
		leap.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		try {
			mouse = new Robot();
			screen = leap.locatedScreens().get(0);
			buttonUp = true;
		} catch (AWTException e) {
		}
	}

	public void onFrame(Controller leap) {
		Frame cFrame = leap.frame();
		Pointable pointer = cFrame.pointables().frontmost();
		HandList hands = cFrame.hands();

		if (hands.count() > 0) {
			if (pointer.isValid()) {
				// pinch(cFrame);
			}
			handZoom(cFrame);
			grab(cFrame);
		}
	}

	// Exactly what it says.
	void pinch(Frame frame) {
		PointableList pointables = frame.pointables();
		Pointable pointer = pointables.frontmost();
		if (prevenTimer > 0) {
			prevenTimer -= 1;
		}
		if (pointables.count() == 2) {
			Pointable other = null;
			float dist = 900.0f;
			for (int i = 0; i < pointables.count(); i++) {
				if (!pointables.get(i).equals(pointer)
						&& pointables.count() > 1) {
					other = pointables.get(i);
					dist = pointer.stabilizedTipPosition().distanceTo(
							other.stabilizedTipPosition());
					if (dist > PINCH_BOUND && canUnpinch) {
						mouse.mouseWheel(-30);
						System.out.println("DEGEELO");
						canUnpinch = false;
						prevenTimer = 30;
					}
					break;
				}
			}
			if (dist < PINCH_BOUND) {
				canPinch = true;
				canUnpinch = true;
				System.out.println("it's ture!" + dist);
			} else {
				canPinch = false;
			}
		} else if (pointables.count() == 1 && canPinch) {
			mouse.mouseWheel(30);
			canPinch = false;
			canUnpinch = true;
			System.out.println("DEGEELO~");
		} else {
			canPinch = false;
			canUnpinch = false;
		}
	}

	public void handZoom(Frame cFrame) {
		if (cFrame.hands().count() == 2 && cFrame.pointables().count() == 0) {
			HandList hands = cFrame.hands();
			float dist = hands.get(0).palmPosition().distanceTo(hands.get(1).palmPosition());
			float diff = dist - prevDist;
			prevDist = dist;
			if((diff > 2 && diff < 30) || (diff < -2 && diff > -30)){
				mouse.mouseMove(screen.widthPixels()/2,screen.heightPixels()/2);
				mouse.mouseWheel((int) diff);
				System.out.println("Zoomed: " + diff);
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
			if (pointables.count() >= 4) {
				canGrab = true;
				timer = 80;
			} else if (pointables.count() <= 1 && timer > 0 && canGrab) {
				grabbing = true;
				canGrab = false;
			}
			if (pointables.count() > 0 && grabbing) {
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
}