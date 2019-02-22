package com.chemyoo.spider.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.chemyoo.spider.core.ICallback;
import com.chemyoo.spider.core.MouseEventAdapter;

/** 
 * @author Author : jianqing.liu
 * @version version : created time：2019年2月22日 下午4:25:26 
 * @since since from 2019年2月22日 下午4:25:26 to now.
 * @description class description
 */
public class FindUI extends JFrame {
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	private ICallback callback;
	
	public FindUI(ICallback callback) {
		super();
		this.initFindUI();
		this.callback = callback;
	}
	
	public void getText(String text, boolean coloseable) {
		callback.getText(text, coloseable);
		if(coloseable) {
			setEnabled(false);
			dispose();
		}
	}
	
	
	/**
	 * 初始化UI进行显示
	 */
	public void initFindUI() {
		this.setTitle("查找任务");  
        this.setSize(350, 150);
        this.setUndecorated(true); // 去掉窗口的装饰 
        this.getRootPane().setWindowDecorationStyle(JRootPane.WARNING_DIALOG);//采用指定的窗口装饰风格
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);//窗体居中显示  
        final JPanel contentPane= new JPanel();  
        contentPane.setBorder(new EmptyBorder(20,5,5,5));  
        this.setContentPane(contentPane);  
        contentPane.setLayout(new GridLayout(2,1,5,5));  
        contentPane.setAlignmentY(LEFT_ALIGNMENT);
		JPanel pane1 = new JPanel();
		JPanel buttonPane = new JPanel();
		
		JLabel label1 = new JLabel("任务网址:");  
        Dimension preferredSize = new Dimension(60,20);//设置尺寸
        label1.setPreferredSize(preferredSize);
        label1.setHorizontalAlignment(JTextField.RIGHT);
        final JTextField url = new JTextField();  
        url.setColumns(20);  
        pane1.add(label1);  
        pane1.add(url);
        
        final JButton serach = new JButton("查找");  
        final JButton cancle = new JButton("取消"); 
        buttonPane.add(serach);  
        buttonPane.add(cancle);  
        contentPane.add(pane1);
        contentPane.add(buttonPane);
        
        serach.addMouseListener(new MouseEventAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		getText(url.getText(), false);
        	}
		});
        
        cancle.addMouseListener(new MouseEventAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		getText(url.getText(), true);
        	}
		});
        
        this.addWindowListener(new WindowAdapter() {
        	@Override
	    	public void windowClosing(WindowEvent e) {
        		getText(url.getText(), true);
	    	}
    	});
        
		this.setVisible(true);  
	}
	
}
