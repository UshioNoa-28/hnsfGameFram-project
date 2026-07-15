package com.tedu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.tedu.element.Background;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * 游戏主面板 — Metal Slug风格渲染
 */
public class GameMainJPanel extends JPanel implements Runnable {

	private ElementManager em;
	public static int cameraX = 0;
	public static int cameraY = 0;
	public static final int WORLD_WIDTH = 2400;
	public static final int WORLD_HEIGHT = 600;
	private static int levelEndX = 2200;

	public GameMainJPanel() {
		init();
	}

	public void init() {
		em = ElementManager.getManager();
	}

	public static void followPlayer(int px, int py) {
		cameraX = px - 400;
		cameraY = py - 300;
		if (cameraX < 0) cameraX = 0;
		if (cameraY < 0) cameraY = 0;
		if (cameraX > WORLD_WIDTH - 800) cameraX = WORLD_WIDTH - 800;
		if (cameraY > WORLD_HEIGHT - 600) cameraY = WORLD_HEIGHT - 600;
	}

	public static int getLevelEndX() { return levelEndX; }
	public static void setLevelEndX(int x) { levelEndX = x; }

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		// 视差背景
		Background.drawParallaxBackground(g, cameraX, cameraY, WORLD_WIDTH);

		// 摄像机变换
		g2d.translate(-cameraX, -cameraY);

		Map<GameElement, List<ElementObj>> all = em.getGameElements();

		// 绘制顺序: MAPS → ITEM → ENEMY → BOSS → PLAY → PLAYFILE → EFFECT
		drawElements(all.get(GameElement.MAPS), g2d);
		drawElements(all.get(GameElement.ITEM), g2d);
		drawElements(all.get(GameElement.ENEMY), g2d);
		drawElements(all.get(GameElement.BOSS), g2d);
		drawElements(all.get(GameElement.PLAY), g2d);
		drawElements(all.get(GameElement.PLAYFILE), g2d);
		drawElements(all.get(GameElement.EFFECT), g2d);

		// 恢复摄像机
		g2d.translate(cameraX, cameraY);

