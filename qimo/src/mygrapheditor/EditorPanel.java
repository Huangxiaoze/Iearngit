package mygrapheditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

//画板类
public class EditorPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	PaintBrush paintbrush = new PaintBrush();
	public static int newCreate = -1; // 新建元素的起始坐标
	int editObj = -1; // Ctrl+鼠标左键选中的对象
	; // 复制
	private boolean mouse_Released = true; // 监测鼠标是否放下，控制虚线框的显示
	private boolean confirmPlace = true; // 元素确认放下
	int outlineSize = 10; // 鼠标进入边界多少距离,变化鼠标
	Pointer pointer = Pointer.CREATE_NEW; // 图形所处的状态
	MouseEvent forMove; // 移动图形元素时，记录前一个移动的点
	private boolean isResize = false; // 监测是否处于编辑图形阶段
	int keyCode = -1; // 记录键盘的键值
	public static float[] dash_set = new float[] { 5, 10 }; // 虚线
	private int activeLayer = 0; // 当前操作的图层
	private boolean hasActive = true;
	// 图层元素集合
	private Vector<Layer> layers;

	// 父元素
	private final Editor frame;

	// 构造器
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
	 * 客户端更改图形
	 */
	public void place() { // 模拟图形放下操作
		lefttop = new PixPoint(2000, 2000, Color.RED, Shape.RECTANGLE, 2, true);
		rightbottom = new PixPoint(-2000, -2000, Color.RED, Shape.RECTANGLE, 2, true);
		newCreate = -1;
		confirmPlace = true;
		isResize = false;
		pointer = Pointer.CREATE_NEW;
		this.updateUI();
	}

	public int getLayersSize() { // 获取图层的个数
		return this.layers.size();
	}

	public boolean hasActiveLayer() { // 判断是否有活跃的图层
		return this.hasActive;
	}

	public void setLayers(Vector<Layer> l) { // 设置图层，初始化参数
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

	public int getActiveLayer() { // 获取当前活跃的图层索引
		return activeLayer;
	}

	public void createNewLayer() {
		layers.add(new Layer());
		activeLayer = layers.size() - 1;
		layers.get(activeLayer).setActive(true); // 设置为被操作的层
		hasActive = true;
		this.place();
	}

	public void setActiveLayer(int l) { // 激活图层l为编辑状态
		place();
		layers.get(l).setActive(true);
		this.activeLayer = l;
		hasActive = true;
		this.updateUI();
	}

	public void setLayerActiveProperty(int l, boolean s) { // 设置图层的显示属性
		place();
		this.layers.get(l).setActive(s);
		if (l == activeLayer) { // 操作当前活跃的图层
			if (s == false) {// 隐藏活跃图层，重新选择一个活跃的图层
				boolean hasActive = false;
				for (int i = layers.size() - 1; i >= 0; i--) {
					if (layers.get(i).isActive()) {
						activeLayer = i;
						hasActive = true;
						break;
					}
				}
				this.hasActive = hasActive;
			} else { // 显示活跃图层
				activeLayer = l;
				hasActive = true;
			}
		} else { // 操作非活跃图层
			if (s == false) { // 隐藏，啥也不做

			} else { // 显示，如果

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

	public void clearLayer() { // 清空图层
		hasActive = false;
		layers = new Vector<Layer>();
		this.updateUI();
	}

	public Vector<Layer> getLayers() { // 返回图层对象
		return layers;
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g.create();

		for (int j = 0; j < layers.size(); j++) { // 依次绘制所有图层元素
			Layer layer = layers.get(j);
//			System.out.println("layer " + (j + 1) + " size is " + layer.getElements().size());
			boolean drawframe = mouse_Released && j == activeLayer&&newCreate!=-1;
			if (layer.isActive()) {
				layer.draw(g2d, g, drawframe, (Vector<Integer>)clickObject.clone(), lefttop, rightbottom);
			}
		}

		// 副本用完要销毁掉
		g2d.dispose();
	}
	
	private Position getPosition(PixPoint p1, PixPoint p2,  MouseEvent e, int offset) {
		Position pos = Position.OUTER;
		if (p1.paintbrush.getGraphicsType().isEditable()) { // 元素是可以编辑的
			if (p1.x < e.getX() && p2.x > e.getX() && p1.y < e.getY() && p2.y > e.getY()) { // 位于内部，平移
				pos = Position.INNER;
			} else { // 指针位于外部
				if (Math.abs(e.getX() - p1.x) < offset/2) { // 左侧
					if (Math.abs(e.getY() - p1.y) < offset) { // 上方
						pos = Position.NW;
					} else if (Math.abs(e.getY() - p2.y) < offset) { // 下方
						pos = Position.SW;
					} else if (Math.abs(e.getY() - (p1.y + p2.y) / 2) < offset) { // 中间
						pos = Position.WEST;
					}
				} else if (Math.abs(e.getX() - p2.x) < offset/2) { // 右侧
					if (Math.abs(e.getY() - p1.y) < offset) { // 上方
						pos = Position.NE;
					} else if (Math.abs(e.getY() - p2.y) < offset) { // 下方
						pos = Position.SE;
					} else if (Math.abs(e.getY() - (p1.y + p2.y) / 2) < offset) { // 中间
						pos = Position.EAST;
					}
				} else if (Math.abs(e.getX() - (p1.x + p2.x) / 2) < offset/2) { // 中间
					if (Math.abs(e.getY() - p1.y) < offset) { // 上方
						pos = Position.NORTH;
					} else if (Math.abs(e.getY() - p2.y) < offset) {
						pos = Position.SOUTH;
					}
				} else {
					pos = Position.OUTER;
				}
			}

		}
		
		return pos;
	}
	
	// 选中多个对象
	Vector<Integer> clickObject = new Vector<Integer>();
	PixPoint lefttop;
	PixPoint rightbottom;
	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("mousePressed "+confirmPlace+" isResize="+isResize);		
		if (!hasActive) {return;}
		forMove = e;
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();

		if (keyCode == 17 || paintbrush.getGraphicsType() == Shape.FILLCOLOR) { // 按下了Ctrl键, 编辑; 或者处于填充颜色模式
			editObj = -1;
			PixPoint p1 = null;
			PixPoint p2 =null;
			// 查看是否有可编辑的元素包含该点
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
			
			
			if (paintbrush.getGraphicsType() == Shape.FILLCOLOR) {// 处于填充颜色模式
				if (editObj != -1) { // 有元素包含该点
					// 进行填色
					layers.get(activeLayer).getElements().get(editObj).paintbrush.setFillColor(paintbrush.getFillColor());
					this.updateUI();
				}
			} else { // 处于编辑元素模式
				System.out.println("editObj="+editObj);
				this.place(); // 将先前的元素放下
				if(editObj!=-1) { // 有元素包含该点
					if(!clickObject.contains(editObj)) {// 该元素未被添加到编辑队列中
						clickObject.add(editObj); // 将它加入到编辑队列
					}
					confirmPlace = false; // 设置为元素未放下状态		
					newCreate = editObj;
					System.out.println("clickObject:"+clickObject+"elements.size"+elements.size());
					for(int i=0;i<clickObject.size();i++) {
						System.out.println(elements.get(clickObject.get(i)));
					}
					System.out.println("----------");
					// 更新包含所有编辑元素的矩形编辑框，左上角，右下角
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
		
		// 检查是否处于编辑元素状态，键盘事件，和填色事件在上面已经检查。
		// 如果处于编辑状态，第一次点击元素时也会触发mousePress事件。因此该处的处理是需要的。
		if (!confirmPlace && clickObject.size()!=0 ) {
			// 当可编辑元素未确认放置，则认为是处于编辑状态。
			// 点击鼠标不添加新元素,而是检查鼠标按下时的位置，以获取鼠标拖拽时，完成相应的编辑操作
			if(clickObject.size()==1) { // 被编辑元素只有一个，直接将编辑框大小设置为该元素的编辑框大小
				try {
					lefttop = (PixPoint) elements.get(clickObject.get(0)).clone();
					rightbottom = (PixPoint) elements.get(clickObject.get(0)+1).clone();
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
				
			}
			isResize = true;
			// 获取鼠标按下点在编辑框的位置
			Position  pos = getPosition(lefttop, rightbottom, e, 20);
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
			if (pos != Position.OUTER && keyCode == 18) { // 鼠标点击坐标是非外部点，且按下了Alt键， 操作切换成旋转模式
					pointer = Pointer.ROTATE;
			}			
			return;
		}
		
		
		// 创建新的元素
		newCreate = elements.size();// 新建图像的起始点
		mouse_Released = false; // 控制虚线框的描绘
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
			case LINE: // 直线
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;
			case RECTANGLE:
			case SQUARE: // 矩形
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush));
				break;
			case CIRCLE:
			case OVAL: // 圆
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
		// 编辑状态的拖拽，改变特定元素
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
	

		// 生成元素的拖拽操作
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
			Position pos = getPosition(lefttop, rightbottom, e, 20);
			frame.setCursor(pos.getCursor(paintbrush.getGraphicsType().getCursor()));
			if(pos==Position.OUTER) {
				confirmPlace = true;
			} else {
				confirmPlace = false;
			}
		} else {
			confirmPlace = true;
			pointer = Pointer.CREATE_NEW;
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
	Vector<PixPoint> copy = new Vector<PixPoint>();
	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println(e);
		if (!hasActive) {
			return;
		}
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
		keyCode = e.getKeyCode();
		if (keyCode == 16) { // ctrl键
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
				copy = new Vector<PixPoint>();
				PixPoint p1 = null, p2 = null;
				int offset = -50;
				for(Integer c: clickObject) {

					try {
						p1 = (PixPoint) elements.get(c).clone();
						p2 = (PixPoint) elements.get(c+1).clone();
						
					} catch (CloneNotSupportedException e1) {
						// TODO Auto-generated catch block
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
			} else if (keyCode == 86) { // V
				System.out.println("???"+copy);
				if(copy!=null&&copy.size()!=0) {
					elements.addAll(copy);
					this.updateUI();
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
			} else if (keyCode == 38) { // 上箭头
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
				this.updateUI();
			} else if (keyCode == 40) { // 下箭头
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
				this.updateUI();			
			} else if (keyCode==37) {// 左箭头
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
				this.updateUI();				
			} else if (keyCode == 39) {
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
				this.updateUI();			
			} else if (keyCode==96) {
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
				this.updateUI();				
			} else if (keyCode == 97) { // 1
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
				this.updateUI();			
			} else if(keyCode==101) { // 空格
				
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
				this.updateUI();			
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