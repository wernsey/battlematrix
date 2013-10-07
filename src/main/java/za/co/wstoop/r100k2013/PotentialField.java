package za.co.wstoop.r100k2013;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.Point;
import za.co.wstoop.r100k2013.imaging.Raster;

public class PotentialField {
	private Raster raster;	
	
	private List<Point> goals;
		
	public PotentialField(GameState state) {
		raster = new Raster(state);
		goals = new LinkedList<Point>();
	}

	public Raster getRaster() {
		return raster;
	}
	
	public void addGoal(Point goal) {
		goals.add(goal);
	}
	
	public void addGoal(int x, int y) {
		goals.add(new Point(x, y));
	}

	private Raster dilateSteps(Raster next, int steps){
		Raster that = new Raster(raster.getWidth(), raster.getHeight());
		for(int i = 0; i < steps; i++) {
			next = next.dilate();
			that = that.add(next);			
		}		
		return that;
	}
	
	private Raster dilateSteps8(Raster next, int steps){
		Raster that = new Raster(raster.getWidth(), raster.getHeight());
		for(int i = 0; i < steps; i++) {
			next = next.dilate8();
			that = that.add(next);			
		}		
		return that;
	}
	
	public void computeGoals(){
		computeGoals(1);
	}
	
	public void computeGoals(int value) {
		Raster next = new Raster(raster.getWidth(), raster.getHeight());
		
		for(Point point : goals) {
			next.set(point.getX(), point.getY(), 1);
		}
		
		int steps = raster.getWidth() + raster.getHeight();
		raster = raster.add(dilateSteps(next, steps).multiply(value));
	}
	
	public void subtractWalls(GameState state, int value) {
		Raster w = (new Raster(state)).overlay(state, 1);
		
		/* Lower potential around the walls */	
		Raster n = (new Raster(state)).overlay(state, 1);
		Raster m = n.dilate8().dilate8();
		m = m.subtract(n.multiply(2));
		
		w = w.add(m);
		
		/*
		// Stay away from corners
		n = (new Raster(state)).overlay(state).morphCorners().dilate();
		m = dilateSteps8(n, 3);
		n = (new Raster(state)).overlay(state).morphEnds().dilate();
		m = m.add(dilateSteps8(n, 3));	
		
		m.subtract((new Raster(state)).overlay(state, 3));
		w = w.add(m);
		*/
		
		/** FIXME: morphSingles() doesn't work. It is supposed to 
		 * detect isolated pieces of walls
		n = (new Raster(state)).overlay(state).morphSingles();
		m = m.add(dilateSteps(n, 4));
		*/
		
		raster = raster.subtract(w.multiply(value));		
	}

	private void computeWedge(Raster next, GameState state, Direction d, int x, int y, int c) {
		if(d == Direction.LEFT) {
			computeWedgeEW(next, state, x, y, -1, c);
		} else if(d == Direction.RIGHT) {
			computeWedgeEW(next, state, x, y, +1, c);
		} else if(d == Direction.UP) {
			computeWedgeNS(next, state, x, y, -1, c);
		} else if(d == Direction.DOWN) {
			computeWedgeNS(next, state, x, y, +1, c);
		} 
	}
	
	private void computeWedgeEW(Raster next, GameState state, int x, int y, int dx, int c) {
		while (state.isEmpty(x, y) && c > 0) {
			next.set(x, y, c);
			if(state.isEmpty(x, y - 1)) next.set(x, y - 1, c - 4);
			if(state.isEmpty(x, y - 2)) next.set(x, y - 2, c - 6);
			if(state.isEmpty(x, y + 1)) next.set(x, y + 1, c - 4);
			if(state.isEmpty(x, y + 2)) next.set(x, y + 2, c - 6);
			c--;
			x += dx;
		}
	}
	private void computeWedgeNS(Raster next, GameState state, int x, int y, int dy, int c) {
		while (state.isEmpty(x, y) && c > 0) {
			next.set(x, y, c);
			if(state.isEmpty(x - 1, y)) next.set(x - 1, y, c - 4);
			if(state.isEmpty(x - 2, y)) next.set(x - 2, y, c - 6);
			if(state.isEmpty(x + 1, y)) next.set(x + 1, y, c - 4);
			if(state.isEmpty(x + 2, y)) next.set(x + 2, y, c - 6);
			c--;
			y += dy;
		}
	}
	
