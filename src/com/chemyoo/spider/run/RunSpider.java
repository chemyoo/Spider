package com.chemyoo.spider.run;

import org.apache.log4j.Logger;

import com.chemyoo.spider.ui.SpiderUI;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月30日 上午9:40:50 
 * @since 2018年5月30日 上午9:40:50 
 * @description 类说明 
 */
public class RunSpider {
	
	private static final Logger LOG = Logger.getLogger(RunSpider.class);

	public static void main(String[] args) {
		
		SpiderUI ui = new SpiderUI();
		ui.initSpiderUI();
		LOG.info("启动服务...");
	}

}
