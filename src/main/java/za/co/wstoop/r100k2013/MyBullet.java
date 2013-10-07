package za.co.wstoop.r100k2013;

import za.co.entelect.challenge.Bullet;
import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.Point;

public class MyBullet implements HasPosition{

	private Bullet bullit;

	private static final long serialVersionUID = 1L;

	private Tank owner;

	private boolean alive;

	public MyBullet(Tank owner) {
		bullit = new Bullet();
		this.owner = owner;
		alive = true;
	}

	public MyBullet(Bullet unit, Tank owner) {
		this.bullit = unit;
		this.owner = owner;
		alive = true;
	}
	
	public boolean update() {
		if (getDirection() == Direction.NONE) {
			return false;
		}
		if (getDirection() == Direction.UP) {
			setY(getY() - 1);
		} else if (getDirection() == Direction.DOWN) {
			setY(getY() + 1);
		} else if (getDirection() == Direction.LEFT) {
			setX(getX() - 1);
		} else if (getDirection() == Direction.RIGHT) {
			setX(getX() + 1);
		}
		return true;
	}

	public Tank getOwner() {
		return owner;
	}

	public void setOwner(Tank owner) {
		this.owner = owner;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public Direction getDirection() {
		return bullit.getDirection();
	}

	public void setDirection(Direction direction) {
		bullit.setDirection(direction);
	}

	public int getId() {
		return bullit.getId();
	}

	public void setId(int id) {
		bullit.setId(id);
	}

	public int getX() {
		return bullit.getX();
	}

	public void setX(int x) {
		bullit.setX(x);
	}

	public int getY() {
		return bullit.getY();
	}

	public void setY(int y) {
		bullit.setY(y);
	}

	public Bullet getBullet() {
		return bullit;
	}
	
	public Point getPosition() {
		return new Point(getX(), getY());
	}
}