	public void subtractEnemyPositions(GameState state, int value) {
		Raster next = new Raster(raster.getWidth(), raster.getHeight());
		Raster other = new Raster(raster.getWidth(), raster.getHeight());
		
		for(Tank tank : state.getHisTanks()) {

			if (!tank.canFire()) {
				
				computeWedge(next, state, Direction.UP, tank.getX(), tank.getY(), value/4);
				computeWedge(next, state, Direction.DOWN, tank.getX(), tank.getY(), value/4);
				computeWedge(next, state, Direction.LEFT, tank.getX(), tank.getY(), value/4);
				computeWedge(next, state, Direction.RIGHT, tank.getX(), tank.getY(), value/4);
				
				continue;
			}
			
			other.set(tank.getX(),tank.getY(), 1);
			
			computeWedge(next, state, Direction.UP, tank.getX(), tank.getY(), (tank.getDirection() != Direction.UP)?value:value/2);
			computeWedge(next, state, Direction.DOWN, tank.getX(), tank.getY(), (tank.getDirection() != Direction.DOWN)?value:value/2);
			computeWedge(next, state, Direction.LEFT, tank.getX(), tank.getY(), (tank.getDirection() != Direction.LEFT)?value:value/2);
			computeWedge(next, state, Direction.RIGHT, tank.getX(), tank.getY(), (tank.getDirection() != Direction.RIGHT)?value:value/2);
			
		}
		raster = raster.subtract(next);
		raster = raster.subtract(dilateSteps(other, value/2));
	}
	
	/* Adds an enemy tank's position if that tank can't fire 
	 * (i.e it is vulnerable) */
	public void addEnemyPositions(GameState state, int value) {
		Raster next = new Raster(raster.getWidth(), raster.getHeight());
		
		for(Tank tank : state.getHisTanks()) {
			if (tank.canFire()) continue;
			next.set(tank.getX(),tank.getY(), 1);
		}
		raster = raster.add(dilateSteps(next, value));
	}
	
	public void addFriendlyPositions(GameState state, int value, Tank me) {
		Raster next = new Raster(raster.getWidth(), raster.getHeight());
		
		for(Tank tank : state.getYourTanks()) {
			if(tank == me) continue;
			next.set(tank.getX(), tank.getY(), 1);
		}
		
		raster = raster.add(dilateSteps(next, value));
	}

	public void ownBaseOffLimits(int x, int y) {
		int min = raster.getMin() - 1;
		for(int i = x - 4; i <= x + 4; i++) {
			for(int j = y - 4; j <= y + 4; j++) {
				int dist = Math.abs(i - x) + Math.abs(j-y);
				raster.set(i, j, min + dist);
			}
		}
	}
	
	public void subtractBullets(GameState state, int dist, int multiplier) {
		
		Raster next = new Raster(raster.getWidth(), raster.getHeight());
		for(MyBullet bullet : state.getBullets()) {
			computeWedge(next, state, bullet.getDirection(), bullet.getX(), bullet.getY(), dist);
		}
		raster = raster.subtract(next.multiply(multiplier));
	}
	
	int maxLevel;
	
	public boolean spillRoughly(GameState state, int sx, int sy, Tank t) {
		Random rand = new Random();
		int x = sx, y = sy;
		int count = 1;
		
		/* Try a couple of times to make sure we can reach the tank */
		outer:for (int i = 0; i < 3; i++) {
			while (!state.isOpen(x, y)) {
				count++;
				if (count > 20) {
					System.err.println("Couldn't spill roughly!!!");
					break outer;
				}
				x = sx + rand.nextInt(count * 2 + 1) - count;
				y = sy + rand.nextInt(count * 2 + 1) - count;				
			}
			if (spill(state, x, y, t))
				return true;
		}
		return false;
	}
	
