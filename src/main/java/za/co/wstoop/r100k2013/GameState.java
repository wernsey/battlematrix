package za.co.wstoop.r100k2013;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import za.co.entelect.challenge.Board;
import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.Point;
import za.co.entelect.challenge.State;
import za.co.entelect.challenge.StateArray;

public class GameState {
	private int width, height;

	private State[] cells;

	private List<Tank> yourTanks;
	private List<Tank> hisTanks;
	
	private List<MyBullet> bullets;

	private MyBase yourBase;
	private MyBase hisBase;
	
	private Map<Tank, Direction> previousDirections;

	private int boardHash;	
	
	private int currentTick;
	
	public GameState(int width, int height) {
		this.width = width;
		this.height = height;
		cells = new State[width * height];
		
		bullets = new LinkedList<MyBullet>();

		yourTanks = new ArrayList<Tank>(2);
		hisTanks = new ArrayList<Tank>(2);
		
		previousDirections = new HashMap<Tank, Direction>();
		
		currentTick = 1;
	}
	
	public GameState(Board board) {
		width = board.getStates().length;
		height = board.getStates(0).getItem().length;
		
		System.out.println("Board dimensions: " + width + " x " + height);

		cells = new State[width * height];
		
		bullets = new LinkedList<MyBullet>();
		
		yourTanks = new ArrayList<Tank>(2);
		hisTanks = new ArrayList<Tank>(2);

		previousDirections = new HashMap<Tank, Direction>();
		
		currentTick = 1;

		for (int i = 0; i < width; i++) {
			StateArray stateArray = board.getStates()[i];
			for (int j = 0; j < height; j++) {
				setState(i, j, stateArray.getItem(j).getValue());
			}
		}
		
		computeBoardHash();
	}

	public String getState(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return State._OUT_OF_BOUNDS;
		}

		State state = cells[y * width + x];
		if (state == null) {
			return State._NONE;
		}

