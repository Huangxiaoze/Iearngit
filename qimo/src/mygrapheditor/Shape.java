package mygrapheditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;


/*
 * 静态工厂方法
 */
class ShapeFactory {
	public static Shape getShape(String s) throws Exception {
		if(s.equals("line")) {
			return Shape.LINE;
		} else if(s.equals("rect")) {
			return Shape.SQUARE;
		} else if(s.equals("pan")) {
			return Shape.BRUSH;
		} else if(s.equals("circle")) {
			return Shape.CIRCLE;
		} else if (s.equals("eraser")) {
			return Shape.ERASER;
		} else if (s.equals("fill")) {
			return Shape.FILLCOLOR;
		}
		throw new Exception("shape not found");
	}
}

/*
 * 图形元素枚举类
 */
public enum Shape {
	LINE(true), CIRCLE(true), OVAL(true), RECTANGLE(true), SQUARE(true), BRUSH(false), CUT(false), ERASER(false), FILLCOLOR(false);
	private boolean editable;
	Shape(boolean editable){
		this.editable = editable;
	}
	public boolean isEditable() {
		return editable;
	}
	public Color getDashColor() {
		switch(this) {
		case SQUARE:case RECTANGLE:
			return Color.white;
		default:
			return Color.darkGray;
		}
	}
	public Cursor getCursor() {
		Toolkit kit=Toolkit.getDefaultToolkit();
		Image img = null;
		Cursor mouse = null;
		if(this == BRUSH) {
			img=kit.getImage(Editor.IMGURL+"brush_small.png");
			mouse=kit.createCustomCursor(img, new Point(2,30), "stick");
		} else if(this == ERASER) {
			img=kit.getImage(Editor.IMGURL+"eraser.png");
			mouse=kit.createCustomCursor(img, new Point(7,28), "stick");
		} else if(this == FILLCOLOR) {
			img=kit.getImage(Editor.IMGURL+"fillcolor.png");
			mouse=kit.createCustomCursor(img, new Point(2,30), "stick");
		} else {
			mouse = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		
		return mouse;
	}
	
}
