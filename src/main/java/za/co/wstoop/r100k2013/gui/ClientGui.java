package za.co.wstoop.r100k2013.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import za.co.wstoop.r100k2013.GameState;
import za.co.wstoop.r100k2013.PFieldDriver2;
import za.co.wstoop.r100k2013.PotentialField;
import za.co.wstoop.r100k2013.Tank;
import za.co.wstoop.r100k2013.client.Client;
import za.co.wstoop.r100k2013.client.TickListener;

public class ClientGui extends JFrame implements TickListener {

	private static final long serialVersionUID = 1L;
	
	private Client client;
	
	private Surface surface;
	
	private RasterRenderer [] rasterPainter;

	public ClientGui(String endpoint) throws IOException {
		
		super(endpoint);
		
		client = new Client(endpoint);
		client.addListener(this);
		
		this.rasterPainter = new RasterRenderer [2];
		
		GameState state = client.getState();
		
		this.rasterPainter[0] = new RasterRenderer(state.getWidth(), state.getHeight());
		this.rasterPainter[1] = new RasterRenderer(state.getWidth(), state.getHeight());
		
		client.start();
				
		Container pane = getContentPane();
						
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
	public void tickAction(final GameState state) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				int i = 0;
				for(Tank t : state.getYourTanks()) {
					if(t.getDriver() instanceof PFieldDriver2) {
						PFieldDriver2 d = (PFieldDriver2)t.getDriver();
						PotentialField pf = d.getPfield();
						rasterPainter[i].setState(state);
						rasterPainter[i].setRaster(pf.getRaster());			
						rasterPainter[i].repaint();
					}
					i++;
				}
				surface.repaint();
			}			
		});
	}
	
	public static void main(String[] args) {
		
		if (args.length > 0) {
		} else {
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ClientGui sk;
				try {
					sk = new ClientGui("http://localhost:7070/Challenge/ChallengeService");
					sk.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
