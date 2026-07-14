package com.tedu.element;

import java.awt.Graphics;

/**
 * @说明 背景装饰元素 (远景建筑/树木) — 标记位置, 绘制由面板视差系统负责
 * @author renjj
 */
public class Background extends ElementObj {

	@Override
	public void showElement(Graphics g) {
		// 由 GameMainJPanel 视差系统统一绘制
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		setX(Integer.parseInt(split[1]));
		setY(Integer.parseInt(split[2]));
		return this;
	}
}
