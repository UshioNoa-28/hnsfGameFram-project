package com.tedu.element;

import java.awt.Graphics;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.GameLoad;

/**
 * 掉落道具 — 使用合金弹头gift道具精灵
 */
public class ItemDrop extends ElementObj {

	private String itemType = "WEAPON_H";
	private int lifetime = 900, age, baseY, flashTimer;

	@Override
	public void showElement(Graphics g) {
		List<ImageIcon> frames = GameLoad.getSprites("bullet", "gift");
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("bullet");

		ImageIcon frame = null;
		if (frames != null && !frames.isEmpty()) {
			frame = frames.get((flashTimer / 10) % frames.size());
		}

		if (frame != null && frame.getImage() != null) {
			int iw = frame.getIconWidth(), ih = frame.getIconHeight();
			g.drawImage(frame.getImage(), getX() - iw / 2 + getW() / 2, getY() - ih / 2 + getH() / 2, iw, ih, null);
		} else {
			String label = "H";
			java.awt.Color c = new java.awt.Color(230, 190, 54);
			switch (itemType) {
				case "WEAPON_S": c = new java.awt.Color(240, 126, 42); label = "S"; break;
				case "WEAPON_F": c = new java.awt.Color(235, 62, 35); label = "F"; break;
				case "HEALTH": c = new java.awt.Color(80, 220, 95); label = "+"; break;
				case "LIFE": c = new java.awt.Color(250, 230, 80); label = "1"; break;
			}
			g.setColor(c); g.fillRect(getX(), getY(), getW(), getH());
			g.setColor(java.awt.Color.BLACK);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
			g.drawString(label, getX() + 6, getY() + 16);
		}
		flashTimer++;
	}

	@Override
	public String getItemType() { return itemType; }

	@Override
	protected void add(long gameTime) {
		age++; setY(baseY + (int)(Math.sin(age / 10.0) * 3));
		if (age >= lifetime) setLive(false);
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		int offset = split[0].equalsIgnoreCase("ITEM") ? 1 : 0;
		setX(Integer.parseInt(split[offset])); setY(Integer.parseInt(split[offset + 1]));
		baseY = getY(); itemType = split[offset + 2];
		setW(24); setH(24); return this;
	}
}
