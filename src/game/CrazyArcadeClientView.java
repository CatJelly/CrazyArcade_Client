package game;

// JavaObjClientView.java ObjecStram 기반 Client
//실질적인 채팅 창
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Random;

import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.GridLayout;

public class CrazyArcadeClientView extends JFrame {
	public static Random random = new Random();
	public static final int BLOCK_SIZE = 43;
	public ImageIcon backgroundImg = null;
	public ImageIcon startBtnImg = null;
	/* status
	 * 0 : 타이틀 화면 , 1 : 스타트, 2 : 게임화면, 3 : 게임 오버    
	 */
	public int status;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtInput;
	private String UserName;
	private JButton btnSend;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	public Player [] players = null;
	public MapObject [][] mapObjects = null;
	private int playerNum;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	// private JTextArea textArea;
	private JTextPane textArea;

	private Frame frame;
	private FileDialog fd;
	private JButton imgBtn;

	JPanel panel;
	private JLabel lblMouseEvent;
	private Graphics gc;
	private int pen_size = 2; // minimum 2
	// 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
	private Image panelImage = null; 
	private Graphics gc2 = null;
	public JavaGameClientViewDrawing drawing;
	public CrazyArcadeClientView view;
	public GamePanel gamePanel;
	public Map map;
	
	private JButton startBtn;

	/**
	 * Create the frame.
	 * @throws BadLocationException 
	 */
	public CrazyArcadeClientView(String username, String ip_addr, String port_no)  {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1070, 827);
		contentPane = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
		        g.drawImage(backgroundImg.getImage(), 0, 0, null);
		    }
		};
		backgroundImg = new ImageIcon("maps/play_bg.png");
		startBtnImg = new ImageIcon("maps/start_button.png");
		Image btnImg = startBtnImg.getImage();
		Image chgImg = btnImg.getScaledInstance(60, 40, Image.SCALE_SMOOTH);
		startBtnImg = new ImageIcon(chgImg);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		contentPane.setLayout(null);
		
		gamePanel = new GamePanel();
		gamePanel.setBounds(26,50, 780, 680);
		contentPane.add(gamePanel);
		
