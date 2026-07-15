package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.Random;

/** Lightweight muzzle flash, impact spark, smoke and floating-text effect. */
public class CombatEffect extends ElementObj {
	private static final Random RNG = new Random();
	private String type = "impact";
	private int age;
	private int life = 14;
	private int direction = 1;
	private String text = "";

	@Override public void showElement(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		float fade = Math.max(0f, 1f - age / (float) life);
		if ("muzzle".equals(type)) {
			int r = 12 + age * 3;
			g2.setColor(new Color(255, 245, 170, (int)(230 * fade)));
			Polygon p = new Polygon();
			p.addPoint(getX(), getY()); p.addPoint(getX() + direction * r, getY() - 7);
			p.addPoint(getX() + direction * (r + 12), getY()); p.addPoint(getX() + direction * r, getY() + 7);
			g2.fillPolygon(p);
			g2.setColor(new Color(255, 106, 24, (int)(210 * fade)));
			g2.fillOval(getX() - 5, getY() - 5, 10, 10);
		} else if ("smoke".equals(type)) {
			int r = 8 + age * 2;
			g2.setColor(new Color(48, 48, 45, (int)(120 * fade)));
			g2.fillOval(getX() - r / 2, getY() - age - r / 2, r, r);
		} else if ("text".equals(type)) {
			g2.setColor(new Color(255, 224, 92, (int)(255 * fade)));
			g2.setFont(new java.awt.Font("Microsoft YaHei", java.awt.Font.BOLD, 14));
			g2.drawString(text, getX(), getY() - age);
		} else {
			g2.setColor(new Color(255, 205, 60, (int)(240 * fade)));
			for (int i = 0; i < 6; i++) {
				double a = i * Math.PI / 3 + getX() * .07;
				int len = 5 + age * 2 + (i & 1) * 5;
				g2.drawLine(getX(), getY(), getX() + (int)(Math.cos(a) * len), getY() + (int)(Math.sin(a) * len));
			}
		}
		g2.dispose();
	}

	@Override protected void move() { if (++age >= life) setLive(false); }

	@Override public ElementObj createElement(String spec) {
		String[] s = spec.split(",", 5);
		setX(Integer.parseInt(s[0])); setY(Integer.parseInt(s[1]));
		type = s.length > 2 ? s[2] : "impact";
		direction = s.length > 3 ? Integer.parseInt(s[3]) : (RNG.nextBoolean() ? 1 : -1);
		if (s.length > 4) text = s[4];
		life = "smoke".equals(type) ? 26 : "text".equals(type) ? 42 : "muzzle".equals(type) ? 5 : 12;
		setW(1); setH(1); return this;
	}
}
