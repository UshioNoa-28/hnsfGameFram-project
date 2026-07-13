package com.tedu.controller;

import java.util.List;
import java.util.Map;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameMainJPanel;

/**
 * @说明 游戏主线程：关卡加载 → 游戏运行(移动/碰撞) → 关卡切换
 * @author renjj
 */
public class GameThread extends Thread {
	private ElementManager em;

	/** 当前关卡编号 */
	private static int currentLevel = 1;

	public static void resetGame() {
		currentLevel = 1;
		gameState = STATE_MENU;
	}
	/** 最大关卡数 */
	private static final int MAX_LEVEL = 3;
	/** 游戏状态 */
	public static int STATE_MENU = 0;
	public static int STATE_PLAYING = 1;
	public static int STATE_GAMEOVER = 2;
	public static int STATE_WIN = 3;
	public static int STATE_LEVEL_CLEAR = 4; // 单关通关

	public static int gameState = STATE_MENU;

	public GameThread() {
		em = ElementManager.getManager();
	}

	@Override
	public void run() {
		while (true) {
			switch (gameState) {
				case 0: // 菜单
					gameMenu();
					break;
				case 1: // 游戏中
					gameLoad();
					gameRun();
					gameOver();
					break;
				case 2: // 游戏结束
				case 3: // 胜利
					sleepFrame(50);
					break;
				case 4: // 关卡通关动画
					sleepFrame(50);
					break;
				default:
					sleepFrame(50);
					break;
			}
			sleepFrame(50);
		}
	}

	private void gameMenu() {
		sleepFrame(30);
	}

	/**
	 * 关卡加载
	 */
	private void gameLoad() {
		// 清空上一关的所有元素
		em.clearAll();
		// 加载图片
		GameLoad.loadImg();
		// 加载对象注册表
		GameLoad.loadObj();
		// 加载关卡地图
		GameLoad.MapLoad(currentLevel);
		// 加载玩家
		GameLoad.loadPlay();
		// 重置摄像机位置
		GameMainJPanel.cameraX = 0;
		GameMainJPanel.cameraY = 0;
		System.out.println("=== 第 " + currentLevel + " 关 加载完成 ===");
	}

	/**
	 * 游戏运行时主循环
	 */
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
			if (checkLevelEnd(players)) break;
			spawnEnemyWave(gameTime);
			checkGameOver(players);
			updateCamera(players);

			gameTime++;
			sleepFrame(10);
		}
	}

	/**
	 * 子弹碰撞地图元素
	 */
	public void ElementPK(List<ElementObj> listA, List<ElementObj> listB) {
		if (listA == null || listB == null) return;
		for (int i = listA.size() - 1; i >= 0; i--) {
			ElementObj a = listA.get(i);
			if (!a.isLive()) continue;
			for (int j = listB.size() - 1; j >= 0; j--) {
				ElementObj b = listB.get(j);
				if (!b.isLive()) continue;
				if (a.pk(b)) {
					a.onHit(b); // 子弹被击中
					b.onHit(a); // 目标被击中
				}
			}
		}
	}

	/**
	 * 敌人碰撞玩家 -> 玩家受伤
	 */
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

	/**
	 * 敌人子弹伤害玩家
	 */
	private void enemyBulletHitPlayer(List<ElementObj> bullets, List<ElementObj> players) {
		if (bullets == null || players == null) return;
		for (ElementObj bullet : bullets) {
			if (!bullet.isLive()) continue;
			// 只处理敌方子弹
			if (!"enemy".equals(bullet.getFrom())) continue;
			for (ElementObj player : players) {
				if (!player.isLive()) continue;
				if (bullet.pk(player)) {
					bullet.setLive(false);
					player.onHit(bullet);
				}
			}
		}
	}

	/**
	 * 玩家拾取道具
	 */
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
	 * 移动和更新所有活跃元素
	 */
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

	/**
	 * 检查玩家是否到达终点旗帜
	 */
	private boolean checkLevelEnd(List<ElementObj> players) {
		if (players == null || players.isEmpty()) return false;
		ElementObj player = players.get(0);
		if (!player.isLive()) return false;
		if (player.getX() >= GameMainJPanel.getLevelEndX()) {
			if (currentLevel >= MAX_LEVEL) {
				gameState = STATE_WIN;
			} else {
				currentLevel++;
				sleepFrame(500);
			}
			return true;
		}
		return false;
	}

	/**
	 * 波次生成敌人
	 */
	private void spawnEnemyWave(long gameTime) {
		// 每300帧生成一波敌人
		if (gameTime % 300 == 0 && gameTime > 0) {
			String[] types = {"SOLDIER", "SOLDIER", "RUNNER"};
			ElementObj proto = GameLoad.getObj("enemy");
			if (proto == null) return;
			for (String type : types) {
				int x = GameMainJPanel.cameraX + 800 + (int)(Math.random() * 100);
				int y = 400;
				// 格式: ENEMY,x,y,type  (匹配.map格式)
				ElementObj enemy = proto.createElement("ENEMY," + x + "," + y + "," + type);
				if (enemy != null) {
					em.addElement(enemy, GameElement.ENEMY);
				}
			}
		}
	}

	/**
	 * 检查游戏结束
	 */
	private void checkGameOver(List<ElementObj> players) {
		if (players == null || players.isEmpty()) {
			gameState = STATE_GAMEOVER;
			return;
		}
		boolean allDead = true;
		for (ElementObj p : players) {
			if (p.isLive()) allDead = false;
		}
		if (allDead) {
			gameState = STATE_GAMEOVER;
		}
	}

	/**
	 * 摄像机跟随玩家
	 */
	private void updateCamera(List<ElementObj> players) {
		if (players == null || players.isEmpty()) return;
		ElementObj player = players.get(0);
		GameMainJPanel.followPlayer(player.getX(), player.getY());
	}

	/** 关卡切换时的回收 */
	private void gameOver() {
		// 由gameLoad中的clearAll处理
	}

	private void sleepFrame(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static int getCurrentLevel() {
		return currentLevel;
	}
}
