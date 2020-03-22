import java.awt.Rectangle;

public class SuperResPoint {
	private double x;
	private double y;
	private int slice;
	
	private int leftX;
	private int leftY;
	private int width;
	private int height;
	
	SuperResPoint(double x, double y, int slice) {
		this.x = x;
		this.y = y;
		this.slice = slice;
	}

	SuperResPoint(double x, double y, int slice, Rectangle r) {
		this.x = x;
		this.y = y;
		this.slice = slice;
		
		leftX = (int)r.getX();
		leftY = (int)r.getY();
		width = (int)r.getWidth();
		height = (int)r.getHeight();
	}
	
	void setBounds(Rectangle r) {
		leftX = (int)r.getX();
		leftY = (int)r.getY();
		width = (int)r.getWidth();
		height = (int)r.getHeight();
	}
	
	double getX() {
		return x;
	}

	double getY() {
		return y;
	}

	int getSlice() {
		return slice;
	}
	
	public int getLeftX() {
		return leftX;
	}

	public int getLeftY() {
		return leftY;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
