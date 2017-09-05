package com.wilddog.conversation.utils;

import com.wilddog.conversation.bean.UserInfo;

import java.util.Comparator;

public class PinyinComparator implements Comparator {

	@Override
	public int compare(Object arg0, Object arg1) {
		// 按照名字排序
		UserInfo user0 = (UserInfo) arg0;
		UserInfo user1 = (UserInfo) arg1;
		String catalog0 = "";
		String catalog1 = "";

		if (user0 != null && user0.getNickname() != null
				&& user0.getNickname().length() > 1)
			catalog0 = PingYinUtil.converterToFirstSpell(user0.getNickname())
					.substring(0, 1);

		if (user1 != null && user1.getNickname() != null
				&& user1.getNickname().length() > 1)
			catalog1 = PingYinUtil.converterToFirstSpell(user1.getNickname())
					.substring(0, 1);
		if(catalog0.contains("#")&&!catalog1.contains("#")){
			return 1;
		}else if (catalog1.contains("#")&&!catalog0.contains("#")){
			return -1;
		}else{
			return catalog0.compareToIgnoreCase(catalog1);
		}


	}

}
