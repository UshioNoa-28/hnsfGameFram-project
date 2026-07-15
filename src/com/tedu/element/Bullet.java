package com.tedu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.GameLoad;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.show.GameMainJPanel;

/**
 * 子弹 — 合金弹头子弹精灵
 */
public class Bullet extends ElementObj {

	private String bulletType = "normal";
	private int damage = 1;
	private int lifeFrames = 120;
	private int frameCount = 0;

	public Bullet() { setW(8); setH(4); }

	@Override
	public void showElement(Graphics g) {
		String animName;
		switch (bulletType) {
			case "heavy": case "boss": case "grenade": animName = "bomb"; break;
			case "flame": animName = "boss_bomb"; break;
			case "spread": animName = "bullet"; break;
			default: animName = "bullet"; break;
		}
		List<ImageIcon> frames = GameLoad.getSprites("bullet", animName);
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("bullet");

		// 不同枪种不同子弹图片，不切换动画帧，取第一帧
		ImageIcon frame = null;
		if (frames != null && !frames.isEmpty()) frame = frames.get(0);

		if ("grenade".equals(bulletType)) {
			g.setColor(new java.awt.Color(45, 52, 42)); g.fillOval(getX(), getY(), getW(), getH());
			g.setColor(java.awt.Color.YELLOW); g.drawArc(getX() + 2, getY() - 4, 10, 10, 30, 80);
		} else if (frame != null && frame.getImage() != null) {
			int iw = Math.max(frame.getIconWidth(), getW()), ih = Math.max(frame.getIconHeight(), getH());
			int dir = getVx() >= 0 ? 1 : -1;
			if (dir >= 0) g.drawImage(frame.getImage(), getX(), getY(), iw, ih, null);
			else g.drawImage(frame.getImage(), getX() + iw, getY(), -iw, ih, null);
		} else {
			g.setColor("flame".equals(bulletType) ? java.awt.Color.ORANGE : "heavy".equals(bulletType) ? java.awt.Color.GRAY : java.awt.Color.YELLOW);
			g.fillOval(getX(), getY(), getW(), getH());
		}
	}

	@Override
	protected void move() {
		if ("grenade".equals(bulletType)) setVy(getVy() + 1);
		setX(getX() + getVx()); setY(getY() + getVy()); frameCount++;
		if (getX() < -100 || getX() > 2600 || getY() < -100 || getY() > 700) setLive(false);
		if (frameCount > lifeFrames) setLive(false);
	}

	@Override
	public void onHit(ElementObj attacker) {
		if (!isLive()) return;
		setLive(false);
		if ("grenade".equals(bulletType)) {
			Explosion exp = new Explosion(); exp.createElement(getX() + "," + getY() + ",55");
			ElementManager.getManager().addElement(exp, GameElement.EFFECT);
			GameElement[] groups = "enemy".equals(getFrom())
					? new GameElement[] { GameElement.PLAY }
					: new GameElement[] { GameElement.ENEMY, GameElement.BOSS };
			for (GameElement group : groups) {
				for (ElementObj target : ElementManager.getManager().getElementsByKey(group)) {
					if (target.isLive() && Math.abs(target.getX() - getX()) < 85 && Math.abs(target.getY() - getY()) < 75) target.onHit(this);
				}
			}
			GameMainJPanel.shake(7);
		} else {
			CombatEffect hit = new CombatEffect(); hit.createElement(getX() + "," + getY() + ",impact,1,");
			ElementManager.getManager().addElement(hit, GameElement.EFFECT);
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		setX(Integer.parseInt(split[0])); setY(Integer.parseInt(split[1]));
		setW(Integer.parseInt(split[2])); setH(Integer.parseInt(split[3]));
		setVx((int) Double.parseDouble(split[4])); setVy((int) Double.parseDouble(split[5]));
		bulletType = split[6]; damage = Integer.parseInt(split[7]); setFrom(split[8]);

			switch (bulletType) {
			case "grenade": lifeFrames = 100; break;
			case "flame": lifeFrames = 30; break;
			case "spread": lifeFrames = 60; break;
			case "heavy": lifeFrames = 90; break;
			default: lifeFrames = 80; break;
		}
		return this;
	}

	@Override
	public Rectangle getRectangle() {
		return new Rectangle(getX() + 1, getY() + 1, Math.max(getW() - 2, 2), Math.max(getH() - 2, 2));
	}

	public int getDamage() { return damage; }
}
