package com.tedu.element;

import java.awt.Graphics;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.SoundManager;

/**
 * 人质 — 合金弹头人质精灵 (oder*.png)
 */
public class Hostage extends ElementObj {

	private boolean rescuedAnim = false;
	private int animFrame = 0, animTimer = 0, rescueTimer = 0;

	public Hostage() { setW(36); setH(54); setHp(1); setMaxHp(1); }

	@Override
	public void showElement(Graphics g) {
		if (rescuedAnim) {
			g.setColor(new java.awt.Color(255, 255, 200, 200));
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
			g.drawString("+200", getX() - 5, getY() - 12);
		}

		List<ImageIcon> frames = GameLoad.getSprites("hostage", "idle");
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("hostage");

		ImageIcon frame = null;
		if (frames != null && !frames.isEmpty()) frame = frames.get(animFrame % frames.size());

		if (frame != null && frame.getImage() != null) {
			int iw = frame.getIconWidth(), ih = frame.getIconHeight();
			g.drawImage(frame.getImage(), getX(), getY() + getH() - ih, iw, ih, null);
		} else {
			g.setColor(new java.awt.Color(255, 220, 140)); g.fillRect(getX(), getY(), getW(), getH());
			g.setColor(java.awt.Color.WHITE);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
			g.drawString("P", getX() + 8, getY() + 30);
		}
	}

	@Override
	protected void move() {
		if (rescuedAnim) { rescueTimer++; if (rescueTimer > 50) setLive(false); return; }
		animTimer++; if (animTimer >= 12) { animTimer = 0; animFrame++; }
	}

	@Override
	public void onPickItem(ElementObj player) {
		if (rescuedAnim) return;
		rescuedAnim = true;
		player.setScore(player.getScore() + 200);

		String[] items = {"HEALTH", "WEAPON_H", "WEAPON_S", "WEAPON_F", "LIFE"};
		String itemType = items[new java.util.Random().nextInt(items.length)];
		if (!"LIFE".equals(itemType) || new java.util.Random().nextInt(100) < 20) {
			ItemDrop drop = new ItemDrop();
			drop.createElement(getX() + "," + (getY() - 10) + "," + itemType);
			ElementManager.getManager().addElement(drop, GameElement.ITEM);
		}
		try { SoundManager.getInstance().playSFX("buzi"); } catch (Exception e) {}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		int offset = split[0].equalsIgnoreCase("HOSTAGE") ? 1 : 0;
		setX(Integer.parseInt(split[offset])); setY(Integer.parseInt(split[offset + 1]));
		return this;
	}
}
