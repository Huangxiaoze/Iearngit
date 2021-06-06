package mygrapheditor;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Vector;



class KeyBoardFactory {
	public static KeyBoard createKeyBoard(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == 16) { // Shift键
			return new Shift();
		}
		if (e.getModifiers() == 2) { // Ctrl
			if (keyCode == 68) { // D
				return new CtrlD();
			} else if (keyCode == 67) { // C
				return new CtrlC();
			} else if (keyCode == 86) { // V
				return new CtrlV();
			} else if (keyCode == 90) { // Z
				return new CtrlZ();
			} else if (keyCode == 83) { // S
				return new CtrlS();
			} else if (keyCode == 38) { // 上箭头
				return new CtrlUp();
			} else if (keyCode == 40) { // 下箭头
				return new CtrlDown();
			} else if (keyCode==37) {// 左箭头
				return new CtrlLeft();
			} else if (keyCode == 39) { // 右箭头
				return new CtrlRight();
			} else if (keyCode==96) {
				return new Ctrl0();
			} else if (keyCode == 97) { // 1
				return new Ctrl1();
			} else if(keyCode==101) { // 5
				return new Ctrl5();
			}
		}
		return null;
	}
}


class KeyBoardContext {
	private KeyBoard keyboard;
	public KeyBoardContext(KeyEvent e) {
		this.keyboard = KeyBoardFactory.createKeyBoard(e);
	}
	public void run() {
		if(keyboard!=null)
		keyboard.run();
	}
	public void set(KeyEvent e, PixPoint lefttop, PixPoint rightbottom, Vector<Integer> clickObject,
			Vector<PixPoint> elements, PaintBrush paintbrush, int newCreate, Editor frame, EditorPanel editorpanel,
			Vector<PixPoint> copy, int activeLayer, Vector<Layer> layers, Pointer pointer) {
		if(keyboard!=null)
		keyboard.set(e, lefttop, rightbottom, clickObject, elements, paintbrush, newCreate, frame, editorpanel, copy, activeLayer, layers, pointer);
	}
}



public abstract class KeyBoard {
	KeyEvent e;
	PixPoint lefttop;
	PixPoint rightbottom;
	Vector<Integer> clickObject;
	Vector<PixPoint> elements;
	PaintBrush paintbrush;
	int newCreate;
	Editor frame;
	EditorPanel editorpanel;
	Vector<PixPoint> copy;
	int activeLayer;
	Vector<Layer> layers;
	Pointer pointer;
	
	public void set(KeyEvent e, PixPoint lefttop, PixPoint rightbottom, Vector<Integer> clickObject,
			Vector<PixPoint> elements, PaintBrush paintbrush, int newCreate, Editor frame, EditorPanel editorpanel,
			Vector<PixPoint> copy, int activeLayer, Vector<Layer> layers, Pointer pointer) {
		this.e = e;
		this.lefttop = lefttop;
		this.rightbottom = rightbottom;
		this.clickObject = clickObject;
		this.elements = elements;
		this.paintbrush = paintbrush;
		this.newCreate = newCreate;
		this.frame = frame;
		this.editorpanel = editorpanel;
		this.copy = copy;
		this.activeLayer = activeLayer;
		this.layers = layers;
		this.pointer = pointer;
	}
	
	public abstract void run();
}

class Shift extends KeyBoard {
	
	@Override
	public void run() {
		if (paintbrush.getGraphicsType() == Shape.SQUARE) {
			paintbrush.setGraphicsType(Shape.RECTANGLE);
		} else if (paintbrush.getGraphicsType() == Shape.CIRCLE) {
			paintbrush.setGraphicsType(Shape.OVAL);
		}
	}
}

class CtrlD extends KeyBoard {

	@Override
	public void run() {
		Vector<PixPoint> all = new Vector<PixPoint>();
		for(Integer c: clickObject) {
			all.add(elements.get(c));
			all.add(elements.get(c+1));
			all.add(elements.get(c+2));
		}
		elements.removeAll(all);
		clickObject.clear();
		editorpanel.place();
		editorpanel.updateUI();	
	}
	
}

class CtrlC extends KeyBoard {

	@Override
	public void run() {
		copy.clear();
		PixPoint p1 = null, p2 = null;
		int offset = -50;
		for(Integer c: clickObject) {

			try {
				p1 = (PixPoint) elements.get(c).clone();
				p2 = (PixPoint) elements.get(c+1).clone();
				
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			p1.move(p1.x + offset, p1.y + offset);
			p2.move(p1.x + deltax, p1.y + deltay);
			
			copy.add(p1);
			copy.add(p2);
			copy.add(new PixPoint(-1, -1, paintbrush.panColor, Shape.CUT, paintbrush.panSize,
					paintbrush.dash));				
		}
	}
	
}

class CtrlV extends KeyBoard {

	@Override
	public void run() {
		if(copy!=null&&copy.size()!=0) {
			elements.addAll(copy);
			editorpanel.updateUI();
		}	
	}
	
}

class CtrlZ extends KeyBoard {

