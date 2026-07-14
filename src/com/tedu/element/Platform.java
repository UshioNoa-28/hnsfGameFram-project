package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;

/**
 * @说明 地面/平台 — 静态地形, 供玩家和敌人站立
 * @author renjj
 */
public class Platform extends ElementObj {

	private String platformType = "GROUND";
	private boolean solid = true;

	@Override
	public void showElement(Graphics g) {
		int x = getX();
		int y = getY();
		int w = getW() > 0 ? getW() : 100;
		int h = getH() > 0 ? getH() : 20;

		if ("GROUND".equals(platformType)) {
			// 地面 — 绿色草地 + 棕色泥土
			g.setColor(new Color(80, 160, 60));
			g.fillRect(x, y, w, 6);
			g.setColor(new Color(120, 80, 40));
			g.fillRect(x, y + 6, w, h - 6);
			// 草地纹理 (小草)
			g.setColor(new Color(60, 140, 40));
			for (int i = 0; i < w; i += 8) {
				g.drawLine(x + i, y + 4, x + i + 2, y - 2);
			}
		} else if ("PLATFORM".equals(platformType)) {
			// 高台 — 灰色石质
			g.setColor(new Color(140, 130, 120));
			g.fillRect(x, y, w, h);
			g.setColor(new Color(100, 90, 80));
			g.drawRect(x, y, w, h);
			// 石纹
			g.setColor(new Color(160, 150, 140));
			g.fillRect(x + 4, y + 4, w - 8, 4);
			// 边缘高光
			g.setColor(new Color(200, 190, 180));
			g.drawLine(x, y, x + w, y);
		}
	}

	@Override
	public ElementObj createElement(String str) {
		// 格式: GROUND/PLATFORM,x,y,w,h
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
		// Static terrain is not destructible.
	}
}
