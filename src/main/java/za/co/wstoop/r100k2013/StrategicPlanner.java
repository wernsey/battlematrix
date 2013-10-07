package za.co.wstoop.r100k2013;

import java.util.List;

import za.co.entelect.challenge.Action;
import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.Point;
import za.co.wstoop.r100k2013.imaging.Raster;

public class StrategicPlanner {
	
	private int board;

	private boolean forcePlanA;
	
	private static PFieldDriver2 dummyDriver = new PFieldDriver2();
	
	public StrategicPlanner(GameState state) {
		board = state.getBoardHash();
		System.out.println("Board hash = " + board);
	}
	
	/* Manhattan distance */
	private int distance(HasPosition d1, HasPosition d2) {
		return Math.abs(d1.getX() - d2.getX()) + Math.abs(d1.getY() - d2.getY());
	}
	
	/* Does some standard stuff on the potential fields */
	private void PFieldStandard(PotentialField pf, GameState state) {
		pf.subtractWalls(state, 1);
		
		pf.subtractEnemyPositions(state, 18);

		pf.subtractBullets(state, 24, 2);

		MyBase b = state.getYourBase();
		pf.ownBaseOffLimits(b.getX(), b.getY());
	}
	
	public void plan(GameState state) {
		if(!forcePlanA) {
			// Consider special plans for special boards.
			
			if (board == -2033906928 || board == -658290236) {
				planZ(state);
				return;			
			}
			// -2033906928 = navigation.txt
			// -658290236 = board-center-counter.txt
			// -1706931086 = board.txt
			// 923634960 = board-maze-warfare.txt
			// -458951248 = board-optical-illusion.txt
			// 1130430864 = lattice.txt
			// -2082741318 = original.txt
			// 923634960 = board-maze-warfare.txt
		}
		planA(state);
	}
	
