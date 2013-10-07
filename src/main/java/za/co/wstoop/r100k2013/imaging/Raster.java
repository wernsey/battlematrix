package za.co.wstoop.r100k2013.imaging;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import za.co.entelect.challenge.State;
import za.co.wstoop.r100k2013.GameState;

/**
 * A Google to see if anyone has tried to apply Image techniques to mazes led
 * me to these links:
 *  + http://www.cb.uu.se/~cris/blog/index.php/archives/277
 *  + http://blog.wolfram.com/2010/11/03/amazeing-image-processing-in-mathematica/
 * 
 * Here are links on Morphology: 
 *  + http://www.viz.tamu.edu/faculty/parke/ends489f00/notes/sec1_9.html
 *  + http://homepages.inf.ed.ac.uk/rbf/HIPR2/morops.htm
 *  + http://www.cs.auckland.ac.nz/courses/compsci773s1c/lectures/ImageProcessing-html/topic4.htm
 *  + http://en.wikipedia.org/wiki/Morphological_image_processing
 *  + http://www.dca.fee.unicamp.br/~lotufo/khoros/mmach/tutor/util/fast.html
 */
public class Raster {
	private int[] pixels;

	public static final double [] SOBEL_H_KERNEL = new double [] {	-1, -2, -1, 
																	0, 0, 0,
																	1, 2, 1 };
	public static final double [] SOBEL_V_KERNEL = new double [] {	-1, 0, 1, 
																	-2, 0, 2,
																	-1, 0, 1 };
	
	public static final double [] LAPLACE_KERNEL = new double [] {	0, -1, 0, 
																	-1, 4, -1,
																	0, -1, 0 };
	
	private int width, height;
	
