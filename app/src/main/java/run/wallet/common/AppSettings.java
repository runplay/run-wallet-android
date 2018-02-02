package run.wallet.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import run.wallet.common.json.JSONObject;


public final class AppSettings extends BJSONBean {

	//public static final String SET_="";
    public static final String BOOL_IS_SETUP="isset";
	public static final String INT_COUNT_LAUNCH="launched";
	public static final String INT_STYLE_LIST="sty";
	public static final String INT_STYLE_LIST_BY="stylb";
    public static final String BOOL_STYLE_DARK="style";

	public static final String BOOL_USE_EMOTICONS="emo";
	public static final String BOOL_WRAP_ACTIONBAR="action_bar_wrap";
	public static final String BOOL_OVERRIDE_SMS_PROVIDER="sms_prov";
	public static final String BOOL_WARN_DELETE_SMS="del_sms";
	public static final String BOOL_WARN_DELETE_EMAIL="del_email";
	
	public static final String BOOL_BRIEF_SHOW_SMS="bs_sms";
	public static final String BOOL_BRIEF_SHOW_PHONE="bs_phone";
	public static final String BOOL_BRIEF_SHOW_CHAT="bs_chat";
	public static final String BOOL_BRIEF_SHOW_EMAIL="bs_email";
	public static final String BOOL_BRIEF_SHOW_TWITTER="bs_twitter";
	public static final String BOOL_BRIEF_SHOW_NEWS="bs_news";
    public static final String INT_BRIEF_SHOW_NEWS_STYLE="bs_news_s";
	public static final String BOOL_BRIEF_SHOW_NOTES="bs_notes";

    public static final String BOOL_VOICE_LAUNCH="voicel";
    public static final String BOOL_VOICE_INTERNAL="voicei";

    public static final String INT_KEYBOARD_HEIGHT="keybh";
    public static final String INT_KEYBOARD_HEIGHT_LANDSCAPE="keybhl";
	
	//public static final String BOOL_USE_LIVE_COMMS="bs_livecomms";
	//public static final String STRING_LOCKER_PASSWORD="lo_pss";
	public static final String LONG_LOCKER_OPEN_TIME_MILLIS="lo_open";

    public static final String STRING_FLOAT_DEF_FONT_SIZE="df_size";
    public static final String STRING_STYLE_FONT_SIZE="st_font_size";
    public static final String STRING_STYLE_FONT_FACE="st_font_face";

    public static final String BOOL_NOTIFY_SMS_SOUND="not_sms_s";
    public static final String BOOL_NOTIFY_SMS_VIBRATE="not_sms_v";
    public static final String BOOL_NOTIFY_EMAIL_SOUND="not_email_s";
    public static final String BOOL_NOTIFY_EMAIL_VIBRATE="not_email_v";
    public static final String BOOL_NOTIFY_NEWS_SOUND="not_news_s";
    public static final String BOOL_NOTIFY_NEWS_VIBRATE="not_news_v";
    public static final String BOOL_NOTIFY_REPEAT_VIBRATE="not_repeat_v";

    public static final String INT_WEBVIEW_COLOR="webcol";
    public static final String INT_WEBVIEW_SIZE="websize";

    public static final String STRING_THEME="theme";

    public static final String STRING_ALERT_TIMES="altme";

	public static final String BOOL_USE_DIRECT ="bs_p2pcomms";

    public static final String LONG_LAST_INDEX_QUICK="indq";

    public static final String INT_NEWS_DAYS_DELETE_STORIES="stodel";
    public static final String INT_NEWS_DAYS_DELETE_IMAGES="picdel";
    public static final String BOOL_NEWS_MANUAL_REFRESH="newsman";
    public static final String LONG_LAST_24HR_ARCHIVE_DELETE="lardel";

    public static final String BOOL_WEBVIEW_DISABLE_JAVASCRIPT="jsdis";
	//public static final String BOOL_USE_P2P_CONTACTS="bs_p2pcontacts";
	//public static final String BOOL_USE_P2P_ENCRYPT="bs_p2pencrypt";



	public static final String INT_ORDER_BRIEF_BY="b_ord";
	
