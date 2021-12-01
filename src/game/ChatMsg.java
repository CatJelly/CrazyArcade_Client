package game;

// ChatMsg.java 梨��� 硫���吏� ObjectStream ��.
import java.awt.event.MouseEvent;
import java.io.Serializable;
import javax.swing.ImageIcon;

class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	public String code; // 100:濡�洹몄��, 400:濡�洹몄����, 200:梨���硫���吏�, 300:Image, 500: Mouse Event
	public String UserName;
	public String data;
	public ImageIcon img;
	public MouseEvent mouse_e;
	public int pen_size; // pen size
	public int [][] mapInfo;
	public int left_right;
	public int up_down;
	public int p_xPos, p_yPos;
	public int direction;
	public int motionIdx;
	public int playerNum;

	public ChatMsg(String UserName, String code, String msg) {
		this.code = code;
		this.UserName = UserName;
		this.data = msg;
	}
}