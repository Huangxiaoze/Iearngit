package mygrapheditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.Vector;

import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;

class PaintBrush implements Serializable, Cloneable {
	// 画笔颜色
	Color panColor = Color.black;
	Shape graphicsType = Shape.BRUSH;  // 图像类型, 可以考虑用枚举  graphicsType=-1代表截断 0 代表画笔 1直线 2矩形
	int panSize = 2;   // 画笔大小
	boolean dash = false;
	double rotateAngle = 0; // 旋转角
	public PaintBrush(Color color, Shape type, int size, boolean dash) {
		this.panColor = color;
		this.graphicsType = type;
		this.panSize = size;
		this.dash = dash;
	}
	public PaintBrush() {
		// TODO Auto-generated constructor stub
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
}

class PixPoint implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	int x; // x轴坐标
	int y; // y轴坐标
	PaintBrush paintbrush;
	
	PixPoint (int x, int y, Color color, Shape type, int size, boolean dash) {
		this.x = x;
		this.y = y;
		paintbrush = new PaintBrush(color, type, size, dash);
	}
	
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
}

class Layer implements Serializable {
	private Vector<PixPoint> elements = new Vector<PixPoint>();
	private boolean active = true;
	public Vector<PixPoint> getElements() {
		return elements;
	}
	public void setElements(Vector<PixPoint> elements) {
		this.elements = elements;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	
}


enum Shape {
	LINE(true), CIRCLE(true), OVAL(true), RECTANGLE(true), SQUARE(true), BRUSH(false), CUT(false), ERASER(false);
	private boolean editable;
	Shape(boolean editable){
		this.editable = editable;
	}
	public boolean isEditable() {
		return editable;
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
			mouse=kit.createCustomCursor(img, new Point(2,30), "stick");
		} else {
			mouse = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		}
		
		return mouse;
	}
}
enum Pointer {MOVE, W_RESIZE, E_RESIZE, N_RESIZE, S_RESIZE, NW_RESIZE, NE_RESIZE, SW_RESIZE, SE_RESIZE, CREATE_NEW, ROTATE}


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
		}
		throw new Exception("shape not found");
	}
}


// 画板类
class EditorPanel extends JPanel implements MouseListener,MouseMotionListener, KeyListener {

	PaintBrush paintbrush = new PaintBrush();
	int newCreate = -1;  // 新建元素的起始坐标
	int editObj = -1; // Ctrl+鼠标左键选中的对象
	int copy = -1; // 复制
	private boolean mouse_Released=true; // 监测鼠标是否放下，控制虚线框的显示
	private boolean confirmPlace = true; // 元素确认放下
	int outlineSize = 10;  // 鼠标进入边界多少距离,变化鼠标
	Pointer pointer = Pointer.CREATE_NEW; // 图形所处的状态
	MouseEvent forMove; // 移动图形元素时，记录前一个移动的点
	private boolean isResize = false; // 监测是否处于编辑图形阶段
	int keyCode = -1; //记录键盘的键值
	float[] dash_set = new float[] { 5, 10 }; // 虚线
	private int activeLayer = 0; // 当前操作的图层
	private boolean hasActive = true;
	// 图层元素集合
	private Vector<Layer> layers;
	
	private final JFrame frame;
	
