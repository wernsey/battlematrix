package za.co.wstoop.r100k2013.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

import javax.swing.JPanel;

import za.co.entelect.challenge.Base;
import za.co.wstoop.r100k2013.GameState;
import za.co.wstoop.r100k2013.MyBase;
import za.co.wstoop.r100k2013.Tank;
import za.co.wstoop.r100k2013.imaging.Raster;

public class RasterRenderer extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private Raster raster;
	
	private GameState state;
	
	public static final int PIXELS_PER_CELL = 1;
	
	private static boolean DRAW_WALL_OVERLAY = true;
	
	private int fx_ctr, fx_dir = 3;
	
	RasterRenderer(int width, int height) {
		raster = new Raster(width, height);
		fx_ctr = this.hashCode() % raster.getWidth();
	}

	public void drawFrame() {
		repaint();
	}
	
	@Override
	public int getWidth() {
		return raster.getWidth() * PIXELS_PER_CELL + 10;
	}
	
	@Override
	public int getHeight() {
		return raster.getHeight() * PIXELS_PER_CELL + 10 + 30;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), getHeight());		
	}
	
	@Override
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);
		
		Graphics2D g = (Graphics2D)gg;
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth() * PIXELS_PER_CELL + 10, getHeight()* PIXELS_PER_CELL + 10 + 30);
		
		int min = raster.getMin();
		int max = raster.getMax();
		if(min >= max) {
			System.err.println("Invalid raster to render: " + min + " " + max);
			return;
		}
				
		for (int y = 0; y < raster.getHeight(); y++) {
			for (int x = 0; x < raster.getWidth(); x++) {

				int c = raster.get(x, y);
				
				if((DRAW_WALL_OVERLAY && state != null && state.isOccupied(x, y)) ||
						x == (fx_ctr % raster.getWidth()) ||
						y == (fx_ctr % raster.getHeight())) {
					g.setColor(occupiedGradientColor(c, min, max));
				} else {
					g.setColor(gradientColor(c, min, max));
				}
				putCell(g, x, y);
			}
		}

		if(fx_dir > 0 && fx_ctr > raster.getWidth() ||
				fx_dir < 0 && fx_ctr < 0)
			fx_dir = -fx_dir;
			
		fx_ctr += fx_dir;

		g.setColor(Color.GREEN);
		for(Tank tank : state.getYourTanks()) {
			putCell(g, tank.getX(), tank.getY());
			putCell(g, tank.getX()-1, tank.getY()-1);
			putCell(g, tank.getX()+1, tank.getY()-1);
			putCell(g, tank.getX()-1, tank.getY()+1);
			putCell(g, tank.getX()+1, tank.getY()+1);
		}
		MyBase b = state.getYourBase();
		putCell(g, b.getX()-1, b.getY());
		putCell(g, b.getX()+1, b.getY());
		putCell(g, b.getX(), b.getY()-1);
		putCell(g, b.getX(), b.getY()+1);

		g.setColor(Color.MAGENTA);
		for(Tank tank : state.getHisTanks()) {
			putCell(g, tank.getX(), tank.getY());
			putCell(g, tank.getX()-1, tank.getY()-1);
			putCell(g, tank.getX()+1, tank.getY()-1);
			putCell(g, tank.getX()-1, tank.getY()+1);
			putCell(g, tank.getX()+1, tank.getY()+1);
		}
		b = state.getHisBase();
		putCell(g, b.getX()-1, b.getY());
		putCell(g, b.getX()+1, b.getY());
		putCell(g, b.getX(), b.getY()-1);
		putCell(g, b.getX(), b.getY()+1);
		
		g.setColor(Color.WHITE);		
		g.drawString("Kill-O-Scope™", 10, getHeight() - 10);
	}
	
	private Color gradientColor(int c, int min, int max) {
		
		double i = (double)(c - min) / (max - min);
		
		int r = 0, g = 0, b = 0;
		
		if(i < 0.66) {
			r = 255 - (int)(255.0 * i/0.66);
		}
		
		if(i > 0.33) {
			b = (int)(255.0 * (i-0.33)/0.66);
		}
		
		if(r < 0) r = 0;
		else if(r > 255) r = 255;
		if(g < 0) g = 0;
		else if(g > 255) g = 255;
		if(b < 0) b = 0;
		else if(b > 255) b = 255;
				
		return new Color(r,g,b);
	}
	
	private Color occupiedGradientColor(int c, int min, int max) {
		
		double i = (double)(c - min) / (max - min);
		
		int r1 = 0, g1 = 0, b1 = 0;
		int r2 = 0, g2 = 0, b2 = 0;
		
		if(i < 0.66) {
			r1 = 255 - (int)(255.0 * i/0.66);
			g1 = (255 - (int)(255.0 * i/0.66))/4;
			b1 = (255 - (int)(255.0 * i/0.66))/4;
		}
		
		if(i > 0.33) {
			r2 = ((int)(255.0 * (i-0.33)/0.66))/4;
			g2 = ((int)(255.0 * (i-0.33)/0.66))/4;
			b2 = (int)(255.0 * (i-0.33)/0.66);
		}
		
		r1 += r2;
		g1 += g2;
		b1 += b2;
		
		if(r1 < 0) r1 = 0;
		else if(r1 > 255) r1 = 255;
		if(g1 < 0) g1 = 0;
		else if(g1 > 255) g1 = 255;
		if(b1 < 0) b1 = 0;
		else if(b1 > 255) b1 = 255;
				
		return new Color(r1,g1,b1);
	}
	

	private void putCell(Graphics2D g, int x, int y) {
		g.fillRect(x * PIXELS_PER_CELL + 5, y * PIXELS_PER_CELL + 5, PIXELS_PER_CELL, PIXELS_PER_CELL);
	}

	public Raster getRaster() {
		return raster;
	}

	public void setRaster(Raster raster) {
		this.raster = raster;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public GameState getState() {
		return state;
	}
	
}
