package game;
import java.awt.Image;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import game.CrazyArcadeClientView.GamePanel;

public class Player extends MapObject {
	private Socket socket;
	public int left_right = 0;
	public int up_down = 0;
	public int bomb;
	public int power;
	public int speed;
	public int direction;
	public int motionIdx;
	public boolean moveStatus = false;
	public ImageIcon [] waitImage = new ImageIcon[1];
	public ImageIcon [] upImage = new ImageIcon[7];
	public ImageIcon [] downImage = new ImageIcon[7];
	public ImageIcon [] leftImage = new ImageIcon[6];
	public ImageIcon [] rightImage = new ImageIcon[6];
	
	public Player(int xPos, int yPos, int code, String name, GamePanel gamePanel, Map map) {
		super(xPos, yPos, code, name, gamePanel);
		this.map = map;
		this.bomb = 1;
		this.power = 1;
		this.speed = 1;
		direction = 0;
		motionIdx = 0;
		
		for(int i=0; i<waitImage.length; i++) 
			this.waitImage[i] = new ImageIcon(String.format("player/wait%d.png", i));
		for(int i=0; i<upImage.length; i++) 
			this.upImage[i] = new ImageIcon(String.format("player/up%d.png", i));
		for(int i=0; i<downImage.length; i++) 
			this.downImage[i] = new ImageIcon(String.format("player/down%d.png", i));
		for(int i=0; i<leftImage.length; i++) 
			this.leftImage[i] = new ImageIcon(String.format("player/left%d.png", i));
		for(int i=0; i<rightImage.length; i++) 
			this.rightImage[i] = new ImageIcon(String.format("player/right%d.png", i));
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public void positionCheck() {
		if(left_right <= -MapObject.BLOCK_SIZE) {
			left_right = 0;
			xPos -= 1;
		}
		else if(left_right >= MapObject.BLOCK_SIZE) {
			left_right = 0;
			xPos += 1;
		}
		else if(up_down <= -(MapObject.BLOCK_SIZE)) {
			up_down = 0;
			yPos -= 1;
		}
		else if(up_down >= MapObject.BLOCK_SIZE) {
			up_down = 0;
			yPos += 1;
		}
	}
	public void setBomb() {
		Bomb bomb = new Bomb(this.xPos, this.yPos, 4, "Bomb", this.gamePanel);
		map.setBomb(bomb);
	}
}