	public Raster(int width, int height) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
	}
	
	public Raster(int width, int height, int value) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
		for(int i = 0; i < width * height; i++) {
			pixels[i] = value;
		}
	}
	
	public Raster(Raster that) {
		this.pixels = Arrays.copyOf(that.pixels, that.pixels.length);
		this.width = that.width;
		this.height = that.height;
	}
	
	public Raster(GameState state) {
		this.width = state.getWidth();
		this.height = state.getHeight();
		pixels = new int[state.getWidth() * state.getHeight()];
	}
	
	public static Raster fromState(GameState state) {
		return (new Raster(state)).overlay(state,1);
	}
		
	public int get(int x, int y) {
		int index = x + y * width;

		return pixels[index];
	}

	public void set(int x, int y, int c) {
		if (x >= width || x < 0 || y >= height || y < 0) {
			return;
		}
		pixels[x + y * width] = c;
	}
	
	public int getMin() {
		int min = Integer.MAX_VALUE;
		for (int x = 0; x < width * height; x++) {
			if (pixels[x] < min)
				min = pixels[x];
		}
		return min;
	}
	
	public int getMax() {
		int max = Integer.MIN_VALUE;
		for (int x = 0; x < width * height; x++) {
			if (pixels[x] > max)
				max = pixels[x];
		}
		return max;
	}

	public Raster toPgm(String filename) {
		FileWriter fw = null;
		try {
			int max = getMax();

			fw = new FileWriter(filename);

			StringBuilder sb = new StringBuilder();

			sb.append("P2\n");
			sb.append(width);
			sb.append(" ");
			sb.append(height);
			sb.append("\n");
			sb.append(max);
			sb.append("\n");
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					sb.append(get(x, y));
					sb.append(" ");
				}
				sb.append("\n");
			}

			fw.write(sb.toString());
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public Raster add(Raster that) {
		Raster next = new Raster(width, height);		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				next.set(x, y, get(x,y) + that.get(x, y));			
			}
		}
		return next;
	}

	public Raster subtract(Raster that) {
		Raster next = new Raster(width, height);		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				next.set(x, y, get(x,y) - that.get(x, y));			
			}
		}
		return next;
	}
	
	public Raster and(Raster that) {
		Raster next = new Raster(width, height);		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				boolean b = get(x,y) != 0 && that.get(x, y) != 0;				
				next.set(x, y, b?1:0);			
			}
		}
		return next;
	}
	
	public Raster or(Raster that) {
		Raster next = new Raster(width, height);		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				boolean b = get(x,y) != 0 || that.get(x, y) != 0;				
				next.set(x, y, b?1:0);			
			}
		}
		return next;
	}
	
	public Raster not() {
		Raster next = new Raster(width, height);		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				boolean b = get(x,y) == 0;				
				next.set(x, y, b?1:0);			
			}
		}
		return next;
	}
	
	public Raster blur() {
		return blur1();
	}
	
	public Raster apply3x3Kernel(double [] kernel) {
		return apply3x3Kernel(kernel, 1.0);
	}
	
	public Raster apply3x3Kernel(double [] kernel, double divisor) {
		/* The kernel is like
		 * [0, 1, 2
		 *  3, 4, 5,
		 *  6, 7, 8]
		 */
		if(kernel.length != 9) return null;
		Raster next = new Raster(width,height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double tl = kernel[0] * getb(x - 1, y - 1);
				double t  = kernel[1] * getb(x, y - 1);
				double tr = kernel[2] * getb(x + 1, y - 1);
				double l  = kernel[3] * getb(x - 1, y);
				double c  = kernel[4] * getb(x,y);
				double r  = kernel[5] * getb(x + 1, y);
				double bl = kernel[6] * getb(x - 1, y + 1);
				double b  = kernel[7] * getb(x, y + 1);
				double br = kernel[8] * getb(x + 1, y + 1);
				double sum = (tl + t + tr + l + c + r + bl + b + br)/divisor;
				next.set(x, y, (int)sum);
			}
		}
		
		return next;
	}
	
	public Raster morphUnion(int [] kernel) {
		/* The kernel is like
		 * [0, 1, 0
		 *  1, 1, 1,
		 *  0, 1, 0]
		 */
		if(kernel.length != 9) return null;
		Raster next = new Raster(width,height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
								
				boolean set = kernel[0] != 0 && getb(x - 1, y - 1) != 0 ||
								kernel[1] != 0 && getb(x, y - 1) != 0 ||
								kernel[2] != 0 && getb(x + 1, y - 1) != 0 ||
								kernel[3] != 0 && getb(x - 1, y) != 0 ||
								kernel[4] != 0 && getb(x,y) != 0 ||
								kernel[5] != 0 && getb(x + 1, y) != 0 ||
								kernel[6] != 0 && getb(x - 1, y + 1) != 0 ||
								kernel[7] != 0 && getb(x, y + 1) != 0 ||
								kernel[8] != 0 && getb(x + 1, y + 1) != 0;
				next.set(x, y, set?1:0);
			}
		}
		
		return next;
	}
	
	public Raster morphUnion(Raster that) {
		Raster next = new Raster(width,height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(get(x, y) != 0 || that.get(x, y) != 0) {
					next.set(x, y, 1);
				}
			}
		}
		return next;
	}
	
	public Raster morphIntersect(int [] kernel) {
		/* The kernel is like
		 * [0, 1, 0
		 *  1, 1, 1,
		 *  0, 1, 0]
		 */
		if(kernel.length != 9) return null;
		Raster next = new Raster(width,height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
								
				boolean set = true;
				if (kernel[0] != 0 && getb(x - 1, y - 1) == 0)
					set = false;
				if (kernel[1] != 0 && getb(x, y - 1) == 0)
					set = false;
				if (kernel[2] != 0 && getb(x + 1, y - 1) == 0)
					set = false;
				if (kernel[3] != 0 && getb(x - 1, y) == 0)
					set = false;
				if (kernel[4] != 0 && getb(x, y) == 0)
					set = false;
				if (kernel[5] != 0 && getb(x + 1, y) == 0)
					set = false;
				if (kernel[6] != 0 && getb(x - 1, y + 1) == 0)
					set = false;
				if (kernel[7] != 0 && getb(x, y + 1) == 0)
					set = false;
				if (kernel[8] != 0 && getb(x + 1, y + 1) == 0)
					set = false;
				
				next.set(x, y, set?1:0);
			}
		}
		
		return next;
	}
	
	public Raster morphIntersect(Raster that) {
		Raster next = new Raster(width,height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(get(x, y) != 0 && that.get(x, y) != 0) {
					next.set(x, y, 1);
				}
			}
		}
		return next;
	}
	
	public Raster morphSubtract(Raster that) {
		Raster next = new Raster(width,height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				boolean set = false;
				if(getb(x, y) != 0) {
					if(that.getb(x, y) == 0)
						set = true;
				}
				next.set(x, y, set?1:0);
			}
		}
		return next;
	}

	public Raster dilate() {		
		int [] kernel = new int [] { 0, 1, 0,
									1, 1, 1,
									0, 1, 0};
		return morphUnion(kernel);
	}

	public Raster dilate8() {		
		int [] kernel = new int [] { 1, 1, 1,
									1, 1, 1,
									1, 1, 1};
		return morphUnion(kernel);
	}
	
	public Raster erode() {		
		int [] kernel = new int [] { 0, 1, 0,
									1, 1, 1,
									0, 1, 0};
		return morphIntersect(kernel);
	}
	
	public Raster morphOpen() {		
		return dilate().erode();
	}
	
	public Raster morphClose() {		
		return erode().dilate();
	}
	
	public Raster morphNot() {
		Raster next = new Raster(width,height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				next.set(x, y, get(x,y)!=0?0:1);
			}
		}
		return next;
	}
	
	public Raster morphHitMiss(int [] kernelJ, int [] kernelK) {
		return this.morphIntersect(kernelJ).morphIntersect(this.morphNot().morphIntersect(kernelK));
	}
	
	public Raster morphCorners() {
		Raster ul = morphHitMiss(
				new int [] { 0, 0, 0,
								0, 1, 1,
								0, 1, 0},
				new int [] { 1, 1, 0,
								1, 0, 0,
								0, 0, 0}
				);
		Raster ur = morphHitMiss(
				new int [] { 0, 0, 0,
								1, 1, 0,
								0, 1, 0},
				new int [] { 0, 1, 1,
								0, 0, 1,
								0, 0, 0}
				);
		Raster ll = morphHitMiss(
				new int [] { 0, 1, 0,
								0, 1, 1,
								0, 0, 0},
				new int [] { 0, 0, 0,
								1, 0, 0,
								1, 1, 0}
				);
		Raster lr = morphHitMiss(
				new int [] { 0, 1, 0,
								1, 1, 0,
								0, 0, 0},
				new int [] { 0, 0, 0,
								0, 0, 1,
								0, 1, 1}
				);
		return ul.morphUnion(ur.morphUnion(ll.morphUnion(lr)));
	}
	
	public Raster morphThin() {
		
		Raster next = new Raster(this);
		
		while(true) {
			
			Raster prev = next;
			
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 0, 0, 0,
									0, 1, 1,
									0, 1, 1},
					new int [] { 1, 1, 0,
									1, 0, 0,
									0, 0, 0}
					));
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 0, 0, 1,
									0, 1, 1,
									0, 0, 1},
					new int [] { 1, 0, 0,
									1, 0, 0,
									1, 0, 0}
					));
			
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 0, 0, 0,
									1, 1, 0,
									1, 1, 0},
					new int [] { 0, 1, 1,
									0, 0, 1,
									0, 0, 0}
					));			
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 0, 0, 0,
									0, 1, 0,
									1, 1, 1},
					new int [] { 1, 1, 1,
									0, 0, 0,
									0, 0, 0}
					));
			
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 0, 1, 1,
									0, 1, 1,
									0, 0, 0},
					new int [] { 0, 0, 0,
									1, 0, 0,
									1, 1, 0}
					));	
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 1, 1, 1,
									0, 1, 0,
									0, 0, 0},
					new int [] { 0, 0, 0,
									0, 0, 0,
									1, 1, 1}
					));
			
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 1, 1, 0,
									1, 1, 0,
									0, 0, 0},
					new int [] { 0, 0, 0,
									0, 0, 1,
									0, 1, 1}
					));
			next = next.morphSubtract(next.morphHitMiss(
					new int [] { 1, 0, 0,
									1, 1, 0,
									1, 0, 0},
					new int [] { 0, 0, 1,
									0, 0, 1,
									0, 0, 1}
					));
			
			
			if(prev.equals(next)) 
				break;
		}
		return next;
	}
	
	public Raster morphThick() {
		return morphNot().morphThin().morphNot();
	}
	
	public Raster morphEnds() {
		Raster ul = morphHitMiss(
				new int [] { 0, 0, 0,
								0, 1, 1,
								0, 0, 0},
				new int [] { 1, 1, 1,
								1, 0, 0,
								1, 1, 1}
				);
		Raster ur = morphHitMiss(
				new int [] { 0, 0, 0,
								0, 1, 0,
								0, 1, 0},
				new int [] { 1, 1, 1,
								1, 0, 1,
								1, 0, 1}
				);
		Raster ll = morphHitMiss(
				new int [] { 0, 1, 0,
								0, 1, 0,
								0, 0, 0},
				new int [] { 1, 0, 1,
								1, 0, 1,
								1, 1, 1}
				);
		Raster lr = morphHitMiss(
				new int [] { 0, 0, 0,
								1, 1, 0,
								0, 0, 0},
				new int [] { 1, 1, 1,
								0, 0, 1,
								1, 1, 1}
				);
		return ul.morphUnion(ur.morphUnion(ll.morphUnion(lr)));
	}
	
	/* FIXME: This is not actually working */
	public Raster morphSingles() {
		 
		return morphIntersect(
				new int [] {0, 0, 0,
							0, 1, 0,
							0, 0, 0});
	}
	
	public Raster binary() {
		return binary(1);
	}
	
	public Raster binary(int how) {
		Raster next = new Raster(width,height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int b = get(x,y);
				next.set(x, y, (b == 0)?0:how);
			}
		}
		return next;
	}
	
	public Raster threshold(int v) {
		Raster next = new Raster(width,height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int b = get(x,y);
				next.set(x, y, (b < v)?0:1);
			}
		}
		return next;
	}
	
	public Raster threshold(double x) {
		return threshold((int)(x * getMax()));
	}
	
	public Raster threshold() {
		return threshold(getMax()/2);
	}
	
	public Raster normalize() {
		return normalize(128);
	}
	
	public Raster normalize(int top) {
		
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int b = get(x,y);
				if(b < min) min = b;
				if(b > max) max = b;
			}
		}
		
		Raster next = new Raster(width,height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double b = get(x,y);
				b = (b - min)/(max - min) * top;
				next.set(x, y, (int)b);
			}
		}
		return next;
	}
	
	public Raster multiply(int v) {
		Raster next = new Raster(width,height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int b = get(x,y);
				next.set(x, y, b * v);
			}
		}
		return next;
	}
	
	/**
	 * Get bounded values, returns 0 for invalid x,y
	 */
	public int getb(int x, int y) {
		if (x >= width)
			return 0;
		if (x < 0)
			return 0;

		if (y >= height)
			return 0;
		if (y < 0)
			return 0;
		
		int index = x + y * width;

		return pixels[index];
	}
	
	/*
	 * http://www.gamerendering.com/2008/10/11/gaussian-blur-filter-shader/
	 */
	private Raster blur1() {

		Raster result = new Raster(width,height);

		// Horizontal phase
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double sum = 
						0.05 * getb(x - 4, y) + 
						0.09 * getb(x - 3, y) + 
						0.12 * getb(x - 2, y) + 
						0.15 * getb(x - 1, y) + 
						0.16 * getb(x, y) + 
						0.15 * getb(x + 1, y) + 
						0.12 * getb(x + 2, y) + 
						0.09 * getb(x + 3, y) + 
						0.05 * getb(x + 4, y);
				result.set(x, y, (int) sum);
			}
		}

		Raster result2 = new Raster(width,height);

		// Vertical phase
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double sum = 
						0.05 * result.getb(x, y - 4) + 
						0.09 * result.getb(x, y - 3) + 
						0.12 * result.getb(x, y - 2) + 
						0.15 * result.getb(x, y - 1) + 
						0.16 * result.getb(x, y) + 
						0.15 * result.getb(x, y + 1) + 
						0.12 * result.getb(x, y + 2) + 
						0.09 * result.getb(x, y + 3) + 
						0.05 * result.getb(x, y + 4);
				result2.set(x, y, (int) sum);
			}
		}

		return result2;
	}
		
	public Raster getBoundaries(int what) {
		Raster next = new Raster(width,height);
		
		for (int y = 1; y < height - 1; y++) {
			for (int x = 0; x < width; x++) {
				
				int color = get(x,y);
				if (color == 0) {
					next.set(x, y, what);
					continue;
				}
				
				boolean gotcha = false;
				if(getb(x,y-1) != 0 && getb(x,y-1) != color) gotcha = true;
				if(getb(x-1,y) != 0 && getb(x-1,y) != color) gotcha = true;
				
				if(gotcha) next.set(x, y, 10);				
			}
		}		
		return next;
	}
	
	public Raster overlay(GameState state) {
		return overlay(state, 1);
	}
	
	public Raster overlay(GameState state, int level) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if(!state.getState(x, y).equals(State._EMPTY)) {
					set(x, y, level);
				}
				//if (state.isOccupied(x, y)) {
				//	set(x, y, level);
				//}
			}
		}
		return this;
	}
	
	private static class FillPoint {
		public int x, y;
		public FillPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public void floodfill(int x, int y, int target) {
		
		int src = get(x,y);
		
		Queue<FillPoint> que = new LinkedList<FillPoint>();
		que.add(new FillPoint(x,y));
		while(!que.isEmpty()){
			FillPoint p = que.poll();
			
			x = p.x;
			y = p.y;
			
			if(y < 0 || y >= height) continue;
			if(x < 0) x = width - 1;
			if(x >= width) x = 0;
			
			if(get(x,y) != src) {
				continue;
			}
						
			int w = x, e = x;
			while(w > 0 && get(w - 1, y) == src) {
				w--;
			}
			if(w == 0) {
				que.add(new FillPoint(width - 1,y));
			}
			
			while(e < width - 1 && get(e + 1, y) == src) {
				e++;
			}
			if(e == width - 1) {
				que.add(new FillPoint(0,y));
			}
			
			boolean up = false, down = false;
			for(int i = w; i <= e; i++) {
				set(i, y, target);
				
				if(!down) {
					if(y < height - 1 && get(i, y + 1) == src) {
						que.add(new FillPoint(i,y + 1));
						down = true;
					}
				} else {
					if(y < height - 1 && get(i, y + 1) != src) {
						down = false;
					}
				}
				
				if (!up) {
					if (y > 0 && get(i, y - 1) == src) {
						que.add(new FillPoint(i, y - 1));
						up = true;
					}
				} else {
					if (y > 0 && get(i, y - 1) != src) {
						up = false;
					}
				}
								
			}			
		}
	}
	
	public int countValues(int value) {
		int count = 0;
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				if(get(x, y) == value)
					count++;
		return count;
	}
	
	public Raster getTopology() {
		return getTopology(100);
	}
	
	public Raster getTopology(int max) {
		Raster src = binary(1);
		Raster dest = new Raster(src);
		do {
			dest = dest.add(src);
			src = src.dilate();
		} while(dest.countValues(0) > 0 && --max > 0);		
		return dest;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj.getClass() != this.getClass()) return false;		
		Raster that = (Raster)obj;		
		return this.width == that.width && this.height == that.height && Arrays.equals(pixels, that.pixels);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
