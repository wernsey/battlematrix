package za.co.wstoop.r100k2013.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.entelect.challenge.Bullet;
import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.Unit;
import za.co.wstoop.r100k2013.MyBullet;
import za.co.wstoop.r100k2013.Tank;

public class BulletTracker {
	
	private Map<Integer, Integer> bulletTankMap;
	
	public BulletTracker() {
		bulletTankMap = new HashMap<Integer, Integer>();
	}
	
	public void put(Bullet b, Tank t) {
		bulletTankMap.put(b.getId(), t.getId());
	}
	
	public int get(Bullet b) {
		return bulletTankMap.get(b.getId());
	}
	
	public boolean isTracked(Bullet b) {
		return bulletTankMap.containsKey(b.getId());
	}
	
	public void clearTankBullets(List<Tank> tanks) {
		for(Tank tank : tanks) 
			tank.setBullet(null);
	}
	
	public void setTankBullet(Client c, MyBullet mb, List<Tank> tanks) {
		if(!isTracked(mb.getBullet())) {
			c.err("Eh? Bullet " + mb.getId() + " is not tracked");
			return;
		}
		
		int id = bulletTankMap.get(mb.getId());
		for(Tank tank : tanks) {
			if(tank.getId() == id) {
				tank.setBullet(mb);
				return;
			}
		}
		c.err("Couldn't find bullet's tank (has tank been destroyed?)");
	}
	
	public boolean findBulletTank(Client c, Bullet b, List<Tank> tanks)
	{
		int x = b.getX();
		int y = b.getY();
		if(b.getDirection().equals(Direction.UP)) {
			y+=2;
		} else if(b.getDirection().equals(Direction.DOWN)) {
			y-=2;
		} else if(b.getDirection().equals(Direction.LEFT)) {
			x+=2;
		} else if(b.getDirection().equals(Direction.RIGHT)) {
			x-=2;
		}
		for(Tank tank : tanks) {
			//c.log("" + tank.getId() + ": " + tank.getX() + "," + tank.getY() + " vs " + x + "," + y);
			if(x >= tank.getX() - 2 && x <= tank.getX() + 2 &&
					y >= tank.getY() - 2 && y <= tank.getY() + 2) {
				c.log("Bullet " + b.getId() + " belongs to Tank " + tank.getId());
				put(b, tank);
				return true;
			}
		}
		return false;
	}
}
