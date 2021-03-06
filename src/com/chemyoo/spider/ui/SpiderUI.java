package com.chemyoo.spider.ui;

import com.chemyoo.spider.core.DeleteImages;
import com.chemyoo.spider.core.ICallback;
import com.chemyoo.spider.core.LinkQueue;
import com.chemyoo.spider.core.MouseEventAdapter;
import com.chemyoo.spider.core.SelectFiles;
import com.chemyoo.spider.core.Spider;
import com.chemyoo.spider.util.Message;
import com.chemyoo.spider.util.PropertiesUtil;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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

	private static final SystemTray tray = SystemTray.getSystemTray();
	
	public static final String DEFAULT_PATH = System.getProperty("user.dir");
	
	private Long startTime = 0L;
	
	private TrayIcon trayIcon = null;
	
	private static final String CHAR_SET = "utf-8";
	
	private Color darkGreen = new Color(51, 102, 51);
	
	public SpiderUI() {
		super();
	}
	
	/**
	 * 初始化UI进行显示
	 */
	public void initSpiderUI() {
		this.setTitle("SpiderUI");  
        this.setUndecorated(true); // 去掉窗口的装饰 
        this.getRootPane().setWindowDecorationStyle(JRootPane.QUESTION_DIALOG);//采用指定的窗口装饰风格
        this.setSize(550, 280);
        this.setLocationRelativeTo(null);//窗体居中显示  
        final JPanel contentPane= new JPanel();  
        contentPane.setBorder(new EmptyBorder(20,5,5,5));  
        this.setContentPane(contentPane);  
        contentPane.setLayout(new GridLayout(6,1,5,5));  
        contentPane.setAlignmentY(LEFT_ALIGNMENT);
		JPanel pane1 = new JPanel();
		JPanel pane2 = new JPanel();
		JPanel pane3 = new JPanel();
		JPanel pane4 = new JPanel();
		JPanel pane5 = new JPanel();
		JPanel pane6 = new JPanel(); 

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
        pane6.setAlignmentX(LEFT_ALIGNMENT);
        
        
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
        
        final JButton loadTast = new JButton("载入任务");
        loadTast.setPreferredSize(preferredSize);
        pane3.add(loadTast);
        
        final JButton findTask = new JButton("查找任务");
        findTask.setPreferredSize(preferredSize);
        pane3.add(findTask);
        
        final JLabel tip = new JLabel("网站爬取完成...");
        tip.setVisible(false);
        tip.setForeground(darkGreen);
        pane4.add(tip);
        
        final JLabel pause = new JLabel("程序暂停中...");
		pause.setVisible(false);
		pause.setForeground(Color.RED);
		pane4.add(pause);

		final JLabel message = new JLabel();
		message.setForeground(Color.BLUE);
		message.setVisible(false);
		pane4.add(message);
		
		final JLabel urlNumLable = new JLabel();
		urlNumLable.setVisible(true);
		urlNumLable.setText("当前链接数量:");
		urlNumLable.setHorizontalAlignment(JTextField.LEFT);
		pane6.add(urlNumLable);
		urlNumLable.setForeground(darkGreen);
		
		final JLabel urlNum = new JLabel();
		urlNum.setVisible(true);
		urlNum.setText("0个 已下载0张图片");
		urlNum.setHorizontalAlignment(JTextField.LEFT);
		pane6.add(urlNum);
		
		urlNum.setForeground(darkGreen);
		
		final JLabel speedLabel = new JLabel();
		speedLabel.setVisible(true);
		speedLabel.setText("已访问链接:");
		speedLabel.setHorizontalAlignment(JTextField.LEFT);
		pane6.add(speedLabel);
		
		speedLabel.setForeground(darkGreen);
		
		final JLabel speed = new JLabel();
		speed.setVisible(true);
		speed.setText("0个");
		speed.setHorizontalAlignment(JTextField.LEFT);
		pane6.add(speed);
		
		speed.setForeground(darkGreen);
		
		final JLabel runTimeTitle = new JLabel();
		runTimeTitle.setVisible(true);
		runTimeTitle.setText("运行时间:");
		runTimeTitle.setHorizontalAlignment(JTextField.LEFT);
		pane6.add(runTimeTitle);
		
		final JLabel runTime = new JLabel();
		runTime.setVisible(true);
		runTime.setText("00s");
		runTime.setHorizontalAlignment(JTextField.LEFT);
		pane6.add(runTime);
		
		runTime.setForeground(darkGreen);
		runTimeTitle.setForeground(darkGreen);
		
//		pane6.setBackground(new Color(255,255,224))

		final URL workdir = SpiderUI.class.getClassLoader().getResource("settings.png");
		
        start.addMouseListener(new MouseEventAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final String netUrl = url.getText();
				final String fileDir = path.getText();
				final String refererUrl = referer.getText();
				if(isNotBlank(netUrl, fileDir)) {
					// 如果取消按钮不可用说明，真正执行暂停过程，则返回，不新建线程进行爬取
					// 只允许创建一个爬虫
					if(!start.isEnabled() || !cancle.isEnabled()) {
						return;
					}
					start.setEnabled(false);
					pause.setVisible(false);
					tip.setVisible(false);
					message.setVisible(true);
					Thread thread = new Thread("Spider") {
						@Override
						public void run() {
							String res = Message.SUCCESS;
							start.setText("正在爬取");
							String fileName = DEFAULT_PATH + PropertiesUtil.getFileSeparator() 
							+ refererUrl.split("//")[1].split("/")[0] + ".Error.task";
							try (FileWriter fw = new FileWriter(fileName, false);){
								StringBuilder buider = new StringBuilder();
								buider.append(netUrl).append(PropertiesUtil.getLineSeparator())
									  .append(refererUrl).append(PropertiesUtil.getLineSeparator())
									  .append(fileDir).append(PropertiesUtil.getLineSeparator());
								
								fw.write(new String(buider.toString().getBytes(CHAR_SET)));
								fw.flush();
								fw.close();
								
								Spider spider = new Spider(netUrl.trim(), fileDir.trim(), 
										start, message, refererUrl.trim());
								startTime = Calendar.getInstance().getTimeInMillis();
								res = spider.start();
							} catch (Exception e) {
								LOG.error("程序运行发生异常",e);
								start.setText("开始爬取");
								start.setEnabled(true);
							}
							if(!Message.SUCCESS.equals(res)) {
								tip.setText(message.getText());
							} else {
								tip.setText("网站爬取完成...");
							}
							if(!pause.isVisible()) {
								tip.setVisible(true);
							}
							message.setVisible(false);
							cancle.setEnabled(true);
							start.setSelected(false);
							start.setText("开始爬取");
							start.setEnabled(true);
							DeleteImages.delete(fileDir.trim());
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
					start.setSelected(true);
					new Thread() {
						@Override
						public void run() {
							try {
								while(!cancle.isEnabled()) {
									TimeUnit.MILLISECONDS.sleep(250);
								}
								start.setEnabled(true);
							} catch (InterruptedException e) {
								LOG.error("暂停异常", e);
								Thread.currentThread().interrupt();
							}
						}
					}.start();
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
        
        loadTast.addMouseListener(new MouseEventAdapter() {
        	@Override
			public void mouseClicked(MouseEvent e) {
        		try {
        			loadTast.setEnabled(false);
        			File file = SelectFiles.getFile(DEFAULT_PATH);
        			if(file == null) return;
        			LinkQueue.clear(); // 载入前清空队列内存
					java.util.List<String> lines = FileUtils.readLines(file, CHAR_SET);
					if(!lines.isEmpty()) {
						url.setText(lines.remove(0));
						referer.setText(lines.remove(0));
						path.setText(lines.remove(0));
						Iterator<String> it = lines.iterator();
						while(it.hasNext()) {
							LinkQueue.push(it.next());
						}
					}
					FileUtils.deleteQuietly(file);
				} catch (IOException e1) {
					LOG.error(e1.getMessage(), e1);
				} finally {
					loadTast.setEnabled(true);
				}
        	}
        });
        
        findTask.addMouseListener(new MouseEventAdapter() {
        	@Override
			public void mouseClicked(MouseEvent e) {
        		if(!findTask.isEnabled()) return;
        		
        		findTask.setEnabled(false);
        		new FindUI(new ICallback() {
					/**
					 * serialVersionUID
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void getText(boolean coloseable) {
						findTask.setEnabled(coloseable);
					}
        		});
        	}
        });
        
        contentPane.add(pane1);  
        contentPane.add(pane5); 
        contentPane.add(pane2); 
        contentPane.add(pane3); 
        contentPane.add(pane4);
        contentPane.add(pane6);

        this.addWindowListener(new WindowAdapter() {
			
			// 窗口激活时调用的方法 windowActivated
			@Override
			// 窗口被最小化时调用的方法
			public void windowIconified(WindowEvent e){
				// 只当点击最小化按钮时才最小化到托盘，失去活性时不触发
				if(e.getWindow().isFocused()) {
					setVisible(false);
				}
			}
			
			@Override
			// 关闭窗口
			public void windowClosing(WindowEvent e) {
				windowIconified(e);
			}
			
		});

        this.setVisible(true);  
        
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(!start.isEnabled()) {
					Long spend = System.currentTimeMillis() - startTime;
					runTime.setText(long2TimeStr(spend));
					StringBuilder urlText = new StringBuilder();
					urlText.append(LinkQueue.getUnVisitedSize());
					urlText.append("个 已下载").append(LinkQueue.getImageSize()).append("张图片");
					urlNum.setText(urlText.toString());
					if(spend > 0 && spend > 10000) {
						int velocity = LinkQueue.getVisitedSize();
						speed.setText(velocity + "个");
					} else {
						speed.setText("0个");
					}
				} 
			}
        	
        }, 0, 1000);
        
        // 初始化到托盘区
        miniTray(workdir, path, url, referer);
	}
	
	public static boolean isNotBlank(String...args) {
		for(String arg : args) {
			if(arg == null || "".equals(arg.trim())) {
				return false;
			}
		}
		return true;
	}

	private void miniTray(final URL workdir,final JTextField path, final JTextField netUrl, final JTextField referer) { //窗口最小化到任务栏托盘

		ImageIcon trayImg = new ImageIcon(workdir);//托盘图标
		PopupMenu pop = new PopupMenu(); //增加托盘右击菜单
		MenuItem show = new MenuItem("还原");
		MenuItem exit = new MenuItem("退出");
		
		show.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { // 按下还原键
				// tray.remove(trayIcon) // 移除图标
				setVisible(true);
				setExtendedState(JFrame.NORMAL);
				toFront();
			}

		});

		exit.addActionListener(new ActionListener() { // 按下退出键
			@Override
			public void actionPerformed(ActionEvent e) {
				if(new File(path.getText()).isDirectory()) {
					DeleteImages.delete(path.getText());
				}
				try {
					tray.remove(trayIcon);
					saveStatus(netUrl.getText(), referer.getText(), path.getText());
					dispose(); // 关闭窗体
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					LOG.error(ex.getMessage(), ex);
				} finally {
					System.exit(0); // 延时3s关闭JVM
				}
			}

		});

		pop.add(show);
		pop.add(exit);

		trayIcon = new TrayIcon(trayImg.getImage(), "壁纸下载后台任务", pop);
		trayIcon.setImageAutoSize(true);

		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) { // 鼠标器双击事件

				if (e.getClickCount() == 2) {
					setVisible(true);
					setExtendedState(JFrame.NORMAL); // 还原窗口
					toFront();
				}
			}

		});

		try {
			tray.add(trayIcon);
		} catch (AWTException e1) {
			LOG.error(e1.getMessage(), e1);
		}

	}
	
	private String long2TimeStr(Long time) {
		long spendSecond = time / 1000;
		long hour = spendSecond / 3600;
		long minute = (spendSecond - hour * 3600) / 60;
		long second = spendSecond - hour * 3600 - minute * 60;
		StringBuilder timeBuilder = new StringBuilder();
		if (hour > 0) {
			timeBuilder.append(transformNum(hour, "h "));
		}
		if (minute > 0 || hour > 0) {
			timeBuilder.append(transformNum(minute, "min "));
		}
		if (second >= 0 || timeBuilder.length() == 0) {
			timeBuilder.append(transformNum(second, "s"));
		}
		return timeBuilder.toString();
	}
	
	private String transformNum(long num, String unit) {
		StringBuilder numBuilder = new StringBuilder();
		if(num < 10) {
			numBuilder.append("0");
		}
		return numBuilder.append(num).append(unit).toString();
	}
	
	private void saveStatus(String netUrl, String origin, String savePath) {
		LOG.warn("关闭程序...");
		StringBuilder buider = new StringBuilder();
		buider.append(netUrl).append(PropertiesUtil.getLineSeparator())
			  .append(origin).append(PropertiesUtil.getLineSeparator())
			  .append(savePath).append(PropertiesUtil.getLineSeparator());
		int index = 0;
		if(LinkQueue.unVisitedEmpty())
			return;
		while(!LinkQueue.unVisitedEmpty()) {
			buider.append(LinkQueue.unVisitedPop()).append(PropertiesUtil.getLineSeparator());
			index ++;
			if(index > 5000)
				break;
		}
		String fileName = DEFAULT_PATH + PropertiesUtil.getFileSeparator() 
							+ origin.split("//")[1].split("/")[0] + ".task";
		try (FileWriter fw = new FileWriter(fileName)){
			fw.write(new String(buider.toString().getBytes(),CHAR_SET));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		LinkQueue.clear();
	}

}
