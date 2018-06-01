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

import com.chemyoo.spider.core.MouseEventAdapter;
import com.chemyoo.spider.core.SelectFiles;
import com.chemyoo.spider.core.Spider;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月30日 上午9:35:07 
 * @since 2018年5月30日 上午9:35:07 
 * @description 类说明 
 */
public class SpiderUI extends JFrame{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1987615425247905123L;
	
	public SpiderUI() {}
	
	public void initSpiderUI() {
		this.setTitle("SpiderUI");  
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        this.setSize(550, 250);
        this.setLocationRelativeTo(null);//窗体居中显示  
        final JPanel contentPane= new JPanel();  
        contentPane.setBorder(new EmptyBorder(20,5,5,5));  
        this.setContentPane(contentPane);  
        contentPane.setLayout(new GridLayout(4,1,5,5));  
        contentPane.setAlignmentY(LEFT_ALIGNMENT);
        JPanel pane1=new JPanel();  
        JPanel pane2=new JPanel();  
        JPanel pane3=new JPanel();  
        JPanel pane4=new JPanel(); 

        JLabel label1=new JLabel("网址：");  
        Dimension preferredSize = new Dimension(98,20);//设置尺寸
        label1.setPreferredSize(preferredSize);
        label1.setHorizontalAlignment(JTextField.RIGHT);
        final JTextField url = new JTextField();  
        url.setColumns(31);  
        pane1.add(label1);  
        pane1.add(url);  
        
        pane1.setAlignmentX(LEFT_ALIGNMENT);
        
        
        JLabel label2=new JLabel("本地保存路径：");  
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
        
        start.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final String netUrl = url.getText();
				final String fileDir = path.getText();
				if(isNotBlank(netUrl, fileDir)) {
					start.setEnabled(false);
					pause.setVisible(false);
					tip.setVisible(false);
					Thread thread = new Thread("Spider") {
						@Override
						public void run() {
							start.setText("正在爬取");
							Spider spider = new Spider(netUrl, fileDir, start);
							spider.start();
							tip.setVisible(true);
						}
					};
					
					thread.start();
					
				} else {
					JOptionPane.showMessageDialog(contentPane, "所有的内容都不允许为空", "提示", 
							JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
        
        //暂停爬虫程序
        cancle.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!start.isEnabled()) {
					start.setEnabled(true);
					pause.setVisible(true);
				}
			}
		});
        
        stop.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				start.setText("开始爬取");
			}
		});
        
        contentPane.add(pane1);  
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
