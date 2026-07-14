package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;

/**
 * @说明 掉落道具 — 武器升级/回血/加命
 * @author renjj
 */
public class ItemDrop extends ElementObj {

	private String itemType = "WEAPON_H";
	private int lifetime = 900;
	private int age;
	private int baseY;

	@Override
	public void showElement(Graphics g) {
		int x = getX();
		int y = getY();
		int w = getW() > 0 ? getW() : 20;
		int h = getH() > 0 ? getH() : 20;

		// 道具背景框 (闪烁效果: 根据时间交替颜色)
		long time = System.currentTimeMillis() / 200;
		boolean flash = time % 2 == 0;

		switch (itemType) {
			case "WEAPON_H":
				g.setColor(flash ? new Color(255, 200, 50) : new Color(200, 150, 0));
				g.fillRect(x, y, w, h);
				g.setColor(Color.BLACK);
				g.drawString("重", x + 3, y + 16);
				break;
			case "WEAPON_S":
				g.setColor(flash ? new Color(255, 150, 50) : new Color(200, 100, 0));
				g.fillRect(x, y, w, h);
				g.setColor(Color.BLACK);
				g.drawString("散", x + 3, y + 16);
				break;
			case "WEAPON_F":
				g.setColor(flash ? new Color(255, 80, 50) : new Color(200, 40, 0));
				g.fillRect(x, y, w, h);
				g.setColor(Color.BLACK);
				g.drawString("火", x + 3, y + 16);
				break;
			case "HEALTH":
				g.setColor(flash ? new Color(100, 255, 100) : new Color(0, 200, 0));
				g.fillRect(x, y, w, h);
				// 红色十字
				g.setColor(Color.RED);
				g.fillRect(x + 8, y + 4, 4, 12);
				g.fillRect(x + 4, y + 8, 12, 4);
				break;
			case "LIFE":
				g.setColor(flash ? new Color(255, 255, 100) : new Color(200, 200, 0));
				g.fillRect(x, y, w, h);
				g.setColor(Color.RED);
				g.drawString("命", x + 3, y + 16);
				break;
		}

		// 边框
		g.setColor(Color.WHITE);
		g.drawRect(x, y, w, h);
	}

	@Override
	public String getItemType() {
		return itemType;
	}

	@Override
	protected void add(long gameTime) {
		age++;
		setY(baseY + (int) (Math.sin(age / 10.0) * 3));
		if (age >= lifetime) {
			setLive(false);
		}
	}

	@Override
	public ElementObj createElement(String str) {
		// 兼容地图加载的 "ITEM,x,y,type" 和运行时掉落的 "x,y,type"。
		String[] split = str.split(",");
		int offset = split[0].equalsIgnoreCase("ITEM") ? 1 : 0;
		if (split.length < offset + 3) {
			throw new IllegalArgumentException("Invalid item definition: " + str);
		}
		setX(Integer.parseInt(split[offset]));
		setY(Integer.parseInt(split[offset + 1]));
		baseY = getY();
		itemType = split[offset + 2];
		setW(20);
		setH(20);
		return this;
	}
}
