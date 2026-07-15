package com.tedu.element;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;

import com.tedu.controller.GameThread;
import javax.imageio.ImageIO;
import java.io.File;

/** Procedural multi-layer battlefield with camera parallax. */
public class Background extends ElementObj {
	private static Image battlefield;
	static {
		try { battlefield = ImageIO.read(new File("image/stage1_battlefield_v2.png")); }
		catch (Exception ignored) { battlefield = null; }
	}
	@Override public void showElement(Graphics g) { }

	public static void drawParallaxBackground(Graphics g, int cameraX, int cameraY, int worldWidth) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int level = GameThread.getCurrentLevel();
		if (battlefield != null) {
			int iw = battlefield.getWidth(null), ih = battlefield.getHeight(null);
			int sourceW = Math.min(iw, Math.max(1, (int)(ih * (800.0 / 540.0))));
			int maxSourceX = Math.max(0, iw - sourceW);
			int sourceX = (int)(maxSourceX * Math.min(1.0, cameraX / 1600.0));
			g2.drawImage(battlefield, 0, 0, 800, 540, sourceX, 0, sourceX + sourceW, ih, null);
			if (level == 2) { g2.setColor(new Color(86, 35, 55, 48)); g2.fillRect(0, 0, 800, 540); }
			if (level == 3) { g2.setColor(new Color(15, 24, 45, 100)); g2.fillRect(0, 0, 800, 540); }
			g2.setColor(new Color(20, 18, 15)); g2.fillRect(0, 540, 800, 60);
			g2.dispose(); return;
		}
		Color top = level == 1 ? new Color(34, 88, 132) : level == 2 ? new Color(84, 62, 74) : new Color(26, 33, 48);
		Color horizon = level == 1 ? new Color(239, 188, 111) : level == 2 ? new Color(211, 124, 82) : new Color(138, 80, 69);
		g2.setPaint(new GradientPaint(0, 0, top, 0, 455, horizon));
		g2.fillRect(0, 0, 800, 600);

		// Sun/moon and drifting cloud bands.
		g2.setColor(level == 3 ? new Color(238, 225, 190, 170) : new Color(255, 226, 140, 190));
		g2.fillOval(625 - (cameraX / 35), 70, level == 3 ? 58 : 82, level == 3 ? 58 : 82);
		for (int i = -1; i < 5; i++) {
			int cx = i * 230 - (cameraX / 10) % 230;
			int cy = 85 + (i & 1) * 45;
			g2.setColor(new Color(235, 224, 199, 55));
			g2.fillOval(cx, cy, 160, 28); g2.fillOval(cx + 42, cy - 15, 105, 35);
		}

		drawMountains(g2, cameraX / 6, 295, new Color(45, 62, 76, 210), 150, 260);
		drawMountains(g2, cameraX / 3, 355, new Color(58, 64, 61, 235), 105, 180);

		// Ruined industrial skyline in the near parallax layer.
		g2.setColor(level == 1 ? new Color(53, 62, 57) : new Color(52, 45, 47));
		int off = (cameraX / 2) % 260;
		for (int i = -1; i < 5; i++) {
			int x = i * 260 - off;
			g2.fillRect(x + 15, 325, 82, 115);
			g2.fillRect(x + 112, 360, 125, 80);
			g2.fillRect(x + 44, 285, 12, 42);
			g2.setColor(new Color(245, 173, 70, 100));
			for (int wx = x + 27; wx < x + 88; wx += 24) for (int wy = 342; wy < 420; wy += 25) g2.fillRect(wx, wy, 8, 10);
			g2.setColor(level == 1 ? new Color(53, 62, 57) : new Color(52, 45, 47));
		}

		g2.setColor(new Color(18, 24, 25, 105));
		g2.fillRect(0, 426, 800, 174);
		g2.dispose();
	}

	private static void drawMountains(Graphics2D g, int offset, int baseY, Color color, int peak, int spacing) {
		g.setColor(color);
		for (int x = -spacing - offset % spacing; x < 900; x += spacing) {
			Polygon p = new Polygon();
			p.addPoint(x, baseY); p.addPoint(x + spacing / 2, baseY - peak); p.addPoint(x + spacing, baseY); p.addPoint(x + spacing, 460); p.addPoint(x, 460);
			g.fillPolygon(p);
		}
	}

	@Override public ElementObj createElement(String str) { return this; }
}
