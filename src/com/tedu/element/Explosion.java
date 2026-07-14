package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;

/**
 * @说明 爆炸特效 — 定时消失的粒子动画
 * @author renjj
 */
public class Explosion extends ElementObj {

	private int maxRadius = 25;
	private int currentRadius = 0;
	private int frameCount = 0;
	private int totalFrames = 20;

	@Override
	public void showElement(Graphics g) {
		int cx = getX();
		int cy = getY();

		int alpha = Math.max(0, 255 - frameCount * 12);

		// 外圈
		g.setColor(new Color(255, 150, 50, alpha));
		g.fillOval(cx - currentRadius, cy - currentRadius,
				   currentRadius * 2, currentRadius * 2);

		// 中圈
		int midR = currentRadius * 2 / 3;
		g.setColor(new Color(255, 220, 80, alpha));
		g.fillOval(cx - midR, cy - midR, midR * 2, midR * 2);

		// 内圈
		int innerR = currentRadius / 3;
		g.setColor(new Color(255, 255, 200, alpha));
		g.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);

		// 碎片粒子
		if (frameCount < 10) {
			g.setColor(new Color(255, 100, 0, alpha));
			for (int i = 0; i < 6; i++) {
				double angle = i * 60 + frameCount * 10;
				double dist = currentRadius * 0.8 * (frameCount / 10.0);
				int px = cx + (int)(Math.cos(Math.toRadians(angle)) * dist);
				int py = cy + (int)(Math.sin(Math.toRadians(angle)) * dist);
				g.fillRect(px - 2, py - 2, 4, 4);
			}
		}
	}

	@Override
	protected void move() {
		frameCount++;
		currentRadius = maxRadius * frameCount / totalFrames;

		if (frameCount >= totalFrames) {
			setLive(false);
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		setX(Integer.parseInt(split[0]));
		setY(Integer.parseInt(split[1]));
		if (split.length > 2) {
			maxRadius = Integer.parseInt(split[2]);
		}
		setW(maxRadius * 2);
		setH(maxRadius * 2);
		return this;
	}
}
