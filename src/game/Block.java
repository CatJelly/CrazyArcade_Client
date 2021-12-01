package game;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import game.CrazyArcadeClientView.GamePanel;

public class Block extends MapObject {
	public Block(int xPos, int yPos, int code, String name, GamePanel gamePanel) {
		super(xPos, yPos, code, name, gamePanel);
		
		String filename = String.format("maps/block%d.png", CrazyArcadeClientView.random.nextInt(3) + 1);
		this.image = new ImageIcon(filename);
	}
}
