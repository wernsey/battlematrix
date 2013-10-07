package za.co.wstoop.r100k2013.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;

import javax.swing.JPanel;

import za.co.entelect.challenge.Direction;
import za.co.entelect.challenge.State;
import za.co.wstoop.r100k2013.MyBase;
import za.co.wstoop.r100k2013.MyBullet;
import za.co.wstoop.r100k2013.GameState;
import za.co.wstoop.r100k2013.Tank;

class Surface extends JPanel implements MouseListener {
	
	private static final long serialVersionUID = 1L;

	private GameState gameState;
	
	public static final int PIXELS_PER_CELL = 5;
	
	public Surface(GameState gameState) {
		super();
		this.gameState = gameState;
		
		addMouseListener(this);
	}
	
	private void putCell(Graphics2D g, int x, int y) {
		g.fillRect(x * PIXELS_PER_CELL, y * PIXELS_PER_CELL, PIXELS_PER_CELL, PIXELS_PER_CELL);
	}
	
	private void drawTank(Graphics2D g, Tank tank, Color color1, Color color2) {
		int x = tank.getX() - 2, y = tank.getY() - 2;
		
		int odd = ((x + y) % 2);
		
		g.setColor(color1);
		for(int i = 1; i <= 3; i++) {
			for(int j = 1; j <= 3; j++) {
				putCell(g, x + i, y + j);
			}
		}
		
		if(tank.getDirection() == Direction.LEFT || tank.getDirection() == Direction.RIGHT) {
			for(int i = 0; i < 5; i++) {
				if((i % 2) == odd) {
					g.setColor(color1);
				} else {
					g.setColor(color2);						
				}
				putCell(g, x + i, y);
				putCell(g, x + i, y + 4);
			}			
		} else {
			for(int i = 0; i < 5; i++) {
				if ((i % 2) == odd) {
					g.setColor(color1);
				} else {
					g.setColor(color2);
				}
				putCell(g, x, y + i);
				putCell(g, x + 4, y + i);
			}			
		}
		
		g.setColor(color2);	
		if(tank.getDirection() == Direction.NONE || tank.getDirection() == Direction.UP) {
			putCell(g, x + 2, y);
			putCell(g, x + 2, y + 1);
			putCell(g, x + 2, y + 2);
		} else if(tank.getDirection() == Direction.DOWN) {
			putCell(g, x + 2, y + 2);
			putCell(g, x + 2, y + 3);
			putCell(g, x + 2, y + 4);			
		} else if(tank.getDirection() == Direction.LEFT) {
			putCell(g, x + 0, y + 2);
			putCell(g, x + 1, y + 2);
			putCell(g, x + 2, y + 2);			
		} else if(tank.getDirection() == Direction.RIGHT) {
			putCell(g, x + 2, y + 2);
			putCell(g, x + 3, y + 2);
			putCell(g, x + 4, y + 2);
		}
	}
	
	private void drawBase(Graphics2D g, MyBase base) {
		if(base == null) return;
		putCell(g, base.getX(), base.getY());
		GeneralPath flag =  new GeneralPath(GeneralPath.WIND_EVEN_ODD, 5);
		int x = base.getX() * PIXELS_PER_CELL;
		int y = base.getY() * PIXELS_PER_CELL;
		flag.moveTo(x + 2, y + 2);
		flag.lineTo(x + 2, y - 20);
		flag.lineTo(x + 12, y - 16);
		flag.lineTo(x+3, y - 12);
		flag.lineTo(x+3, y + 2);
		flag.closePath();
		g.fill(flag);
	}
	
