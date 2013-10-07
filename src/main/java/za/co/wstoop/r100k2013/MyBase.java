package za.co.wstoop.r100k2013;

import za.co.entelect.challenge.Base;
import za.co.entelect.challenge.Point;

public class MyBase implements HasPosition {
	
	private static final long serialVersionUID = -961265694463480605L;
	
	private boolean friendly;
	
	private Base base;
	
	public MyBase(int x, int y) {
		this.base = new Base();
		base.setX(x);
		base.setY(y);
	}
	
	public MyBase(Base base) {
		this.base = base;
	}

	public void setFriendly(boolean friendly) {
		this.friendly = friendly;
	}

	public boolean isFriendly() {
		return friendly;
	}

	public int getX() {
		return base.getX();
	}

	public void setX(int x) {
		base.setX(x);
	}

	public int getY() {
		return base.getY();
	}

	public void setY(int y) {
		base.setY(y);
	}

	public Point getPosition() {
		return new Point(getX(), getY());
	}
}
