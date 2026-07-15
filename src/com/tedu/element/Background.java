package com.tedu.element;

import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import com.tedu.manager.GameLoad;

/**
 * 背景 — 使用合金弹头背景图做视差滚动
 */
public class Background extends ElementObj {

	@Override
	public void showElement(Graphics g) {
		// 由GameMainJPanel统一调用drawParallaxBackground
	}

	/**
	 * 静态绘制方法 — 在GameMainJPanel中调用
	 */
	public static void drawParallaxBackground(Graphics g, int cameraX, int cameraY, int worldWidth) {
		List<Image> bgs = GameLoad.bgImages;
		int screenW = 800, screenH = 600;

		// 使用第二张背景图 (backimage.jpg)
		Image bg = (bgs.size() >= 2) ? bgs.get(1) : (bgs.isEmpty() ? null : bgs.get(0));

		if (bg == null) {
			g.setColor(new java.awt.Color(45, 80, 160));
			g.fillRect(0, 0, screenW, screenH);
			return;
		}

		int bw = bg.getWidth(null), bh = bg.getHeight(null);
		if (bw <= 0 || bh <= 0) {
			g.setColor(new java.awt.Color(45, 80, 160));
			g.fillRect(0, 0, screenW, screenH);
			return;
		}

		// 背景缓慢视差滚动 + 平铺
		float parallax = 0.15f;
		int offset = (int)(cameraX * parallax) % bw;
		int y = screenH - bh;
		for (int x = -offset; x < screenW; x += bw) {
			g.drawImage(bg, x, y, bw, bh, null);
		}

		// 顶部填充色
		if (y > 0) {
			g.setColor(new java.awt.Color(45, 80, 160));
			g.fillRect(0, 0, screenW, y);
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		setX(Integer.parseInt(split[1]));
		setY(Integer.parseInt(split[2]));
		return this;
	}
}
