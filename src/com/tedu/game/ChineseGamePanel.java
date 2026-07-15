package com.tedu.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.controller.GameThread;
import com.tedu.element.ElementObj;
import com.tedu.element.Player;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.SoundManager;
import com.tedu.show.GameMainJPanel;

/**
 * 合金弹头 — 中文界面层 (覆盖父类HUD绘制)
 */
public class ChineseGamePanel extends GameMainJPanel {
	private static final long serialVersionUID = 1L;
	private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 48);
	private static final Font LARGE_FONT = new Font("Microsoft YaHei", Font.BOLD, 28);
	private static final Font NORMAL_FONT = new Font("Microsoft YaHei", Font.PLAIN, 16);
	private static final ImageIcon MENU_BG = new ImageIcon("image/bg/background1.png");

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawChineseHud(g);

		int state = GameThread.gameState;
		if (state == GameThread.STATE_MENU) drawCnMenu(g);
		else if (state == GameThread.STATE_GAMEOVER) drawCnStateScreen(g, "任 务 失 败", Color.RED, "按回车键返回主菜单");
		else if (state == GameThread.STATE_WIN) drawCnStateScreen(g, "任 务 完 成", Color.YELLOW, "恭喜通关！按回车键重新开始");
		else if (state == GameThread.STATE_LEVEL_CLEAR) drawCnStateScreen(g, "关 卡 完 成", new Color(80, 255, 100), "正在进入下一关……");
		else if (state == GameThread.STATE_PAUSED) drawCnStateScreen(g, "暂 停", new Color(255, 220, 80), "按 P 键继续任务");
		if (state == GameThread.STATE_PLAYING && GameThread.getStageTime() < 110) drawMissionStart(g);
		if (state == GameThread.STATE_PLAYING) { drawCombatAlert(g); drawBossBar(g); drawBossWarning(g); }
	}

	private void drawChineseHud(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(new GradientPaint(0, 0, new Color(7, 10, 14, 245), 800, 0, new Color(42, 35, 20, 225)));
		g2.fillRect(0, 0, 800, 48);
		g.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
		g.setColor(Color.WHITE);

		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (players.isEmpty()) {
			g.drawString("生命：0   分数：0   武器：普通枪", 16, 28);
			return;
		}

		ElementObj p = players.get(0);
		// 血量条
		g.drawString("血量", 8, 28);
		g.setColor(new Color(100, 20, 20));
		g.fillRect(48, 12, 100, 18);
		g.setColor(new Color(50, 210, 70));
		int hpW = Math.max(0, p.getHp() * 100 / Math.max(1, p.getMaxHp()));
		g.fillRect(48, 12, hpW, 18);
		g.setColor(Color.WHITE);
		g.drawRect(48, 12, 100, 18);
		g.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
		g.drawString(p.getHp() + "/" + p.getMaxHp(), 80, 27);

		g.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
		g.drawString(String.format("得分 %07d", p.getScore()), 165, 28);
		String ammo = "∞"; int grenades = 0;
		if (p instanceof Player) { int a = ((Player)p).getAmmo(); ammo = a < 0 ? "∞" : String.valueOf(a); grenades = ((Player)p).getGrenades(); }
		g.drawString(cnWeapon(p.getWeaponName()) + "  " + ammo, 330, 28);
		g.drawString("炸弹 " + grenades, 470, 28);
		g.drawString("命 × " + p.getLives(), 565, 28);
		g.setColor(new Color(255, 212, 65)); g.drawString("MISSION " + GameThread.getCurrentLevel(), 675, 28);
		// Level progress line.
		g.setColor(new Color(255,255,255,60)); g.fillRect(0, 45, 800, 3);
		g.setColor(new Color(255,180,45)); g.fillRect(0, 45, Math.min(800, p.getX() * 800 / 2220), 3);
	}

	private void drawCnMenu(Graphics g) {
		if (MENU_BG.getIconWidth() > 0) g.drawImage(MENU_BG.getImage(), 0, 0, 800, 600, null);
		else {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setPaint(new GradientPaint(0, 0, new Color(18, 22, 40), 0, 600, new Color(45, 28, 18)));
			g2d.fillRect(0, 0, 800, 600);
		}
		g.setColor(new Color(8, 14, 24, 30)); g.fillRect(0, 0, 800, 600);

		g.setFont(TITLE_FONT);
		g.setColor(new Color(255, 210, 50));
		drawCentered(g, "合 金 弹 头", 155);

		g.setColor(new Color(255, 140, 20));
		g.fillRect(200, 170, 400, 3);

		g.setFont(LARGE_FONT);
		g.setColor(Color.WHITE);
		drawCentered(g, "觉 醒 版", 215);

		g.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
		g.setColor(new Color(80, 220, 255));
		drawCentered(g, "按 回 车 键 开 始 游 戏", 295);

		g.setFont(NORMAL_FONT);
		g.setColor(Color.WHITE);
		drawCentered(g, "A / D 或 ← →：移动       W：跳跃       ↑ / I：向上瞄准", 360);
		drawCentered(g, "S / ↓：蹲下       J / 空格：射击 / 近战", 395);
		drawCentered(g, "K / Ctrl：手雷       P：暂停       M：静音", 430);

		g.setColor(new Color(255, 220, 100));
		drawCentered(g, "消灭敌人、解救人质，击败第三关首领！", 490);

		g.setFont(new Font("Microsoft YaHei", Font.ITALIC, 11));
		g.setColor(Color.GRAY);
		drawCentered(g, "基于 JavaSE + Swing 构建  |  合金弹头美术资源", 565);
	}

	private void drawCnStateScreen(Graphics g, String title, Color color, String tip) {
		g.setColor(new Color(0, 0, 0, 230));
		g.fillRect(0, 0, 800, 600);
		g.setFont(TITLE_FONT);
		g.setColor(color);
		drawCentered(g, title, 260);
		g.setFont(NORMAL_FONT);
		g.setColor(Color.WHITE);
		drawCentered(g, tip, 340);
	}

	private void drawMissionStart(Graphics g) {
		long t = GameThread.getStageTime();
		int alpha = (int)Math.min(255, Math.min(t * 8, (110 - t) * 8));
		alpha = Math.max(0, alpha);
		g.setColor(new Color(0, 0, 0, Math.min(150, alpha / 2))); g.fillRect(0, 220, 800, 125);
		g.setFont(new Font("Arial Black", Font.BOLD, 43)); g.setColor(new Color(255, 205, 50, alpha));
		drawCentered(g, "MISSION " + GameThread.getCurrentLevel() + " START!", 285);
		g.setFont(new Font("Microsoft YaHei", Font.BOLD, 16)); g.setColor(new Color(255, 255, 255, alpha));
		drawCentered(g, GameThread.getCurrentLevel() == 3 ? "突破防线 · 摧毁敌军首领" : "向右推进 · 消灭敌军 · 解救人质", 320);
	}

	private void drawCombatAlert(Graphics g) {
		int alert = GameThread.getCombatAlert();
		if (alert <= 0) return;
		String msg = GameThread.isCombatActive() ? "!!  ENEMY ATTACK  !!" : "AREA CLEAR — GO!  >>";
		int alpha = Math.min(230, alert * 7);
		g.setColor(new Color(0, 0, 0, Math.min(145, alpha))); g.fillRect(215, 82, 370, 44);
		g.setColor(GameThread.isCombatActive() ? new Color(255, 74, 35, alpha) : new Color(255, 220, 65, alpha));
		g.setFont(new Font("Arial Black", Font.BOLD, 24)); drawCentered(g, msg, 113);
	}

	private void drawBossBar(Graphics g) {
		List<ElementObj> bosses = ElementManager.getManager().getElementsByKey(GameElement.BOSS);
		if (bosses == null || bosses.isEmpty() || !bosses.get(0).isLive()) return;
		ElementObj boss = bosses.get(0);
		g.setColor(new Color(0, 0, 0, 205)); g.fillRoundRect(170, 520, 460, 42, 8, 8);
		g.setColor(new Color(130, 22, 18)); g.fillRect(230, 538, 360, 10);
		g.setColor(new Color(255, 72, 30)); g.fillRect(230, 538, Math.max(0, 360 * boss.getHp() / boss.getMaxHp()), 10);
		g.setColor(Color.WHITE); g.drawRect(230, 538, 360, 10);
		g.setFont(new Font("Arial Black", Font.BOLD, 13)); g.setColor(new Color(255, 215, 60)); g.drawString("BOSS", 181, 548);
	}

	private void drawBossWarning(Graphics g) {
		int t = GameThread.getBossAlert(); if (t <= 0) return;
		g.setColor(new Color(110, 0, 0, 170)); g.fillRect(0, 165, 800, 100);
		g.setColor((t / 8) % 2 == 0 ? Color.WHITE : new Color(255, 55, 30));
		g.setFont(new Font("Arial Black", Font.BOLD, 45)); drawCentered(g, "WARNING!", 225);
		g.setFont(new Font("Microsoft YaHei", Font.BOLD, 15)); g.setColor(Color.WHITE);
		drawCentered(g, "大型敌军单位接近", 252);
	}

	private String cnWeapon(String w) {
		switch (w) {
			case "HEAVY": return "重机枪";
			case "SPREAD": return "散弹枪";
			case "FLAME": return "火焰枪";
			default: return "普通枪";
		}
	}

	private void drawCentered(Graphics g, String text, int y) {
		int wd = g.getFontMetrics().stringWidth(text);
		g.drawString(text, (800 - wd) / 2, y);
	}
}
