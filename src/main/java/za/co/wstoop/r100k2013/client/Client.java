package za.co.wstoop.r100k2013.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import za.co.entelect.challenge.Action;
import za.co.entelect.challenge.BlockEvent;
import za.co.entelect.challenge.Board;
import za.co.entelect.challenge.Bullet;
import za.co.entelect.challenge.Challenge;
import za.co.entelect.challenge.ChallengeServiceLocator;
import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.EndOfGameException;
import za.co.entelect.challenge.Game;
import za.co.entelect.challenge.NoBlameException;
import za.co.entelect.challenge.Player;
import za.co.entelect.challenge.Point;
import za.co.entelect.challenge.Unit;
import za.co.entelect.challenge.UnitEvent;
import za.co.wstoop.r100k2013.BasicDriver;
import za.co.wstoop.r100k2013.GameState;
import za.co.wstoop.r100k2013.MyBase;
import za.co.wstoop.r100k2013.MyBullet;
import za.co.wstoop.r100k2013.StrategicPlanner;
import za.co.wstoop.r100k2013.Tank;
import za.co.wstoop.r100k2013.TankDriver;

public class Client implements Runnable {
	
	private Challenge challenge;
	
	private GameState state;
	
	private StrategicPlanner planner;
	
	private List<TickListener> tickListeners;
	
	private Thread thread;
	private volatile boolean toStop;
		
	private String name;
	
	/* Tracks which bullets belong to which tanks. */
	private BulletTracker bulletTracker;
	
	/* Maps the units' IDs to the Tank objects managing them */
	private Map<Integer, Tank> tankMap;
	
	public Client(String endpoint) throws IOException {
	
		this.name = endpoint;
				
		tickListeners = new LinkedList<TickListener>();
		
		bulletTracker = new BulletTracker();
		
		ChallengeServiceLocator cl = new ChallengeServiceLocator();
		
		tankMap = new HashMap<Integer, Tank>();
		
		try {
			//String address = "http://" + host + ":" + port + "/Challenge/ChallengeService";
			challenge = cl.getChallengePort(new URL(endpoint));
			log("Got Challenge; Logging in.");
			Board board = challenge.login();
			log("Logged in");
			
			state = new GameState(board);
			
			planner = new StrategicPlanner(state);
			
		} catch (ServiceException e) {
			throw new IOException("Unable to connect to server", e);
		} catch (NoBlameException e) {
			throw new IOException("Unable to connect to server", e);
		} catch (EndOfGameException e) {
			throw new IOException("Unable to connect to server", e);
		} catch (RemoteException e) {
			throw new IOException("Unable to connect to server", e);
		} catch (MalformedURLException e) {
			throw new IOException("Unable to connect to server", e);
		}
	}
	
