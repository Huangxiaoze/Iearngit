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
	// 
	public static final String IMGURL = "./image/";
	public static final String FILEURL = "./file/";
	
	// 打开的文件路径
	public static String openFilePath = null;
	
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
	
	public void load() {
		loadDialog.setVisible(true);
		if (loadDialog.getFile()!=null) {
			
			try {
				

				File file = new File(loadDialog.getDirectory(), loadDialog.getFile());
				inFileInputStream = new FileInputStream(file);
				inObjectInputStream = new ObjectInputStream(inFileInputStream);
				Vector<Layer> elements = (Vector<Layer>) inObjectInputStream.readObject(); // 从文件中获取对象
				
				
				this.setLayerPanel(elements);
				
				openFilePath = loadDialog.getDirectory()+loadDialog.getFile();
				
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
	}
	
	public void saveFile(String path) {
		try {
			File file = new File(path);
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
	
	public void dump() {
		dumpDialog.setVisible(true);
		if (dumpDialog.getFile()!=null) {
			if(openFilePath==null) {
				openFilePath = dumpDialog.getDirectory()+dumpDialog.getFile();
			}			
			saveFile(dumpDialog.getDirectory()+dumpDialog.getFile());
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
			load();
		} else if(e.getActionCommand().equals("dump")) { // 导出图像
			dump();		
		} else if (cmd.equals("fillcolor")) {
//			drawingBoard.place();
			Color newColor = JColorChooser.showDialog(this, "填充色", drawingBoard.paintbrush.getPanColor());
			drawingBoard.setFillColor(newColor);
			JButton btn = (JButton)e.getSource();
			btn.setBackground(newColor);		
			
		} else if (e.getActionCommand().equals("colorpicker")) {
			Color newColor = JColorChooser.showDialog(this, "画笔颜色", drawingBoard.paintbrush.getPanColor());
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
