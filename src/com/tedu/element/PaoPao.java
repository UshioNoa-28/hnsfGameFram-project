package com.tedu.element;

import java.awt.Graphics;

import javax.swing.ImageIcon;

import com.tedu.manager.GameLoad;

/**
 * @author renjj
 * 1.继承父类
 * 2.重写各种方法，实现业务逻辑
 * 3.编写主线程的碰撞
 * 4.如果配置文件格式有改变，请重写 GameLoad里面的 加载方法
 */
public class PaoPao extends ElementObj{
	private int imgx=0;
	private int imgy=0;//由方向来控制
	private long imgtime=0;//用于控制图片变化速度
	@Override
	public void showElement(Graphics g) {
//		g.drawImage(this.getIcon().getImage(), 
//				this.getX(), this.getY(), 
//				this.getW(), this.getH(), null);
//		做图片的分割
		g.drawImage(this.getIcon().getImage(), 
				this.getX(), this.getY(), 
				this.getX()+this.getW()/4, this.getY()+this.getH()/4, 
				26+(imgx*100), 42, 
				72+(imgx*100), 99, 
				null);		
	}
	
	@Override
	protected void updateImage(long time) {
		if(time -imgtime>10) {
			imgtime=time;
			imgx++;
			if(imgx>3) {
				imgx=0;
			}
		}
	}
	@Override
	protected void move() {
		
		
	}
	public void keyClick(boolean bl,int key) {
		
	}
	
	@Override
	protected void add(long gameTime) {
		// TODO Auto-generated method stub
		super.add(gameTime);
	}
	
	
	@Override  //"500,500,paopao"
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		this.setX(Integer.parseInt(split[0]));
		this.setY(Integer.parseInt(split[1]));
		ImageIcon icon=GameLoad.imgMap.get(split[2]);
		this.setIcon(icon);
		this.setW(icon.getIconWidth());
		this.setH(icon.getIconHeight());
		return this; //注意别忘啦 刚刚返回的是父类
	}
	

}





