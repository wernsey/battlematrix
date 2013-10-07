package za.co.wstoop.r100k2013;

public class Fuzzy {
	private double fValue;
	
	public Fuzzy(double v) {
		if(v > 1.0) v = 1.0;
		if(v < 0) v = 0.0;
		this.fValue = v;
	}
	
	public Fuzzy(boolean b) {
		this.fValue = b ? 1.0 : 0.0;
	}

	public Fuzzy and(Fuzzy that) {
		return new Fuzzy(Math.min(this.fValue, that.fValue));
	}
	
	public Fuzzy or(Fuzzy that) {
		return new Fuzzy(Math.max(this.fValue, that.fValue));
	}

	public Fuzzy not() {
		return new Fuzzy(1.0 - this.fValue);
	}
	
	public Fuzzy very() {
		return new Fuzzy(Math.pow(fValue, 2));
	}
	
	public Fuzzy somewhat() {
		return new Fuzzy(Math.pow(fValue, 0.5));
	}

	public boolean truth() {
		return this.fValue >= 0.5;
	}
	
	public double value() {
		return fValue;
	}
	
	public String toString() {
		return String.format("~%1.3f", fValue);
	}
}
