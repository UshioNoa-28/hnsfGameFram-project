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
			// 草地 + 泥土
			g.setColor(new Color(34, 102, 34));
			g.fillRect(x, y - 6, w, 10);
			// 草丛纹理
			g.setColor(new Color(22, 68, 22));
			for (int i = 0; i < w; i += 16) {
				g.fillRect(x + i + 3, y - 8, 3, 5);
				g.fillRect(x + i + 9, y - 6, 2, 4);
			}
			// 泥土层
			g.setColor(new Color(120, 82, 42));
			g.fillRect(x, y, w, 18);
			g.setColor(new Color(92, 60, 28));
			g.fillRect(x, y, w, 2);
			// 深层
			g.setColor(new Color(64, 46, 32));
			g.fillRect(x, y + 18, w, h - 18);
			// 纹理线
			g.setColor(new Color(78, 52, 36));
			for (int i = 20; i < w; i += 40) {
				g.drawLine(x + i, y + 20, x + i - 6, y + h - 4);
			}
		} else {
			// 灰色平台
			g.setColor(new Color(96, 100, 106));
			g.fillRect(x, y, w, h);
			g.setColor(new Color(168, 174, 178));
			g.fillRect(x, y, w, 3);
			g.fillRect(x, y + 3, 3, h - 3);
			g.setColor(new Color(58, 62, 68));
			g.fillRect(x, y + h - 3, w, 3);
			g.fillRect(x + w - 3, y, 3, h);
			// 铆钉装饰
			g.setColor(new Color(200, 200, 200));
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
