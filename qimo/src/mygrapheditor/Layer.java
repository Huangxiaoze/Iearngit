package mygrapheditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Vector;

/*
 * ͼ���࣬����ͼ��Ԫ��
 */
public class Layer implements Serializable {
	private Vector<PixPoint> elements = new Vector<PixPoint>();
	
	// ���ڿ��Ƶ�ǰͼ�����ء���ʾ
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
	 * ����ͼ��Ԫ��
	 * 
	 */
	public void draw(Graphics2D g2d, Graphics g, boolean drawFrame, Vector<Integer> clickObject, PixPoint p0, PixPoint p00) {
	    if(clickObject.size()>1) { // ѡ�ж��Ԫ��		
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
        
        // �Ȼ������ɫ
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
        		// �������ɫ
        		g2d.setColor(p1.paintbrush.fillColor);
        		g2d.fillRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		break;
        	case CIRCLE:case OVAL:
        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		// �������ɫ
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
        
        // ����Ʊ߿�
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
        		// ���Ʊ߿�
        		g2d.setColor(p1.paintbrush.panColor);
        		g2d.drawRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		break;
        	case CIRCLE:case OVAL:
        		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        		// ���Ʊ߿�
        		g2d.setColor(p1.paintbrush.panColor);
        		g2d.drawArc(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y), 0, 360);
        		
        		g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);        		
        		break;
        	case CUT:
        		continue;
        	case ERASER:
        		g.clearRect(p1.x, p1.y, p1.paintbrush.panSize, p2.paintbrush.panSize);break;
        	}
        	
        	switch(p1.paintbrush.graphicsType) {// �´�������ʾ�༭��
        	
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
     * ���Ʊ༭��8�������ͼ��Ԫ�ؽ��г���任
     */
    public static void  drawEditorFrame(PixPoint p1, PixPoint p2, Graphics2D g2d, Color dashColor, Color pointColor) { 
    	// ��ת
		g2d.rotate(p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
		
    	float[] dash_set = {5, 5};
    	BasicStroke size = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10.0f,dash_set,0.0f);
    	
    	g2d.setColor(dashColor);
    	g2d.setStroke(size);
    	// �������߿�
    	g2d.drawRect(p1.x, p1.y, Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
    	
    	// ����8����
    	size = new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
    	g2d.setStroke(size);
    	g2d.setColor(pointColor);
//    	g2d.setColor(Color.cyan);
    	g2d.drawArc(p1.x-1, p1.y-1, 2, 2, 0, 360); // ���Ͻ�
    	g2d.drawArc(p1.x-1, p2.y-1, 2, 2, 0, 360); // ���½�
    	g2d.drawArc(p2.x-1, p1.y-1, 2, 2, 0, 360); // ���Ͻ�
    	g2d.drawArc(p2.x-1, p2.y-1, 2, 2, 0, 360); // ���½�
    	g2d.drawArc((p1.x-1 + p2.x-1)/2, p1.y-1, 2, 2, 0, 360); // �ϱ�����
    	g2d.drawArc((p1.x-1 + p2.x-1)/2, p2.y-1, 2, 2, 0, 360); // �±�����
    	g2d.drawArc(p1.x-1, (p1.y-1 + p2.y-1)/2, 2, 2, 0, 360); // �������
    	g2d.drawArc(p2.x-1, (p1.y-1 + p2.y-1)/2, 2, 2, 0, 360); // �ұ�����
    	
    	// ����
    	g2d.rotate(-p1.paintbrush.rotateAngle, (p1.x+p2.x)/2, (p1.y+p2.y)/2);
    }
	
	
}
