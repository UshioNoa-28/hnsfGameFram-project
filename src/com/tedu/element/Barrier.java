package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * 可破坏障碍物 — Metal Slug风格木箱/油桶
 */
public class Barrier extends ElementObj {

	private String barrierType = "CRATE";
	private static Random rand = new Random();

	public Barrier() {
		setW(40); setH(40);
		setHp(2); setMaxHp(2);
	}

	@Override
	public void showElement(Graphics g) {
		int x = getX(), y = getY(), w = getW(), h = getH();

		if ("BARREL".equals(barrierType)) {
			// 金属油桶
			g.setColor(new Color(52, 54, 58));
			g.fillRoundRect(x + 4, y, w - 8, h, 10, 10);
			g.setColor(new Color(128, 130, 134));
			g.fillRoundRect(x + 7, y + 4, w - 14, h - 8, 8, 8);
			g.setColor(new Color(72, 74, 78));
			g.fillRect(x + 4, y + 10, w - 8, 5);
			g.fillRect(x + 4, y + h - 15, w - 8, 5);
			// 红色警告标志
			g.setColor(new Color(210, 55, 35));
			g.fillOval(x + w / 2 - 8, y + h / 2 - 8, 16, 16);
			g.setColor(Color.YELLOW);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
			g.drawString("!", x + w / 2 - 2, y + h / 2 + 4);
		} else {
			// 木箱
			g.setColor(new Color(110, 72, 38));
			g.fillRect(x, y, w, h);
			g.setColor(new Color(155, 112, 54));
			g.fillRect(x + 3, y + 3, w - 6, h - 6);
			g.setColor(new Color(185, 140, 68));
			g.fillRect(x + 6, y + 6, w - 12, h - 12);
			// X纹理
			g.setColor(new Color(90, 56, 28));
			g.drawLine(x + 4, y + 4, x + w - 5, y + h - 5);
			g.drawLine(x + w - 5, y + 4, x + 4, y + h - 5);
			// 木板条纹
			g.setColor(new Color(160, 118, 72));
			g.fillRect(x + 2, y + h / 2 - 2, w - 4, 3);
			g.fillRect(x + w / 2 - 2, y + 2, 3, h - 4);
			// 铁钉
			g.setColor(new Color(50, 50, 50));
			g.fillOval(x + 5, y + 5, 3, 3);
			g.fillOval(x + w - 8, y + 5, 3, 3);
			g.fillOval(x + 5, y + h - 8, 3, 3);
			g.fillOval(x + w - 8, y + h - 8, 3, 3);
		}

		// 受伤闪烁
		if (getHp() < getMaxHp() && System.currentTimeMillis() / 150 % 2 == 0) {
			g.setColor(new Color(255, 100, 100, 100));
			g.fillRect(x, y, w, h);
		}
	}

	@Override
	public void onHit(ElementObj attacker) {
		if ("enemy".equals(attacker.getFrom())) return;
		int damage = attacker instanceof Bullet ? ((Bullet) attacker).getDamage() : 1;
		setHp(getHp() - damage);
		if (getHp() <= 0) {
			setLive(false);
			Explosion exp = new Explosion();
			exp.createElement(getX() + "," + getY() + ",28");
			ElementManager.getManager().addElement(exp, GameElement.EFFECT);
			tryDropItem();
		}
	}

	private void tryDropItem() {
		if (rand.nextInt(100) < 30) {
			String[] types = {"WEAPON_H", "WEAPON_S", "WEAPON_F", "HEALTH"};
			ItemDrop item = new ItemDrop();
			item.createElement(getX() + "," + (getY() - 10) + "," + types[rand.nextInt(types.length)]);
			ElementManager.getManager().addElement(item, GameElement.ITEM);
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		int idx = split[0].equalsIgnoreCase("BARRIER") ? 1 : 0;
		setX(Integer.parseInt(split[idx]));
		setY(Integer.parseInt(split[idx + 1]));
		barrierType = split.length > idx + 2 ? split[idx + 2] : "CRATE";

		if ("BARREL".equals(barrierType)) {
			setHp(3); setMaxHp(3);
		} else {
			setHp(2); setMaxHp(2);
		}
		return this;
	}
}
