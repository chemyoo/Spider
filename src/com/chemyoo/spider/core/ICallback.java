package com.chemyoo.spider.core;

import java.io.Serializable;

/** 
 * @author Author : jianqing.liu
 * @version version : created time：2019年2月22日 下午5:12:06 
 * @since since from 2019年2月22日 下午5:12:06 to now.
 * @description class description
 */
public interface ICallback extends Serializable {
	
	public void getText(String text, boolean coloseable);
	
}
