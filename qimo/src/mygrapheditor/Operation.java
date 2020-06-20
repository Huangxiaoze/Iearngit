package mygrapheditor;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Vector;

/*
 * 静态工厂模式
 */

class OperationFactory {
	public static Operation createOperation(Pointer pointer) {
		switch (pointer) {
		case MOVE: return new Move();
		case NW_RESIZE: return new NWResize();
		case SW_RESIZE: return new SWResize();
		case W_RESIZE: return new WResize();
		case NE_RESIZE: return new NEResize();
		case SE_RESIZE: return new SEResize();
		case E_RESIZE: return new EResize();
		case N_RESIZE: return new NResize();
		case S_RESIZE: return new SResize();
		case ROTATE: return new Rotate();
		default:return null;
		}	
	}
}

/*
 * 策略模式
 */
class Context {
	private Operation oper = null;
	public Context(Pointer pointer) {
		oper = OperationFactory.createOperation(pointer);
	}
	public void set(MouseEvent e, MouseEvent forMove, PixPoint lefttop, PixPoint rightbottom, Vector<Integer> clickObject,
			Vector<PixPoint> elements, int keyCode) {
		oper.set(e, forMove, lefttop, rightbottom, clickObject,elements, keyCode);
		
	}
	public void run() {
		oper.action();
	}
	
}

abstract public class Operation {
	MouseEvent e;
	MouseEvent forMove;
	PixPoint lefttop;
	PixPoint rightbottom;
	Vector<Integer> clickObject;
	Vector<PixPoint> elements;
	PixPoint p1 = null;
	PixPoint p2 = null;
	int deltax, deltay;
	int keyCode;
	public Operation() {}
	public Operation(MouseEvent e, MouseEvent forMove, PixPoint lefttop, PixPoint rightbottom,
			Vector<Integer> clickObject, Vector<PixPoint> elements, int keyCode) {
		super();
		this.e = e;
		this.forMove = forMove;
		this.lefttop = lefttop;
		this.rightbottom = rightbottom;
		this.clickObject = clickObject;
		this.elements = elements;
		this.keyCode = keyCode;
	}

	public void set(MouseEvent e, MouseEvent forMove, PixPoint lefttop, 
			PixPoint rightbottom, Vector<Integer> clickObject,
			Vector<PixPoint> elements, int keyCode) {
		this.e = e;
		this.forMove = forMove;
		this.lefttop = lefttop;
		this.rightbottom = rightbottom;
		this.clickObject = clickObject;
		this.elements = elements;
		this.keyCode = keyCode;
	}
	
	abstract public void action();
}

class Move extends Operation {
	public Move() {}
	
	@Override
	public void action() {
		deltax = e.getX() - forMove.getX();
		deltay = e.getY() - forMove.getY();
		for(Integer c: clickObject) {
			p1 = elements.get(c);
			p2 = elements.get(c + 1);
			p1.move(p1.x + deltax, p1.y + deltay);
			p2.move(p2.x + deltax, p2.y + deltay);
		}
		lefttop.move(lefttop.x+deltax, lefttop.y+deltay);
		rightbottom.move(rightbottom.x + deltax, rightbottom.y + deltay);
	}
}

class NWResize extends Operation {
	@Override
	public void action() {
		PixPoint lefttop1 = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		deltax = e.getX() - lefttop.x;
		deltay = e.getY() - lefttop.y;
		int increment = deltax;
		for(Integer c: clickObject) {
			p1 = elements.get(c);
			p2 = elements.get(c+1);
			int x = p2.x - p1.x;
			int y = p2.y - p1.y;
			if(keyCode==16) { // 按下了shift键
				int x1 = p2.x - (p1.x + increment);
				p1.move(p1.x + increment, p2.y - x1*y/x);
				lefttop1.move(Math.min(p1.x, lefttop1.x), Math.min(p1.y, lefttop1.y));
			} else {
				p1.move(p1.x + deltax, p1.y + deltay);
			}
		}
		if(keyCode==16) {
			lefttop.move(lefttop1.x, lefttop1.y);
		} else {
			lefttop.move(e.getX(), e.getY());
		}
	}
}

