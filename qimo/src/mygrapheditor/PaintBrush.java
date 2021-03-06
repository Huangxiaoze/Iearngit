package mygrapheditor;

import java.awt.Color;
import java.io.Serializable;

 /*
 * 画笔属性类，保存元素的画笔信息
 */
public class PaintBrush implements Serializable, Cloneable {
	public static float[] dash_set = new float[] { 5, 10 }; // 虚线
	
	Color panColor = Color.black;// 画笔颜色
	Color fillColor = new Color(0,0,0,0) ; // 填充颜色
	Shape graphicsType = Shape.BRUSH;  // 图元类型
	int panSize = 2;   // 画笔大小
	boolean dash = false; // 线条是否是虚线
	double rotateAngle = 0; // 旋转角
	
	public PaintBrush(Color color, Shape type, int size, boolean dash) {
		this.panColor = color;
		this.graphicsType = type;
		this.panSize = size;
		this.dash = dash;
	}
	public int getPanSize() {
		return this.panSize;
	}
	public void setPanSize(int s) {
		this.panSize = s;
	}
	
	public PaintBrush() {}
	
	public Color getFillColor() {
		return fillColor;
	}
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	public void setDash(boolean d) {
		this.dash = d;
	}
	public boolean getDash() {
		return this.dash;
	}
	
	public void raisePanSize() {
		panSize++;
	}
	
	public void decreasePanSize() {
		if(panSize > 1) {
			panSize--;
		};
	}
	
	public void setPanColor(Color c) {
		this.panColor = c;
	}
	
	public Color getPanColor() {
		return this.panColor;
	}
	public void setGraphicsType(Shape type) {
		this.graphicsType = type;
	}
	public Shape getGraphicsType() {
		return this.graphicsType;
	}
	public double getRotateAngle() {
		return this.rotateAngle;
	}
	public void setRotateAngle(double angle) {
		this.rotateAngle = angle;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
		
	}

	@Override
	public String toString() {
		return "PaintBrush [panColor=" + panColor + ", fillColor=" + fillColor + ", graphicsType=" + graphicsType
				+ ", panSize=" + panSize + ", dash=" + dash + ", rotateAngle=" + rotateAngle + "]";
	}
	
	
}
