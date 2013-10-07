package za.co.wstoop.r100k2013.gui;

/*
Based on the one at http://www.zetcode.com/gfx/java2d/introduction/
Run from the command line like so:
*/
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import za.co.wstoop.r100k2013.StrategicPlanner;
import za.co.wstoop.r100k2013.GameState;
import za.co.wstoop.r100k2013.PFieldDriver2;
import za.co.wstoop.r100k2013.PotentialField;
import za.co.wstoop.r100k2013.Tank;
import za.co.wstoop.r100k2013.TankDriver;

class Skeleton extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private GameState state;
	
	private Surface surface;
		
	private RasterRenderer [] rasterPainter;
	
	private StrategicPlanner planner;

	public Skeleton(GameState state) {
		this.state = state;
		
		planner = new StrategicPlanner(state);
		
		this.rasterPainter = new RasterRenderer [2];
		this.rasterPainter[0] = new RasterRenderer(state.getWidth(), state.getHeight());
		this.rasterPainter[1] = new RasterRenderer(state.getWidth(), state.getHeight());
		
		initUI();
		Timer timer = new Timer(100, this);
		timer.start(); 
	}
	
	private void initUI() {
		Container pane = getContentPane();
		setTitle("Battle Matrix");
				
		setSize(state.getWidth() * Surface.PIXELS_PER_CELL + 40, state.getHeight() * Surface.PIXELS_PER_CELL + 40);
		
		setLocationRelativeTo(null);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		surface = new Surface(state);
		
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy = 0;
		c.gridheight = 1;
		pane.add(surface, c);	

		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 2;
		pane.add(rasterPainter[0], c);
		c.gridx = 1;
		c.gridy = 2;
		pane.add(rasterPainter[1], c);
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		planner.plan(state);
		
		int i = 0;
		for(Tank t : state.getYourTanks()) {
			PFieldDriver2 d = (PFieldDriver2)t.getDriver();
			PotentialField pf = d.getPfield();
			rasterPainter[i].setState(state);
			rasterPainter[i].setRaster(pf.getRaster());			
			rasterPainter[i].repaint();
			i++;
		}
		
		state.update();
		surface.drawFrame();	
		
		repaint();
	}
	
	public static void main(String ... args) {
		
		String levelfile = "level.txt";
						
		if(args.length > 0) {
			levelfile = args[0];
		}

		try {
			final GameState state = GameState.fromShowFile(levelfile);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Skeleton sk = new Skeleton(state);
					sk.setVisible(true);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
