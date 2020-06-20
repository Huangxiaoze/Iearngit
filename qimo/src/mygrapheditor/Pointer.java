package mygrapheditor;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

/*
 * ÷∏’Î∫¨“Â¿‡
 */
public enum Pointer {
	MOVE, W_RESIZE, E_RESIZE, N_RESIZE, S_RESIZE, NW_RESIZE, 
	NE_RESIZE, SW_RESIZE, SE_RESIZE, CREATE_NEW, ROTATE;
}

enum Position {
	INNER, OUTER, WEST, EAST, SOUTH, NORTH, NW, NE, SW, SE;
	
	public Cursor getCursor(Cursor outer) {
		switch(this) {
		case EAST:
			return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
		case INNER:
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		case NE:
			 return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
		case NORTH:
			 return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
		case NW:
			 return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
		case OUTER:
			 return outer;
		case SE:
			 return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
		case SOUTH:
			 return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
		case SW:
			return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
			 
		case WEST:
			 return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
		default:
			 return null;
		}
	}

}
