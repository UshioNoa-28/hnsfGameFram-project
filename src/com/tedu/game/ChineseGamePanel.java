package com.tedu.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import com.tedu.controller.GameThread;
import com.tedu.element.ElementObj;
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

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		drawChineseHud(g);

		int state = GameThread.gameState;
		if (state == GameThread.STATE_MENU) drawCnMenu(g);
		else if (state == GameThread.STATE_GAMEOVER) drawCnStateScreen(g, "任 务 失 败", Color.RED, "按回车键返回主菜单");
		else if (state == GameThread.STATE_WIN) drawCnStateScreen(g, "任 务 完 成", Color.YELLOW, "恭喜通关！按回车键重新开始");
		else if (state == GameThread.STATE_LEVEL_CLEAR) drawCnStateScreen(g, "关 卡 完 成", new Color(80, 255, 100), "正在进入下一关……");
	}

	private void drawChineseHud(Graphics g) {
		g.setColor(new Color(0, 0, 0, 230));
		g.fillRect(0, 0, 800, 44);
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
		g.drawString("分数：" + p.getScore(), 165, 28);
		g.drawString("武器：" + cnWeapon(p.getWeaponName()), 330, 28);
		g.drawString("生命：" + p.getLives(), 510, 28);
		g.drawString("第 " + GameThread.getCurrentLevel() + " 关", 660, 28);
	}

	private void drawCnMenu(Graphics g) {
		g.setColor(new Color(5, 12, 25));
		g.fillRect(0, 0, 800, 600);

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
		drawCentered(g, "A / D 或方向键：移动       W / ↑：跳跃", 360);
		drawCentered(g, "S / ↓：蹲下闪避       J / 空格：射击", 395);
		drawCentered(g, "M 键：开关音乐", 430);

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
