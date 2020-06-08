package mygrapheditor;

import java.awt.Color;
import java.io.Serializable;

 /*
 * ���������࣬����Ԫ�صĻ�����Ϣ
 */
public class PaintBrush implements Serializable, Cloneable {
	public static float[] dash_set = new float[] { 5, 10 }; // ����
	
	// ������ɫ
	Color panColor = Color.black;
	Color fillColor = new Color(0,0,0,0) ;
	Shape graphicsType = Shape.BRUSH;  // ͼ������, ���Կ�����ö��  graphicsType=-1����ض� 0 ������ 1ֱ�� 2����
	int panSize = 2;   // ���ʴ�С
	boolean dash = false;
	double rotateAngle = 0; // ��ת��
	public PaintBrush(Color color, Shape type, int size, boolean dash) {
		this.panColor = color;
		this.graphicsType = type;
		this.panSize = size;
		this.dash = dash;
	}
	
	public PaintBrush() {
		// TODO Auto-generated constructor stub
	}
	
	public Color getFillColor() {
		return fillColor;
	}
	public void setFillColor(Color fillColor) {
		System.out.println("???? setfillcolor"+this.fillColor);
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
