package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * @说明 可破坏障碍物 (木箱/油桶)
 * @author renjj
 */
public class Barrier extends ElementObj {

	private String barrierType = "CRATE";
	private static Random rand = new Random();

	public Barrier() {
		setW(36);
		setH(36);
		setHp(2);
		setMaxHp(2);
	}

	@Override
	public void showElement(Graphics g) {
		int x = getX();
		int y = getY();
		int w = getW() > 0 ? getW() : 36;
		int h = getH() > 0 ? getH() : 36;

		if ("BARREL".equals(barrierType)) {
			g.setColor(new Color(120, 120, 120));
			g.fillRoundRect(x, y, w, h, 8, 8);
			g.setColor(new Color(80, 80, 80));
			g.fillRect(x, y + h / 3, w, 3);
			g.fillRect(x, y + h * 2 / 3, w, 3);
			g.setColor(new Color(255, 50, 0));
			g.fillOval(x + w / 4, y + h / 3, w / 2, h / 3);
			g.setColor(new Color(255, 200, 0));
			g.drawString("!", x + w / 2 - 3, y + h / 2 + 4);
		} else {
			g.setColor(new Color(160, 120, 60));
			g.fillRect(x, y, w, h);
			g.setColor(new Color(120, 80, 40));
			g.drawRect(x, y, w, h);
			g.drawLine(x, y + h / 3, x + w, y + h / 3);
			g.drawLine(x, y + h * 2 / 3, x + w, y + h * 2 / 3);
			g.setColor(new Color(140, 100, 50));
			g.fillRect(x + w / 2 - 2, y, 4, h);
			g.fillRect(x, y + h / 2 - 2, w, 4);
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
			exp.createElement(getX() + "," + getY() + ",25");
			ElementManager.getManager().addElement(exp, GameElement.EFFECT);
			tryDropItem();
		}
	}

	private void tryDropItem() {
		if (rand.nextInt(100) < 25) {
			String[] types = {"WEAPON_H", "WEAPON_S", "HEALTH"};
			String type = types[rand.nextInt(types.length)];
			ItemDrop item = new ItemDrop();
			item.createElement(getX() + "," + (getY() - 10) + "," + type);
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

		setW(36);
		setH(36);

		if ("BARREL".equals(barrierType)) {
			setHp(3);
			setMaxHp(3);
		} else {
			setHp(2);
			setMaxHp(2);
		}
		return this;
	}
}
