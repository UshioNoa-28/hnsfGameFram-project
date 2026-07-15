package com.tedu.controller;

import java.util.List;
import java.util.Map;

import com.tedu.element.ElementObj;
import com.tedu.element.Enemy;
import com.tedu.element.ItemDrop;
import com.tedu.element.CombatEffect;
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
	public static final int STATE_PAUSED = 5;

	public static int gameState = STATE_MENU;
	private static long stageTime = 0;
	private int transitionTicks = 0;
	private int[] battleZones;
	private int battleZoneIndex, battleWave, battleDelay;
	private boolean battleActive;
	private static int combatLeft = -1, combatRight = -1, combatAlert = 0;
	private static int bossAlert = 0;
	private boolean bossTriggered;

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
					sleepFrame(50);
					break;
				case STATE_LEVEL_CLEAR:
					if (--transitionTicks <= 0) { currentLevel++; gameState = STATE_PLAYING; }
					break;
				case STATE_PAUSED:
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
		GameMainJPanel.resetCamera();
		stageTime = 0;
		battleZones = currentLevel == 1 ? new int[] {520, 1220, 1800}
				: currentLevel == 2 ? new int[] {430, 1080, 1720} : new int[] {430, 1120};
		battleZoneIndex = 0; battleWave = 0; battleDelay = 0; battleActive = false;
		combatLeft = combatRight = -1; combatAlert = 0;
		bossAlert = 0; bossTriggered = false;

		// 启动背景音乐
		try {
			SoundManager.getInstance().startBGM();
		} catch (Exception e) {}

		System.out.println("=== STAGE " + currentLevel + " READY ===");
	}

	private void gameRun() {
		long gameTime = 0L;
		while (gameState == STATE_PLAYING || gameState == STATE_PAUSED) {
			if (gameState == STATE_PAUSED) { sleepFrame(40); continue; }
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
			updateBattleDirector(players);
			if (checkLevelEnd(players)) break;
			spawnAircraft(gameTime);
			checkGameOver(players);
			updateCamera(players);

			gameTime++; stageTime = gameTime;
			sleepFrame(16);
		}
	}

	private void updateBattleDirector(List<ElementObj> players) {
		if (players == null || players.isEmpty() || !players.get(0).isLive()) return;
		ElementObj player = players.get(0);
		if (combatAlert > 0) combatAlert--;
		if (bossAlert > 0) bossAlert--;
		if (currentLevel == 3 && !bossTriggered && player.getX() >= 1620
				&& !em.getElementsByKey(GameElement.BOSS).isEmpty()) {
			bossTriggered = true; bossAlert = 170; GameMainJPanel.shake(9);
			try { SoundManager.getInstance().playSFX("boss"); } catch (Exception ignored) {}
		}
		if (!battleActive) {
			if (battleZoneIndex < battleZones.length && player.getX() >= battleZones[battleZoneIndex]) {
				battleActive = true; battleWave = 1; battleDelay = 0;
				combatLeft = Math.max(0, battleZones[battleZoneIndex] - 220);
				combatRight = Math.min(2180, battleZones[battleZoneIndex] + 500);
				combatAlert = 110;
				spawnDirectedWave(battleWave);
			}
			return;
		}

		int living = 0;
		for (ElementObj e : em.getElementsByKey(GameElement.ENEMY)) {
			if (e instanceof Enemy && e.isLive() && e.getX() >= combatLeft - 100 && e.getX() <= combatRight + 100) living++;
		}
		if (living > 0) { battleDelay = 0; return; }
		if (++battleDelay < 55) return;
		battleDelay = 0;
		if (battleWave < (currentLevel == 1 ? 2 : 3)) {
			spawnDirectedWave(++battleWave); combatAlert = 45;
		} else {
			String rewardType = player.getHp() * 2 <= player.getMaxHp() ? "HEALTH"
					: (battleZoneIndex % 2 == 0 ? "WEAPON_H" : "WEAPON_S");
			ItemDrop reward = new ItemDrop(); reward.createElement((combatRight - 115) + ",370," + rewardType);
			em.addElement(reward, GameElement.ITEM);
			player.setScore(player.getScore() + 250);
			CombatEffect clearText = new CombatEffect();
			clearText.createElement((player.getX() - 15) + "," + (player.getY() - 10) + ",text,1,AREA BONUS +250");
			em.addElement(clearText, GameElement.EFFECT);
			battleActive = false; battleZoneIndex++; combatLeft = combatRight = -1; combatAlert = 75;
		}
	}

	private void spawnDirectedWave(int wave) {
		String[][] patterns = currentLevel == 1
				? new String[][] {{"SOLDIER","SOLDIER","RUNNER"},{"RUNNER","SOLDIER","SNIPER"}}
				: new String[][] {{"SOLDIER","RUNNER","SOLDIER"},{"SNIPER","RUNNER","RUNNER"},{"TURRET","SOLDIER","SNIPER"}};
		String[] pattern = patterns[Math.min(wave - 1, patterns.length - 1)];
		for (int i = 0; i < pattern.length; i++) {
			ElementObj proto = GameLoad.getObj("enemy"); if (proto == null) continue;
			int x = combatRight - 35 - i * 55;
			ElementObj enemy = proto.createElement("ENEMY," + x + ",390," + pattern[i]);
			if (enemy != null) em.addElement(enemy, GameElement.ENEMY);
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
				if ("enemy".equals(a.getFrom()) && "enemy".equals(b.getFrom())) continue;
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
				gameState = STATE_LEVEL_CLEAR;
				transitionTicks = 22;
			}
			return true;
		}
		return false;
	}

	private void spawnEnemyWave(long gameTime) {
		if (gameTime % 420 == 0 && gameTime > 0 && em.getElementsByKey(GameElement.ENEMY).size() < 9
				&& em.getElementsByKey(GameElement.BOSS).isEmpty()) {
			String[] types = {"SOLDIER", "SOLDIER", "RUNNER"};
			ElementObj proto = GameLoad.getObj("enemy");
			if (proto == null) return;
			for (String type : types) {
				int x = Math.min(2180, GameMainJPanel.cameraX + 820 + (int) (Math.random() * 100));
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
		if (gameTime % 900 == 0 && gameTime > 0) {
			int aircraftCount = 0;
			for (ElementObj e : em.getElementsByKey(GameElement.ENEMY)) if (e instanceof com.tedu.element.Aircraft && e.isLive()) aircraftCount++;
			if (aircraftCount >= 1) return;
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
	public static long getStageTime() { return stageTime; }
	public static boolean isCombatActive() { return combatRight >= 0; }
	public static int getCombatLeft() { return combatLeft; }
	public static int getCombatRight() { return combatRight; }
	public static int getCombatAlert() { return combatAlert; }
	public static int getBossAlert() { return bossAlert; }

	public static void setCurrentLevel(int lv) { currentLevel = lv; }
}
