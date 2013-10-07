package za.co.wstoop.r100k2013;

import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.State;
import za.co.wstoop.r100k2013.Tank.StopLookingException;
import za.co.wstoop.r100k2013.imaging.Raster;

public class PFieldDriver2 implements TankDriver {
	
	private PotentialField pfield;
	
	private PotentialField lastField;
	
	private boolean agro = false;
	
	@Override
	public void drive(Tank tank, GameState state) {
		
		if (tank.lookFor(Direction.UP, state, new EnemyBaseSearcher()) != null) {
			if (tank.getDirection().equals(Direction.UP))
				tank.fire();
			else
				tank.setDirection(Direction.UP);
			return;
		} else if (tank.lookFor(Direction.DOWN, state, new EnemyBaseSearcher()) != null) {
			if (tank.getDirection().equals(Direction.DOWN))
				tank.fire();
			else
				tank.setDirection(Direction.DOWN);
			return;
		} else if (tank.lookFor(Direction.LEFT, state, new EnemyBaseSearcher()) != null) {
			if (tank.getDirection().equals(Direction.LEFT))
				tank.fire();
			else
				tank.setDirection(Direction.LEFT);
			return;
		} else if (tank.lookFor(Direction.RIGHT, state, new EnemyBaseSearcher()) != null) {
			if (tank.getDirection().equals(Direction.RIGHT))
				tank.fire();
			else
				tank.setDirection(Direction.RIGHT);
			return;
		}

		if (agro && tank.canFire()) {
			if (tank.lookFor(Direction.UP, state, new EnemySearcher()) != null) {
				if (tank.getDirection().equals(Direction.UP))
					tank.fire();
				else
					tank.setDirection(Direction.UP);
				return;
			} else if (tank.lookFor(Direction.DOWN, state, new EnemySearcher()) != null) {
				if (tank.getDirection().equals(Direction.DOWN))
					tank.fire();
				else
					tank.setDirection(Direction.DOWN);
				return;
			} else if (tank.lookFor(Direction.LEFT, state, new EnemySearcher()) != null) {
				if (tank.getDirection().equals(Direction.LEFT))
					tank.fire();
				else
					tank.setDirection(Direction.LEFT);
				return;
			} else if (tank.lookFor(Direction.RIGHT, state, new EnemySearcher()) != null) {
				if (tank.getDirection().equals(Direction.RIGHT))
					tank.fire();
				else
					tank.setDirection(Direction.RIGHT);
				return;
			}
		}
		
		if(pfield != null) {
			
			Raster r = pfield.getRaster();
			
			int x = tank.getX();
			int y = tank.getY();
			
			int max = Integer.MIN_VALUE;
			Direction maxDirection = Direction.NONE;
			
			int v = r.getb(x, y - 1);
			if(v > max) {
				max = v;
				maxDirection = Direction.UP;
			}
			v = r.getb(x, y + 1);
			if(v > max) {
				max = v;
				maxDirection = Direction.DOWN;
			}
			v = r.getb(x - 1, y);
			if(v > max) {
				max = v;
				maxDirection = Direction.LEFT;
			}
			v = r.getb(x + 1, y);
			if(v > max) {
				max = v;
				maxDirection = Direction.RIGHT;
			}
			
			if(tank.canFire() && lookForEnemyTanks(tank, state) != null) {
				tank.fire();
			} else if(tank.getDirection().equals(maxDirection) && !tank.clearAhead(state) && tank.canFire()) {
				fireIfReady(tank, state);
			} else {
				tank.setDirection(maxDirection);
			}
			
		} else {
			tank.setDirection(Direction.NONE);
		}
	}
		
	private class EnemySearcher implements Tank.CellExaminer {		
		
		public Object lookAtCell(Tank tank, GameState state, int x, int y) throws StopLookingException {
			
			MyBase b = state.getHisBase();
			if(b.getX() == x && b.getY() == y)
				return b;
			
			for(Tank t : state.getHisTanks()) {
				if(x >= t.getX() - 2 && x <= t.getX() + 2 
						&& y >= t.getY() - 2 && y <= t.getY() + 2) {
					return t;
				}
			}
			
			if(state.isOccupied(x, y)) /* Found something else? */
				throw new StopLookingException();
			
			return null;
		}
		
	}

	private class EnemyBaseSearcher implements Tank.CellExaminer {		
		
		public Object lookAtCell(Tank tank, GameState state, int x, int y) throws StopLookingException {
			
			MyBase b = state.getHisBase();
			if(b.getX() == x && b.getY() == y)
				return b;			
			if(state.isOccupied(x, y)) /* Found something else? */
				throw new StopLookingException();
			
			return null;
		}
		
	}
	
	private Tank lookForEnemyTanks(Tank tank, GameState state) {
		
		Object o = tank.lookAheadFor(state, new EnemySearcher());
		
		return (Tank)o;
	}
	
	public boolean lookForFriendlies(Tank tank, GameState state) {
		
		Object o = tank.lookAheadFor(state, new Tank.CellExaminer() {		
			
			public Object lookAtCell(Tank tank, GameState state, int x, int y) throws StopLookingException {
				
				MyBase b = state.getYourBase();
				if(b.getX() == x && b.getY() == y)
					return b;
				
				for(Tank t : state.getYourTanks()) {
					if(x >= t.getX() - 2 && x <= t.getX() + 2 
							&& y >= t.getY() - 2 && y <= t.getY() + 2) {
						return t;
					}
				}
				
				if(state.isOccupied(x, y))
					throw new StopLookingException();
								
				return null;
			}
			
		});
		
		return o != null;
	}
	
	private void fireIfReady(Tank tank, GameState state) {
		
		/* Don't fire if there are friendlies in your way */
		if(lookForFriendlies(tank, state))
			return;
		
		tank.fire();
	}

	public PotentialField getPfield() {
		return pfield;
	}
	
	public PotentialField getLastField() {
		return lastField;
	}

	public void setPfield(PotentialField pfield) {
		this.lastField = this.pfield;
		this.pfield = pfield;
	}

	public boolean isAgro() {
		return agro;
	}

	public void setAgro(boolean agro) {
		this.agro = agro;
	}

}