	// db store on sdcard
	public static final String BOOL_DB_ON_SDCARD="db_sd";
	public static final String BOOL_DB_ON_SDCARD_PATH="db_sdp";
	
	// quite mode
	public static final String BOOL_QUIET_MODE="quiet";
	public static final String BOOL_QUIET_MODE_FLIGHT="quiet_flight";
	public static final String INT_QUIET_MODE_RINGER="quiet_ringer";
	
	public static final String BOOL_PROXY="prox";
	public static final String INT_PROXY_PORT="proxport";
	public static final String STRING_PROXY_IP="proxip";
	public static final String BOOL_PROXY_AUTH="proxauth";
	public static final String STRING_PROXY_USER="proxuser";
	public static final String STRING_PROXY_PASSWORD="proxpass";
	
	// auto open brief on user_present
	public static final String BOOL_BRIEF_OPEN_ON_PRESENT="open_user_present";
	
	public static final String INT_CHAT_VIEW_LAYOUT="chatvl";
	
	
	public static final int CHAT_VIEW_LAYOUT_BOTTOM_UP=0;
	public static final int CHAT_VIEW_LAYOUT_TOP_DOWN=1;
	
	public static final int STYLE_LIST_PODS=0;
	public static final int STYLE_LIST_COLOR=1;
	public static final int STYLE_LIST_PLAIN=2;
	
	public static final int STYLE_ITEM_LIST_BY_DATE=0;
	public static final int STYLE_ITEM_LIST_BY_PERSON=1;

	public static final int ORDER_BRIEF_BY_CRONOLOGICAL=0;
	public static final int ORDER_BRIEF_BY_PERSON=1;


    public static final String FONT_FACE_DEFAULT="Default";
    public static final String FONT_FACE_CAVIAR="Caviar";
    public static final String FONT_FACE_COMIC="Comic";

    public static final String FONT_SIZE_SMALL="Small";
    public static final String FONT_SIZE_MEDIUM="Medium";
    public static final String FONT_SIZE_LARGE="Large";
    public static final String FONT_SIZE_XLARGE="XLarge";

    public static final String THEME_DEFAULT="Default";
    public static final String THEME_BLUE_CLOG="BlueSoft";
    public static final String THEME_GREEN_CLOUD="GreenSoft";
    public static final String THEME_WORK_DAY="WorkDay";

    public static final String deftimes = "0,0,0,0,0,0,0,0,0,0,0,0,"
            + "0,0,1,1,2,2,2,2,2,2,2,2,"
            + "2,2,2,2,2,2,2,2,2,2,2,2,"
            + "2,2,2,2,2,2,2,2,1,1,1,1";
    public static final List<String> tztimes = new ArrayList<String>();

    private static SharedPreferences sp = null;

    static {
        tztimes.add("00:00"); tztimes.add("00:30"); tztimes.add("01:00"); tztimes.add("01:30"); tztimes.add("02:00"); tztimes.add("02:30");
        tztimes.add("03:00"); tztimes.add("03:30"); tztimes.add("04:00"); tztimes.add("04:30"); tztimes.add("05:00"); tztimes.add("05:30");
        tztimes.add("06:00"); tztimes.add("06:30"); tztimes.add("07:00"); tztimes.add("07:30"); tztimes.add("08:00"); tztimes.add("08:30");
        tztimes.add("09:00"); tztimes.add("09:30"); tztimes.add("10:00"); tztimes.add("10:30"); tztimes.add("11:00"); tztimes.add("11:30");
        tztimes.add("12:00"); tztimes.add("12:30"); tztimes.add("13:00"); tztimes.add("13:30"); tztimes.add("14:00"); tztimes.add("14:30");
        tztimes.add("15:00"); tztimes.add("15:30"); tztimes.add("16:00"); tztimes.add("16:30"); tztimes.add("17:00"); tztimes.add("17:30");
        tztimes.add("18:00"); tztimes.add("18:30"); tztimes.add("19:00"); tztimes.add("19:30"); tztimes.add("20:00"); tztimes.add("20:30");
        tztimes.add("21:00"); tztimes.add("21:30"); tztimes.add("22:00"); tztimes.add("22:30"); tztimes.add("23:00"); tztimes.add("23:30");
    }
    public Integer getTimeSlotSetting(String timeSlotHHMM) {
        int useindex=0;
        for(int i=0; i<tztimes.size(); i++) {
            String str= tztimes.get(i);
            if(str.equals(timeSlotHHMM)) {
                useindex=i;
            }

        }
        String[] splits = this.getString(STRING_ALERT_TIMES).split(",");
        return Integer.parseInt(splits[useindex]);
    }
	
