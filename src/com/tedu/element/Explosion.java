package com.tedu.element;

import java.awt.Graphics;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.GameLoad;

/**
 * 爆炸特效 — 合金弹头爆炸精灵序列帧 (bomb_bang / fire)
 */
public class Explosion extends ElementObj {

	private int maxRadius = 40;
	private int frameCount = 0;
	private int totalFrames = 24;

	@Override
	public void showElement(Graphics g) {
		// 大爆炸用bomb_bang, 小爆炸用fire
		String anim = maxRadius >= 40 ? "bomb_bang" : "fire";
		List<ImageIcon> frames = GameLoad.getSprites("explosion", anim);
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("explosion");

		ImageIcon frame = null;
		if (frames != null && !frames.isEmpty()) {
			int idx = frameCount * frames.size() / totalFrames;
			frame = frames.get(Math.min(idx, frames.size() - 1));
		}

		if (frame != null && frame.getImage() != null) {
			int iw = frame.getIconWidth(), ih = frame.getIconHeight();
			g.drawImage(frame.getImage(), getX() - iw / 2, getY() - ih / 2, iw, ih, null);
		} else {
			int r = maxRadius * frameCount / totalFrames;
			g.setColor(new java.awt.Color(255, 160, 30, 200 - frameCount * 8));
			g.fillOval(getX() - r, getY() - r, r * 2, r * 2);
		}
	}

	@Override
	protected void move() {
		frameCount++;
		if (frameCount >= totalFrames) setLive(false);
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		setX(Integer.parseInt(split[0])); setY(Integer.parseInt(split[1]));
		if (split.length > 2) maxRadius = Integer.parseInt(split[2]);
		setW(maxRadius * 2); setH(maxRadius * 2);
		return this;
	}
}
