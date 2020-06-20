package mygrapheditor;

import java.util.Vector;

public class Main {
public static void main(String[] args) {
	Vector<Integer> a = new Vector();
	a.add(1);
	a.add(2);
	a.add(3);
	a.remove(1);
	a.remove(0);
	System.out.println(a);
}
}
