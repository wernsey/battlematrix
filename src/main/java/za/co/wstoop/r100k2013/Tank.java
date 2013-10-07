package za.co.wstoop.r100k2013;

import za.co.entelect.challenge.Action;
import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.Point;
import za.co.entelect.challenge.Unit;

public class Tank implements HasPosition {
	
	private MyBullet bullet;
	
	private boolean fired;
	
	private TankDriver driver;
	
	private boolean alive;
	
	private Unit unit;
	
	private int stuckCounter, stuckIncrement;
		
	public Tank(int x, int y) {
		this.unit = new Unit();
		setX(x);
		setY(y);
		setDirection(Direction.NONE);
		this.setBullet(null);
		this.fired = false;
		this.driver = null;
		alive = true;
		stuckCounter = 0;
		stuckIncrement = 5;
	}
	
	public Tank(Unit u) {
		this.unit = u;
		u.getDirection();
	}
	
	public Tank clone() {
		Tank it = new Tank(getX(),getY());
		it.setDirection(getDirection());
		return it;
	}
	
	public Action getAction() {
		if(fired) {
			fired = false;
			return Action.FIRE;
		}
		return Action.fromString(getDirection().getValue());
	}
	
	public boolean clearAhead(GameState game) {
		if(getDirection().equals(Direction.NONE)) {
			return false;
		}		
		if(getDirection() == Direction.UP) {
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() + i, getY() - 3))
					return false;
			}
		} else if(getDirection() == Direction.DOWN) {
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() + i, getY() + 3))
					return false;
			}		
		} else if(getDirection() == Direction.LEFT) {
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() - 3, getY() + i))
					return false;
			}					
		} else if(getDirection() == Direction.RIGHT) {	
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() + 3, getY() + i))
					return false;
			}					
		}		
		return true;
	}
	
	public Point ahead() {
		if(getDirection() == Direction.UP) {
			return new Point(getX(), getY() - 1);
		} else if(getDirection() == Direction.DOWN) {
			return new Point(getX(), getY() + 1);		
		} else if(getDirection() == Direction.LEFT) {
			return new Point(getX() - 1, getY());					
		} else if(getDirection() == Direction.RIGHT) {	
			return new Point(getX() + 1, getY());								
		}
		return new Point(getX(), getY());
	}
	
	public Point nextPosition() {
		if(getDirection() == Direction.UP) {
			return new Point(getX(), getY() - 1);
		} else if(getDirection() == Direction.DOWN) {
			return new Point(getX(), getY() + 1);		
		} else if(getDirection() == Direction.LEFT) {
			return new Point(getX() - 1, getY());					
		} else if(getDirection() == Direction.RIGHT) {	
			return new Point(getX() + 1, getY());								
		}
		return new Point(getX(), getY());
	}
	
	public boolean isStuck(GameState game) {
		
		/* You're "stuck" if the cell ahead of you is not occupied 
		 * but it's not "open" either.
		 * That means that you cannot shoot through the wall.
		 */
	
		if(stuckCounter > 0) {
			stuckCounter--;
			return true;
		}
		
		if (getDirection().equals(Direction.NONE)) {
			return false;
		}
		
		boolean stuck = false;
		if (getDirection() == Direction.UP) {
			if (game.isEmpty(getX(), getY() - 3) && !game.isOpen(getX(), getY() - 1))
				stuck = true;
		} else if (getDirection() == Direction.DOWN) {
			if (game.isEmpty(getX(), getY() + 3) && !game.isOpen(getX(), getY() + 1))
				stuck = true;
		} else if (getDirection() == Direction.LEFT) {
			if (game.isEmpty(getX() - 3, getY()) && !game.isOpen(getX() - 1, getY()))
				stuck = true;
		} else if (getDirection() == Direction.RIGHT) {
			if (game.isEmpty(getX() + 3, getY()) && !game.isOpen(getX() + 1, getY()))
				stuck = true;
		}
		if(stuck) {
			
			System.out.println(getId() + " is Stuck (" + stuckIncrement + ")");
			
			stuckCounter = stuckIncrement;
			stuckIncrement += 5;
		}
		return stuck;
	}
	
	public static class StopLookingException extends Exception {
		
	}
	
	public interface CellExaminer {
		Object lookAtCell(Tank tank, GameState state, int x, int y) throws StopLookingException;
	}
	
	public Object lookFor(Direction direction, GameState state, CellExaminer cellExaminer) {
		int dx = 0, dy = 0;
		int x = getX(), y = getY();
		
		if(direction == Direction.UP) {
			y -= 3;
			dy = -1;
		} else if(direction == Direction.DOWN) {
			y += 3;
			dy = 1;
		} else if(direction == Direction.LEFT) {
			x -= 3;
			dx = -1;
		} else if(direction == Direction.RIGHT) {	
			x += 3;
			dx = 1;
		}	
		
		while(x >= 0 && x < state.getWidth() && y >= 0 && y < state.getHeight()) {
			Object o;
			try {
				o = cellExaminer.lookAtCell(this, state, x, y);
			} catch (StopLookingException e) {
				return null;
			}
			if(o != null) {
				return o;
			}
			x += dx;
			y += dy;
		}
		
		return null;
	}
	
	public Object lookAheadFor(GameState state, CellExaminer cellExaminer) {
		return lookFor(getDirection(), state, cellExaminer);
	}
	
	public void turnLeft() {
		if(getDirection() == Direction.UP) {
			setDirection(Direction.LEFT);
		} else if(getDirection() == Direction.LEFT) {
			setDirection(Direction.DOWN);			
		} else if(getDirection() == Direction.DOWN) {
			setDirection(Direction.RIGHT);		
		} else if (getDirection() == Direction.RIGHT) {
			setDirection(Direction.UP);
		} else {
			setDirection(Direction.UP);
		}
	}
	
	public void turnRight() {
		if(getDirection() == Direction.UP) {
			setDirection(Direction.RIGHT);
		} else if(getDirection() == Direction.RIGHT) {
			setDirection(Direction.DOWN);			
		} else if(getDirection() == Direction.DOWN) {
			setDirection(Direction.LEFT);		
		} else if (getDirection() == Direction.LEFT) {
			setDirection(Direction.UP);
		} else {
			setDirection(Direction.UP);
		}
	}
	
	/*
	 * This method should ONLY be called from my test harness.
	 * In normal operation, the server should take care of
	 * updating the tanks...
	 */
	public boolean update(GameState game, Direction previousDirection) {
		
		if(getDirection() == Direction.NONE) {
			return false;
		}		
		
		if(fired) {
			this.bullet = new MyBullet(this);
			
			if(getDirection() == Direction.UP) {
				bullet.setX(getX());
				bullet.setY(getY() - 3);
			} else if(getDirection() == Direction.DOWN) {
				bullet.setX(getX());
				bullet.setY(getY() + 3);		
			} else if(getDirection() == Direction.LEFT) {
				bullet.setX(getX() - 3);
				bullet.setY(getY());					
			} else if(getDirection() == Direction.RIGHT) {	
				bullet.setX(getX() + 3);
				bullet.setY(getY());								
			}
			bullet.setDirection(getDirection());
			
			game.addBullet(bullet);
			
			fired = false;
			return true;
		}
		
		if(previousDirection != null && previousDirection != Direction.NONE && getDirection() != previousDirection) {
			return true;
		}
		if(getDirection() == Direction.UP) {
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() + i, getY() - 3))
					return false;
			}
			setY(getY() - 1);
		} else if(getDirection() == Direction.DOWN) {
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() + i, getY() + 3))
					return false;
			}
			setY(getY() + 1);			
		} else if(getDirection() == Direction.LEFT) {
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() - 3, getY() + i))
					return false;
			}
			setX(getX() - 1);						
		} else if(getDirection() == Direction.RIGHT) {	
			for(int i = -2; i <= 2; i++) {
				if(game.isOccupied(getX() + 3, getY() + i))
					return false;
			}
			setX(getX() + 1);						
		}		
		return true;
	}
	
	public void fire() {
		if(canFire())
			this.fired = true;
	}
	
	public boolean canFire() {
		return !this.fired && this.bullet == null;
	}

	public void setBullet(MyBullet bullet) {
		this.bullet = bullet;
	}

	public MyBullet getBullet() {
		return bullet;
	}

	public void setDriver(TankDriver driver) {
		this.driver = driver;
	}

	public TankDriver getDriver() {
		return driver;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public Direction getDirection() {
        return unit.getDirection();
    }
    public void setDirection(Direction direction) {
		unit.setDirection(direction);
    }
    public int getId() {
		return unit.getId();
    }
    public void setId(int id) {
		unit.setId(id);
    }
    public int getX() {
		return unit.getX();
    }
    public void setX(int x) {
		unit.setX(x);
    }
    public int getY() {
		return unit.getY();
    }
    public void setY(int y) {
		unit.setY(y);
    }

	public void setUnit(Unit u) {
		this.unit = u;
	}
	
	public Point getPosition() {
		return new Point(getX(), getY());
	}

	public int getStuckCounter() {
		return stuckCounter;
	}

	public void setStuckCounter(int stuckCounter) {
		this.stuckCounter = stuckCounter;
	}
}
