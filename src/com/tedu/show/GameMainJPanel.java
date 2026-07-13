package com.tedu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * @说明 游戏主面板 — 带摄像机滚动、HUD、状态画面
 * @author renjj
 */
public class GameMainJPanel extends JPanel implements Runnable {

	private ElementManager em;

	/** 摄像机偏移量 */
	public static int cameraX = 0;
	public static int cameraY = 0;
	/** 世界大小 */
	public static final int WORLD_WIDTH = 2400;
	public static final int WORLD_HEIGHT = 600;
	/** 关卡终点X坐标 (由地图配置) */
	private static int levelEndX = 2200;

	public GameMainJPanel() {
		init();
	}

	public void init() {
		em = ElementManager.getManager();
	}

	public static void followPlayer(int px, int py) {
		cameraX = px - 400; // 玩家在屏幕中间
		cameraY = py - 300;
		// 边界限制
		if (cameraX < 0) cameraX = 0;
		if (cameraY < 0) cameraY = 0;
		if (cameraX > WORLD_WIDTH - 800) cameraX = WORLD_WIDTH - 800;
		if (cameraY > WORLD_HEIGHT - 600) cameraY = WORLD_HEIGHT - 600;
	}

	public static int getLevelEndX() {
		return levelEndX;
	}

	public static void setLevelEndX(int x) {
		levelEndX = x;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;

		// 绘制天空背景 (固定在摄像机坐标)
		drawSky(g2d);

		// 摄像机变换
		g2d.translate(-cameraX, -cameraY);

		Map<GameElement, List<ElementObj>> all = em.getGameElements();

		// 绘制顺序：MAPS(地形背景) → ITEM → ENEMY → BOSS → PLAY → PLAYFILE(子弹) → EFFECT
		drawElements(all.get(GameElement.MAPS), g2d);
		drawElements(all.get(GameElement.ITEM), g2d);
		drawElements(all.get(GameElement.ENEMY), g2d);
		drawElements(all.get(GameElement.BOSS), g2d);
		drawElements(all.get(GameElement.PLAY), g2d);
		drawElements(all.get(GameElement.PLAYFILE), g2d);
		drawElements(all.get(GameElement.EFFECT), g2d);

		// 恢复摄像机
		g2d.translate(cameraX, cameraY);

		// 绘制HUD (固定位置)
		drawHUD(g2d);
	}

	private void drawSky(Graphics g) {
		// 渐变天空
		Graphics2D g2d = (Graphics2D) g;
		Color skyTop = new Color(100, 150, 255);
		Color skyBottom = new Color(200, 230, 255);
		for (int y = 0; y < 600; y++) {
			float ratio = y / 600f;
			int r = (int) (skyTop.getRed() * (1 - ratio) + skyBottom.getRed() * ratio);
			int gr = (int) (skyTop.getGreen() * (1 - ratio) + skyBottom.getGreen() * ratio);
			int b = (int) (skyTop.getBlue() * (1 - ratio) + skyBottom.getBlue() * ratio);
			g.setColor(new Color(r, gr, b));
			g.drawLine(0, y, 800, y);
		}

		// 绘制远景山 (相对缓慢移动 = 视差)
		g2d.setColor(new Color(120, 180, 120));
		for (int i = 0; i < 5; i++) {
			int baseX = (i * 200 - cameraX / 3 % 600 + 600) % 600 - 100;
			int baseY = 480;
			int[] xs = {baseX, baseX + 80, baseX + 160, baseX + 200};
			int[] ys = {baseY, baseY - 80, baseY - 60, baseY};
			g2d.fillPolygon(xs, ys, 4);
		}
	}

	private void drawElements(List<ElementObj> list, Graphics g) {
		if (list == null) return;
		for (int i = 0; i < list.size(); i++) {
			ElementObj obj = list.get(i);
			if (obj.isLive()) {
				obj.showElement(g);
			}
		}
	}

