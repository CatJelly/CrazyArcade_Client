package game;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import game.CrazyArcadeClientView.GamePanel;

public class Item extends MapObject {
	private int effectValue;
	private int lifeTime;
	
	public Item(int xPos, int yPos, int code, String name, GamePanel gamePanel) {
		super(xPos, yPos, code, name, gamePanel);
		effectValue = 1;
		lifeTime = 1;
		
		try {
			Image img = ImageIO.read(new File("item/item1.bmp"));
			this.image = new ImageIcon(img);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
