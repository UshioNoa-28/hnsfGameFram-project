package com.tedu.show;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * @说明 游戏窗体
 * @author renjj
 */
public class GameJFrame extends JFrame{
	public static int GameX = 800;
	public static int GameY = 600;
	private JPanel jPanel =null;
	private KeyListener  keyListener=null;
	private MouseMotionListener mouseMotionListener=null;
	private MouseListener mouseListener=null;
	private Thread thead=null;

	public GameJFrame() {
		init();
	}
	public void init() {
		this.setSize(GameX, GameY);
		this.setTitle("合金弹头 · JavaSE Edition");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	}

	public void addButton() {
	}

	public void start() {
		if(jPanel!=null) {
			this.add(jPanel);
		}
		if(keyListener !=null) {
			this.addKeyListener(keyListener);
		}
		if(thead !=null) {
			thead.start();
		}
		this.setVisible(true);

		if(this.jPanel instanceof Runnable) {
			Runnable run=(Runnable)this.jPanel;
			Thread th=new Thread(run);
			th.start();
		}
	}

	public void setjPanel(JPanel jPanel) {
		this.jPanel = jPanel;
	}
	public void setKeyListener(KeyListener keyListener) {
		this.keyListener = keyListener;
	}
	public void setMouseMotionListener(MouseMotionListener mouseMotionListener) {
		this.mouseMotionListener = mouseMotionListener;
	}
	public void setMouseListener(MouseListener mouseListener) {
		this.mouseListener = mouseListener;
	}
	public void setThead(Thread thead) {
		this.thead = thead;
	}

}
