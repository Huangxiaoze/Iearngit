package mygrapheditor;

import java.awt.Color;
import java.io.Serializable;

/*
 * 图形元素坐标
 */
public class PixPoint implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	int x; // x轴坐标
	int y; // y轴坐标
	PaintBrush paintbrush = null;
	
	public void move(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public void rotate(double angle) {
		paintbrush.setRotateAngle(angle);
	}
	public double getRotate() {
		return paintbrush.getRotateAngle();
	}	
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}	
	
	PixPoint (int x, int y, Color color, Shape type, int size, boolean dash) {
		this.x = x;
		this.y = y;
		paintbrush = new PaintBrush(color, type, size, dash);
	}
	PixPoint (int x, int y, PaintBrush p) throws CloneNotSupportedException{
		this.x = x;
		this.y = y;
		if(p!=null) {
			paintbrush = (PaintBrush) p.clone();
		}
	}


	@Override
	public String toString() {
		return "PixPoint [x=" + x + ", y=" + y + ", paintbrush=" + paintbrush + "]";
	}
	
	
}