package mygrapheditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

//������
public class EditorPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	PaintBrush paintbrush = new PaintBrush();
	public static int newCreate = -1; // �½�Ԫ�ص���ʼ����
	int editObj = -1; // Ctrl+������ѡ�еĶ���
	int copy = -1; // ����
	private boolean mouse_Released = true; // �������Ƿ���£��������߿����ʾ
	private boolean confirmPlace = true; // Ԫ��ȷ�Ϸ���
	int outlineSize = 10; // ������߽���پ���,�仯���
	Pointer pointer = Pointer.CREATE_NEW; // ͼ��������״̬
	MouseEvent forMove; // �ƶ�ͼ��Ԫ��ʱ����¼ǰһ���ƶ��ĵ�
	private boolean isResize = false; // ����Ƿ��ڱ༭ͼ�ν׶�
	int keyCode = -1; // ��¼���̵ļ�ֵ
	public static float[] dash_set = new float[] { 5, 10 }; // ����
	private int activeLayer = 0; // ��ǰ������ͼ��
	private boolean hasActive = true;
	// ͼ��Ԫ�ؼ���
	private Vector<Layer> layers;

	// ��Ԫ��
	private final Editor frame;

	// ������
	public EditorPanel(Editor frame) {
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);

		
		
		this.setBackground(Color.white);
		this.frame = frame;
		layers = new Vector<Layer>();
		layers.add(new Layer());
		layers.get(0).setActive(true);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		setFocusable(true);
	}

	public void setFillColor(Color fillColor) {
		this.paintbrush.setFillColor(fillColor);
		if (newCreate == -1) {
			return;
		}
		PixPoint target = layers.get(activeLayer).getElements().get(newCreate);
		if (target.paintbrush.getGraphicsType().isEditable()) {
			target.paintbrush.setFillColor(fillColor);
			this.updateUI();
		}
	}

	/*
	 * �ͻ��˸���ͼ��
	 */
	public void place() { // ģ��ͼ�η��²���
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);
		newCreate = -1;
		confirmPlace = true;
		isResize = false;
		pointer = Pointer.CREATE_NEW;
		this.updateUI();
	}

	public int getLayersSize() { // ��ȡͼ��ĸ���
		return this.layers.size();
	}

	public boolean hasActiveLayer() { // �ж��Ƿ��л�Ծ��ͼ��
		return this.hasActive;
	}

	public void setLayers(Vector<Layer> l) { // ����ͼ�㣬��ʼ������
		place();
		this.layers = l;
		hasActive = false;
		for (int i = 0; i < layers.size(); i++) {
			if (layers.get(i).isActive()) {
				activeLayer = i;
				hasActive = true;
				break;
			}
		}
		this.updateUI();
	}

	public int getActiveLayer() { // ��ȡ��ǰ��Ծ��ͼ������
		return activeLayer;
	}

	public void createNewLayer() {
		layers.add(new Layer());
		activeLayer = layers.size() - 1;
		layers.get(activeLayer).setActive(true); // ����Ϊ�������Ĳ�
		hasActive = true;
		this.place();
	}

	public void setActiveLayer(int l) { // ����ͼ��lΪ�༭״̬
		place();
		layers.get(l).setActive(true);
		this.activeLayer = l;
		hasActive = true;
		this.updateUI();
	}

	public void setLayerActiveProperty(int l, boolean s) { // ����ͼ�����ʾ����
		place();
		this.layers.get(l).setActive(s);
		if (l == activeLayer) { // ������ǰ��Ծ��ͼ��
			if (s == false) {// ���ػ�Ծͼ�㣬����ѡ��һ����Ծ��ͼ��
				boolean hasActive = false;
				for (int i = layers.size() - 1; i >= 0; i--) {
					if (layers.get(i).isActive()) {
						activeLayer = i;
						hasActive = true;
						break;
					}
				}
				this.hasActive = hasActive;
			} else { // ��ʾ��Ծͼ��
				activeLayer = l;
				hasActive = true;
			}
		} else { // �����ǻ�Ծͼ��
			if (s == false) { // ���أ�ɶҲ����

			} else { // ��ʾ�����

			}
		}
		this.updateUI();
	}

	public void clearCurLayer() {
		System.out.println("clearCurLayer");
		this.place();
		if (this.layers.size() == 0) {
			return;
		}
		this.layers.set(activeLayer, new Layer());
		this.updateUI();
	}

	public void clearLayer() { // ���ͼ��
		hasActive = false;
		layers = new Vector<Layer>();
		this.updateUI();
	}

	public Vector<Layer> getLayers() { // ����ͼ�����
		return layers;
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g.create();

		for (int j = 0; j < layers.size(); j++) { // ���λ�������ͼ��Ԫ��
			Layer layer = layers.get(j);
//			System.out.println("layer " + (j + 1) + " size is " + layer.getElements().size());
			boolean drawframe = mouse_Released && j == activeLayer&&newCreate!=-1;
			if (layer.isActive()) {
				layer.draw(g2d, g, drawframe, (Vector<Integer>)clickObject.clone(), lefttop, rightbottom);
			}
		}

		// ��������Ҫ���ٵ�
		g2d.dispose();
	}
	
	private Position getPosition(PixPoint p1, PixPoint p2,  MouseEvent e, int size) {
		Position pos = Position.OUTER;
		if (p1.paintbrush.getGraphicsType().isEditable() && size> 0) { // Ԫ���ǿ��Ա༭��
			isResize = true;
			if (p1.x < e.getX() && p2.x > e.getX() && p1.y < e.getY() && p2.y > e.getY()) { // λ���ڲ���ƽ��
				pos = Position.INNER;
			} else { // ָ��λ���ⲿ
				if (Math.abs(e.getX() - p1.x) < 25) { // ���
					if (Math.abs(e.getY() - p1.y) < 50) { // �Ϸ�
						pos = Position.NW;
					} else if (Math.abs(e.getY() - p2.y) < 50) { // �·�
						pos = Position.SW;
					} else if (Math.abs(e.getY() - (p1.y + p2.y) / 2) < 50) { // �м�
						pos = Position.WEST;
					}
				} else if (Math.abs(e.getX() - p2.x) < 25) { // �Ҳ�
					if (Math.abs(e.getY() - p1.y) < 50) { // �Ϸ�
						pos = Position.NE;
					} else if (Math.abs(e.getY() - p2.y) < 50) { // �·�
						pos = Position.SE;
					} else if (Math.abs(e.getY() - (p1.y + p2.y) / 2) < 50) { // �м�
						pos = Position.EAST;
					}
				} else if (Math.abs(e.getX() - (p1.x + p2.x) / 2) < 25) { // �м�
					if (Math.abs(e.getY() - p1.y) < 50) { // �Ϸ�
						pos = Position.NORTH;
					} else if (Math.abs(e.getY() - p2.y) < 50) {
						pos = Position.SOUTH;
					}
				} else {
					pos = Position.OUTER;
				}
			}

		}
		
		return pos;
	}
	
	// ѡ�ж������
	Vector<Integer> clickObject = new Vector<Integer>();
	PixPoint lefttop;
	PixPoint rightbottom;
	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("mousePressed "+confirmPlace+" isResize="+isResize);		
		if (!hasActive) {return;}
		forMove = e;
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();

		if (keyCode == 17 || paintbrush.getGraphicsType() == Shape.FILLCOLOR) { // ������Ctrl��, �༭; ���ߴ��������ɫģʽ
			editObj = -1;
			PixPoint p1 = null;
			PixPoint p2 =null;
			// �鿴�Ƿ��пɱ༭��Ԫ�ذ����õ�
			for (int i = 0; i < elements.size(); i++) {
				p1 = elements.get(i);
				if (!p1.paintbrush.getGraphicsType().isEditable()) {
					continue;
				}
				p2 = elements.get(i + 1);
				if (p1.x < e.getX() && e.getX() < p2.x && p1.y < e.getY() && e.getY() < p2.y) {
					editObj = i;
				}
				i++;
			}
			
			
			if (paintbrush.getGraphicsType() == Shape.FILLCOLOR) {// ���������ɫģʽ
				if (editObj != -1) { // ��Ԫ�ذ����õ�
					// ������ɫ
					layers.get(activeLayer).getElements().get(editObj).paintbrush.setFillColor(paintbrush.getFillColor());
					this.updateUI();
				}
			} else { // ���ڱ༭Ԫ��ģʽ
				System.out.println("editObj="+editObj);
				this.place(); // ����ǰ��Ԫ�ط���
				if(editObj!=-1) { // ��Ԫ�ذ����õ�
					if(!clickObject.contains(editObj)) {// ��Ԫ��δ����ӵ��༭������
						clickObject.add(editObj); // �������뵽�༭����
					}
					confirmPlace = false; // ����ΪԪ��δ����״̬		
					newCreate = editObj;
					System.out.println("clickObject:"+clickObject+"elements.size"+elements.size());
					for(int i=0;i<clickObject.size();i++) {
						System.out.println(elements.get(clickObject.get(i)));
					}
					System.out.println("----------");
					// ���°������б༭Ԫ�صľ��α༭�����Ͻǣ����½�
					for(int i=0;i<clickObject.size();i++) {

						try {
							p1 = (PixPoint) elements.get(clickObject.get(i)).clone();
							p2 = (PixPoint) elements.get(clickObject.get(i)+1).clone();
						} catch (CloneNotSupportedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						lefttop.move(Math.min(p1.x, lefttop.x), Math.min(p1.y, lefttop.y));
						rightbottom.move(Math.max(rightbottom.x, p2.x), Math.max(rightbottom.y, p2.y));
					}
					
					System.out.println("mousePress"+clickObject);
				} 
			}
			this.updateUI();
			return;
		}
		
		// ����Ƿ��ڱ༭Ԫ��״̬�������¼�������ɫ�¼��������Ѿ���顣
		// ������ڱ༭״̬����һ�ε��Ԫ��ʱҲ�ᴥ��mousePress�¼�����˸ô��Ĵ�������Ҫ�ġ�
		if (!confirmPlace && clickObject.size()!=0 ) {
			// ���ɱ༭Ԫ��δȷ�Ϸ��ã�����Ϊ�Ǵ��ڱ༭״̬��
			// �����겻�����Ԫ��,���Ǽ����갴��ʱ��λ�ã��Ի�ȡ�����קʱ�������Ӧ�ı༭����
			if(clickObject.size()==1) { // ���༭Ԫ��ֻ��һ����ֱ�ӽ��༭���С����Ϊ��Ԫ�صı༭���С
				try {
					lefttop = (PixPoint) elements.get(clickObject.get(0)).clone();
					rightbottom = (PixPoint) elements.get(clickObject.get(0)+1).clone();
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
				
			}
			// ��ȡ��갴�µ��ڱ༭���λ��
			Position  pos = getPosition(lefttop, rightbottom, e, elements.size());
			System.out.println(pos);
			switch(pos) {
			case INNER:pointer = Pointer.MOVE;break;
			case NW:pointer = Pointer.NW_RESIZE;break;
			case SW:pointer = Pointer.SW_RESIZE;break;
			case WEST:pointer = Pointer.W_RESIZE;break;
			case NE:pointer = Pointer.NE_RESIZE;break;
			case SE:pointer = Pointer.SE_RESIZE;break;
			case EAST:pointer = Pointer.E_RESIZE;break;
			case NORTH:pointer = Pointer.N_RESIZE;break;
			case SOUTH:pointer = Pointer.S_RESIZE;break;
			case OUTER:pointer = Pointer.CREATE_NEW;break;
			}
			if (pos != Position.OUTER && keyCode == 18) { // ����������Ƿ��ⲿ�㣬�Ұ�����Alt���� �����л�����תģʽ
					pointer = Pointer.ROTATE;
			}			
			return;
		}
		
		
		// �����µ�Ԫ��
		newCreate = elements.size();// �½�ͼ�����ʼ��
		mouse_Released = false; // �������߿�����
		clickObject.clear();
		if(paintbrush.getGraphicsType().isEditable()) {
			clickObject.add(newCreate);
		}
//		pointer = Pointer.CREATE_NEW;
		System.out.println("hahhahahah");
		
		System.out.println("newCreate = "+newCreate+", clickObject="+clickObject);
		try {
			switch (paintbrush.graphicsType) {
			case BRUSH:
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;
			case LINE: // ֱ��
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;
			case RECTANGLE:
			case SQUARE: // ����
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;
			case CIRCLE:
			case OVAL: // Բ
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;

			}
		} catch (CloneNotSupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.updateUI();

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!hasActive || keyCode == 17) {
			return;
		}

		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
		if (newCreate == -1 || paintbrush.getGraphicsType() == Shape.FILLCOLOR) {
			confirmPlace = true;
			return;
		}
		// �༭״̬����ק���ı��ض�Ԫ��
		if (!confirmPlace) {
			if (elements.size() > 0) {		
				isResize = true;
				Operation oper = OperationFactory.getOperation(pointer);
				oper.set(e, forMove, lefttop, rightbottom, clickObject, elements);	
				oper.action();
				forMove = e;
			}
			this.updateUI();
			if (pointer != Pointer.CREATE_NEW) {
				return;
			}
		}
	

		// ����Ԫ�ص���ק����
		try {
			switch (paintbrush.graphicsType) {
			case BRUSH:
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;

			case SQUARE:
			case CIRCLE:
				PixPoint p1 = elements.get(newCreate);
				PixPoint p2 = elements.get(newCreate + 1);
				int width = Math.min(Math.abs(p1.x - e.getX()), Math.abs(p1.y - e.getY()));
				p2.move(p1.x + width, p1.y + width);
				break;

			case LINE:
			case RECTANGLE:
			case OVAL:
				elements.set(elements.size() - 1, new PixPoint(e.getX(), e.getY(), paintbrush));
				break;
			case ERASER:
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;
			}
		} catch (CloneNotSupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(elements.get(newCreate).paintbrush.getGraphicsType().isEditable()) {
			try {
				lefttop = (PixPoint) elements.get(newCreate).clone();
				rightbottom = (PixPoint) elements.get(newCreate+1).clone();
			} catch (CloneNotSupportedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		}
		
		this.updateUI();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		if (!hasActive || isResize || newCreate == -1) {
			return;
		}
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
		if (newCreate == -1 || elements.size() == 0) {
			frame.setCursor(paintbrush.getGraphicsType().getCursor());
			return;
		}
		
		PixPoint p1 = elements.get(newCreate);
		if (p1.paintbrush.getGraphicsType().isEditable()) {
			checkPosition(lefttop, rightbottom, e);
		} else {
			confirmPlace = true;
			pointer = Pointer.CREATE_NEW;
			frame.setCursor(paintbrush.getGraphicsType().getCursor());			
		}

	}
	private void checkPosition(PixPoint p1, PixPoint p2, MouseEvent e) {
		if (p1.paintbrush.getGraphicsType().isEditable()) {
			if (p1.x < e.getX() && p2.x > e.getX() && p1.y < e.getY() && p2.y > e.getY()) { // λ���ڲ�
				confirmPlace = false;
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else { // ָ��λ���ⲿ
				if (Math.abs(e.getX() - p1.x) < outlineSize) { // ���
					if (Math.abs(e.getY() - p1.y) < outlineSize) { // �Ϸ�
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - p2.y) < outlineSize) { // �·�
						// ��bug
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - (p1.y + p2.y) / 2) < outlineSize) { // �м�
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
					} else {
						confirmPlace = true;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
				} else if (Math.abs(e.getX() - p2.x) < outlineSize) { // �Ҳ�
					if (Math.abs(e.getY() - p1.y) < outlineSize) { // �Ϸ�
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - p2.y) < outlineSize) { // �·�
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - (p1.y + p2.y) / 2) < outlineSize) { // �м�
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					} else {
						confirmPlace = true;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
				} else if (Math.abs(e.getX() - (p1.x + p2.x) / 2) < outlineSize) { // �м�
					if (Math.abs(e.getY() - p1.y) < 50) { // �Ϸ�
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - p2.y) < outlineSize) {
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
					} else {
						confirmPlace = true;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
				} else {
					confirmPlace = true;
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				}
			}
		} else {
			confirmPlace = true;
			frame.setCursor(paintbrush.getGraphicsType().getCursor());
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!hasActive) {
			return;
		}
		System.out.println("mouseRelease" + ", " + isResize + ", " + confirmPlace);

		Vector<PixPoint> elements = layers.get(activeLayer).getElements();

		isResize = false;
		if (!confirmPlace || keyCode == 17) {
			return;
		}
		mouse_Released = true;

		if (paintbrush.getGraphicsType().isEditable()) {
			PixPoint p1 = elements.get(newCreate);
			PixPoint p2 = elements.get(newCreate + 1);
			if (p1.x == p2.x && p1.y == p2.y) {
				elements.remove(newCreate + 1);
				elements.remove(newCreate);
				clickObject.remove((Integer)newCreate);
				newCreate = -1;
			} else {
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, Shape.CUT, paintbrush.panSize,
						paintbrush.dash));
			}
		} else {
			if (paintbrush.getGraphicsType() != Shape.FILLCOLOR) {
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, Shape.CUT, paintbrush.panSize,
						paintbrush.dash));
			}
		}

		this.updateUI();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		frame.setCursor(paintbrush.getGraphicsType().getCursor());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!hasActive) {
			return;
		}
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
//		System.out.println(e);
		keyCode = e.getKeyCode();
		if (keyCode == 16) { // ctrl��
			if (paintbrush.getGraphicsType() == Shape.SQUARE) {
				paintbrush.setGraphicsType(Shape.RECTANGLE);
			} else if (paintbrush.getGraphicsType() == Shape.CIRCLE) {
				paintbrush.setGraphicsType(Shape.OVAL);
			}
		}
		if (e.getModifiers() == 2) { // Ctrl
			if (keyCode == 68) { // D
				if (!elements.isEmpty() && newCreate >= 0) {
					PixPoint p1 = elements.get(newCreate);
					if (p1.paintbrush.getGraphicsType().isEditable()) {
						elements.remove(newCreate + 1);
						elements.remove(newCreate);
						this.place();
						this.updateUI();
					}
				}
			} else if (keyCode == 67) { // C
				copy = newCreate;
			} else if (keyCode == 86) { // V
				if (!elements.isEmpty() && copy >= 0) {
					PixPoint p1 = elements.get(copy); // �˴����Կ�������clone
					if (p1.paintbrush.getGraphicsType().isEditable()) {
						PixPoint p2 = elements.get(copy + 1);
						try {
							int offset = -50;
							p1 = (PixPoint) p1.clone();
							p2 = (PixPoint) p2.clone();
							int deltax = p2.x - p1.x;
							int deltay = p2.y - p1.y;
							p1.move(p1.x + offset, p1.y + offset);
							p2.move(p1.x + deltax, p1.y + deltay);
							elements.add(new PixPoint(-1, -1, paintbrush.panColor, Shape.CUT, paintbrush.panSize,
									paintbrush.dash));
							elements.add(copy, (PixPoint) p2.clone());
							elements.add(copy, (PixPoint) p1.clone());
						} catch (CloneNotSupportedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						this.updateUI();
					}
				}
			} else if (keyCode == 90) { // Z
				Layer layer = layers.get(activeLayer);
				int start = newCreate;
				if (pointer == Pointer.CREATE_NEW) {
					place();
					layer.removeElementFrom(start);
					this.updateUI();
				}
			} else if (keyCode == 83) { // S
				if (frame.openFilePath == null) {
					frame.dump();
				} else {
					frame.saveFile(frame.openFilePath);
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!hasActive) {
			return;
		}
		// TODO Auto-generated method stub
		if (keyCode == 16) {
			if (paintbrush.getGraphicsType() == Shape.RECTANGLE) {
				paintbrush.setGraphicsType(Shape.SQUARE);
			} else if (paintbrush.getGraphicsType() == Shape.OVAL) {
				paintbrush.setGraphicsType(Shape.CIRCLE);
			}
		}
		keyCode = -1;
	}

}