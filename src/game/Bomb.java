package game;

import javax.swing.ImageIcon;

import game.CrazyArcadeClientView.GamePanel;

public class Bomb extends MapObject {
	public final int BOMB_DELAY = 60;
	public int bombImgIdx = 0;
	public ImageIcon [] bombImage = new ImageIcon[4];
	public ImageIcon [] up1Image = new ImageIcon[11];
	public ImageIcon [] up2Image = new ImageIcon[11];
	public ImageIcon [] down1Image = new ImageIcon[11];
	public ImageIcon [] down2Image = new ImageIcon[11];
	public ImageIcon [] left1Image = new ImageIcon[11];
	public ImageIcon [] left2Image = new ImageIcon[11];
	public ImageIcon [] right1Image = new ImageIcon[11];
	public ImageIcon [] right2Image = new ImageIcon[11];
	public Bomb(int xPos, int yPos, int code, String name, GamePanel gamePanel) {
		super(xPos, yPos, code, name, gamePanel);
		
		for(int i=0; i<bombImage.length; i++) 
			this.bombImage[i] = new ImageIcon(String.format("bomb/bomb%d.png", i));
		for(int i=0; i<up1Image.length; i++) 
			this.up1Image[i] = new ImageIcon(String.format("bomb/up1_%d.png", i));
		for(int i=0; i<up2Image.length; i++) 
			this.up2Image[i] = new ImageIcon(String.format("bomb/up2_%d.png", i));
		for(int i=0; i<down1Image.length; i++) 
			this.down1Image[i] = new ImageIcon(String.format("bomb/down1_%d.png", i));
		for(int i=0; i<down2Image.length; i++) 
			this.down2Image[i] = new ImageIcon(String.format("bomb/down2_%d.png", i));
		for(int i=0; i<left1Image.length; i++) 
			this.left1Image[i] = new ImageIcon(String.format("bomb/left1_%d.png", i));
		for(int i=0; i<left2Image.length; i++) 
			this.left2Image[i] = new ImageIcon(String.format("bomb/left2_%d.png", i));
		for(int i=0; i<right1Image.length; i++) 
			this.right1Image[i] = new ImageIcon(String.format("bomb/right1_%d.png", i));
		for(int i=0; i<right2Image.length; i++) 
			this.right2Image[i] = new ImageIcon(String.format("bomb/right2_%d.png", i));
	}
	public void explode() {
		
	}
}
