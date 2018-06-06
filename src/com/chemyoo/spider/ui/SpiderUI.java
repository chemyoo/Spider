package com.chemyoo.spider.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.apache.log4j.Logger;
import com.chemyoo.spider.core.LinkQueue;
import com.chemyoo.spider.core.MouseEventAdapter;
import com.chemyoo.spider.core.SelectFiles;
import com.chemyoo.spider.core.Spider;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月30日 上午9:35:07 
 * @since 2018年5月30日 上午9:35:07 
 * @description 用户图形界面
 */
public class SpiderUI extends JFrame{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1987615425247905123L;
	
	private static final Logger LOG = Logger.getLogger(SpiderUI.class);
	
	public SpiderUI() {
		super();
	}
	
	/**
	 * 初始化UI进行显示
	 */
	public void initSpiderUI() {
		this.setTitle("SpiderUI");  
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        this.setSize(550, 250);
        this.setLocationRelativeTo(null);//窗体居中显示  
        final JPanel contentPane= new JPanel();  
        contentPane.setBorder(new EmptyBorder(20,5,5,5));  
        this.setContentPane(contentPane);  
        contentPane.setLayout(new GridLayout(5,1,5,5));  
        contentPane.setAlignmentY(LEFT_ALIGNMENT);
        JPanel pane1=new JPanel();  
        JPanel pane5=new JPanel();  
        JPanel pane2=new JPanel();  
        JPanel pane3=new JPanel();  
        JPanel pane4=new JPanel(); 

        JLabel label1=new JLabel("网址*:");  
        Dimension preferredSize = new Dimension(98,20);//设置尺寸
        label1.setPreferredSize(preferredSize);
        label1.setHorizontalAlignment(JTextField.RIGHT);
        final JTextField url = new JTextField();  
        url.setColumns(31);  
        pane1.add(label1);  
        pane1.add(url);  
        
        JLabel label4 = new JLabel("网址源:");  
        label4.setPreferredSize(preferredSize);
        label4.setHorizontalAlignment(JTextField.RIGHT);
        final JTextField referer = new JTextField();  
        referer.setColumns(31);  
        pane5.add(label4);  
        pane5.add(referer);
        
        pane1.setAlignmentX(LEFT_ALIGNMENT);
        pane5.setAlignmentX(LEFT_ALIGNMENT);
        
        
        JLabel label2=new JLabel("本地保存路径*:");  
        preferredSize = new Dimension(98,20);//设置尺寸
        label2.setPreferredSize(preferredSize);
        label2.setHorizontalAlignment(JTextField.RIGHT);
        final JTextField path = new JTextField();  
        path.setColumns(25);  
        pane2.add(label2);  
        pane2.add(path);  
        
        pane2.setAlignmentX(LEFT_ALIGNMENT);
        
        JButton button = new JButton("选择");
        preferredSize = new Dimension(60,20);//设置尺寸
        button.setPreferredSize(preferredSize);
        pane2.add(button);
        button.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				File file = SelectFiles.getSavePath();
				if(file == null) {
					path.setText("");
				} else {
					path.setText(file.getAbsolutePath());
				}
			}
		});
        
        final JButton start = new JButton("开始爬取");
        preferredSize = new Dimension(90,25);//设置尺寸
        start.setPreferredSize(preferredSize);
        pane3.add(start);
        
        final JButton cancle = new JButton("暂停爬取");
        cancle.setPreferredSize(preferredSize);
        pane3.add(cancle);
        
        final JButton stop = new JButton("停止爬取");
        stop.setPreferredSize(preferredSize);
        pane3.add(stop);
        
        final JLabel tip = new JLabel("网站爬取完成...");
        tip.setVisible(false);
        tip.setForeground(Color.GREEN);
        pane4.add(tip);
        
        final JLabel pause = new JLabel("程序暂停中...");
		pause.setVisible(false);
		pause.setForeground(Color.RED);
		pane4.add(pause);

		final JLabel message = new JLabel();
		message.setForeground(Color.BLUE);
		message.setVisible(false);
		pane4.add(message);
        
        start.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final String netUrl = url.getText();
				final String fileDir = path.getText();
				final String refererUrl = referer.getText();
				if(isNotBlank(netUrl, fileDir)) {
					//如果取消按钮不可用说明，真正执行暂停过程，则返回，不新建线程进行爬取
					if(!cancle.isEnabled()) {
						return;
					}
					start.setEnabled(false);
					pause.setVisible(false);
					tip.setVisible(false);
					message.setVisible(true);
					Thread thread = new Thread("Spider") {
						@Override
						public void run() {
							start.setText("正在爬取");
							try {
							Spider spider = new Spider(netUrl, fileDir, start, message, refererUrl);
							spider.start();
							} catch (Exception e) {
								LOG.error("程序运行发生异常:", e);
								start.setText("开始爬取");
								start.setEnabled(true);
							}
							tip.setText("网站爬取完成...");
							if(!pause.isVisible()) {
								tip.setVisible(true);
							}
							cancle.setEnabled(true);
						}
					};
					
					thread.start();
					
				} else {
					JOptionPane.showMessageDialog(contentPane, "网址或保存路径不允许为空", "提示", 
							JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
        
        //暂停爬虫程序
        cancle.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!start.isEnabled()) {
					pause.setVisible(true);
					cancle.setEnabled(false);
					message.setVisible(false);
					start.setEnabled(true);
				}
			}
		});
        
        stop.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pause.setVisible(false);
				tip.setVisible(true);
				if(!start.isEnabled()) {
					message.setVisible(false);
					cancle.setEnabled(false);
					tip.setText("正在停止...");
				}
				LinkQueue.clear();
			}
		});
        
        contentPane.add(pane1);  
        contentPane.add(pane5); 
        contentPane.add(pane2); 
        contentPane.add(pane3); 
        contentPane.add(pane4); 
        this.setVisible(true);  
	}
	
	private static boolean isNotBlank(String...args) {
		for(String arg : args) {
			if(arg == null || "".equals(arg.trim())) {
				return false;
			}
		}
		return true;
	}

}
