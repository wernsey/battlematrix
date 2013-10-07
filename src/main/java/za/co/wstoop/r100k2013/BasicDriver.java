package za.co.wstoop.r100k2013;

import java.util.Random;

import za.co.entelect.challenge.Direction;

public class BasicDriver implements TankDriver {

	private static Random rand = new Random();
	
	private int turned = 0;
	
	public BasicDriver() {
	}
	
	@Override
	public void drive(Tank tank, GameState state) {
		if(tank.getDirection().equals(Direction.NONE)) {
			tank.setDirection(Direction.UP);
		}

	    int ctr = 10;
	    
		if (tank.clearAhead(state)) {
			//tank.update(state);
			if(turned > 0) turned--;
			
			if (turned == 0 && rand.nextDouble() < 0.1) {
				turned = 10;
				if (rand.nextDouble() < 0.5) {
					tank.turnLeft();
					while (!tank.clearAhead(state) && ctr-- > 0)
						tank.turnRight();
				} else {
					tank.turnRight();
					while (!tank.clearAhead(state) && ctr-- > 0)
						tank.turnLeft();
				}
			} else if(tank.canFire() && rand.nextDouble() < 0.1) {
				//System.out.println("Firing..." + r);
				tank.fire();
			}
		} else {
				if (rand.nextDouble() < 0.5) {
					while (!tank.clearAhead(state) && ctr-- > 0)
						tank.turnLeft();
				}else{
					while (!tank.clearAhead(state) && ctr-- > 0)
						tank.turnRight();
				}
		}
	}

}
