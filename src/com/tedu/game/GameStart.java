package com.tedu.game;

import java.awt.KeyboardFocusManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.tedu.controller.GameThread;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameMainJPanel;

public class GameStart {
	/**
	 * 程序的唯一入口
	 */
	public static void main(String[] args) {
		GameJFrame gj=new GameJFrame();
		/**实例化面板，注入到jframe中*/
		GameMainJPanel jp=new ChineseGamePanel();
//		实例化监听
		GameInputDispatcher inputDispatcher = new GameInputDispatcher();
//		实例化主线程
		GameThread th=new GameThread();
//		注入
		gj.setjPanel(jp);
		gj.setTitle("合金弹头 · Java 实训版");
		gj.setThead(th);
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(inputDispatcher);
		gj.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				inputDispatcher.clearPressedKeys();
			}
		});

		gj.start();

	}
}
