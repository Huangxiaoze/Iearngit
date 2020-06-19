package mygrapheditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Vector;

/*
 * 图层类，保存图层元素
 */
public class Layer implements Serializable {
	private Vector<PixPoint> elements = new Vector<PixPoint>();
	
	// 用于控制当前图层隐藏、显示
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
	public void removeElementFrom(int start) {
		if(start<0 || start>=elements.size()) {
			return;
		}
		while(elements.size()>start) {
			elements.remove(elements.size()-1);
		}
	}
	
	/*
	 * 绘制图层元素
	 * 
	 */
	public void draw(Graphics2D g2d, Graphics g, boolean drawFrame, Vector<Integer> clickObject, PixPoint p0, PixPoint p00) {
	    if(clickObject.size()>1) { // 选中多个元素		
	    	drawEditorFrame(p0, p00, g2d, Color.red, Color.cyan);
	    }
		clickObject.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
    	});
		
        BasicStroke size = null;
        PixPoint p1, p2;
        int newCreate = 0;
        
        // 先绘制填充色
        for(int i=0;i<elements.size()-1;i++) {
        	
        	p1 = elements.get(i);
        	p2 = elements.get(i+1);
        	
        	if(p1.paintbrush.dash) {
        		size = new BasicStroke(p1.paintbrush.panSize,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f,PaintBrush.dash_set,0.0f);	     		
        	} else {
        		size = new BasicStroke(p1.paintbrush.panSize,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);	  		
        	}

        	g2d.setColor(p1.paintbrush.panColor);
        	g2d.setStroke(size);
        	
        	switch(p1.paintbrush.graphicsType) {
        	case SQUARE:     	
        	case RECTANGLE:
        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		// 绘制填充色
        		g2d.setColor(p1.paintbrush.fillColor);
        		g2d.fillRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		break;
        	case CIRCLE:case OVAL:
        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		// 绘制填充色
        		g2d.setColor(p1.paintbrush.fillColor);       		
        		g2d.fillArc(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y), 0, 360);
        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);        		
        		break;
        	default:
        		break;
        	}
        	
        	switch(p1.paintbrush.graphicsType) {
        	case SQUARE:case RECTANGLE:
        	case CIRCLE:case OVAL:
        		i++;
        		break;
        	}
        }
        
        // 后绘制边框
        for(int i=0;i<elements.size()-1;i++) {
        	
        	p1 = elements.get(i);
        	p2 = elements.get(i+1);
        	
        	if(p1.paintbrush.dash) {
        		size = new BasicStroke(p1.paintbrush.panSize,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f,PaintBrush.dash_set,0.0f);	     		
        	} else {
        		size = new BasicStroke(p1.paintbrush.panSize,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);	  		
        	}

        	g2d.setColor(p1.paintbrush.panColor);
        	g2d.setStroke(size);
        	
        	switch(p1.paintbrush.graphicsType) {
        	case BRUSH:
        		g2d.drawLine(p1.x, p1.y, p2.x, p2.y);break;
        	case LINE:
        		
        		g2d.drawLine(p1.x, p1.y, p2.x, p2.y);i++;break;
        	case SQUARE:     	
        	case RECTANGLE:
        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		// 绘制边框
        		g2d.setColor(p1.paintbrush.panColor);
        		g2d.drawRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		break;
        	case CIRCLE:case OVAL:
        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		// 绘制边框
        		g2d.setColor(p1.paintbrush.panColor);
        		g2d.drawArc(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y), 0, 360);
        		
        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);        		
        		break;
        	case CUT:
        		continue;
        	case ERASER:
        		g.clearRect(p1.x, p1.y, p1.paintbrush.panSize, p2.paintbrush.panSize);break;
        	}
        	
        	switch(p1.paintbrush.graphicsType) {// 新创建的显示编辑框
        	
        	case SQUARE:case RECTANGLE:
        	case CIRCLE:case OVAL:
        		if(drawFrame&&clickObject.contains(i)) { 
        			drawEditorFrame(p1, p2, g2d, p1.paintbrush.getGraphicsType().getDashColor(), Color.cyan);
        		}
        		i++;
        		break;
        	}
        	
        }
        
	}
    /*
     * 绘制编辑框，8个方向对图形元素进行长宽变换
     */
    public static void  drawEditorFrame(PixPoint p1, PixPoint p2, Graphics2D g2d, Color dashColor, Color pointColor) { 
    	// 旋转
		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
		
    	float[] dash_set = {5, 5};
    	BasicStroke size = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10.0f,dash_set,0.0f);
    	
    	g2d.setColor(dashColor);
    	g2d.setStroke(size);
    	// 绘制虚线框
    	g2d.drawRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
    	
    	// 绘制8个点
    	size = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
    	g2d.setStroke(size);
    	g2d.setColor(pointColor);
//    	g2d.setColor(Color.cyan);
    	g2d.drawArc(p1.x-1, p1.y-1, 2, 2, 0, 360); // 左上角
    	g2d.drawArc(p1.x-1, p2.y-1, 2, 2, 0, 360); // 左下角
    	g2d.drawArc(p2.x-1, p1.y-1, 2, 2, 0, 360); // 右上角
    	g2d.drawArc(p2.x-1, p2.y-1, 2, 2, 0, 360); // 右下角
    	g2d.drawArc((p1.x-1 + p2.x-1)/2, p1.y-1, 2, 2, 0, 360); // 上边中心
    	g2d.drawArc((p1.x-1 + p2.x-1)/2, p2.y-1, 2, 2, 0, 360); // 下边中心
    	g2d.drawArc(p1.x-1, (p1.y-1 + p2.y-1)/2, 2, 2, 0, 360); // 左边中心
    	g2d.drawArc(p2.x-1, (p1.y-1 + p2.y-1)/2, 2, 2, 0, 360); // 右边中心
    	
    	// 回正
    	g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
    }
	
	
}