	// 构造器
	public EditorPanel(JFrame frame) {
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
	
	/*
	 * 客户端更改图形
	 */
	public void place() { // 模拟图形放下操作
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
		for(int i=0;i<layers.size();i++) {
			if(layers.get(i).isActive()) {
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
		activeLayer = layers.size()-1;
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
		if(l==activeLayer) { // 操作当前活跃的图层
			if(s==false) {// 隐藏活跃图层，重新选择一个活跃的图层 
				boolean hasActive = false;
				for(int i=layers.size()-1;i>=0;i--) {
					if(layers.get(i).isActive()) {
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
			if(s==false) { // 隐藏，啥也不做
				
			} else {  // 显示，如果
				
			}
		}
		this.updateUI();
	}
	
	public void clearCurLayer() {
		System.out.println("clearCurLayer");
		this.place();
		if(this.layers.size()==0) {
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

        g2d.setColor(Color.RED);
        BasicStroke size = null;
        PixPoint p1, p2;
        for(int j=0;j<layers.size();j++) { // 依次绘制所有图层元素
        	Layer layer = layers.get(j);
        	System.out.println("layer "+(j+1)+" size is " +layer.getElements().size());
        	
        	if(!layer.isActive()) {
        		continue;
        	}
        	Vector<PixPoint> elements = layer.getElements();
	        for(int i=0;i<elements.size()-1;i++) {
	        	
	        	p1 = elements.get(i);
	        	p2 = elements.get(i+1);
	        	
	        	if(p1.paintbrush.dash) {
	        		size = new BasicStroke(p1.paintbrush.panSize,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10.0f,dash_set,0.0f);	     		
	        	} else {
	        		size = new BasicStroke(p1.paintbrush.panSize,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);	  		
	        	}
	
	        	g2d.setColor(p1.paintbrush.panColor);
	        	g2d.setStroke(size);
	        	g2d.rotate(0);
	        	
	        	switch(p1.paintbrush.graphicsType) {
	        	case BRUSH:
	        		g2d.drawLine(p1.x, p1.y, p2.x, p2.y);break;
	        	case LINE:
	        		
	        		g2d.drawLine(p1.x, p1.y, p2.x, p2.y);i++;break;
	        	case SQUARE:     	
	        	case RECTANGLE:
	        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
	        		g2d.drawRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
	        		
	        		if(i==newCreate && mouse_Released) { // 新创建的显示编辑框
	        			drawEditorFrame(p1, p2, g2d);
	        		}
	        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
	        		i++;
	        		break;
	        	case CIRCLE:case OVAL:
	        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
	        		g2d.drawArc(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y), 0, 360);
	        		if(i==newCreate && mouse_Released) { // 新创建的显示编辑框
	        			drawEditorFrame(p1, p2, g2d);
	        		}
	        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
	        		i++;
	        		break;
	        	case CUT:
	        		continue;
	        	case ERASER:
	        		g.clearRect(p1.x, p1.y, p1.paintbrush.panSize, p2.paintbrush.panSize);break;
	        	}
	        	
	        }
        }
        
        // 自己创建的副本用完要销毁掉
        g2d.dispose();
    }
    /*
     * 绘制编辑框，8个方向对图形元素进行长宽变换
     */
    private void drawEditorFrame(PixPoint p1, PixPoint p2, Graphics2D g2d) { 
    	float[] dash_set = {5, 5};
    	BasicStroke size = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10.0f,dash_set,0.0f);
    	if(p1.paintbrush.graphicsType==Shape.SQUARE || p1.paintbrush.graphicsType == Shape.RECTANGLE) {
    		g2d.setColor(Color.white);
    	} else {
    		g2d.setColor(Color.darkGray);    		
    	}

    	g2d.setStroke(size);
    	// 绘制虚线框
    	g2d.drawRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
    	
    	// 绘制8个点
    	size = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
    	g2d.setStroke(size);
    	g2d.setColor(Color.cyan);
    	g2d.drawArc(p1.x-1, p1.y-1, 2, 2, 0, 360); // 左上角
    	g2d.drawArc(p1.x-1, p2.y-1, 2, 2, 0, 360); // 左下角
    	g2d.drawArc(p2.x-1, p1.y-1, 2, 2, 0, 360); // 右上角
    	g2d.drawArc(p2.x-1, p2.y-1, 2, 2, 0, 360); // 右下角
    	g2d.drawArc((p1.x-1 + p2.x-1)/2, p1.y-1, 2, 2, 0, 360); // 上边中心
    	g2d.drawArc((p1.x-1 + p2.x-1)/2, p2.y-1, 2, 2, 0, 360); // 下边中心
    	g2d.drawArc(p1.x-1, (p1.y-1 + p2.y-1)/2, 2, 2, 0, 360); // 左边中心
    	g2d.drawArc(p2.x-1, (p1.y-1 + p2.y-1)/2, 2, 2, 0, 360); // 右边中心
    }
    
    
	@Override
	public void mousePressed(MouseEvent e) {
		if(!hasActive) {
			return;
		}
		forMove = e;
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
//		System.out.println("mousePressed "+confirmPlace+" isResize="+isResize);
		System.out.println(keyCode);
		if(keyCode==17) { // 按下了Ctrl键, 编辑
			System.out.println("??????");
			for(int i=0;i<elements.size();i++) {
				PixPoint p1 = elements.get(i);
				if(!p1.paintbrush.getGraphicsType().isEditable()) {
					continue;
				}
				PixPoint p2 = elements.get(i+1);
				if(p1.x < e.getX() && e.getX()< p2.x && p1.y < e.getY() && e.getY() < p2.y) {
					editObj = i;
				}
				i++;
			}
			this.place();
			newCreate = editObj;
			confirmPlace = false;
			System.out.println("NewCreate = "+newCreate);
			
			this.updateUI();
			return;
		}
		// 检查是否处于编辑元素状态
		if(!confirmPlace) { // 未确认放置，处于编辑状态，点击鼠标不需要添加新元素, 获取鼠标按下时执行的操作
				PixPoint p1 = elements.get(newCreate);
				PixPoint p2 = elements.get(newCreate+1);			
			if(p1.paintbrush.getGraphicsType().isEditable() && elements.size()>0) {
				isResize = true;
				if (p1.x<e.getX() && p2.x>e.getX() && p1.y < e.getY() && p2.y>e.getY()) { //位于内部，平移
					pointer = Pointer.MOVE;
				} else { // 指针位于外部
					if( Math.abs(e.getX()-p1.x) < 25 ) { // 左侧
						if(Math.abs(e.getY() - p1.y)<50) { // 上方
							pointer = Pointer.NW_RESIZE;
						} else if (Math.abs(e.getY() - p2.y)<50) { // 下方
							pointer = Pointer.SW_RESIZE;
						} else if (Math.abs(e.getY() - (p1.y+p2.y)/2)<50) { // 中间
							pointer = Pointer.W_RESIZE;
						}
					} else if(Math.abs(e.getX()-p2.x) < 25) { // 右侧
						if(Math.abs(e.getY() - p1.y)<50) { // 上方
							pointer = Pointer.NE_RESIZE;
						} else if (Math.abs(e.getY() - p2.y)<50) { // 下方
							pointer = Pointer.SE_RESIZE;
						} else if (Math.abs(e.getY() - (p1.y+p2.y)/2)<50) { // 中间
							pointer = Pointer.E_RESIZE;
						}					
					} else if(Math.abs(e.getX()-(p1.x+p2.x)/2) < 25) { // 中间
						if (Math.abs(e.getY() - p1.y)<50) { // 上方
							pointer = Pointer.N_RESIZE;
						} else if(Math.abs(e.getY() - p2.y)<50) {
							pointer = Pointer.S_RESIZE;
						}
					} else {
						pointer = Pointer.CREATE_NEW;
					}
				}
				
			}
			if(pointer!=Pointer.CREATE_NEW) {
				if (keyCode==18) { // 按下了Alt, 且处于编辑选中对象状态，旋转
						pointer = Pointer.ROTATE;
				}
				return;
			}
		}
		newCreate = elements.size();// 新建图像的起始点
		mouse_Released = false; // 控制虚线框的描绘		
		switch(paintbrush.graphicsType) {
		case BRUSH:elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));break;
		case LINE: // 直线
			elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));
			elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));
			break;
		case RECTANGLE:case SQUARE: // 矩形
			elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));
			elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));
			break;
		case CIRCLE:case OVAL: // 圆
			elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));
			elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));
			break;
			
		}
		
		this.updateUI();

	}
	double formerAngle = 0;
	@Override
	public void mouseDragged(MouseEvent e) {
		if(!hasActive) {
			return;
		}
		System.out.println("mouseDragged"+" "+confirmPlace+" "+pointer+" "+newCreate);
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
		if(newCreate == -1) {
			confirmPlace = true;
			return;
		}
		// 编辑状态的拖拽，改变特定元素
		if(!confirmPlace) { 
			PixPoint p1 = elements.get(newCreate);
			PixPoint p2 = elements.get(newCreate+1);
			if(p1.paintbrush.getGraphicsType().isEditable() && elements.size()>0) {
				isResize = true;
				switch(pointer) {
				case MOVE:
					int deltax = e.getX() - forMove.getX();
					int deltay = e.getY() - forMove.getY();
					forMove = e;
					p1.move(p1.x + deltax, p1.y+deltay);
					p2.move(p2.x+deltax, p2.y + deltay);
					break;
				case NW_RESIZE:
					p1.move(e.getX(), e.getY());
					break;
				case SW_RESIZE:
					p1.move(e.getX(), p1.y);
					p2.move(p2.x, e.getY());
					break;
				case W_RESIZE:
					p1.move(e.getX(), p1.y);break;
				case NE_RESIZE:
					p1.move(p1.x, e.getY());
					p2.move(e.getX(), p2.y);
					break;
				case SE_RESIZE:
					p2.move(e.getX(), e.getY());break;
				case E_RESIZE:
					p2.move(e.getX(), p2.y);break;
				case N_RESIZE:
					p1.move(p1.x, e.getY());break;
				case S_RESIZE:
					p2.move(p2.x, e.getY());break;
				case ROTATE:
					System.out.println(e.getX() - forMove.getX());
					System.out.println(e.getY() - forMove.getY());
					int x = (p1.x+p2.x)/2;
					int y = (p1.y+p2.y)/2;
					double e12 = (forMove.getX() - x)*(forMove.getX() - x) + (forMove.getY() - y)*(forMove.getY() - y);
					double e22 = (e.getX() - x)*(e.getX() - x) + (e.getY() - y)*(e.getY() - y);
					double e32 = (e.getX() - forMove.getX())*(e.getX() - forMove.getX()) + (e.getY() - forMove.getY())*(e.getY() - forMove.getY());
					double cosValue = (e12+e22-e32)/(2*Math.sqrt(e12)*Math.sqrt(e22));
					
					if(!Double.isNaN(Math.acos(cosValue))) {
						p1.rotate((Math.acos(cosValue)+p1.getRotate())%(2*Math.PI));
					}
					forMove = e;
				default:
					break;
				}
			}
			if(pointer!=Pointer.CREATE_NEW) {
				this.updateUI();
				return;
			}
		}
		switch(paintbrush.graphicsType) {
		case BRUSH:elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));break;
		
		case SQUARE:case CIRCLE:
			PixPoint p1 = elements.get(newCreate);
			PixPoint p2 = elements.get(newCreate+1);
			int width = Math.min(Math.abs(p1.x - e.getX()), Math.abs(p1.y - e.getY()));
			p2.move(p1.x + width, p1.y+width);
			break;
		
		case LINE:
		case RECTANGLE:
		case OVAL:
			elements.set(elements.size()-1, new PixPoint(e.getX(), e.getY(), paintbrush.panColor, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));
			break;
		case ERASER: elements.add(new PixPoint(e.getX(), e.getY(), null, paintbrush.graphicsType, paintbrush.panSize, paintbrush.dash));break;
		}
		this.updateUI();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(!hasActive) {
			return;
		}
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
//		System.out.println("mouseMoved"+", "+isResize);
		if(isResize) {
			return;
		}
		if(newCreate==-1||elements.size()==0) {
			frame.setCursor(paintbrush.getGraphicsType().getCursor());
			return;
		}
		
		PixPoint p1 = elements.get(newCreate);
		if(!p1.paintbrush.getGraphicsType().isEditable()) {
			return;
		}
		
		PixPoint p2 = elements.get(newCreate+1);
		if(p1.paintbrush.getGraphicsType().isEditable()) {
			if (p1.x<e.getX() && p2.x>e.getX() && p1.y < e.getY() && p2.y>e.getY()) { //位于内部
				confirmPlace = false;
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else { // 指针位于外部
				if( Math.abs(e.getX()-p1.x) < outlineSize ) { // 左侧
					if(Math.abs(e.getY() - p1.y)<outlineSize) { // 上方
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - p2.y)<outlineSize) { // 下方
						// 有bug
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - (p1.y+p2.y)/2)<outlineSize) { // 中间
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
					} else {
						confirmPlace = true;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
				} else if(Math.abs(e.getX()-p2.x) < outlineSize) { // 右侧
					if(Math.abs(e.getY() - p1.y)<outlineSize) { // 上方
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - p2.y)<outlineSize) { // 下方
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
					} else if (Math.abs(e.getY() - (p1.y+p2.y)/2)<outlineSize) { // 中间
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					} else {
						confirmPlace = true;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
				} else if(Math.abs(e.getX()-(p1.x+p2.x)/2) < outlineSize) { // 中间
					if (Math.abs(e.getY() - p1.y)<50) { // 上方
						confirmPlace = false;
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));				
					} else if(Math.abs(e.getY() - p2.y)<outlineSize) {
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
		if(!hasActive) {
			return;
		}
		System.out.println("mouseRelease"+", "+isResize+ ", "+confirmPlace);
		
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
		
		isResize = false;
		if(!confirmPlace || keyCode==17) {
			return;
		}
		mouse_Released = true;
		
		
		if(paintbrush.getGraphicsType().isEditable()) {
			PixPoint p1 = elements.get(newCreate);
			PixPoint p2 = elements.get(newCreate+1);
			if(p1.x == p2.x && p1.y== p2.y) {
				elements.remove(newCreate+1);
				elements.remove(newCreate);
				newCreate = -1;
			} else {
				elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, Shape.CUT, paintbrush.panSize, paintbrush.dash));
			}
		} else {
			elements.add(new PixPoint(e.getX(), e.getY(), paintbrush.panColor, Shape.CUT, paintbrush.panSize, paintbrush.dash));
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
		if(!hasActive) {
			return;
		}
		Vector<PixPoint> elements = layers.get(activeLayer).getElements();
//		System.out.println(e);
		keyCode = e.getKeyCode();
		if(keyCode == 16) { // ctrl键
			if(paintbrush.getGraphicsType() == Shape.SQUARE) {
				paintbrush.setGraphicsType(Shape.RECTANGLE);
			} else if (paintbrush.getGraphicsType() == Shape.CIRCLE) {
				paintbrush.setGraphicsType(Shape.OVAL);
			}
		}
		if(e.getModifiers()==2) { // Ctrl
			if(keyCode == 68) { // D
				if(!elements.isEmpty()&&newCreate>=0) {
					PixPoint p1 = elements.get(newCreate);
					if(p1.paintbrush.getGraphicsType().isEditable()) {
						elements.remove(newCreate+1);
						elements.remove(newCreate);
						this.place();
						this.updateUI();
					}
				}
			} else if(keyCode == 67) { // C
				copy = newCreate;
			} else if(keyCode == 86) {  // V
				if(!elements.isEmpty()&&copy>=0) {
					PixPoint p1 = elements.get(copy); // 此处可以考虑重载clone
					if(p1.paintbrush.getGraphicsType().isEditable()) {
						PixPoint p2 = elements.get(copy+1);
						try {
							int offset = -50;
							p1 = (PixPoint) p1.clone();
							p2 = (PixPoint) p2.clone();
							int deltax = p2.x - p1.x;
							int deltay = p2.y - p1.y;
							p1.move(p1.x+offset, p1.y+offset);
							p2.move(p1.x+deltax, p1.y+deltay);
							elements.add(new PixPoint(-1, -1, paintbrush.panColor, Shape.CUT, paintbrush.panSize, paintbrush.dash));							
							elements.add(copy, (PixPoint)p2.clone());
							elements.add(copy, (PixPoint)p1.clone());
						} catch (CloneNotSupportedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						this.updateUI();
					}
				}	
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(!hasActive) {
			return;
		}
		// TODO Auto-generated method stub
		if(keyCode == 16) {
			if(paintbrush.getGraphicsType() == Shape.RECTANGLE) {
				paintbrush.setGraphicsType(Shape.SQUARE);
			} else if (paintbrush.getGraphicsType() == Shape.OVAL) {
				paintbrush.setGraphicsType(Shape.CIRCLE);
			}
		}
		keyCode = -1;
	}


}


public class Editor extends JFrame implements ActionListener {
	// 
	public static final String IMGURL = "./image/";
	public static final String FILEURL = "./file/";
	
	// 画板对象
	EditorPanel drawingBoard;
	
	// 图层布局控制
	JPanel layerPanel = new JPanel();
	// 图形类别
	int graphicsType = 0;
	// 图层的添加删除
	boolean layerAdd = true;
	Color activebtnColor = new Color(218,180,156);
	Color normalbtnColor = new Color(141,124,124);
	// 文件输入输出流
	FileInputStream inFileInputStream = null;
	FileOutputStream outFileOutputStream = null;
	// 序列化，导入导出图像关键的一个API
	ObjectInputStream inObjectInputStream = null;
	ObjectOutputStream outObjectOutputStream = null;
	
	
	// 导入导出文件对话框
	FileDialog loadDialog = new FileDialog(this,"loadDialog",FileDialog.LOAD);
	FileDialog dumpDialog = new FileDialog(this,"SaveDialog",FileDialog.SAVE);
	
	// 图层控件
	Vector<JCheckBox> jcbs = new Vector<JCheckBox>();
	Vector<JButton> jbs = new Vector<JButton>();
	
	
	public Editor() {
		this.init();
	}
	
	private void init() { // 界面初始化
		initMenu();
		initToolBar();
		
		drawingBoard = new EditorPanel(this);

		this.add(drawingBoard, BorderLayout.CENTER);
		
		this.initLeftPanel();
		
		this.setTitle("GraphEditor");
		this.setSize(800, 600);
		this.setLocation(200, 200);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭编辑器
		this.setResizable(false);   
		this.setVisible(true);
	}

	private JPanel makeLayer(String text, int order, boolean selected) {
		JPanel layer = new JPanel();
		BoxLayout hlayout = new BoxLayout(layer, BoxLayout.X_AXIS);
		layer.setLayout(hlayout);
		JCheckBox jcb = new JCheckBox("", selected);
		jcb.setActionCommand("jcb"+order);
		jcb.addActionListener(this);
		jcb.setFocusable(false);
		JButton jb = makeButton(text, "layer"+ order , text);
		jb.setFocusable(false);
		layer.add(jcb);
		layer.add(jb);
		jcbs.add(jcb);
		jbs.add(jb);
		return layer;
	}
	private void initLeftPanel() {
		BoxLayout vlayout = new BoxLayout(layerPanel, BoxLayout.Y_AXIS);
		layerPanel.setLayout(vlayout);
		layerPanel.add(makeLayer("图层1",1, true));
		jbs.get(0).setBackground(activebtnColor);
		layerPanel.setSize(100,this.getHeight());
		layerPanel.setBackground(Color.DARK_GRAY);
		this.add(layerPanel, BorderLayout.EAST);
	}
	
	private void initToolBar() {
		// 工具栏
		initTopToolBar();
		initLeftToolBar();
		
		JPanel jPanel = new JPanel();
		jPanel.setBackground(Color.DARK_GRAY);
		jPanel.setMinimumSize(new Dimension(this.getWidth(), 50));
		this.add(jPanel, BorderLayout.SOUTH);
	}
	private void initTopToolBar() {
		String[][] args = {
				{"新建","new", "新建图像文件"},
				{"导入", "load", "导入图像对象"},
				{"导出", "dump", "导出图像对象"},
				{"清空图层", "clearCurLayer", "清空当前活跃图层"},
				{"清空所有", "clearLayer", "删除所有图层"},
				{"颜色", "colorpicker", "画笔颜色"},
		};
		
		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentX(CENTER_ALIGNMENT);
		toolBar.setBackground(Color.DARK_GRAY);
//		toolBar.setMinimumSize(new Dimension(300, this.getHeight()));
		addButtons(toolBar, args);
		this.add(toolBar, BorderLayout.PAGE_START);
	}
	private void initLeftToolBar() {
		String[][] args = {
				{"画笔", "pan", ""},				
				{"橡皮", "eraser", "擦除图像对象"},
				{"直线", "line", ""},
				{"矩形", "rect", ""},
				{"圆形", "circle", ""}
		};
		JToolBar toolBar = new JToolBar();
		toolBar.setOrientation(JToolBar.VERTICAL);
		toolBar.setBackground(Color.DARK_GRAY);
		addButtons(toolBar, args);
		this.add(toolBar, BorderLayout.WEST);
	}
	private void initMenu() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu1 = new JMenu("画笔设置");
		JMenuItem raise = new JMenuItem("增大 +");
		raise.setActionCommand("raisePanSize");
		raise.addActionListener(this);
		menu1.add(raise);
		
		JMenuItem decrease = new JMenuItem("减小 -");
		decrease.setActionCommand("decreasePanSize");
		decrease.addActionListener(this);
		menu1.add(decrease);
		
		JMenuItem dashControl = new JMenuItem("虚线");
		dashControl.setActionCommand("dashControl");
		dashControl.addActionListener(this);
		menu1.add(dashControl);
		
		JMenu menu2 = new JMenu("图层");
		JMenuItem newLayer = new JMenuItem("新建");
		newLayer.setActionCommand("newLayer");
		newLayer.addActionListener(this);
		menu2.add(newLayer);
		
		JMenuItem delLayer = new JMenuItem("删除");
		delLayer.setActionCommand("delLayer");
		delLayer.addActionListener(this);
		menu2.add(delLayer);

		menubar.add(menu1);
		menubar.add(menu2);
		this.setJMenuBar(menubar);
	}
	private void addButtons(JToolBar toolBar, String[][] args) {
		for(int i=0;i<args.length;i++) {
			String text = args[i][0];
			String acmd = args[i][1];
			String tooltip = args[i][2];
			toolBar.add(makeButton(text, acmd, tooltip));
		}
	}
	private JButton makeButton(String altText, String actionCommand, String toolTip, String...args) {
		JButton btn = new JButton();
		btn.setText(altText);
		btn.setToolTipText(toolTip);
		btn.addActionListener(this);
		btn.setActionCommand(actionCommand);
		btn.setFocusable(false);
		return btn;
	}
	public static void main(String[] args) throws IOException {
		printHelp();
		new Editor();
		
	}
	public static void printHelp() throws IOException {
		File help = new File(FILEURL + "help.txt");
		FileInputStream fin = new FileInputStream(help);
		DataInputStream din = new DataInputStream(fin);
		InputStreamReader rin = new InputStreamReader(din, "gbk");
		StringBuilder sb = new StringBuilder();
		int c;
		String helpStr = "";
		while((c=rin.read())!=-1) {
			sb.append((char)c);
		}
		System.out.println(sb.toString());
	}
	private void revalidateLayer() {
		for(int i=0;i<drawingBoard.getLayersSize();i++) {
			jbs.get(i).setBackground(normalbtnColor);
		}
		if(drawingBoard.hasActiveLayer()) {
			jbs.get(drawingBoard.getActiveLayer()).setBackground(activebtnColor);
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		System.out.println(e.getActionCommand());
		boolean islayer = false;
		for(int i=0;i<jbs.size();i++) {
			if(cmd.equals("layer"+(i+1))) {
				islayer = true;
				if(layerAdd) { // 激活某个图层，进行绘制操作
					drawingBoard.setActiveLayer(i);
					jcbs.get(i).setSelected(true);
				} else {
					
				}
				
			} else if(cmd.equals("jcb"+(i+1))) {
				islayer = true;
				JCheckBox jcb = (JCheckBox)e.getSource();
				System.out.println("Jcb");
				drawingBoard.setLayerActiveProperty(i, jcb.isSelected());
			}
		}

		if(islayer) {
			revalidateLayer();
			return;
		}
		
		
		if(e.getActionCommand().equals("new")) {
			
		} else if(e.getActionCommand().equals("load")) { // 导入图像

			loadDialog.setVisible(true);
			if (loadDialog.getFile()!=null) {
				
				try {
					File file = new File(loadDialog.getDirectory(), loadDialog.getFile());
					inFileInputStream = new FileInputStream(file);
					inObjectInputStream = new ObjectInputStream(inFileInputStream);
					Vector<Layer> elements = (Vector<Layer>) inObjectInputStream.readObject(); // 从文件中获取对象
					
					
					this.setLayerPanel(elements);
					
		
					inFileInputStream.close();
					inObjectInputStream.close();
					
		
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}catch (IOException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}	
		} else if(e.getActionCommand().equals("dump")) { // 导出图像
			dumpDialog.setVisible(true);
			if (dumpDialog.getFile()!=null) {
				
				try {
					File file = new File(dumpDialog.getDirectory()+dumpDialog.getFile());
					outFileOutputStream = new FileOutputStream(file);
					outObjectOutputStream = new ObjectOutputStream(outFileOutputStream); 
					outObjectOutputStream.writeObject(drawingBoard.getLayers());  // 将图形对象导出到文件
					outFileOutputStream.close();
					outObjectOutputStream.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}catch (IOException e1) {
					e1.printStackTrace();
				}
			}			
			
		} else if (e.getActionCommand().equals("colorpicker")) {
			Color newColor = JColorChooser.showDialog(this, "Color Select", drawingBoard.paintbrush.getPanColor());
			drawingBoard.paintbrush.setPanColor(newColor);
			JButton btn = (JButton)e.getSource();
			btn.setBackground(newColor);
		} else if (e.getActionCommand().equals("raisePanSize")) {
			drawingBoard.paintbrush.raisePanSize();
		} else if (e.getActionCommand().equals("decreasePanSize")) {
			drawingBoard.paintbrush.decreasePanSize();
		} else if (e.getActionCommand().equals("dashControl")) {
			JMenuItem dash = (JMenuItem) e.getSource();
			if(drawingBoard.paintbrush.getDash() == true) {
				dash.setText("虚线");
				drawingBoard.paintbrush.setDash(false);
			} else {
				dash.setText("实线");
				drawingBoard.paintbrush.setDash(true);
			}
		} else if(cmd.equals("clearCurLayer")) {
			drawingBoard.clearCurLayer();
		} else if (e.getActionCommand().equals("clearLayer")) {
			clearLayerPanel();
			drawingBoard.clearLayer();
			drawingBoard.place();
			revalidate();
			
		} else if (cmd.equals("newLayer")) {
			layerPanel.add(makeLayer("图层"+(drawingBoard.getLayersSize()+1),drawingBoard.getLayersSize()+1, true));
			drawingBoard.createNewLayer();
			revalidateLayer();
		} else {
			Shape s = null;
			try {
				s = ShapeFactory.getShape(e.getActionCommand());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			drawingBoard.paintbrush.setGraphicsType(s);
			drawingBoard.place();
		}
		
	}
	private void setLayerPanel(Vector<Layer> elements) {
		clearLayerPanel();
		for(int i=0;i<elements.size();i++) {
			layerPanel.add(makeLayer("图层"+(i+1), i+1, elements.get(i).isActive()));
		}
		drawingBoard.setLayers(elements);
		revalidateLayer();
	}
	private void clearLayerPanel() {
		layerPanel.removeAll();
		jcbs = new Vector<JCheckBox>();
		jbs = new Vector<JButton>();	
	}
	
}
