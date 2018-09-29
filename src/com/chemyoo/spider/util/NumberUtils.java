package com.chemyoo.spider.util;

import java.math.BigDecimal;

/** 
 * @author 作者 : jianqing.liu
 * @version 创建时间：2018年5月11日 下午1:22:32 
 * @since 2018年5月11日 下午1:22:32 
 * @description 关于数字的工具类 
 */
public class NumberUtils {
	
	private NumberUtils() {}
	
	/**
	 * 为浮点型数据设置精度
	 * @param num
	 * @param scale 小数位数
	 * @return
	 */
	public static double setScale(double num, int scale) {
		return BigDecimal.valueOf(num)
						 .setScale(scale, BigDecimal.ROUND_HALF_UP)
						 .doubleValue();
	}
	
	/**
	 * 将对象转为Double数值
	 * @param obj
	 * @return
	 */
	public static Double castObjectToDouble(Object obj) {
		Double value = 0D;
		if(obj instanceof BigDecimal) {
			value = ((BigDecimal)obj).doubleValue();
		} else if(obj instanceof Long) {
			value = ((Long)obj).doubleValue();
		} else if(obj != null){
			value = (Double)obj;
		} 
		return value;
	}
	
	/**
	 * 获取占比
	 * @param obj
	 * @return
	 */
	public static Double getPercent(Double num, Double total) {
		Double value;
		if(total == 0) {
			value = 0D;
		} else {
			value = setScale(num * 100 / total, 2);
		}
		return value;
	}

}
