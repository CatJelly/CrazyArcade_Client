package game;

import java.util.Vector;

import javax.swing.ImageIcon;

import game.CrazyArcadeClientView.GamePanel;

public class Bomb extends MapObject {
	public MapObject origin = null;
	//public final int BOMB_DELAY = 40; //노트북
	public final int BOMB_DELAY = 200; //컴퓨터
	public final int CENTER_DELAY = 70;
	public final int EXPLODE_DELAY = 110;
	public int bombImgIdx = 0;
	public int explodeImgIdx = 0;
	public long startTime;
	public long explodeStart;
	public boolean explodeStatus = false;
	public ImageIcon [] bombImage = new ImageIcon[4];
	public ImageIcon [] centerImage = new ImageIcon[6];
	public ImageIcon [] up1Image = new ImageIcon[11];
	public ImageIcon [] up2Image = new ImageIcon[11];
	public ImageIcon [] down1Image = new ImageIcon[11];
	public ImageIcon [] down2Image = new ImageIcon[11];
	public ImageIcon [] left1Image = new ImageIcon[11];
	public ImageIcon [] left2Image = new ImageIcon[11];
	public ImageIcon [] right1Image = new ImageIcon[11];
	public ImageIcon [] right2Image = new ImageIcon[11];
	
	public Vector<Bomb> bombs = new Vector<Bomb>();
	
	
	public Bomb(int xPos, int yPos, int code, String name, GamePanel gamePanel, Map map, Vector<Bomb> bombs) {
		super(xPos, yPos, code, name, gamePanel);
		this.map = map;
		this.bombs = bombs;
		
		for(int i=0; i<bombImage.length; i++) 
			this.bombImage[i] = new ImageIcon(String.format("bomb/bomb%d.png", i));
		for(int i=0; i<centerImage.length; i++) 
			this.centerImage[i] = new ImageIcon(String.format("bomb/pop%d.png", i));
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
	public boolean explode(int x, int y) {
		return !map.collideCheck(x, y);
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public void run() {
		while(true) {
			long currentTime = System.currentTimeMillis();
			long diff = (currentTime - startTime) / 1000;
			
			if(diff > 1) { 
				explodeStatus = true;
				explodeStart = System.currentTimeMillis();
				bombImgIdx = 0;
				break;
			}
		}
		while(true) {
			long explodeCurrent = System.currentTimeMillis();
			long diff = (explodeCurrent - explodeStart) / 1000;
			if(diff > 0.4) {
				bombs.remove(this);
				map.deleteBomb(this);
				break;
			}
		}
		
	}
}