	/**
	 * 绘制HUD (固定在屏幕左上角,不受摄像机影响)
	 */
	private void drawHUD(Graphics g) {
		Map<GameElement, List<ElementObj>> all = em.getGameElements();
		List<ElementObj> players = all.get(GameElement.PLAY);

		// 半透明背景
		g.setColor(new Color(0, 0, 0, 150));
		g.fillRect(0, 0, 800, 40);

		g.setFont(new Font("Monospaced", Font.BOLD, 18));
		g.setColor(Color.WHITE);

		// 显示玩家信息
		if (players != null && !players.isEmpty()) {
			ElementObj player = players.get(0);
			int hp = player.getHp();
			int maxHp = player.getMaxHp();
			String weapon = player.getWeaponName();
			int score = player.getScore();
			int lives = player.getLives();

			// 血量条
			g.drawString("HP:", 10, 28);
			g.setColor(Color.RED);
			g.fillRect(50, 12, 100, 18);
			g.setColor(Color.GREEN);
			int hpW = Math.max(0, hp * 100 / maxHp);
			g.fillRect(50, 12, hpW, 18);
			g.setColor(Color.WHITE);
			g.drawRect(50, 12, 100, 18);

			g.drawString("SCORE:" + score, 170, 28);
			g.drawString("WEAPON:" + weapon, 370, 28);
			g.drawString("LIVES:" + lives, 560, 28);
		} else {
			g.drawString("SCORE:0  WEAPON:NORMAL  LIVES:0", 20, 28);
		}

		// 绘制游戏状态画面
		int state = com.tedu.controller.GameThread.gameState;
		if (state == com.tedu.controller.GameThread.STATE_MENU) {
			drawMenu(g);
		} else if (state == com.tedu.controller.GameThread.STATE_GAMEOVER) {
			drawGameOver(g);
		} else if (state == com.tedu.controller.GameThread.STATE_WIN) {
			drawWin(g);
		} else if (state == com.tedu.controller.GameThread.STATE_LEVEL_CLEAR) {
			drawLevelClear(g);
		}
	}

	private void drawMenu(Graphics g) {
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 48));
		g.setColor(Color.YELLOW);
		drawCenteredString(g, "METAL SLUG", 800, 180);

		g.setFont(new Font("Arial", Font.BOLD, 24));
		g.setColor(Color.WHITE);
		drawCenteredString(g, "JavaSE Edition", 800, 230);

		g.setFont(new Font("Arial", Font.PLAIN, 18));
		g.setColor(Color.CYAN);
		drawCenteredString(g, "Press ENTER to Start", 800, 320);
		drawCenteredString(g, "A/D or <-/-> Move", 800, 360);
		drawCenteredString(g, "W or Up Jump", 800, 390);
		drawCenteredString(g, "J or Space Shoot", 800, 420);
		drawCenteredString(g, "S or Down Crouch", 800, 450);
	}

	private void drawGameOver(Graphics g) {
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 56));
		g.setColor(Color.RED);
		drawCenteredString(g, "GAME OVER", 800, 260);

		g.setFont(new Font("Arial", Font.PLAIN, 20));
		g.setColor(Color.WHITE);
		drawCenteredString(g, "Press ENTER to Restart", 800, 340);
	}

	private void drawWin(Graphics g) {
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 56));
		g.setColor(Color.YELLOW);
		drawCenteredString(g, "MISSION COMPLETE", 800, 260);

		g.setFont(new Font("Arial", Font.BOLD, 24));
		g.setColor(Color.WHITE);
		drawCenteredString(g, "Congratulations! You Win!", 800, 320);

		g.setFont(new Font("Arial", Font.PLAIN, 18));
		g.setColor(Color.CYAN);
		drawCenteredString(g, "Press ENTER to Play Again", 800, 380);
	}

	private void drawLevelClear(Graphics g) {
		g.setColor(new Color(0, 0, 0, 150));
		g.fillRect(0, 0, 800, 600);

		g.setFont(new Font("Arial", Font.BOLD, 48));
		g.setColor(Color.GREEN);
		drawCenteredString(g, "LEVEL CLEAR!", 800, 280);
	}

	private void drawCenteredString(Graphics g, String str, int w, int y) {
		int sw = g.getFontMetrics().stringWidth(str);
		g.drawString(str, (w - sw) / 2, y);
	}

	@Override
	public void run() {
		while (true) {
			this.repaint();
			try {
				Thread.sleep(16); // ~60 FPS
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