class SWResize extends Operation {
	public void action() {
		deltax = e.getX() - lefttop.x;
		deltay = e.getY() - rightbottom.y;
		
		int increment = deltax;
		int min = 2000;
		int max = -2000;
		for(Integer c: clickObject) {
			p1 = elements.get(c);
			p2 = elements.get(c + 1);
			int x = p2.x - p1.x;
			int y = p2.y - p1.y;
	
			if(keyCode==16) { // 按下了shift键
				int x1 = p2.x - (p1.x + increment);	
				p1.move(p1.x + increment, p1.y);
				p2.move(p2.x, p1.y + x1*y/x);
				min = Math.min(p1.x, min);
				max = Math.max(p2.y, max);
			} else {
				p1.move(p1.x + deltax, p1.y);
				p2.move(p2.x, p2.y + deltay);
			}
		}
		if(keyCode==16) {
			lefttop.move(min, lefttop.y);
			rightbottom.move(rightbottom.x, max);		
		} else {
			lefttop.move(e.getX(), lefttop.y);
			rightbottom.move(rightbottom.x, e.getY());
		}
	}
}

class WResize extends Operation {
	public void action() {
		deltax = e.getX() - lefttop.x;
		for(Integer c: clickObject) {
			p1 = elements.get(c);
			p2 = elements.get(c + 1);

			p1.move(p1.x+deltax, p1.y);
		}				

		lefttop.move(e.getX(), lefttop.y);

	}
}

class NEResize extends Operation {
	public void action() {
		deltax = e.getX() - rightbottom.x;
		deltay = e.getY() - lefttop.y;
		for(Integer c: clickObject) {
			p1 = elements.get(c);
			p2 = elements.get(c + 1);
			p1.move(p1.x, p1.y + deltay);
			p2.move(p2.x + deltax, p2.y);
		}
		lefttop.move(lefttop.x, e.getY());
		rightbottom.move(e.getX(), rightbottom.y);
	}
}

class SEResize extends Operation {
	public void action() {
		deltax = e.getX() - rightbottom.x;
		deltay = e.getY() - rightbottom.y;
		
		for(Integer c: clickObject) {
			p2 = elements.get(c + 1);
			p2.move(p2.x + deltax, p2.y + deltay);
		}
		rightbottom.move(e.getX(), e.getY());
	}
}
class EResize extends Operation {
	public void action() {
		deltax = e.getX() - rightbottom.x;
		
		for(Integer c: clickObject) {
			p2 = elements.get(c + 1);
			p2.move(p2.x + deltax, p2.y);
		}
		rightbottom.move(e.getX(), rightbottom.y);
	}
}
class NResize extends Operation {
	public void action() {
		deltay = e.getY() - lefttop.y;
		for(Integer c: clickObject) {
			p1 = elements.get(c);
			p1.move(p1.x, p1.y + deltay);
		}
		lefttop.move(lefttop.x, e.getY());
	}
}
class SResize extends Operation {
	public void action() {
		deltay = e.getY() - rightbottom.y;
		for(Integer c: clickObject) {
			p2 = elements.get(c + 1);
			p2.move(p2.x, p2.y + deltay);
		}
		rightbottom.move(rightbottom.x, e.getY());
	}
}

class Rotate extends Operation {
	public void action() {
		int x = (lefttop.x + rightbottom.x) / 2;
		int y = (lefttop.y + rightbottom.y) / 2;
		double e12 = (forMove.getX() - x) * (forMove.getX() - x)
				+ (forMove.getY() - y) * (forMove.getY() - y);
		double e22 = (e.getX() - x) * (e.getX() - x) + (e.getY() - y) * (e.getY() - y);
		double e32 = (e.getX() - forMove.getX()) * (e.getX() - forMove.getX())
				+ (e.getY() - forMove.getY()) * (e.getY() - forMove.getY());
		double cosValue = (e12 + e22 - e32) / (2 * Math.sqrt(e12) * Math.sqrt(e22));

		if (!Double.isNaN(Math.acos(cosValue))) {
			for(Integer c: clickObject) {
				p1 = elements.get(c);
				p1.rotate((Math.acos(cosValue) + p1.getRotate()) % (2 * Math.PI));
			}
			lefttop.rotate((Math.acos(cosValue) + p1.getRotate()) % (2 * Math.PI));
		}
	}
}

