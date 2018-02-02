package run.wallet.iota.model;

import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;

import java.util.Date;


public abstract class JsonBean {

	protected JSONObject bean;

	protected JsonBean() {
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

	public JSONArray getJSONArray(String KEY_) {
		//if(bean.has(KEY_))
			return bean.optJSONArray(KEY_);
		//return new JSONArray();
	}
	
	public void setInt(String KEY_, int value) {
		try{bean.put(KEY_, value);}catch (Exception e){}
	}
	public void setString(String KEY_, String value) {
		try{bean.put(KEY_, value);}catch (Exception e){}
	}
	public void setDouble(String KEY_, double value) {
		try{bean.put(KEY_, value);}catch (Exception e){}
	}
	public void setLong(String KEY_, long value) {
		try{bean.put(KEY_, value);}catch (Exception e){}
	}
	public void setBoolean(String KEY_, Boolean value) {
		try{bean.put(KEY_, value);}catch (Exception e){}
	}
	public void setJSONArray(String KEY_, JSONArray value) {
		try{bean.put(KEY_, value);}catch (Exception e){}
	}
}