//		JPanel panel_1 = new JPanel();
//		panel_1.setBounds(40, 47, 742, 550);
//		contentPane.add(panel_1);
		
		startBtn = new JButton("start button") {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
		        g.drawImage(startBtnImg.getImage(), 0, 0, null);
		    }
		};
		startBtn.setBounds(892, 66, 120, 50);
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "900", "Game Start");
				SendObject(msg);
				//gamePanel.setFocusable(true);
				gamePanel.requestFocus();
			}
		});
		contentPane.add(startBtn);

		map = new Map(gamePanel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(854, 143, 167, 459);
		contentPane.add(scrollPane);
		textArea = new JTextPane();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 14));

		txtInput = new JTextField();
		txtInput.setBounds(864, 612, 184, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);
		
		btnSend = new JButton("Send");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 9));
		btnSend.setBounds(966, 683, 57, 34);
		contentPane.add(btnSend);
		setVisible(true);

		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;

		imgBtn = new JButton("+");
		imgBtn.setFont(new Font("굴림", Font.PLAIN, 16));
		imgBtn.setBounds(800, 619, 50, 40);
		contentPane.add(imgBtn);

		JButton btnExit = new JButton("\uC885\uB8CC");
		btnExit.setFont(new Font("굴림", Font.PLAIN, 9));
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "400", "Bye");
				SendObject(msg);
				System.exit(0);
			}
		});
		btnExit.setBounds(882, 680, 57, 40);
		contentPane.add(btnExit);
		


		view = this;
		

		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

			// SendMessage("/login " + UserName);
			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello");
			SendObject(obcm);

			ListenNetwork net = new ListenNetwork();
			net.start();
			TextSendAction action = new TextSendAction();
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			ImageSendAction action2 = new ImageSendAction();
			imgBtn.addActionListener(action2);
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}
	}
	
	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {

					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s]\n%s", cm.UserName, cm.data);
					} else
						continue;
					switch (cm.code) {
					case "101": //loginSuccess
						playerNum = cm.playerNum;
						System.out.println("this PlayerNum " + playerNum);
						break;
					case "200": // chat message
						if (cm.UserName.equals(UserName))
							AppendTextR(msg); // 내 메세지는 우측에
						else
							AppendText(msg);
						break;
					case "300": // Image 첨부
						if (cm.UserName.equals(UserName))
							AppendTextR("[" + cm.UserName + "]");
						else
							AppendText("[" + cm.UserName + "]");
						//AppendImage(cm.img);
						break;
					case "400": //게임 일시정지 요청
						break;
					case "401":
						break;
					case "402":
						break;
					case "500": //게임 승리
						break;
					case "501": //게임 패배
						break;
					case "502": //게임 비김
						break;
					case "600": //플에이어 이동
						players = map.getPlayers();
						players[cm.playerNum].direction = cm.direction;
						players[cm.playerNum].left_right = cm.left_right;
						players[cm.playerNum].up_down = cm.up_down;
						players[cm.playerNum].motionIdx = cm.motionIdx;
						players[cm.playerNum].xPos = cm.p_xPos; 
						players[cm.playerNum].yPos = cm.p_yPos;
						players[cm.playerNum].positionCheck();
						break;
//					case "601":
//						players[playerNum].positionCheck();
//						break;
					case "700": //폭탄 설치
						int bombX = cm.bomb_xPos;
						int bombY = cm.bomb_yPos;
						players[cm.playerNum].setBomb(bombX, bombY);
						break;
					case "702":
						map.mapInfo = cm.mapInfo;
						map.refreshMapInfo();
						break;
					case "800": //플레이어 사살
						break;
					case "801": //플레이어 사망
						break;
					case "900": //맵 변동
						map.setMapInfo(cm.mapInfo);
						map.setPlayer(playerNum, socket);
						players = map.getPlayers();
						new Thread(gamePanel).start();
						break;					
					case "1000": // Mouse Event 수신
						drawing.DoMouseEvent(cm);
						break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						socket.close();
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}
	class GamePanel extends JPanel implements Runnable {
		public final int MOTION_DELAY = 30; //노트북
		//public final int MOTION_DELAY = 120; //컴퓨터
		public Graphics buffG;
		public int xAdd = 20;
		public int yAdd = 10;

		GamePanel() {
			setBounds(26, 23, 802, 798);
			setLayout(null);
			setBackground(new Color(73,175,36));
			addKeyListener(new PlayerKeyListener());
			setFocusable(true);
			requestFocus();
		}
		
		@Override
		public void run() {
			while(true) {
				players = map.getPlayers();
				mapObjects = map.getMapObjects();
				repaint();
			}
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);			
			if(mapObjects != null) {
				for(int i=0; i<mapObjects.length; i++) {
					for(int j=0; j<mapObjects[i].length; j++) {
						if(mapObjects[i][j] != null) {
							int x = mapObjects[i][j].xPos;
							int y = mapObjects[i][j].yPos;
							
							if(!(mapObjects[i][j] instanceof Bomb)) {
								g.drawImage(
										mapObjects[i][j].image.getImage(),
										x * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
										BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
										null);
							}
						}														
					}
				}
				for(int i=0; i<mapObjects.length; i++) {
					for(int j=0; j<mapObjects[i].length; j++) {
						int x = mapObjects[i][j].xPos;
						int y = mapObjects[i][j].yPos;
						
						if(mapObjects[i][j] instanceof Bomb) {
							Bomb bomb = (Bomb)mapObjects[i][j];
							MapObject temp = bomb.origin;
							g.drawImage(
									temp.image.getImage(),
									x * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
									BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
									null);
							int motionIdx = bomb.bombImgIdx;
							
							if(bomb.explodeStatus == false) {
								g.drawImage(
										bomb.bombImage[(int)(motionIdx++ / bomb.BOMB_DELAY)].getImage(),
										x * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
										BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
										null);
								if(motionIdx == bomb.bombImage.length * bomb.BOMB_DELAY) {
									motionIdx = 0;
								}
								bomb.bombImgIdx = motionIdx;
							}
							else {
								g.drawImage(
										bomb.centerImage[(int)(motionIdx++ / bomb.CENTER_DELAY)].getImage(),
										x * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
										BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
										null);
								if(motionIdx == bomb.bombImage.length * bomb.CENTER_DELAY) {
									motionIdx = 0;
								}
								bomb.bombImgIdx = motionIdx;
								int explodeIdx = bomb.explodeImgIdx;
								int explodeLen = 3;
								int [] block = {0,0,0,0};
								
								for(int k=1; k<=explodeLen; k++) {
									if(!bomb.explode(x - k, y)) {
										block[0] = 1;
									}
									if(block[0] == 0) {
										if(k == explodeLen)
											g.drawImage(
													bomb.left1Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
													(x - k) * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
													BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
													null);	
										else
											g.drawImage(
												bomb.left2Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
												(x - k) * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
												BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
												null);	
									}
									if(!bomb.explode(x + k, y)) {
										block[1] = 1;
									}
									if(block[1] == 0) {
										if(k == explodeLen)
											g.drawImage(
													bomb.right1Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
													(x + k) * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
													BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
													null);
										else
											g.drawImage(
												bomb.right2Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
												(x + k) * (BLOCK_SIZE + 6) + xAdd, y * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
												BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
												null);
									}
									if(!bomb.explode(x, y - k)) {
										block[2] = 1;
									}
									if(block[2] == 0) {
										if(k == explodeLen)
											g.drawImage(
													bomb.up1Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
													x * (BLOCK_SIZE + 6) + xAdd, (y - k) * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
													BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
													null);	
										else
											g.drawImage(
												bomb.up2Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
												x * (BLOCK_SIZE + 6) + xAdd, (y - k) * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
												BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
												null);	
									}
									if(!bomb.explode(x, y + k)) {
										block[3] = 1;
									}
									if(block[3] == 0) {
										if(k == explodeLen)
											g.drawImage(
													bomb.down1Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
													x * (BLOCK_SIZE + 6) + xAdd, (y + k) * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
													BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
													null);	
										else
											g.drawImage(
												bomb.down2Image[(int)(explodeIdx / bomb.EXPLODE_DELAY)].getImage(),
												x * (BLOCK_SIZE + 6) + xAdd, (y + k) * (BLOCK_SIZE + 6) + i * 7 + yAdd, 
												BLOCK_SIZE + 6, BLOCK_SIZE + 10, 
												null);	
									}
									explodeIdx++;	
								}
								
								if(explodeIdx == bomb.left1Image.length * bomb.EXPLODE_DELAY) {
									String msg = "explode end";
									explodeIdx = 0;
									bomb.explodeImgIdx = explodeIdx;
									if(map.brokeCheck(x - 1, y)) {
										map.mapInfo[y][x-1] = 0;
									}
									if(map.brokeCheck(x + 1, y)) {
										map.mapInfo[y][x+1] = 0;
									}
									if(map.brokeCheck(x, y - 1)) {
										map.mapInfo[y-1][x] = 0;
									}
									if(map.brokeCheck(x, y + 1)) {
										map.mapInfo[y+1][x] = 0;
									}
									map.mapInfo[y][x] = 0;
									SendMessage(msg, "702");
								}
								bomb.explodeImgIdx = explodeIdx;
							}
						}
					}
				}
			}
			if(players != null) {
				for(int i=0; i<players.length; i++) {
					if(players[i] != null) {
						int xPosition = players[i].xPos * (BLOCK_SIZE + 6) + players[i].left_right + xAdd;
						int yPosition = players[i].yPos * (BLOCK_SIZE + 6)+ players[i].up_down + yAdd;
						int motionIdx = players[i].motionIdx;
						
						switch(players[i].direction) {
						case 0: //wait
							g.drawImage(
									players[i].waitImage[(int)(motionIdx / MOTION_DELAY)].getImage(),
									xPosition, yPosition, 
									BLOCK_SIZE, BLOCK_SIZE + 20,
									null);
							if(players[i].moveStatus == true) 
								motionIdx++;
							if(motionIdx == players[i].waitImage.length * MOTION_DELAY)
								motionIdx = 0;
							break;
						case 1: //up
							g.drawImage(
									players[i].upImage[(int)(motionIdx / MOTION_DELAY)].getImage(),
									xPosition, yPosition, 
									BLOCK_SIZE, BLOCK_SIZE + 20,
									null);
							if(players[i].moveStatus == true) 
								motionIdx++;
							if(motionIdx == players[i].upImage.length * MOTION_DELAY)
								motionIdx = 0;
							break;
						case 2: //down
							g.drawImage(
									players[i].downImage[(int)(motionIdx / MOTION_DELAY)].getImage(),
									xPosition, yPosition, 
									BLOCK_SIZE, BLOCK_SIZE + 20,
									null);
							if(players[i].moveStatus == true) 
								motionIdx++;
							if(motionIdx == players[i].downImage.length * MOTION_DELAY)
								motionIdx = 0;
							break;
						case 3: //left
							g.drawImage(
									players[i].leftImage[(int)(motionIdx / MOTION_DELAY)].getImage(),
									xPosition, yPosition, 
									BLOCK_SIZE, BLOCK_SIZE + 20,
									null);
							if(players[i].moveStatus == true) 
								motionIdx++;
							if(motionIdx == players[i].leftImage.length * MOTION_DELAY)
								motionIdx = 0;
							break;
						case 4: //right
							g.drawImage(
									players[i].rightImage[(int)(motionIdx / MOTION_DELAY)].getImage(),
									xPosition, yPosition, 
									BLOCK_SIZE, BLOCK_SIZE + 20,
									null);
							if(players[i].moveStatus == true) 
								motionIdx++;
							if(motionIdx == players[i].rightImage.length * MOTION_DELAY)
								motionIdx = 0;
							break;
						}	
						players[i].motionIdx = motionIdx;
					}
				}
			}
		}
		@Override
		public void update(Graphics g) {
			
		}
	}

	
	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", UserName, txtInput.getText());
				msg = txtInput.getText();
				SendMessage(msg);
				gamePanel.requestFocus();
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				if (msg.contains("/exit")) // 종료 처리
					System.exit(0);
									
			}
		}
	}

	class ImageSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 액션 이벤트가 sendBtn일때 또는 textField 에세 Enter key 치면
			if (e.getSource() == imgBtn) {
				frame = new Frame("이미지첨부");
				fd = new FileDialog(frame, "이미지 선택", FileDialog.LOAD);
				// frame.setVisible(true);
				// fd.setDirectory(".\\");
				fd.setVisible(true);
				// System.out.println(fd.getDirectory() + fd.getFile());
				if (fd.getDirectory().length() > 0 && fd.getFile().length() > 0) {
					ChatMsg obcm = new ChatMsg(UserName, "300", "IMG");
					ImageIcon img = new ImageIcon(fd.getDirectory() + fd.getFile());
					obcm.img = img;
					SendObject(obcm);
				}
			}
		}
	}

	ImageIcon icon1 = new ImageIcon("src/icon1.jpg");

	public void AppendIcon(ImageIcon icon) {
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.insertIcon(icon);
	}

	// 화면에 출력
	public void AppendText(String msg) {
		// textArea.append(msg + "\n");
		// AppendIcon(icon1);
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		//textArea.setCaretPosition(len);
		//textArea.replaceSelection(msg + "\n");
		
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);
	    doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), msg+"\n", left );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
	}
	// 화면 우측에 출력
	public void AppendTextR(String msg) {
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.	
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setForeground(right, Color.BLUE);	
	    doc.setParagraphAttributes(doc.getLength(), 1, right, false);
		try {
			doc.insertString(doc.getLength(),msg+"\n", right );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);

	}
	
	
	public void AppendImage(ImageIcon ori_icon) {
		//drawing.AppendImage(ori_icon); 
		
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len); // place caret at the end (with no selection)
		Image ori_img = ori_icon.getImage();
		Image new_img;
		ImageIcon new_icon;
		int width, height;
		double ratio;
		width = ori_icon.getIconWidth();
		height = ori_icon.getIconHeight();
		// Image가 너무 크면 최대 가로 또는 세로 200 기준으로 축소시킨다.
		if (width > 200 || height > 200) {
			if (width > height) { // 가로 사진
				ratio = (double) height / width;
				width = 200;
				height = (int) (width * ratio);
			} else { // 세로 사진
				ratio = (double) width / height;
				height = 200;
				width = (int) (height * ratio);
			}
			new_img = ori_img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			new_icon = new ImageIcon(new_img);
			textArea.insertIcon(new_icon);
		} else {
			textArea.insertIcon(ori_icon);
			new_img = ori_img;
		}
		len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		// ImageViewAction viewaction = new ImageViewAction();
		// new_icon.addActionListener(viewaction); // 내부클래스로 액션 리스너를 상속받은 클래스로
		// panelImage = ori_img.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_DEFAULT);

		//gc2.drawImage(ori_img,  0,  0, panel.getWidth(), panel.getHeight(), panel);
		//gc.drawImage(panelImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
		
	}

	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	// Server에게 network으로 전송
	public void SendMessage(String msg) {
		try {
			// dos.writeUTF(msg);
//			byte[] bb;
//			bb = MakePacket(msg);
//			dos.write(bb, 0, bb.length);
			ChatMsg obcm = new ChatMsg(UserName, "300", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			// AppendText("dos.write() error");
			AppendText("oos.writeObject() error");
			try {
//				dos.close();
//				dis.close();
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
	public void SendMessage(String msg, String protocol) {
		try {
			// dos.writeUTF(msg);
//			byte[] bb;
//			bb = MakePacket(msg);
//			dos.write(bb, 0, bb.length);
			ChatMsg obcm = new ChatMsg(UserName, protocol, msg);
			if(protocol.equals("700")) {
				obcm.bomb_xPos = players[playerNum].xPos;
				obcm.bomb_yPos = players[playerNum].yPos;
			}
			else if(protocol.equals("701")) {
				obcm.bomb_xPos = players[playerNum].xPos;
				obcm.bomb_yPos = players[playerNum].yPos;
			}
			obcm.mapInfo = map.mapInfo;
			obcm.left_right = players[playerNum].left_right;
			obcm.up_down = players[playerNum].up_down;
			obcm.p_xPos = players[playerNum].xPos;
			obcm.p_yPos = players[playerNum].yPos;
			obcm.direction = players[playerNum].direction;
			obcm.motionIdx = players[playerNum].motionIdx;
			obcm.playerNum = playerNum;
			oos.writeObject(obcm);
			oos.reset();
		} catch (IOException e) {
			// AppendText("dos.write() error");
			AppendText("oos.writeObject() error");
			try {
//				dos.close();
//				dis.close();
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
			oos.reset();
		} catch (IOException e) {
			// textArea.append("메세지 송신 에러!!\n");
			AppendText("SendObject Error");
		}
	}
	class PlayerKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			players[playerNum].moveStatus = true;
			String msg;
			switch(e.getKeyCode()) {
			case KeyEvent.VK_UP:
				if(players[playerNum].direction != 1)
					players[playerNum].motionIdx = 0;
				players[playerNum].direction = 1;
				msg = "up";
				SendMessage(msg, "600");
				break;
			case KeyEvent.VK_DOWN:
				if(players[playerNum].direction != 2)
					players[playerNum].motionIdx = 0;
				players[playerNum].direction = 2;
				msg = "down";
				SendMessage(msg, "600");
				break;
			case KeyEvent.VK_LEFT:
				if(players[playerNum].direction != 3)
					players[playerNum].motionIdx = 0;
				players[playerNum].direction = 3;
				msg = "left";
				SendMessage(msg, "600");
				break;
			case KeyEvent.VK_RIGHT:
				if(players[playerNum].direction != 4)
					players[playerNum].motionIdx = 0;
				players[playerNum].direction = 4;
				msg = "right";
				SendMessage(msg, "600");
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			String msg;
			switch(e.getKeyCode()) {
			case KeyEvent.VK_UP:
				players[playerNum].motionIdx = 0;
				msg = "wait_up";
				SendMessage(msg, "601");
				break;
			case KeyEvent.VK_DOWN:
				players[playerNum].motionIdx = 0;
				msg = "wait_down";
				SendMessage(msg, "601");
				break;
			case KeyEvent.VK_LEFT:
				players[playerNum].motionIdx = 0;
				msg = "wait_left";
				SendMessage(msg, "601");
				break;
			case KeyEvent.VK_RIGHT:
				players[playerNum].motionIdx = 0;
				msg = "wait_right";
				SendMessage(msg, "601");
				break;
			case KeyEvent.VK_SPACE:
				msg = "bomb_set";
				SendMessage(msg, "700");
				break;
			}
			players[playerNum].moveStatus = false;
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}
		
	}
}
