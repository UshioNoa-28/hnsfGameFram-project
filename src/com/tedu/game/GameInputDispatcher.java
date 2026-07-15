package com.tedu.game;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tedu.controller.GameThread;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.SoundManager;

/**
 * 键盘输入分发器 — 不依赖Swing焦点
 */
public class GameInputDispatcher implements KeyEventDispatcher {
	private final ElementManager elementManager = ElementManager.getManager();
	private final Set<Integer> pressedKeys = new HashSet<>();

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getID() == KeyEvent.KEY_PRESSED) {
			onKeyPressed(event.getKeyCode());
		} else if (event.getID() == KeyEvent.KEY_RELEASED) {
			onKeyReleased(event.getKeyCode());
		}
		event.consume();
		return true;
	}

	private void onKeyPressed(int key) {
		// 全局状态切换
		if (key == KeyEvent.VK_ENTER) {
			if (GameThread.gameState == GameThread.STATE_MENU) {
				clearPressedKeys();
				GameThread.gameState = GameThread.STATE_PLAYING;
				return;
			}
			if (GameThread.gameState == GameThread.STATE_GAMEOVER
					|| GameThread.gameState == GameThread.STATE_WIN) {
				clearPressedKeys();
				elementManager.clearAll();
				GameThread.resetGame();
				return;
			}
		}

		// 音乐开关 (M键)
		if (key == KeyEvent.VK_M && !pressedKeys.contains(key)) {
			SoundManager sm = SoundManager.getInstance();
			sm.setMuted(!sm.isMuted());
			System.out.println("[Audio] " + (sm.isMuted() ? "MUTED" : "UNMUTED"));
			pressedKeys.add(key);
			return;
		}

		// 游戏进行中才分发移动/射击
		if (GameThread.gameState != GameThread.STATE_PLAYING || !pressedKeys.add(key)) {
			return;
		}
		notifyPlayers(true, key);
	}

	private void onKeyReleased(int key) {
		if (key == KeyEvent.VK_M) {
			pressedKeys.remove(key);
			return;
		}
		if (!pressedKeys.remove(key)) return;
		notifyPlayers(false, key);
	}

	public void clearPressedKeys() {
		for (Integer key : pressedKeys) {
			notifyPlayers(false, key);
		}
		pressedKeys.clear();
	}

	private void notifyPlayers(boolean pressed, int key) {
		List<ElementObj> players = elementManager.getElementsByKey(GameElement.PLAY);
		for (ElementObj player : players) {
			player.keyClick(pressed, key);
		}
	}
}
