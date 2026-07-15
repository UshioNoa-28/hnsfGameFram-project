package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;

/**
 * 地面/平台 — Metal Slug风格的地形渲染
 */
public class Platform extends ElementObj {

	private String platformType = "GROUND";

	@Override
	public void showElement(Graphics g) {
		int x = getX(), y = getY(), w = getW(), h = getH();

		if ("GROUND".equals(platformType)) {
			// Thin sandy collision lip; the painted battlefield remains visible below it.
			g.setColor(new Color(224, 174, 92)); g.fillRect(x, y - 3, w, 4);
			g.setColor(new Color(105, 69, 38, 135)); g.fillRect(x, y + 1, w, Math.max(0, h - 1));
			g.setColor(new Color(54, 39, 28, 90)); g.fillRect(x, y + 46, w, Math.max(0, h - 46));
			for (int i = 9; i < w; i += 31) {
				g.setColor((i & 1) == 0 ? new Color(62, 47, 35) : new Color(156, 111, 62));
				g.fillRect(x + i, y + 8 + (i % 17), 5 + i % 8, 3);
			}
		} else {
			// Battered military scaffold with chipped highlights.
			g.setColor(new Color(66, 62, 54));
			g.fillRect(x, y, w, h);
			g.setColor(new Color(185, 166, 124));
			g.fillRect(x, y, w, 3);
			g.fillRect(x, y + 3, 3, h - 3);
			g.setColor(new Color(34, 32, 29));
			g.fillRect(x, y + h - 3, w, 3);
			g.fillRect(x + w - 3, y, 3, h);
			// 铆钉装饰
			g.setColor(new Color(215, 188, 122));
			for (int i = 10; i < w; i += 24) {
				g.fillOval(x + i, y + 4, 4, 4);
			}
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		platformType = split[0];
		setX(Integer.parseInt(split[1]));
		setY(Integer.parseInt(split[2]));
		setW(Integer.parseInt(split[3]));
		setH(Integer.parseInt(split[4]));
		return this;
	}

	@Override
	public void onHit(ElementObj attacker) {
		// 地形不可破坏
	}
}