	public AppSettings(Context context) {

        sp= PreferenceManager.getDefaultSharedPreferences(context);
        String settings=sp.getString("settings","1.0");

        if(settings!=null && settings.startsWith("{")) {
            JSONObject job = new JSONObject(settings);
            this.bean=new JSONObject(job);
        } else {

            bean = new JSONObject();
            bean.put(BOOL_IS_SETUP,false);
            bean.put(BOOL_USE_EMOTICONS, Boolean.TRUE);
            bean.put(BOOL_OVERRIDE_SMS_PROVIDER, Boolean.FALSE);
            bean.put(INT_COUNT_LAUNCH, 0);

            bean.put(BOOL_WARN_DELETE_SMS, Boolean.TRUE);
            bean.put(BOOL_WARN_DELETE_EMAIL, Boolean.TRUE);
            bean.put(BOOL_WRAP_ACTIONBAR, Boolean.TRUE);

            bean.put(BOOL_BRIEF_SHOW_SMS, Boolean.TRUE);
            bean.put(BOOL_BRIEF_SHOW_PHONE, Boolean.TRUE);
            bean.put(BOOL_BRIEF_SHOW_CHAT, Boolean.TRUE);
            bean.put(BOOL_BRIEF_SHOW_EMAIL, Boolean.TRUE);
            bean.put(BOOL_BRIEF_SHOW_NOTES, Boolean.TRUE);

            bean.put(BOOL_BRIEF_SHOW_NEWS, Boolean.TRUE);
            bean.put(INT_BRIEF_SHOW_NEWS_STYLE, 1);


            setString(STRING_STYLE_FONT_FACE, FONT_FACE_DEFAULT);
            setString(STRING_STYLE_FONT_SIZE, FONT_SIZE_MEDIUM);
            setString(STRING_THEME, THEME_DEFAULT);

            bean.put(BOOL_NOTIFY_SMS_SOUND, Boolean.TRUE);
            bean.put(BOOL_NOTIFY_SMS_VIBRATE, Boolean.TRUE);
            bean.put(BOOL_NOTIFY_EMAIL_SOUND, Boolean.TRUE);
            bean.put(BOOL_NOTIFY_EMAIL_VIBRATE, Boolean.TRUE);
            bean.put(BOOL_NOTIFY_NEWS_SOUND, Boolean.TRUE);
            bean.put(BOOL_NOTIFY_NEWS_VIBRATE, Boolean.TRUE);
            bean.put(BOOL_NOTIFY_REPEAT_VIBRATE, Boolean.TRUE);


            bean.put(STRING_ALERT_TIMES, deftimes);

            //bean.put(BOOL_USE_DIRECT, Boolean.TRUE);
            //bean.put(BOOL_USE_P2P_CONTACTS, Boolean.TRUE);
            bean.put(INT_STYLE_LIST, Integer.valueOf(STYLE_LIST_PODS));

        }
		
	}
	private AppSettings(JSONObject obj) {

        this.bean=obj;
        if(getString(STRING_STYLE_FONT_FACE).isEmpty())
            setString(STRING_STYLE_FONT_FACE,FONT_FACE_DEFAULT);
        if(getString(STRING_STYLE_FONT_SIZE).isEmpty())
            setString(STRING_STYLE_FONT_SIZE,FONT_SIZE_MEDIUM);
        if(getString(STRING_THEME).isEmpty())
            setString(STRING_THEME,THEME_DEFAULT);

        bean.put(STRING_ALERT_TIMES,deftimes);
	}
	public void save(Context context) {

        sp= PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString("settings",this.bean.toString()).commit();

	    //SettingsDb.Update(this);
	    //SettingsDb.Save();
	}


}