	@Override
	public void run() {
		// system.out.println("hello");
		Layer layer = layers.get(activeLayer);
		int start = newCreate;
		boolean candel = true;
		if(clickObject.size()>1) return;
		for(int i=start+1;i<elements.size();i++) {
			if(elements.get(i).paintbrush.getGraphicsType() == Shape.CUT && i!=elements.size()-1) {
				candel = false;
			}
		}
		if(candel) {
			editorpanel.place();
			layer.removeElementFrom(start);
			editorpanel.updateUI();
		}
		
	}
	
}

class CtrlS extends KeyBoard {

	@Override
	public void run() {
		if (frame.openFilePath == null) {
			frame.dump();
		} else {
			FileManager.saveFile(frame.openFilePath, layers);
		}
	}
	
}

class CtrlUp extends KeyBoard {

	@Override
	public void run() {
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			PixPoint p2 = elements.get(c+1);
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			p1.move(p1.x, lefttop.y);
			p2.move(p1.x + deltax, p1.y + deltay);				
		}	
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p2 = elements.get(c+1);
			rightbottom.move(Math.max(p2.x, rightbottom.x), Math.max(p2.y, rightbottom.y));
		}
		editorpanel.updateUI();
	}
	
}

class CtrlDown extends KeyBoard {

	@Override
	public void run() {
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			PixPoint p2 = elements.get(c+1);
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			
			p1.move(p1.x, rightbottom.y - deltay);
			p2.move(p1.x + deltax, p1.y + deltay);				
		}	
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			lefttop.move(Math.min(lefttop.x, p1.x), Math.min(p1.y, lefttop.y));
		}
		editorpanel.updateUI();	
	}
	
}

class CtrlLeft extends KeyBoard {

	@Override
	public void run() {
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			PixPoint p2 = elements.get(c+1);
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			
			p1.move(lefttop.x, p1.y);
			p2.move(p1.x + deltax, p1.y + deltay);				
		}	
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p2 = elements.get(c+1);
			rightbottom.move(Math.max(p2.x, rightbottom.x), Math.max(p2.y, rightbottom.y));
		}
		editorpanel.updateUI();	
	}
	
}

class CtrlRight extends KeyBoard {

	@Override
	public void run() {
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			PixPoint p2 = elements.get(c+1);
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			
			p1.move(rightbottom.x-deltax, p1.y);
			p2.move(p1.x + deltax, p1.y + deltay);				
		}	
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			lefttop.move(Math.min(lefttop.x, p1.x), Math.min(p1.y, lefttop.y));
		}
		editorpanel.updateUI();
	}
	
}

class Ctrl0 extends KeyBoard {

	@Override
	public void run() {
		for(Integer c: clickObject) {   // 0
			PixPoint p1 = elements.get(c);
			PixPoint p2 = elements.get(c+1);
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			int x = (lefttop.x+rightbottom.x)/2;
			int y = (lefttop.y+rightbottom.y)/2;
			
			p1.move(p1.x, y - deltay/2 );
			p2.move(p1.x + deltax, p1.y + deltay);				
		}	
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			lefttop.move(Math.min(lefttop.x, p1.x), Math.min(p1.y, lefttop.y));
		}
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p2 = elements.get(c+1);
			rightbottom.move(Math.max(p2.x, rightbottom.x), Math.max(p2.y, rightbottom.y));
		}			
		editorpanel.updateUI();
	}
	
}

class Ctrl1 extends KeyBoard {

	@Override
	public void run() {
		for(Integer c: clickObject) {   // 0
			PixPoint p1 = elements.get(c);
			PixPoint p2 = elements.get(c+1);
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			int x = (lefttop.x+rightbottom.x)/2;
			int y = (lefttop.y+rightbottom.y)/2;
			
			p1.move(x-deltax/2, p1.y );
			p2.move(p1.x + deltax, p1.y + deltay);				
		}	
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			lefttop.move(Math.min(lefttop.x, p1.x), Math.min(p1.y, lefttop.y));
		}
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p2 = elements.get(c+1);
			rightbottom.move(Math.max(p2.x, rightbottom.x), Math.max(p2.y, rightbottom.y));
		}			
		editorpanel.updateUI();	
	}
	
}

class Ctrl5 extends KeyBoard {

	@Override
	public void run() {
		for(Integer c: clickObject) {   // 0
			PixPoint p1 = elements.get(c);
			PixPoint p2 = elements.get(c+1);
			int deltax = p2.x - p1.x;
			int deltay = p2.y - p1.y;
			int x = (lefttop.x+rightbottom.x)/2;
			int y = (lefttop.y+rightbottom.y)/2;
			
			p1.move(x-deltax/2, y-deltay/2 );
			p2.move(p1.x + deltax, p1.y + deltay);				
		}	
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p1 = elements.get(c);
			lefttop.move(Math.min(lefttop.x, p1.x), Math.min(p1.y, lefttop.y));
		}
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);
		for(Integer c: clickObject) {
			PixPoint p2 = elements.get(c+1);
			rightbottom.move(Math.max(p2.x, rightbottom.x), Math.max(p2.y, rightbottom.y));
		}			
		editorpanel.updateUI();
	}
	
}

