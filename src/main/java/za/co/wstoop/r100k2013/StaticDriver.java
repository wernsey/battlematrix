package za.co.wstoop.r100k2013;

import za.co.entelect.challenge.Action;
import za.co.entelect.challenge.Direction;

/* Driver that does exactly what the planner tells it to */
public class StaticDriver implements TankDriver {

	private Action action;
	
	public StaticDriver() {
		action = Action.NONE;
	}
		
	@Override
	public void drive(Tank tank, GameState state) {
		if(action.equals(Action.FIRE))
			tank.fire();
		else
			tank.setDirection(Direction.fromString(action.getValue()));
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
}
