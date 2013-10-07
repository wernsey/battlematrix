package za.co.wstoop.r100k2013;

import za.co.entelect.challenge.Direction;
import za.co.wstoop.r100k2013.imaging.Raster;

public class PFieldDriver1 implements TankDriver {
	
	PotentialField pfield;
	
	
	
	@Override
	public void drive(Tank tank, GameState state) {
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
			
			tank.setDirection(maxDirection);
			if(!tank.clearAhead(state)) {
				tank.fire();
			}
			
		} else {
			tank.setDirection(Direction.NONE);
		}
	}

	public PotentialField getPfield() {
		return pfield;
	}

	public void setPfield(PotentialField pfield) {
		this.pfield = pfield;
	}

}
