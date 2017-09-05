package com.wilddog.conversation.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PingYinUtil {
	/**
	 * 将字符串中的中文转化为拼音,其他字符不变
	 * 
	 * @param inputString
	 * @return
	 */
	public static String getPingYin(String inputString) {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();
		String output = "";

		try {
			for (int i = 0; i < input.length; i++) {
				if (java.lang.Character.toString(input[i]).matches(
						"[\\u4E00-\\u9FA5]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(
							input[i], format);
					output += temp[0];
				} else
					output += java.lang.Character.toString(input[i]);
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * 汉字转换位汉语拼音首字母，英文字符不变
	 * 
	 * @param chines
	 *            汉字
	 * @return 拼音
	 */
	public static String converterToFirstSpell(String chines) {
		String pinyinName = "";
		char[] nameChar = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			if (nameChar != null && nameChar.length != 0) {
				//处理特殊符号，例如：￥哈哈
				if(i==0){
					if (java.lang.Character.toString(nameChar[i]).matches("[\\u4E00-\\u9FA5]+")) {
					} else{
						if(Character.isUpperCase(nameChar[i])|Character.isLowerCase(nameChar[i])){}else{
							return "#";
						}
					}
				}
				if (nameChar[i] > 128) {
					// 是汉字
					try {
						String[] pinyinNameStart = PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat);
						if (pinyinNameStart != null&&pinyinNameStart.length != 0)
							pinyinName = pinyinName + pinyinNameStart[0].charAt(0);

					} catch (BadHanyuPinyinOutputFormatCombination e) {
						e.printStackTrace();
					}
				} else if (Character.isUpperCase(nameChar[i])) {
					pinyinName += nameChar[i];
				} else if (Character.isLowerCase(nameChar[i])) {
					pinyinName += (char) (nameChar[i] - 32) + "";
				}
			}
		}
		if (pinyinName == ""){
			pinyinName = "#";
		}
		return pinyinName;
	}
}