	public void start() {
		log("Starting Client thread.");
		toStop = false;
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		try {
			if(toStop) return;
			log("Stopping Client thread.");
			toStop = true;
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public GameState getState() {
		return state;
	}
	
	public void addListener(TickListener listener) {
		tickListeners.add(listener);
	}

	@Override
	public void run() {
		log("Tick thread executing");
		int lastTick = -1;
				
		while (!toStop) {
			try {				
				long start = System.currentTimeMillis();
				
				log("Getting status...");
				Game game = challenge.getStatus();
				
				if (!name.equals(game.getPlayerName())) {
					log("I'm " + game.getPlayerName());
					name = game.getPlayerName();
				}

				log("Got Tick: " + game.getCurrentTick() + " (" + lastTick + ")");
				if(game.getCurrentTick() != lastTick) {
					
					if(lastTick != -1 && game.getCurrentTick() - lastTick > 1) {
						/* It is Very Important that this do not happen */
						err("HELP HELP: We seem to have skipped " + (game.getCurrentTick() - lastTick) + " ticks");
					}
					
					lastTick = game.getCurrentTick();
					
					state.setCurrentTick(lastTick);
					
					int myIndex = 0, hisIndex = 1;
					if(!game.getPlayerName().equals(game.getPlayers(0).getName())) {
						myIndex = 1;
						hisIndex = 0;
					}
					
					List<MyBullet> bullets = new LinkedList<MyBullet>();

					Player you = game.getPlayers(myIndex);		
					List<Tank> tanks = new LinkedList<Tank>();
					if (you.getUnits() != null) {
						for (Unit u : you.getUnits()) {
							// log("My tank: " + u.getId());
							Tank tank = tankMap.get(u.getId());
							if(tank == null) {
								log("Creating new tank for " + u.getId());
								tank = new Tank(u);
								tank.setDriver(new BasicDriver());
								tankMap.put(u.getId(), tank);
							} else {
								tank.setUnit(u);
							}
							tanks.add(tank);
						}
					}
					state.setYourTanks(tanks);
					
					bulletTracker.clearTankBullets(tanks);
					if (you.getBullets() != null) {
						for (Bullet b : you.getBullets()) {
							MyBullet mb = new MyBullet(b, null);
							bullets.add(mb);
							
							if(!bulletTracker.isTracked(b)) {
								log("Bullet " + b.getId() + " is new");								
								if(!bulletTracker.findBulletTank(this, b, state.getYourTanks())) {
									err("Couldn't find the tank that bullet " + b.getId() + " belongs to (YOU) !!!!!! LOL");
								}							
							}
							bulletTracker.setTankBullet(this, mb, state.getYourTanks());
						}
					}
					
					Player him = game.getPlayers(hisIndex);
					tanks = new LinkedList<Tank>();
					if (him.getUnits() != null) {
						for (Unit u : him.getUnits()) {
							// log("His tank: " + u.getId());
							Tank tank = tankMap.get(u.getId());
							if(tank == null) {
								log("Creating new tank for " + u.getId());
								tank = new Tank(u);
								tank.setDriver(new BasicDriver());
								tankMap.put(u.getId(), tank);
							} else {
								tank.setUnit(u);
							}
							tanks.add(tank);
						}
					}
					state.setHisTanks(tanks);
					
					bulletTracker.clearTankBullets(tanks);
					if (him.getBullets() != null) {
						for (Bullet b : him.getBullets()) {
							MyBullet mb = new MyBullet(b, null);
							bullets.add(mb);
							
							if(!bulletTracker.isTracked(b)) {
								log("Bullet " + b.getId() + " is new");								
								if(!bulletTracker.findBulletTank(this, b, state.getHisTanks())) {
									err("Couldn't find the tank that bullet " + b.getId() + " belongs to (HIM) !!!!!! LOL");
								}							
							}
							bulletTracker.setTankBullet(this, mb, state.getHisTanks());
						}
					}
					
					state.setBullets(bullets);
					
					MyBase base = new MyBase(you.getBase());
					state.setYourBase(base);
					
					base = new MyBase(him.getBase());
					state.setHisBase(base);
										
					/* Events */
					if (game.getEvents() != null) {
						if (game.getEvents().getBlockEvents() != null) {
							for (BlockEvent e : game.getEvents().getBlockEvents()) {
								Point p = e.getPoint();
								state.setState(p.getX(), p.getY(), e.getNewState().getValue());
							}
						}
						/* UnitEvents don't work.
						if (game.getEvents().getUnitEvents() != null) {
						...
						}
						*/
					}

				} else {
					err("No tick elapsed.");
					try {					
						Thread.sleep(100);						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					continue;
				}
				
				planner.plan(state);
				
				for(TickListener listener : tickListeners) {
					listener.tickAction(state);					
				}
				
				for(Tank tank : state.getYourTanks()) {
					TankDriver driver = tank.getDriver();
					driver.drive(tank, state);
					
					Action action = tank.getAction(); 
					log("Drove Tank " + tank.getId() + ": " + action);
					
					challenge.setAction(tank.getId(), action);
				}
				
				/* Wait for the next tick... */
				long elapsed = System.currentTimeMillis() - start;				
				try {					
					/* Probable bug in the test harness that game.getMillisecondsToNextTick() ends up being negative */
					long nextTick = game.getMillisecondsToNextTick();
					long waitFor = nextTick - elapsed;
					if(waitFor <= 0) {
						err("We took " + elapsed + "ms to make our move, but only had " + nextTick + "ms");
						waitFor = 0;
					}
					
					waitFor += 500;
					
					log("Sleeping for " + waitFor + "ms");
					Thread.sleep(waitFor);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} catch (RemoteException e) {
				err("Remote exception: " + e.getMessage());
				e.printStackTrace();
				break;
			} catch (Exception e) {
				err("Unexpected exception: " + e.getMessage());
				e.printStackTrace();
				break;
			}
		}
		toStop = true;
		log("Tick thread stopped");
	}
	
	public static void main(String[] args) {
		try {
			Client client = new Client("http://127.0.0.1:7070/Challenge/ChallengeService");			
			client.log("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void log(String message) {
		System.out.println(name + "> " + message);
	}
	
	public void err(String message) {
		System.err.println(name + "> " + message);
	}
}
