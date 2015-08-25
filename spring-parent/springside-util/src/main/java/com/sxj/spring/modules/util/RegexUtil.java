package com.sxj.spring.modules.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

	public static String getFirstMatchedStrs(String input, String regex) {
		if (input == null) {
			return null;
		}
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		while (m.find()) {
			return m.group();
		}
		return null;
	}

	/**
	 * 获取匹配的字符串列表
	 * 
	 * @param input
	 * @param regex
	 * @return
	 */
	public static List<String> getMatchedStrs(String input, String regex) {
		if (input == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		while (m.find()) {
			String str = m.group();
			list.add(str.trim());
		}
		return list;
	}

	/**
	 * 字符串截取
	 * 
	 * @param input
	 *            输入源
	 * @param start
	 *            开始位置
	 * @param end
	 *            结束位置
	 * @return
	 */
	public static String substr(String input, String start, String end) {
		
		
		if (input == null) {
			return null;
		}
		
		int index = input.indexOf(start);
		int index2 = input.indexOf(end, index + start.length());
		if (index == -1 || index2 == -1 || index >= index2) {
			return null;
		}

		return input.substring(index + start.length(), index2);
	}

}
