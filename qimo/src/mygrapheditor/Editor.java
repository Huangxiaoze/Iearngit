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

public class Editor extends JFrame implements ActionListener {
	// 文件路径
	public static final String IMGURL = "./image/";
	public static final String FILEURL = "./file/";
	
	// 打开的文件路径
	public static String openFilePath = null;
	
	// 画板对象
	EditorPanel drawingBoard;
	
	// 图层布局控制
	JPanel layerPanel = new JPanel();
	
	// 图层的添加删除
	boolean layerAdd = true;
	
	// 活跃图层按钮背景色
	Color activebtnColor = new Color(218,180,156);
	// 普通图层按钮背景色
	Color normalbtnColor = new Color(141,124,124);
	
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
		
		this.setTitle("画板");
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
				{"画笔颜色", "colorpicker", "画笔颜色"},
				{"填充颜色", "fillcolor", "填充颜色"},
		};
		
		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentX(CENTER_ALIGNMENT);
		toolBar.setBackground(Color.DARK_GRAY);
		addButtons(toolBar, args);
		this.add(toolBar, BorderLayout.PAGE_START);
	}
	private void initLeftToolBar() {
		String[][] args = {
				{"画笔", "pan", ""},				
				{"橡皮", "eraser", "擦除图像对象"},
				{"填充", "fill", "给选中图形填充颜色"},
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
	
	public void load() { // 导入文件
		loadDialog.setVisible(true);
		if(loadDialog.getFile()!=null) {
			openFilePath = loadDialog.getDirectory()+loadDialog.getFile();
			Vector<Layer> elements = FileManager.loadFile(openFilePath);
			this.setLayerPanel(elements);
		}
	}
	
	public void dump() { // 导出文件
		dumpDialog.setVisible(true);
		if (dumpDialog.getFile()!=null) {
			if(openFilePath==null) {
				openFilePath = dumpDialog.getDirectory()+dumpDialog.getFile();
			}			
			FileManager.saveFile(dumpDialog.getDirectory()+dumpDialog.getFile(), drawingBoard.getLayers());
		}	
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		boolean islayer = operateLayer(e);
		if(islayer) {
			revalidateLayer();
			return;
		}
		
		String[] cmds = {
				"new", "load", "dump", "fillcolor", "colorpicker", 
				"raisePanSize", "decreasePanSize", "dashControl", "clearCurLayer",
				"clearLayer", "newLayer", "delLayer", "delLayer"
		};
		
		int cmd = -1;
		for(int i=0;i<cmds.length;i++) {
			if(cmds[i].equals(e.getActionCommand())) {
				cmd = i;
				break;
			}
		}
		switch(cmd) {
		case 0: break;
		case 1: load();break;
		case 2: dump();break;
		case 3: setFillColor(e);break;
		case 4: setPanColor(e);break;
		case 5: drawingBoard.raisePanSize();break;
		case 6: drawingBoard.decreasePanSize();break;
		case 7: setDash(e); break;
		case 8: drawingBoard.clearCurLayer();break;
		case 9: clearLayerPanel();break;
		case 10: newLayer();break;
		case 11: break;
		default: setShape(e.getActionCommand());break;
		}

	}
	/*
	 * 图层显示、隐藏
	 */
	private boolean operateLayer(ActionEvent e) {
		String cmd = e.getActionCommand();
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
				drawingBoard.setLayerActiveProperty(i, jcb.isSelected());
			}
		}	
		return islayer;
	}
	/*
	 * 设置填充颜色
	 */
	private void setFillColor(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(this, "填充色", drawingBoard.paintbrush.getPanColor());
		drawingBoard.setFillColor(newColor);
		JButton btn = (JButton)e.getSource();
		btn.setBackground(newColor);		
		
	}
	/*
	 * 设置画笔颜色
	 */
	private void setPanColor(ActionEvent e) {
		Color newColor = JColorChooser.showDialog(this, "画笔颜色", drawingBoard.paintbrush.getPanColor());
		drawingBoard.setPanColor(newColor);
		JButton btn = (JButton)e.getSource();
		btn.setBackground(newColor);
	}
	/*
	 * 设置画笔虚实
	 */
	private void setDash(ActionEvent e) {
		JMenuItem dash = (JMenuItem) e.getSource();
		if(drawingBoard.paintbrush.getDash() == true) {
			dash.setText("虚线");
			drawingBoard.setDash(false);
		} else {
			dash.setText("实线");
			drawingBoard.setDash(true);
		}
	}
	/*
	 * 添加新图层
	 */
	private void newLayer() {
		layerPanel.add(makeLayer("图层"+(drawingBoard.getLayersSize()+1),drawingBoard.getLayersSize()+1, true));
		drawingBoard.createNewLayer();
		revalidateLayer();	
	}
	
	/*
	 * 设置图元
	 */
	private void setShape(String cmd) {
		Shape s = null;
		try {
			s = ShapeFactory.getShape(cmd);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		drawingBoard.paintbrush.setGraphicsType(s);
		drawingBoard.place();	
	}
	/*
	 * 导入文件时设置图层显示
	 */
	private void setLayerPanel(Vector<Layer> elements) {
		clearLayerPanel();
		for(int i=0;i<elements.size();i++) {
			layerPanel.add(makeLayer("图层"+(i+1), i+1, elements.get(i).isActive()));
		}
		drawingBoard.setLayers(elements);
		revalidateLayer();
	}
	/*
	 * 清空所有图层
	 */
	private void clearLayerPanel() {
		layerPanel.removeAll();
		jcbs = new Vector<JCheckBox>();
		jbs = new Vector<JButton>();	
		drawingBoard.clearLayer();
		drawingBoard.place();
		revalidate();
	}
	
}