		return state.getValue();
	}

	public void setState(int x, int y, String state) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return;
		}
		cells[y * width + x] = State.fromString(state);
	}

	public boolean isOccupied(int x, int y) {
		if (getState(x, y) != State._EMPTY) {
			return true;
		}

		for (Tank tank : yourTanks) {
			if (x >= tank.getX() - 2 && x <= tank.getX() + 2 && y >= tank.getY() - 2 && y <= tank.getY() + 2)
				return true;
		}
		for (Tank tank : hisTanks) {
			if (x >= tank.getX() - 2 && x <= tank.getX() + 2 && y >= tank.getY() - 2 && y <= tank.getY() + 2)
				return true;
		}
		return false;
	}

	public void addYourTank(Tank tank) {
		yourTanks.add(tank);
	}

	public void addHisTank(Tank tank) {
		hisTanks.add(tank);
	}

	public List<Tank> getYourTanks() {
		return yourTanks;
	}

	public List<Tank> getHisTanks() {
		return hisTanks;
	}

	private void computeBoardHash() {
		boardHash = 0;
		for(int i = 0; i < cells.length; i++) {
			if(!cells[i].equals(State.EMPTY)) {
				boardHash = boardHash * 31 + i;
			}
		}
	}
	
	public static GameState fromShowFile(String filename) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(filename));

		String line = reader.readLine();
		String[] dims = line.split("\\s+");
		if (dims.length < 2) {
			reader.close();
			throw new IOException("Invalid dimensions in showfile");
		}
		int width = Integer.parseInt(dims[0]);
		int height = Integer.parseInt(dims[1]);
		
		int ppc = 5;
		if (dims.length > 2) {
			ppc = Integer.parseInt(dims[2]);
		}

		// System.out.println("Dimensions: " + (width * ppc) + "x" + (height * ppc));

		GameState state = new GameState(width * ppc, height * ppc);

		int id = 0;
		try {
			for (int y = 0; y < height; y++) {
				line = reader.readLine();
				// System.out.println("Line: [" + line + "]");
				for (int x = 0; x < width; x++) {
					char c = line.charAt(x);
					String v = State._NONE;

					switch (c) {
					case ' ':
						v = State._EMPTY;
						break;
					case 'F':
						v = State._FULL;
						break;
					case 'O':
						v = State._OUT_OF_BOUNDS;
						break;
					case 'N':
						v = State._NONE;
						break;
					case 'A': {
						v = State._FULL;
						MyBase base = new MyBase(x * ppc + ppc/2, y * ppc + ppc/2);
						state.setYourBase(base);
					}
						break;
					case 'B': {
						v = State._FULL;
						MyBase base = new MyBase(x * ppc + ppc/2, y * ppc + ppc/2);
						state.setHisBase(base);
					}
						break;
					case '1': {
						v = State._EMPTY;
						Tank tank = new Tank(x * ppc + ppc/2, y * ppc + ppc/2);
						tank.setDriver(new BasicDriver());
						tank.setId(id++);
						state.addYourTank(tank);
					}
						break;
					case '2': {
						v = State._EMPTY;
						Tank tank = new Tank(x * ppc + ppc/2, y * ppc + ppc/2);
						tank.setDriver(new BasicDriver());
						tank.setId(id++);
						state.addHisTank(tank);
					}
						break;
					}

					for (int j = y * ppc; j < (y + 1) * ppc; j++) {
						for (int i = x * ppc; i < (x + 1) * ppc; i++) {
							state.setState(i, j, v);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new IOException("Invalid file format", e);
		} finally {
			reader.close();
		}

		state.computeBoardHash();
		
		return state;
	}

	/* FIXME
	public void toShowFile(String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		StringBuilder sb = new StringBuilder();
		sb.append(getWidth() + " " + getHeight() + " 1\n");
		for(int y = 0; y < getHeight(); y++) {
			for(int x = 0; x < getWidth(); x++){
				//sb.append();
			}
			sb.append("\n");
		}
		fw.write(sb.toString());
		fw.flush();
	}
	*/
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public MyBase getYourBase() {
		return yourBase;
	}

	public void setYourBase(MyBase yourBase) {
		this.yourBase = yourBase;
	}

	public MyBase getHisBase() {
		return hisBase;
	}

	public void setHisBase(MyBase hisBase) {
		this.hisBase = hisBase;
	}
	
	private boolean checkBullet(MyBullet bullet) {
		
		int x = bullet.getX();
		int y = bullet.getY();
		if(this.getState(x, y) != State._EMPTY) {
			if(bullet.getDirection() == Direction.LEFT || bullet.getDirection() == Direction.RIGHT) {
				for(int i = -2; i <= 2; i++) {
					setState(x, y + i, State._EMPTY);
				}
			} else {
				for(int i = -2; i <= 2; i++) {
					setState(x + i, y, State._EMPTY);
				}
			}			
			return false;
		}
		return true;
	}
	
	private boolean moveBullet(MyBullet bullet) {
		if(!bullet.isAlive()) { return false; }
		
		if(!checkBullet(bullet)) return false;
		
		bullet.update();
		
		if(!checkBullet(bullet)) return false;
		
		int x = bullet.getX();
		int y = bullet.getY();
		
		for(Tank tank : yourTanks) {
			if(tank.getX() - 2 <= x && tank.getX() + 2 >= x
					&& tank.getY() - 2 <= y && tank.getY() + 2 >= y) {
				tank.setAlive(false);
				return false;
			}
		}
		for(Tank tank : hisTanks) {
			if(tank.getX() - 2 <= x && tank.getX() + 2 >= x
					&& tank.getY() - 2 <= y && tank.getY() + 2 >= y) {
				tank.setAlive(false);
				return false;
			}
		}
		
		for(MyBullet otherBullet : bullets) {
			if(bullet == otherBullet) {
				continue;
			}
			if(x == otherBullet.getX() &&
					y == otherBullet.getY()) {
				otherBullet.setAlive(false);
				return false;
			}
		}
		
		return true;
	}

	public void update() {
		
		currentTick++;

		//System.out.println("Bullets in play: " + bullets.size());
		List<MyBullet> remain = new LinkedList<MyBullet>();
		for (MyBullet bullet : bullets) {
			if (moveBullet(bullet) && moveBullet(bullet)) {
				remain.add(bullet);
			} else {
				/* Tell the tank it may fire again */
				Tank owner = bullet.getOwner();
				owner.setBullet(null);
			}
		}
		bullets = remain;
		
		List<Tank> remainingTanks = new LinkedList<Tank>();		
		for (Tank tank : yourTanks) {
			if(!tank.isAlive()) {continue;}
			TankDriver driver = tank.getDriver();
			if(driver != null)
				driver.drive(tank, this);
				
			tank.update(this, previousDirections.get(tank));
			previousDirections.put(tank, tank.getDirection());
			
			remainingTanks.add(tank);
		}
		yourTanks = remainingTanks;
		
		remainingTanks = new LinkedList<Tank>(); 
		for (Tank tank : hisTanks) {
			if(!tank.isAlive()) continue;
			TankDriver driver = tank.getDriver();
			if(driver != null)
				driver.drive(tank, this);
			
			tank.update(this, previousDirections.get(tank));
			previousDirections.put(tank, tank.getDirection());
			
			remainingTanks.add(tank);
		}
		hisTanks = remainingTanks;
	}
	
	public List<MyBullet> getBullets() {
		return bullets;
		
	}

	public void addBullet(MyBullet bullet) {
		bullets.add(bullet);
	}

	public boolean isEmpty(int x, int y) {
		return getState(x, y) == State._EMPTY;
	}
	
	public boolean isOpen(Point p) {
		return isOpen(p.getX(), p.getY());
	}
	
	public boolean isOpen(int x, int y) {
		for(int i = x - 2; i <= x + 2; i++) {
			for(int j = y - 2; j <= y + 2; j++) {
				if(getState(i, j) != State._EMPTY)
					return false;
			}
		}
		return true;
	}

	public void setYourTanks(List<Tank> yourTanks) {
		this.yourTanks = yourTanks;
	}

	public void setHisTanks(List<Tank> hisTanks) {
		this.hisTanks = hisTanks;
	}

	public void setBullets(List<MyBullet> bullets) {
		this.bullets = bullets;
	}

	public int getBoardHash() {
		return boardHash;
	}

	public int getCurrentTick() {
		return currentTick;
	}

	public void setCurrentTick(int currentTick) {
		this.currentTick = currentTick;
	}
}
