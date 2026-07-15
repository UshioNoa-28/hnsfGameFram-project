package com.tedu.controller;

import java.util.List;
import java.util.Map;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.SoundManager;
import com.tedu.show.GameMainJPanel;

/**
 * 游戏主线程 — 关卡加载、游戏循环、碰撞检测、波次生成
 */
public class GameThread extends Thread {
	private ElementManager em;
	private static int currentLevel = 1;
	private static final int MAX_LEVEL = 3;

	public static final int STATE_MENU = 0;
	public static final int STATE_PLAYING = 1;
	public static final int STATE_GAMEOVER = 2;
	public static final int STATE_WIN = 3;
	public static final int STATE_LEVEL_CLEAR = 4;

	public static int gameState = STATE_MENU;

	public GameThread() {
		em = ElementManager.getManager();
	}

	public static void resetGame() {
		currentLevel = 1;
		gameState = STATE_MENU;
	}

	@Override
	public void run() {
		while (true) {
			switch (gameState) {
				case STATE_MENU:
					gameMenu();
					break;
				case STATE_PLAYING:
					gameLoad();
					gameRun();
					break;
				case STATE_GAMEOVER:
				case STATE_WIN:
				case STATE_LEVEL_CLEAR:
					sleepFrame(50);
					break;
			}
			sleepFrame(50);
		}
	}

	private void gameMenu() {
		sleepFrame(30);
	}

	private void gameLoad() {
		em.clearAll();
		GameLoad.loadImg();
		GameLoad.loadObj();
		GameLoad.MapLoad(currentLevel);
		GameLoad.loadPlay();
		GameMainJPanel.cameraX = 0;
		GameMainJPanel.cameraY = 0;

		// 启动背景音乐
		try {
			SoundManager.getInstance().startBGM();
		} catch (Exception e) {}

		System.out.println("=== STAGE " + currentLevel + " READY ===");
	}

	private void gameRun() {
		long gameTime = 0L;
		while (gameState == STATE_PLAYING) {
			Map<GameElement, List<ElementObj>> all = em.getGameElements();
			List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
			List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
			List<ElementObj> bullets = em.getElementsByKey(GameElement.PLAYFILE);
			List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
			List<ElementObj> items = em.getElementsByKey(GameElement.ITEM);
			List<ElementObj> boss = em.getElementsByKey(GameElement.BOSS);

			moveAndUpdate(all, gameTime);
			ElementPK(bullets, maps);
			ElementPK(bullets, enemys);
			ElementPK(bullets, boss);
			enemyHitPlayer(enemys, players);
			enemyBulletHitPlayer(bullets, players);
			playerPickItem(players, items);
			playerRescueHostage(players, items);
			if (checkLevelEnd(players)) break;
			spawnEnemyWave(gameTime);
			spawnAircraft(gameTime);
			checkGameOver(players);
			updateCamera(players);

			gameTime++;
			sleepFrame(10);
		}
	}

	public void ElementPK(List<ElementObj> listA, List<ElementObj> listB) {
		if (listA == null || listB == null) return;
		for (int i = listA.size() - 1; i >= 0; i--) {
			ElementObj a = listA.get(i);
			if (!a.isLive()) continue;
			for (int j = listB.size() - 1; j >= 0; j--) {
				ElementObj b = listB.get(j);
				if (!b.isLive()) continue;
				if (a.pk(b)) {
					a.onHit(b);
					b.onHit(a);
				}
			}
		}
	}

	private void enemyHitPlayer(List<ElementObj> enemys, List<ElementObj> players) {
		if (enemys == null || players == null) return;
		for (ElementObj enemy : enemys) {
			if (!enemy.isLive()) continue;
			for (ElementObj player : players) {
				if (!player.isLive()) continue;
				if (enemy.pk(player)) {
					player.onHit(enemy);
				}
			}
		}
	}

	private void enemyBulletHitPlayer(List<ElementObj> bullets, List<ElementObj> players) {
		if (bullets == null || players == null) return;
		for (ElementObj bullet : bullets) {
			if (!bullet.isLive() || !"enemy".equals(bullet.getFrom())) continue;
			for (ElementObj player : players) {
				if (!player.isLive()) continue;
				if (bullet.pk(player)) {
					bullet.setLive(false);
					player.onHit(bullet);
				}
			}
		}
	}

