package run.wallet.common;


import run.wallet.common.json.JSONObject;

public class UserStats extends BJSONBean {
	public static final String INT_COUNT_LAUNCH="lnch";
	public static final String LONG_DATE_STARTED="star";
	public static final String LONG_DATE_NEWS_LAST="news";
	
	public UserStats(JSONObject obj) {
		this.bean=obj;
	}
	public UserStats() {
		super();
	}
	
}