	public void planA(GameState state) {
		List<Tank> tanks = state.getYourTanks();
		
		for(Tank tank : tanks) {
			if(!(tank.getDriver() instanceof PFieldDriver2)) {
				System.out.println("Creating PFieldDriver2 driver for tank " + tank.getId());
				TankDriver driver = new PFieldDriver2();
				tank.setDriver(driver);
			}
		}
		
		Fuzzy startOfGame = new Fuzzy(1.0 - (double)state.getCurrentTick()/50.0);
		//System.out.println("Following Plan A: " + startOfGame);
		
		if(tanks.size() == 0) {
			/* Eh? We lost... */
			return;
		} else if(tanks.size() == 1) {
			
			Tank tank = tanks.get(0);
			
			
			PFieldDriver2 d;
			if (tank.getDriver() instanceof PFieldDriver2)
				d = (PFieldDriver2) tank.getDriver();
			else
				d = dummyDriver;

			d.setAgro(true);
						
			PotentialField pf = new PotentialField(state);
			
			MyBase b = state.getHisBase();
			
			Point goal = b.getPosition();
			
			if(board == -1706931086) {
				if(tank.getY() == state.getHisBase().getY() && tank.getX() == 32) {
					if(tank.getDriver() instanceof StaticDriver) {
						StaticDriver sd = (StaticDriver)tank.getDriver();
						sd.setAction(Action.FIRE);
					} else {
						StaticDriver sd = new StaticDriver();
						if(tank.getDirection().equals(Direction.RIGHT))
							sd.setAction(Action.FIRE);
						else
							sd.setAction(Action.RIGHT);
						tank.setDriver(sd);
					}
				}				
			} else if (board == -458951248) {
				// optical illusion map	has some special needs...	
				int x = Math.abs(tank.getX() - 16) < Math.abs(tank.getX() - 64)? 16 : 64;
				int y = (state.getHisBase().getY() < state.getYourBase().getY())? 32 : 48;
				if (!state.isOpen(x, y)) {
					goal = new Point(x, y);
				}				
			}
			
			if(!tankStuck(tank, state, goal)) {
				pf.addGoal(b.getX(), b.getY());	
				pf.computeGoals(1);
			} else {
				if(!pf.spillRoughly(state, b.getX(), b.getY(), tank)){
					if(tank.canFire() && !d.lookForFriendlies(tank, state))
						tank.fire();
					//else
					//	pf = d.getLastField();
				}
			}
			
			PFieldStandard(pf, state);			
			
			d.setPfield(pf);	
			
		} else {
			/* We still have both our tanks */
			int i;
			
			Tank [] tank = new Tank[2];
			PotentialField pf[] = new PotentialField[2];
			PFieldDriver2 d [] = new PFieldDriver2[2];
			
			for(i = 0; i < 2; i++){
				tank[i] = tanks.get(i);
				pf[i] = new PotentialField(state);
				
				if(tank[i].getDriver() instanceof PFieldDriver2)
				d[i] = (PFieldDriver2)tank[i].getDriver();
				else
					d[i] = dummyDriver;
			}
			
			MyBase b = state.getHisBase();
			
			Point [] goals = new Point[2];
			goals[0] = new Point(b.getX(), b.getY());			
			goals[1] = new Point(b.getX(), b.getY());
			
			List<Tank> baddies = state.getHisTanks(); 
			
			if (startOfGame.somewhat().truth() && board == 1130430864) {
				// The lattice board.
				// Move the tanks to the centre of the arena at the start.				
				goals[0] = new Point(10, 40);
				goals[1] = new Point(70, 40);
				d[0].setAgro(true);
				d[1].setAgro(true);
			} else if(startOfGame.somewhat().truth() && board == 923634960) {	
				// board-maze-warfare.txt
				goals[0] = new Point(20, 40);
				goals[1] = new Point(60, 40);
				d[0].setAgro(true);
				d[1].setAgro(true);
			} else if (board == -1706931086 && !state.isOpen(14,40)  ) {
				// board.txt - Destroy the piece of wall at (14,40) that 
				// baffles my potential fields
				goals[0] = new Point(14,40);
				goals[1] = new Point(57, 40);
				d[0].setAgro(true);
				d[1].setAgro(true);
			} else {
				if (baddies.size() == 2) {
					Tank baddie1 = baddies.get(0);
					Tank baddie2 = baddies.get(1);

					for (int j = 0; j < 2; j++) {
						Fuzzy baddie1far = new Fuzzy((double) distance(tank[j], baddie1) / ((state.getWidth() + state.getHeight()) / 2));
						Fuzzy baddie2far = new Fuzzy((double) distance(tank[j], baddie2) / ((state.getWidth() + state.getHeight()) / 2));

						Fuzzy baddie1close = baddie1far.not();
						Fuzzy baddie2close = baddie2far.not();

						if (baddie1close.very().or(baddie2close.very()).truth()) {
							System.out.println("Tank " + j + " is now agro. Roar!");
							d[j].setAgro(true);
						} else {
							d[j].setAgro(false);
						}
					}

					if (board != 923634960) {
						if (distance(tank[0], b) < distance(tank[0], baddie1) && distance(tank[0], b) < distance(tank[0], baddie2))
							goals[0] = new Point(b.getX(), b.getY());
						else if (distance(tank[0], baddie1) < distance(tank[0], baddie2))
							goals[0] = new Point(baddie1.getX(), baddie1.getY());
						else
							goals[0] = new Point(baddie2.getX(), baddie2.getY());

						if (distance(tank[1], b) < distance(tank[1], baddie1) && distance(tank[1], b) < distance(tank[1], baddie2))
							goals[1] = new Point(b.getX(), b.getY());
						else if (distance(tank[1], baddie1) < distance(tank[1], baddie2))
							goals[1] = new Point(baddie1.getX(), baddie1.getY());
						else
							goals[1] = new Point(baddie2.getX(), baddie2.getY());
					} else {
						// on the board-maze-warfare.txt map, it's better to hunt the baddies down.
						if (distance(tank[0], baddie1) < distance(tank[0], baddie2))
							goals[0] = new Point(baddie1.getX(), baddie1.getY());
						else
							goals[0] = new Point(baddie2.getX(), baddie2.getY());
						if (distance(tank[1], baddie1) < distance(tank[1], baddie2))
							goals[1] = new Point(baddie1.getX(), baddie1.getY());
						else
							goals[1] = new Point(baddie2.getX(), baddie2.getY());
					}

				} else if (baddies.size() == 1) {
					/* 2-to-1. Hunt the little fucker down... */

					Tank baddie = baddies.get(0);

					for (int j = 0; j < 2; j++) {

						Fuzzy baddieFar = new Fuzzy((double) distance(tank[j], baddie) / ((state.getWidth() + state.getHeight()) / 2));
						Fuzzy baddieClose = baddieFar.not();

						if (baddieClose.very().truth()) {
							System.out.println("Tank " + j + " is now agro. ROAR!!!");
							d[j].setAgro(true);
						} else {
							d[j].setAgro(false);
						}
					}
					
					if (distance(tank[0], b) < distance(tank[0], baddie)) {
						goals[0] = new Point(b.getX(), b.getY());
					} else {
						goals[0] = new Point(baddie.getX(), baddie.getY());
					}

					if (distance(tank[1], b) < distance(tank[1], baddie)) {
						goals[1] = new Point(b.getX(), b.getY());
					} else {
						goals[1] = new Point(baddie.getX(), baddie.getY());
					}					
				}
			}
			
			if(board == -1706931086) {
				if(tank[0].getY() == state.getHisBase().getY() && tank[0].getX() == 32) {
					// Because I couldn't convince the AI to shoot by itself in this situation,
					// I have to do it manually.
					System.out.println("Preparation H!");
					if(tank[0].getDriver() instanceof StaticDriver) {
						StaticDriver sd = (StaticDriver)tank[0].getDriver();
						sd.setAction(Action.FIRE);
					} else {
						StaticDriver sd = new StaticDriver();
						if(tank[0].getDirection().equals(Direction.RIGHT))
							sd.setAction(Action.FIRE);
						else
							sd.setAction(Action.RIGHT);
						tank[0].setDriver(sd);
					}
				}				
			} else if (board == -458951248) {
				// optical illusion map has some special needs...
				if (state.getHisBase().getY() < state.getYourBase().getY()) {
					if (!state.isOpen(16, 32)) {
						goals[0] = new Point(16, 32);
					}
					if (!state.isOpen(64, 32)) {
						goals[1] = new Point(64, 32);
					}
				} else {
					if (!state.isOpen(16, 48)) {
						goals[0] = new Point(16, 48);
					}
					if (!state.isOpen(64, 48)) {
						goals[1] = new Point(64, 48);
					}
				}
				d[0].setAgro(true);
				d[1].setAgro(true);
			}		
			
			for (i = 0; i < 2; i++) {
				if (!tankStuck(tank[i], state, goals[i])) {
					pf[i].addGoal(goals[i].getX(), goals[i].getY());
					pf[i].computeGoals(1);
				} else {
					if(!pf[i].spillRoughly(state, goals[i].getX(), goals[i].getY(), tank[i])) {
						if(tank[i].canFire() && !d[i].lookForFriendlies(tank[i], state)) {
							tank[i].fire();
							continue;
						}
						//else
						//	pf[i] = d[i].getLastField();
					}
				}
				
				PFieldStandard(pf[i], state);
				
				d[i].setPfield(pf[i]);
			}
		}
	}
	
