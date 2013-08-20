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
	Frame pFrame;

	final int TIMER_REQ = 80;
	final float CONVERSION_ZOOM = 0.081f;
	final float CONVERSION_MOVE = 0.081f;
	final int LOW_BOUND = 2;
	final int HIGH_BOUND = 30;

	public void onInit(Controller leap) {
		leap.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES); // Allows the program to run in the background.
		try {
			mouse = new Robot(); //Generates the mouse control object. It will throw the AWTexception if there is no mouse interface, and will shut the program down.
					     //If it's not working, try plugging a mouse in. It shouldn't need one, but it might help the Robot get created.
			
			screen = leap.locatedScreens().get(0); // This picks up the screen, so that the leap has an idea of what to interact with.

			buttonUp = true; //If true, the mouse is NOT pressed.

			lockPosition = null; // This is used in the HandZoom, it's the position to lock to when zooming with both hands. If it's null, it won't lock.
		} catch (AWTException e) {
			System.exit(0);
		}
	}
	
	//Runs every time the leap gets a frame.
	public void onFrame(Controller leap) {
		Frame cFrame = leap.frame(); // Loads the current frame from the leap.
		
		//Synchronizes the Robot with the actual cursor position. 
		Point currentPos = MouseInfo.getPointerInfo().getLocation();
		mouse.mouseMove(currentPos.x, currentPos.y);
	
		handZoom(cFrame);
			grab(cFrame);
		
		//Sets the previousFrame to the currentFrame.
		 pFrame = cFrame;
	}

	// Limits the given vector to screen coordinates. Prevents the mouse from moving offscreen.
	// Automatically pulls from the screen object.
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
		//If there aren't two hands, free the cursor, and start emptying the overload.
		if(cFrame.hands().count() != 2){
			lockPosition = null;
			prevDist = 0;
			
		//Runs if the hands aren't open.
		}else if (cFrame.hands().count() == 2 && cFrame.pointables().count() <= 2) {
			
			//Sets the lock position to where the pointer currently is.
			if(lockPosition == null){
				lockPosition = MouseInfo.getPointerInfo().getLocation();
			}
			
			HandList hands = cFrame.hands(); //Gets the current lis of hands, and finds the distance between them.
			float dist = hands.get(0).palmPosition().distanceTo(hands.get(1).palmPosition());
			float diff = dist - prevDist; //If this is negative, the hands are moving together, if it's positive, they're moving apart.
			diff *= -1; //This inverts the zooming. Set it to *1 if you want moving hands APART to zoom OUT.
			prevDist = dist;
			
			//Ignores the hand movement if it is too small or large, or if the hands are touching.
			//Change LOW_BOUND and HIGH_BOUND to allow smaller or bigger gestures respectively.
			if(dist > 10 && ((diff > LOW_BOUND && diff < HIGH_BOUND) || (diff < (-1 * LOW_BOUND) && diff > (-1 * HIGH_BOUND)))){
				//Sets the mouse to the lock position and zooms based on the movement. Change CONVERSION_ZOOM to make the zoom more or less powerful.
				mouse.mouseMove(lockPosition.x,lockPosition.y);
				mouse.mouseWheel((int) (diff * CONVERSION_ZOOM));
				//System.out.println("Zoomed: " + diff); //Debug line.
			}
		}
	}
	
	

	// Holds the mouse button down during a grabbing motion, and releases when the motion is ended.
	// The motion has to be completed quickly in order to count.
	public void grab(Frame frame) {
		//only grabs if there is a single hand in the frame.
		if(frame.hands().count() == 1){
			PointableList pointables = frame.pointables();
			if (timer > 0) {
				timer -= 1;
			}
			
			//Allow grabbing with enough fingers initially present.
			//Change TIMER_REQ to allow more or less time for a grab.
			if (pointables.count() >= 4) {
				canGrab = true;
				timer = TIMER_REQ;
				
			//If the motion is completed quickly enough, starts grabbing.
			//Prevents grabbing multiple times at once.
			} else if (pointables.count() <= 1 && timer > 0 && canGrab) {
				grabbing = true;
				canGrab = false;
			}
			
			//IF more than one finger is present, cancels the grab.
			//Allows one finger, because the thumb can usually be detected.
			//Just makes it easier.
			if (pointables.count() > 1 && grabbing) {
				grabbing = false;
			}
			
			//If the user is TOO SLOW, prevents grabbing.
			if (timer == 0 && canGrab) {
				canGrab = false;
			}
			
			//This bit prevents the Robot from pressing the button every frame or
			//unpressing it every frame.
			if (grabbing && buttonUp) {
				mouse.mousePress(InputEvent.BUTTON1_MASK);
				buttonUp = false;
			} else if (!grabbing && !buttonUp) {
				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
				buttonUp = true;
			}
			
			//If you've made it through the forest of logic, and you're definitely grabbing, 
			//move the cursor around with the grab.
			if (grabbing) {
				grabMove(frame.hands().frontmost());
			}
		}
	}

	// Moves the pointer when grabbing.
	public void grabMove(Hand pointer) {
		//Gets the velocity of the hand and current position of the cursor.
		//Modifies the velocity so it corresoinds to points on the screen. Change CONVERSION_MOVE
		//to change the movement speed during a grab.
		Vector posCurrent = ptv(MouseInfo.getPointerInfo().getLocation());
		Vector i = pointer.palmVelocity().times(CONVERSION_MOVE);
		
		//Inverts the Y, because the leap naturally reads upside-
		//down relative to screen coordinates.
		i.setY(i.getY() * -1);
		
		//Adds the slightly modified velocity to the current position.
		Vector n = posCurrent.plus(i);
		
		//prevents the mouse from going offscreen.
		n = limit(n);
		
		//Actually moves the mouse.
		mouse.mouseMove((int) n.getX(), (int) n.getY());

		//System.out.println(pointer.palmVelocity()); //Debug Lines
		//System.out.print(n);
	}

	// Converts Points to Vectors.
	// SUCH CONVENIENCE
	private Vector ptv(Point p) {
		return new Vector(p.x, p.y, 0);
	}
}