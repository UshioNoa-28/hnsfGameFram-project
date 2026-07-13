package com.tedu.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import com.tedu.controller.GameThread;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.show.GameMainJPanel;

/** 项目层中文界面，不修改课程框架的原始显示面板。 */
public class ChineseGamePanel extends GameMainJPanel {
    private static final long serialVersionUID = 1L;
    private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 48);
    private static final Font LARGE_FONT = new Font("Microsoft YaHei", Font.BOLD, 28);
    private static final Font NORMAL_FONT = new Font("Microsoft YaHei", Font.PLAIN, 18);

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawChineseHud(g);

        int state = GameThread.gameState;
        if (state == GameThread.STATE_MENU) {
            drawMenu(g);
        } else if (state == GameThread.STATE_GAMEOVER) {
            drawStateScreen(g, "任务失败", Color.RED, "按回车键返回主菜单");
        } else if (state == GameThread.STATE_WIN) {
            drawStateScreen(g, "任务完成", Color.YELLOW, "恭喜通关！按回车键重新开始");
        } else if (state == GameThread.STATE_LEVEL_CLEAR) {
            drawStateScreen(g, "关卡完成", new Color(80, 255, 100), "正在进入下一关……");
        }
    }

    private void drawChineseHud(Graphics g) {
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, 800, 42);
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        g.setColor(Color.WHITE);

        List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
        if (players.isEmpty()) {
            g.drawString("生命：0   分数：0   武器：普通枪", 16, 27);
            return;
        }

        ElementObj player = players.get(0);
        g.drawString("血量", 10, 27);
        g.setColor(new Color(120, 25, 25));
        g.fillRect(52, 12, 100, 18);
        g.setColor(new Color(60, 220, 80));
        int hpWidth = Math.max(0, player.getHp() * 100 / Math.max(1, player.getMaxHp()));
        g.fillRect(52, 12, hpWidth, 18);
        g.setColor(Color.WHITE);
        g.drawRect(52, 12, 100, 18);
        g.drawString("分数：" + player.getScore(), 175, 27);
        g.drawString("武器：" + weaponName(player.getWeaponName()), 340, 27);
        g.drawString("生命：" + player.getLives(), 515, 27);
        g.drawString("第 " + GameThread.getCurrentLevel() + " 关", 675, 27);
    }

    private void drawMenu(Graphics g) {
        g.setColor(new Color(8, 18, 30));
        g.fillRect(0, 0, 800, 600);

        g.setFont(TITLE_FONT);
        g.setColor(new Color(255, 210, 50));
        drawCentered(g, "合金弹头", 175);
        g.setFont(LARGE_FONT);
        g.setColor(Color.WHITE);
        drawCentered(g, "Java 实训版", 225);

        g.setFont(NORMAL_FONT);
        g.setColor(new Color(100, 230, 255));
        drawCentered(g, "按回车键开始游戏", 305);
        g.setColor(Color.WHITE);
        drawCentered(g, "A / D 或方向键：移动    W / ↑：跳跃", 355);
        drawCentered(g, "S / ↓：蹲下闪避    J / 空格：射击", 390);
        drawCentered(g, "消灭敌人、收集武器，第三关击败最终首领！", 445);
    }

    private void drawStateScreen(Graphics g, String title, Color color, String tip) {
        g.setColor(new Color(0, 0, 0, 225));
        g.fillRect(0, 0, 800, 600);
        g.setFont(TITLE_FONT);
        g.setColor(color);
        drawCentered(g, title, 255);
        g.setFont(NORMAL_FONT);
        g.setColor(Color.WHITE);
        drawCentered(g, tip, 335);
    }

    private String weaponName(String weapon) {
        if ("HEAVY".equals(weapon)) return "重机枪";
        if ("SPREAD".equals(weapon)) return "散弹枪";
        if ("FLAME".equals(weapon)) return "火焰枪";
        return "普通枪";
    }

    private void drawCentered(Graphics g, String text, int y) {
        int width = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (800 - width) / 2, y);
    }
}
