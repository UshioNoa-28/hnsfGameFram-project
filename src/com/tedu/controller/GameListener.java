package com.tedu.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * @说明 键盘监听 — 支持多键同时按下 + 菜单控制
 * @author renjj
 */
public class GameListener implements KeyListener {
	private ElementManager em = ElementManager.getManager();

	private Set<Integer> set = new HashSet<Integer>();

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		// 游戏状态控制
		if (key == KeyEvent.VK_ENTER) {
			if (GameThread.gameState == GameThread.STATE_MENU) {
				GameThread.gameState = GameThread.STATE_PLAYING;
				return;
			}
			if (GameThread.gameState == GameThread.STATE_GAMEOVER ||
				GameThread.gameState == GameThread.STATE_WIN) {
				// 重置游戏 -> 回菜单重新开始
				ElementManager.getManager().clearAll();
				GameThread.resetGame();
				GameThread.gameState = GameThread.STATE_MENU;
				return;
			}
		}

		if (GameThread.gameState != GameThread.STATE_PLAYING) {
			return;
		}

		if (set.contains(key)) {
			return;
		}
		set.add(key);

		List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
		for (ElementObj obj : play) {
			obj.keyClick(true, key);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (GameThread.gameState != GameThread.STATE_PLAYING) {
			return;
		}

		if (!set.contains(key)) {
			return;
		}
		set.remove(key);

		List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
		for (ElementObj obj : play) {
			obj.keyClick(false, key);
		}
	}
}