	private void doDrawing(Graphics2D g) {
		
		g.setColor(Color.decode("#1f1617"));
		g.fillRect(0, 0, gameState.getWidth() * PIXELS_PER_CELL, gameState.getHeight() * PIXELS_PER_CELL);
		
		for(int y = 0; y < gameState.getHeight(); y++) {
			for(int x = 0; x < gameState.getWidth(); x++) {
				String s = gameState.getState(x, y);				
				if(s.equals(State._EMPTY)) {
					//g.setColor(Color.LIGHT_GRAY);
					//putCell(g, x, y);
				} else if(s.equals(State._FULL)) {
					if(y % 2 == 0) {
						g.setColor(Color.decode("#965920"));
						putCell(g, x, y);
						g.setColor(Color.decode("#b9bcc1"));
						g.drawLine(x * PIXELS_PER_CELL, (y + 1) * PIXELS_PER_CELL -1, (x + 1) * PIXELS_PER_CELL - 1, (y + 1) * PIXELS_PER_CELL - 1);
						g.drawLine((x + 1) * PIXELS_PER_CELL - 1, y * PIXELS_PER_CELL, (x + 1) * PIXELS_PER_CELL - 1, (y + 1) * PIXELS_PER_CELL - 1);
					} else {
						g.setColor(Color.decode("#8d5323"));
						putCell(g, x, y);
						g.setColor(Color.decode("#a5a5a5"));
						g.drawLine(x * PIXELS_PER_CELL, (y + 1) * PIXELS_PER_CELL -1, (x + 1) * PIXELS_PER_CELL - 1, (y + 1) * PIXELS_PER_CELL - 1);
						g.drawLine(x * PIXELS_PER_CELL + 2, y * PIXELS_PER_CELL, x * PIXELS_PER_CELL + 2, (y + 1) * PIXELS_PER_CELL - 1);
					}
				} else if(s.equals(State._OUT_OF_BOUNDS)) {
					g.setColor(Color.CYAN);
					g.drawRect(x * PIXELS_PER_CELL, y * PIXELS_PER_CELL, PIXELS_PER_CELL - 1, PIXELS_PER_CELL - 1);
				} else if(s.equals(State._NONE)) {
					g.setColor(Color.RED);
					g.drawRect(x * PIXELS_PER_CELL, y * PIXELS_PER_CELL, PIXELS_PER_CELL - 1, PIXELS_PER_CELL - 1);
				}
			}
		}

		g.setColor(Color.decode("#1f4fa3"));
		drawBase(g, gameState.getYourBase());

		g.setColor(Color.decode("#ce4024"));
		drawBase(g, gameState.getHisBase());		
		
		for(Tank tank : gameState.getYourTanks()) {
			drawTank(g, tank, Color.decode("#1f4fa3"), Color.decode("#4582c5"));
		}
		
		for(Tank tank : gameState.getHisTanks()) {
			drawTank(g, tank, Color.decode("#ce4024"), Color.decode("#c58245"));
		}
		
		g.setColor(Color.decode("#eb9238"));
		for(MyBullet bullet : gameState.getBullets()) {
			putCell(g, bullet.getX(), bullet.getY());
		}
	}
	
	@Override
	public int getWidth() {
		return gameState.getWidth() * PIXELS_PER_CELL;
	}
	
	@Override
	public int getHeight() {
		return gameState.getHeight() * PIXELS_PER_CELL;
	}
	
	public void drawFrame() {
		repaint();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), getHeight());
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing((Graphics2D)g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX()/PIXELS_PER_CELL;
		int y = e.getY()/PIXELS_PER_CELL;
		System.out.println("** Cell........: " + x + "," + y);
		for(Tank tank : gameState.getYourTanks()) {
			if(x >= tank.getX() - 2 && x <= tank.getX() + 2 &&
					y >= tank.getY() - 2 && y <= tank.getY() + 2){
				System.out.println("** Tank........: " + tank.getId() + " @ " + tank.getX() + "," + tank.getY());
			}
		}
		for(Tank tank : gameState.getHisTanks()) {
			if(x >= tank.getX() - 2 && x <= tank.getX() + 2 &&
					y >= tank.getY() - 2 && y <= tank.getY() + 2){
				System.out.println("** Tank........: " + tank.getId() + " @ " + tank.getX() + "," + tank.getY());
			}
		}
		for(MyBullet bullet : gameState.getBullets()) {
			if(x == bullet.getX() && y == bullet.getY()){
				System.out.println("** Bullet......: " + bullet.getId() + " @ " + bullet.getX() + "," + bullet.getY());
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
