package com.chemyoo.spider.core;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.apache.log4j.Logger;

/**
 *  右下角弹出式提示框
 *  1.自动上升
 *  2.停留一段时间，本例子中5秒
 *  3.自动下降直至消失
 *  4.线程控制窗口的出现和消失，同时添加鼠标事件控制，可以提前使提示框消失
 *  5.鼠标事件结合自己的需求实现，此处只是实现一个点击事件
 */
public class RightCornerPopMessage extends JWindow implements Runnable, MouseListener {
	protected final Logger logger = Logger.getLogger(RightCornerPopMessage.class);
    private static final long serialVersionUID = -3564453685861233338L;
    private Integer screenWidth;  // 屏幕宽度
    private Integer screenHeight; // 屏幕高度
    private Integer windowWidth = 200; // 设置提示窗口宽度
    private Integer windowHeight = 100; // 设置提示窗口高度
    private Integer bottmToolKitHeight; // 底部任务栏高度，如果没有任务栏则为零
    private Integer stayTime = 5000; // 提示框停留时间
    private Integer x; // 窗口起始X坐标
    private Integer y; // 窗口起始Y坐标
    private String title = "Spider温馨提示:";
    private String message = "No Message!";
    private JPanel mainPanel; // 主面板
    private JLabel titleLabel; // 标题栏标签
    private JPanel titlePanel; // 标题栏面板
    private JLabel messageLabel; // 内容标签
    private JPanel messagePanel; // 内容面板

    public RightCornerPopMessage() {
        this.init();
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public RightCornerPopMessage(String message) {
    	this.message = message;
        this.init();
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public String getMessage() {
    	 return this.message;
    }

    private void init() {
        bottmToolKitHeight = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration()).bottom;
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth = dimension.width;
        this.screenHeight = dimension.height;

        this.x = screenWidth - windowWidth;
        this.y = this.screenHeight;
        this.setLocation(x, y - bottmToolKitHeight - windowHeight);
        this.mainPanel = new JPanel(new BorderLayout());

        this.titleLabel = new JLabel(this.title);
        this.titleLabel.setForeground(Color.WHITE);
        this.titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.titlePanel.setBackground(Color.BLACK);
        this.titlePanel.add(this.titleLabel);

        this.messageLabel = new JLabel(message);
        this.messageLabel.setForeground(Color.WHITE);
        this.messagePanel = new JPanel();
        this.messagePanel.add(messageLabel);
        this.messagePanel.setBackground(Color.BLACK);

        this.mainPanel.add(titlePanel, BorderLayout.NORTH);
        this.mainPanel.add(messagePanel, BorderLayout.CENTER);

        this.setSize(windowWidth, windowHeight);
        this.setAlwaysOnTop(false);
        this.getContentPane().add(mainPanel);
        this.addMouseListener(this);
        Toolkit.getDefaultToolkit().beep(); // 播放系统声音，提示一下
        this.setVisible(true);
    }

    @Override
    public void run() {
        Integer delay = 10;
        Integer step = 1;
        Integer end = windowHeight + bottmToolKitHeight;
        while (true) {
            try {
                step++;
                y = y - 1;
                this.setLocation(x, y);
                if (step > end) {
                    Thread.sleep(stayTime);
                    break;
                }
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            	logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
        step = 1;
        while (true) {
            try {
                step++;
                y = y + 1;
                this.setLocation(x, y);
                if (step > end) {
                    this.dispose();
                    break;
                }
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            	logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
//        System.exit(0)
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.dispose();
    }
    
	@Override
	public void mousePressed(MouseEvent e)
	{
		// ignore
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// ignore
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// ignore
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// ignore
	}
}