	public boolean spill(GameState state, int sx, int sy, Tank t) {
		
		Raster ras = new Raster(state.getWidth(), state.getHeight(), Integer.MAX_VALUE);
		
		maxLevel = 0;
		boolean reach = false;
		
		spill(state, ras, sx, sy);
		
		for(int y = 0; y < ras.getHeight(); y++) {
			for(int x = 0; x < ras.getWidth(); x++) {
				
				if(x == t.getX() && y == t.getY()) {
					reach = true;
				}
				
				int p = ras.get(x, y);
				
				if(p == Integer.MAX_VALUE) 
					ras.set(x, y, 0);
				else
					ras.set(x, y, maxLevel - p);
			}
		}
		
		raster = ras;
		
		return reach;
	}

	private static class SpillPoint {
		Point p;
		Point from;
		int level;
		
		public SpillPoint(int x, int y, int level) {
			p = new Point(x, y);
			this.level = level;
		}

		public SpillPoint(int x, int y, int fx, int fy, int level) {
			p = new Point(x, y);
			from = new Point(fx, fy);	
			this.level = level;		
		}
	}
	
	private void checkCell(GameState state, Raster in, int x, int y, int fx, int fy, int level, Queue<SpillPoint> que) {
		if(x < 0 || x >= state.getWidth() || y < 0 || y >= state.getHeight())
			return;
		
		int old = in.get(x, y);
		if(old != Integer.MAX_VALUE) 
			return;

		if(!state.isEmpty(x, y))
			return;
		
		if(!state.isOpen(x,y))
			return;
		
		if(level > maxLevel) {
			maxLevel = level;
		}
		
		in.set(x, y, level);
		que.add(new SpillPoint(x, y, fx, fy, level));
	}
	
	private void spill(GameState state, Raster in, int sx, int sy) {
		
		Queue<SpillPoint> que = new LinkedList<SpillPoint>();
	
		SpillPoint sp = new SpillPoint(sx, sy, 1);
		
		que.add(sp);
		in.set(sx, sy, 1);
		
		int count = 0;
		
		while(!que.isEmpty()){
			sp = que.poll();

			int x = sp.p.getX(); 
			int y = sp.p.getY();
			
			//if(level == Integer.MAX_VALUE) throw new RuntimeException("Invalid spill pixel: " + x + " " + y);
			
			int level = in.get(x, y);
			
			if(sp.from != null) {
				int fx = sp.from.getX(); 
				int fy = sp.from.getY();			
				if(!(x-1 == fx && y == fy)) checkCell(state, in, x - 1, y, x, y, level + 1, que);
				if(!(x+1 == fx && y == fy)) checkCell(state, in, x + 1, y, x, y, level + 1, que);
				if(!(x == fx && y-1 == fy)) checkCell(state, in, x, y - 1, x, y, level + 1, que);
				if(!(x == fx && y+1 == fy)) checkCell(state, in, x, y + 1, x, y, level + 1, que);
			} else {		
				checkCell(state, in, x - 1, y, x, y, level + 1, que);
				checkCell(state, in, x + 1, y, x, y, level + 1, que);
				checkCell(state, in, x, y - 1, x, y, level + 1, que);
				checkCell(state, in, x, y + 1, x, y, level + 1, que);
			}
		}
	}
	

	/* Doesn't really work... */
	public void stuckPoints(GameState state) {
		Raster base = (new Raster(state)).overlay(state);
		
		Raster next = base.dilate8().multiply(5).blur().binary(1).not().dilate().dilate().dilate();
		
		next = next.or(base.not().morphThin().dilate());
		next = next.morphOpen();
		
		//next = dilateSteps(next, 3);
		raster = raster.add(next);
		return;
		
		/*
		Raster next = base.dilate().dilate();
		next = next.erode().erode();
		
		next = next.and(base.not());
		
		next = next.or(base.morphEnds());
		
		next = dilateSteps(next, 3);
		
		*/
		
		//next = next.and(base.not());
		
		//raster = raster.subtract(next);
	}
}