		// HUD
		drawHUD(g2d);
	}

	private void drawElements(List<ElementObj> list, Graphics g) {
		if (list == null) return;
		for (int i = 0; i < list.size(); i++) {
			ElementObj obj = list.get(i);
			if (obj.isLive()) obj.showElement(g);
		}
	}

	private void drawHUD(Graphics g) {
		Map<GameElement, List<ElementObj>> all = em.getGameElements();
		List<ElementObj> players = all.get(GameElement.PLAY);

		// 半透明HUD背景
		g.setColor(new Color(0, 0, 0, 170));
		g.fillRect(0, 0, 800, 44);
		g.setColor(new Color(60, 60, 60, 120));
		g.fillRect(0, 44, 800, 2);

		if (players != null && !players.isEmpty()) {
			ElementObj p = players.get(0);
			int hp = p.getHp(), maxHp = p.getMaxHp();
			String weapon = p.getWeaponName();
			int score = p.getScore(), lives = p.getLives();

			// HP条
			g.setFont(new Font("Arial", Font.BOLD, 13));
			g.setColor(Color.WHITE);
			g.drawString("HP", 8, 28);

			g.setColor(new Color(80, 20, 20));
			g.fillRect(34, 12, 110, 20);
			g.setColor(new Color(40, 200, 60));
			g.fillRect(34, 12, Math.max(0, hp * 110 / maxHp), 20);
			g.setColor(Color.WHITE);
			g.drawRect(34, 12, 110, 20);
			g.setFont(new Font("Arial", Font.BOLD, 12));
			g.drawString(hp + "/" + maxHp, 65, 27);

			// 分数
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, 14));
			g.drawString("SCORE " + score, 160, 28);

			// 武器图标
			g.drawString("WPN", 330, 28);
			g.setColor(Color.YELLOW);
			g.fillRoundRect(370, 10, 80, 24, 6, 6);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Arial", Font.BOLD, 12));
			String wLabel = weapon;
			switch (weapon) {
				case "HEAVY": wLabel = "HEAVY MG"; break;
				case "SPREAD": wLabel = "SPREAD"; break;
				case "FLAME": wLabel = "FLAME"; break;
				default: wLabel = "NORMAL"; break;
			}
			g.drawString(wLabel, 385, 27);

			// 生命数
			g.setColor(Color.WHITE);
			g.drawString("LIVES " + lives, 510, 28);

			// 关卡
			g.setColor(Color.CYAN);
			g.drawString("STAGE " + com.tedu.controller.GameThread.getCurrentLevel(), 650, 28);

			// 武器剩余时间指示
			if (!"NORMAL".equals(weapon)) {
				g.setColor(Color.ORANGE);
				g.fillRect(370, 34, 80, 4);
			}
		}

		// 状态画面
		int state = com.tedu.controller.GameThread.gameState;
		if (state == com.tedu.controller.GameThread.STATE_MENU) drawMenu(g);
		else if (state == com.tedu.controller.GameThread.STATE_GAMEOVER) drawGameOver(g);
		else if (state == com.tedu.controller.GameThread.STATE_WIN) drawWin(g);
		else if (state == com.tedu.controller.GameThread.STATE_LEVEL_CLEAR) drawLevelClear(g);
	}

	private void drawMenu(Graphics g) {
		g.setColor(new Color(0, 0, 0, 200));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 56));
		g.setColor(Color.YELLOW);
		drawCentered(g, "METAL SLUG", 800, 160);

		g.setColor(Color.ORANGE);
		g.fillRect(180, 172, 440, 4);

		g.setFont(new Font("Arial", Font.BOLD, 18));
		g.setColor(Color.WHITE);
		drawCentered(g, "AWAKENED EDITION", 800, 210);

		g.setColor(Color.CYAN);
		g.setFont(new Font("Arial", Font.BOLD, 22));
		drawCentered(g, "PRESS ENTER TO START", 800, 290);

		g.setFont(new Font("Arial", Font.PLAIN, 15));
		g.setColor(new Color(200, 200, 200));
		drawCentered(g, "A/D or Arrow Keys : MOVE", 800, 350);
		drawCentered(g, "W or Up Arrow : JUMP", 800, 375);
		drawCentered(g, "S or Down Arrow : CROUCH", 800, 400);
		drawCentered(g, "J or Space : SHOOT", 800, 425);

		g.setColor(Color.ORANGE);
		drawCentered(g, "M : TOGGLE MUSIC", 800, 470);

		g.setFont(new Font("Arial", Font.ITALIC, 11));
		g.setColor(Color.GRAY);
		drawCentered(g, "Built with JavaSE + Swing  |  Metal Slug Sprite Resources", 800, 560);
	}

	private void drawGameOver(Graphics g) {
		g.setColor(new Color(0, 0, 0, 200));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 60));
		g.setColor(Color.RED);
		drawCentered(g, "MISSION FAILED", 800, 270);

		g.setFont(new Font("Arial", Font.PLAIN, 18));
		g.setColor(Color.WHITE);
		drawCentered(g, "PRESS ENTER TO CONTINUE", 800, 350);
	}

	private void drawWin(Graphics g) {
		g.setColor(new Color(0, 0, 0, 200));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 56));
		g.setColor(Color.YELLOW);
		drawCentered(g, "MISSION COMPLETE!", 800, 250);

		g.setFont(new Font("Arial", Font.BOLD, 22));
		g.setColor(Color.WHITE);
		drawCentered(g, "ALL CLEAR — CONGRATULATIONS!", 800, 310);

		g.setFont(new Font("Arial", Font.PLAIN, 16));
		g.setColor(Color.CYAN);
		drawCentered(g, "PRESS ENTER TO PLAY AGAIN", 800, 380);
	}

	private void drawLevelClear(Graphics g) {
		g.setColor(new Color(0, 0, 0, 160));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 44));
		g.setColor(Color.GREEN);
		drawCentered(g, "STAGE CLEAR!", 800, 280);
	}

	private void drawCentered(Graphics g, String str, int w, int y) {
		int sw = g.getFontMetrics().stringWidth(str);
		g.drawString(str, (w - sw) / 2, y);
	}

	@Override
	public void run() {
		while (true) {
			this.repaint();
			try { Thread.sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
