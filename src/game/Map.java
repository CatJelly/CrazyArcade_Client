package game;


import java.net.Socket;

import game.CrazyArcadeClientView.GamePanel;

public class Map {
	private GamePanel gamePanel = null;
	public int [][] mapInfo = new int[13][15];
	public MapObject [][] objects = new MapObject[13][15];
	private Player [] players = new Player[4];
	private int playerCnt = 0;
	
	public Map(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
	}
	public void setMapInfo(int [][] mapInfo) {
		this.mapInfo = mapInfo; 
	
		for(int i=0; i<this.mapInfo.length; i++) {
			for(int j=0; j<this.mapInfo[i].length; j++) {
				switch(this.mapInfo[i][j]) {
				case 0:
				case 3:
				case 4:
					objects[i][j] = (new Tile(j, i, 0, "Tile", gamePanel));
					break;
				case 5:
					objects[i][j] = (new Tile(j, i, 0, "Tile", gamePanel));
					players[playerCnt++] = (new Player(j, i, 4, "Player", gamePanel, this));
					break;
				case 1:
					objects[i][j] = (new Wall(j, i, 5, "Wall", gamePanel));
					break;
				case 2:
					objects[i][j] = (new Block(j, i, 6, "Block", gamePanel));
					break;
				}
			}
		}
	}
	public void refreshMapInfo() {
		playerCnt = 0;
		for(int i=0; i<this.mapInfo.length; i++) {
			for(int j=0; j<this.mapInfo[i].length; j++) {
				if(mapInfo[i][j] != objects[i][j].code) {
					switch(this.mapInfo[i][j]) {
					case 0:
						objects[i][j] = (new Tile(j, i, 0, "Tile", gamePanel));
					}
				}
			}
		}
	}
	public Player setPlayer(int playerNum, Socket socket) {
		players[playerNum].setSocket(socket);
		return players[playerNum];
	}
	public void mapPrint() {
		for(int i=0; i<objects.length; i++) {
			for(int j=0; j<objects[i].length; j++) {
				objects[i][j].printObject();
			}
		}
		for(int i=0; i<playerCnt; i++) {
			players[i].printObject();
		}
	}
	public Player [] getPlayers() {
		return players;
	}
	public MapObject [][] getMapObjects() {
		return objects;
	}
	
	public boolean collideCheck(int xPos, int yPos) {
		if((xPos < 0 || yPos < 0) || (xPos > 14 || yPos > 12))
			return true;
		
		if(mapInfo[yPos][xPos] == 1 || mapInfo[yPos][xPos] == 2)
			return true;
		else return false;
	}
	public boolean brokeCheck(int xPos, int yPos) {
		if((xPos < 0 || yPos < 0) || (xPos > 14 || yPos > 12))
			return false;
		
		if(mapInfo[yPos][xPos] == 2)
			return true;
		else return false;
	}
	public MapObject setBomb(Bomb bomb) {
		MapObject origin = objects[bomb.yPos][bomb.xPos]; 
		mapInfo[bomb.yPos][bomb.xPos] = 4;
		objects[bomb.yPos][bomb.xPos] = bomb;
		return origin;
	}
	public synchronized void deleteBomb(Bomb bomb) {
		mapInfo[bomb.yPos][bomb.xPos] = 0;
		objects[bomb.yPos][bomb.xPos] = bomb.origin;
	}
}
