package run.wallet.common;

import java.util.Date;

import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;


public abstract class BJSONBean {

	protected JSONObject bean;
	
	public BJSONBean(JSONObject bean) {
		this.bean=bean;
	}
	public BJSONBean() {
		bean=new JSONObject();
	}
	public JSONObject getBean() {
		return bean;
	}
	@Override
	public String toString() {
		return bean.toString();
	}
	public boolean has(String KEY_) {
		return bean.has(KEY_);
	}
	
	public int getInt(String KEY_) {
		//if(bean.has(KEY_))
			return bean.optInt(KEY_);
		//return 0;
	}
	public Integer getInteger(String KEY_) {
		//if(bean.has(KEY_))
			return Integer.valueOf(bean.optInt(KEY_));
		//return Integer.valueOf(0);
	}
    public void incrementInt(String KEY_) {
        //if(bean.has(KEY_))
        bean.put(KEY_, Integer.valueOf(bean.optInt(KEY_))+1);
        //return Integer.valueOf(0);
    }
	public String getString(String KEY_) {
		//if(bean.has(KEY_))
			return bean.optString(KEY_);
		//return "";
	}
	public double getDouble(String KEY_) {
		//if(bean.has(KEY_))
			return bean.optDouble(KEY_);
		//return 0.0D;
	}
	public Boolean getBoolean(String KEY_) {
		//if(bean.has(KEY_))
			return bean.optBoolean(KEY_);
		//return 0.0D;
	}
	public long getLong(String KEY_) {
		//if(bean.has(KEY_))
			return bean.optLong(KEY_);
		//return 0L;
	}
	public Date getLongDate(String KEY_) {
		//if(bean.has(KEY_))
			return new Date(bean.optLong(KEY_));
		//return 0L;
	}
	public Date getLongCal(String KEY_) {
		//if(bean.has(KEY_))
			return new Date(bean.optLong(KEY_));
		//return 0L;
	}
	public JSONArray getJSONArray(String KEY_) {
		//if(bean.has(KEY_))
			return bean.optJSONArray(KEY_);
		//return new JSONArray();
	}
	
	public void setInt(String KEY_, int value) {
		bean.put(KEY_, value);
	}
	public void setString(String KEY_, String value) {
		bean.put(KEY_, value);
	}
	public void setDouble(String KEY_, double value) {
		bean.put(KEY_, value);
	}
	public void setLong(String KEY_, long value) {
		bean.put(KEY_, value);
	}
	public void setBoolean(String KEY_, Boolean value) {
		bean.put(KEY_, value);
	}
	public void setJSONArray(String KEY_, JSONArray value) {
		bean.put(KEY_, value);
	}
}