	public void planZ(GameState state) {
		List<Tank> tanks = state.getYourTanks();
		
		if(tanks.size() == 0) return;
		
		Tank tank = tanks.get(0);

		TankDriver d = tank.getDriver();
		
		if(d instanceof BasicDriver) {
			System.out.println("Creating PFieldDriver2 driver for tank " + tank.getId());
			d = new PFieldDriver2();
			tank.setDriver(d);
		}
		
		if(tank.getX() == state.getHisBase().getX()) {
			if(!(d instanceof StaticDriver)) {
				System.out.println("Creating Static Driver");
				StaticDriver s = new StaticDriver();				
				if (state.getHisBase().getY() < tank.getY()) {
					if (!tank.getDirection().equals(Direction.UP)) {
						s.setAction(Action.UP);
					} else {
						s.setAction(Action.FIRE);
					}
				} else {
					if (!tank.getDirection().equals(Direction.DOWN)) {
						s.setAction(Action.DOWN);
					} else {
						s.setAction(Action.FIRE);
					}
				}
				tank.setDriver(s);				
			} else {
				StaticDriver s = (StaticDriver) d;
				if (tank.canFire()) {
					System.out.println("Firing firing firing");
					s.setAction(Action.FIRE);
				} else {
					s.setAction(Action.fromString(tank.getDirection().toString()));
				}
			}
		} else {
			//System.out.println("Using PFieldDriver for tank");
			int dy = 1;
			if(state.getHisBase().getY() < state.getYourBase().getY()) {
				dy = -1;
			}
			
			PotentialField pf = new PotentialField(state);
			if (board == -658290236) {
				// center counter board
				int x = state.getHisBase().getX();
				
				int y = tank.getY();
				if(Math.abs(y - state.getYourBase().getY()) < 6)
					y = state.getYourBase().getY() + 6 * dy;
				
				pf.addGoal(x + 1, y);
				pf.computeGoals();
			} else {
				// navigation board
				int x = state.getHisBase().getX();
				int y = state.getYourBase().getY();
				y += dy;
				while (state.isOpen(x, y)) {
					y += dy;
				}
				y -= dy;

				pf.spillRoughly(state, x, y, tank);
			}
		
			PFieldStandard(pf, state);
			
			((PFieldDriver2)d).setPfield(pf);
		}
		
		if(tanks.size() == 2) {
			tank = tanks.get(1);
			d = tank.getDriver();
			PFieldDriver2 pfd;
			if(!(d instanceof PFieldDriver2)) {
				System.err.println("Eh? Tank2 doesn't ");
				pfd = new PFieldDriver2();
				tank.setDriver(pfd);				
			} else {			
				pfd = (PFieldDriver2)d;
			}
			
			PotentialField pf = new PotentialField(state);
			if (board == -658290236) {
				// Go towards the (vertical) center of the board
				if(Math.abs(tank.getY() - state.getYourBase().getY()) > 36)
					pf.addGoal(48, 40);
				else
					pf.addGoal(78, 40);
			} else {
				int dy = 1;
				if(state.getHisBase().getY() < state.getYourBase().getY()) {
					dy = -1;
				}
				
				// Just protect the base...
				MyBase base = state.getYourBase();				
				if (!tankStuck(tank, state, base.getPosition())) {
					pf.addGoal(base.getX(), base.getY() + 7*dy);
					pf.computeGoals(1);
				} else {
					pf.spillRoughly(state, base.getX(), base.getY() + 7*dy, tank);
				}
			}
			pf.computeGoals();
			
			// ...with prejudice
			pfd.setAgro(true);
			
			PFieldStandard(pf, state);
			pfd.setPfield(pf);
			
		}
	}
	
	/* Yes, I know this does lots of work already done elsewhere,
	 *  but the elsewhere doesn't happen in the correct sequence,
	 *  therefore I need to do it again */
	private boolean tankStuck(Tank tank, GameState state, Point goal) {
		if (tank.getStuckCounter() > 0) {
			tank.isStuck(state);
			return true;
		}
		if (tank.isStuck(state)) {
			PotentialField pf = new PotentialField(state);
			pf.addGoal(goal.getX(), goal.getY());
			pf.computeGoals(1);

			Raster r = pf.getRaster();

			int x = tank.getX();
			int y = tank.getY();
			
			Point p = tank.nextPosition();
			
			int max = r.getb(p.getX(), p.getY());
			
			// Look in the other directions if there's a higher potential
			// open cell we can move to
			if (r.getb(x, y - 1) >= max && state.isOpen(x, y - 1)){
				return false;
			}
			if (r.getb(x, y + 1) >= max && state.isOpen(x, y + 1)) {
				return false;
			}
			if (r.getb(x - 1, y) >= max && state.isOpen(x - 1, y)) {
				return false;
			}
			if (r.getb(x + 1, y) >= max && state.isOpen(x + 1, y)) {
				return false;
			}
			
			return true;
		}
		return false;
	}
}