	private void playerPickItem(List<ElementObj> players, List<ElementObj> items) {
		if (players == null || items == null) return;
		for (ElementObj player : players) {
			if (!player.isLive()) continue;
			for (int j = items.size() - 1; j >= 0; j--) {
				ElementObj item = items.get(j);
				if (!item.isLive()) continue;
				if (player.pk(item)) {
					player.onPickItem(item);
					item.setLive(false);
				}
			}
		}
	}

	/**
	 * 玩家解救hostage
	 */
	private void playerRescueHostage(List<ElementObj> players, List<ElementObj> items) {
		if (players == null || items == null) return;
		for (ElementObj player : players) {
			if (!player.isLive()) continue;
			for (ElementObj item : items) {
				if (!(item instanceof com.tedu.element.Hostage)) continue;
				if (!item.isLive()) continue;
				if (player.pk(item)) {
					item.onPickItem(player);
				}
			}
		}
	}

	public void moveAndUpdate(Map<GameElement, List<ElementObj>> all, long gameTime) {
		for (GameElement ge : GameElement.values()) {
			List<ElementObj> list = all.get(ge);
			if (list == null) continue;
			for (int i = list.size() - 1; i >= 0; i--) {
				ElementObj obj = list.get(i);
				if (!obj.isLive()) {
					obj.die();
					list.remove(i);
					continue;
				}
				obj.model(gameTime);
			}
		}
	}

	private boolean checkLevelEnd(List<ElementObj> players) {
		if (players == null || players.isEmpty()) return false;
		ElementObj player = players.get(0);
		if (!player.isLive()) return false;
		if (player.getX() >= GameMainJPanel.getLevelEndX()) {
			if (currentLevel >= MAX_LEVEL) {
				gameState = STATE_WIN;
				SoundManager.getInstance().stopBGM();
			} else {
				currentLevel++;
				gameState = STATE_PLAYING; // 自动加载下一关
				sleepFrame(500);
			}
			return true;
		}
		return false;
	}

	private void spawnEnemyWave(long gameTime) {
		if (gameTime % 300 == 0 && gameTime > 0) {
			String[] types = {"SOLDIER", "SOLDIER", "RUNNER"};
			ElementObj proto = GameLoad.getObj("enemy");
			if (proto == null) return;
			for (String type : types) {
				int x = GameMainJPanel.cameraX + 820 + (int) (Math.random() * 100);
				int y = 400;
				ElementObj enemy = proto.createElement("ENEMY," + x + "," + y + "," + type);
				if (enemy != null) em.addElement(enemy, GameElement.ENEMY);
			}
		}
	}

	/**
	 * 飞机波次 — 每600帧从天空飞过
	 */
	private void spawnAircraft(long gameTime) {
		if (gameTime % 600 == 0 && gameTime > 0) {
			ElementObj proto = GameLoad.getObj("aircraft");
			if (proto == null) return;
			boolean fromLeft = Math.random() < 0.5;
			int x = fromLeft ? -100 : 2500;
			int y = 60 + (int) (Math.random() * 100);
			int dir = fromLeft ? 1 : -1;
			ElementObj aircraft = proto.createElement("AIRCRAFT," + x + "," + y + "," + dir);
			if (aircraft != null) em.addElement(aircraft, GameElement.ENEMY);
		}
	}

	private void checkGameOver(List<ElementObj> players) {
		if (players == null || players.isEmpty()) {
			gameState = STATE_GAMEOVER;
			SoundManager.getInstance().stopBGM();
			return;
		}
		boolean allDead = true;
		for (ElementObj p : players) {
			if (p.isLive()) allDead = false;
		}
		if (allDead) {
			gameState = STATE_GAMEOVER;
			SoundManager.getInstance().stopBGM();
		}
	}

	private void updateCamera(List<ElementObj> players) {
		if (players == null || players.isEmpty()) return;
		ElementObj player = players.get(0);
		GameMainJPanel.followPlayer(player.getX(), player.getY());
	}

	private void sleepFrame(long millis) {
		try { Thread.sleep(millis); } catch (InterruptedException e) { e.printStackTrace(); }
	}

	public static int getCurrentLevel() { return currentLevel; }

	public static void setCurrentLevel(int lv) { currentLevel = lv; }
}
