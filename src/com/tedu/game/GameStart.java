package com.tedu.game;

import java.awt.KeyboardFocusManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.tedu.controller.GameThread;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameMainJPanel;

public class GameStart {
	public static void main(String[] args) {
		GameJFrame gj = new GameJFrame();
		GameMainJPanel jp = new ChineseGamePanel();
		GameInputDispatcher inputDispatcher = new GameInputDispatcher();
		GameThread th = new GameThread();

		gj.setjPanel(jp);
		gj.setTitle("合金弹头 · 觉醒版  |  Metal Slug Awakened Edition");
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
