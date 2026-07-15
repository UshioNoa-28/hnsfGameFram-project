package com.tedu.element;

import java.awt.Graphics;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.SoundManager;

/**
 * Boss — 合金弹头Boss精灵动画 (16帧循环)
 */
public class Boss extends ElementObj {
	private int direction = -1;
	private int shootTimer = 30, attackIndex = 0;
	private int animFrame = 0, animTimer = 0;

	public Boss() { setW(88); setH(100); setHp(24); setMaxHp(24); setFrom("enemy"); }

	@Override
	public void showElement(Graphics g) {
		List<ImageIcon> frames = GameLoad.getSprites("boss", "idle");
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("boss");

		ImageIcon frame = null;
		if (frames != null && !frames.isEmpty()) frame = frames.get(animFrame % frames.size());

		if (frame != null && frame.getImage() != null) {
			int iw = frame.getIconWidth(), ih = frame.getIconHeight();
			int dx = getX(), dy = getY() + getH() - ih;
			if (direction >= 0) g.drawImage(frame.getImage(), dx, dy, iw, ih, null);
			else g.drawImage(frame.getImage(), dx + iw, dy, -iw, ih, null);
		} else {
			g.setColor(new java.awt.Color(80, 80, 80)); g.fillRect(getX(), getY(), getW(), getH());
		}

		// 血条
		g.setColor(java.awt.Color.RED); g.fillRect(getX(), getY() - 16, getW(), 8);
		g.setColor(java.awt.Color.GREEN); g.fillRect(getX(), getY() - 16, Math.max(1, getW() * getHp() / getMaxHp()), 8);
		g.setColor(java.awt.Color.WHITE); g.drawRect(getX(), getY() - 16, getW(), 8);
		g.setColor(java.awt.Color.YELLOW);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
		g.drawString("BOSS", getX() + getW() / 2 - 18, getY() - 20);
	}

	@Override
	protected void move() {
		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (players.isEmpty()) return;
		ElementObj player = players.get(0);
		int dx = player.getX() - getX();
		direction = dx >= 0 ? 1 : -1;

		setVx(Math.abs(dx) > 320 ? direction * (getHp() <= getMaxHp() / 2 ? 2 : 1) : 0);
		setX(Math.max(1850, Math.min(2180, getX() + getVx())));
		setY(430 - getH());

		animTimer++; if (animTimer >= 8) { animTimer = 0; animFrame++; }
	}

	@Override
	protected void add(long gameTime) {
		if (--shootTimer > 0) return;
		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (players.isEmpty()) return;

		ElementObj player = players.get(0);
		int dir = player.getX() >= getX() ? 1 : -1;
		int bx = dir > 0 ? getX() + getW() : getX() - 18;
		int by = attackIndex++ % 2 == 0 ? 390 : 420;

		Bullet bullet = new Bullet();
		bullet.createElement(bx + "," + by + ",16,8," + (dir * 8) + ",0,heavy,2,enemy");
		ElementManager.getManager().addElement(bullet, GameElement.PLAYFILE);

		shootTimer = getHp() <= getMaxHp() / 2 ? 32 : 52;
		try { SoundManager.getInstance().playSFX("boss"); } catch (Exception e) {}
	}

	@Override
	public void onHit(ElementObj attacker) {
		if ("enemy".equals(attacker.getFrom())) return;
		setHp(getHp() - (attacker instanceof Bullet ? ((Bullet) attacker).getDamage() : 1));
		if (getHp() > 0) return;
		setLive(false);

		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (!players.isEmpty()) players.get(0).setScore(players.get(0).getScore() + 1000);

		for (int i = 0; i < 3; i++) {
			Explosion exp = new Explosion();
			exp.createElement((getX() + 20 + i * 25) + "," + (getY() + 20 + i * 15) + ",50");
			ElementManager.getManager().addElement(exp, GameElement.EFFECT);
		}

		ItemDrop reward = new ItemDrop();
		reward.createElement(getX() + "," + (getY() + 30) + ",LIFE");
		ElementManager.getManager().addElement(reward, GameElement.ITEM);

		try { SoundManager.getInstance().playSFX("die"); } catch (Exception e) {}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		int offset = split[0].equalsIgnoreCase("BOSS") ? 1 : 0;
		setX(Integer.parseInt(split[offset])); setY(Integer.parseInt(split[offset + 1]));
		return this;
	}
}
