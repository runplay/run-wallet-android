package run.wallet.common;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.GregorianCalendar;
//import org.eclipse.jdt.internal.compiler.ast.ThisReference;

/**
 *
 * @author Peter Cooper
 */
public class Cal extends GregorianCalendar {

    public static final long DAYS_60_IN_MILLIS=5184000000L;
    public static final long DAYS_40_IN_MILLIS=3456000000L;
    public static final long DAYS_30_IN_MILLIS=2592000000L;
    public static final long DAYS_7_IN_MILLIS=604800000L;

    public static final long HOURS_24_IN_MILLIS=86400000L;
    public static final long HOURS_1_IN_MILLIS=3600000L;

    public static final long MINUTES_1_IN_MILLIS=60000L;
    
    DecimalFormat df = new DecimalFormat( "#00" );
    
    public static Cal getCal(Date fromDate) {
        return new Cal(fromDate);
    }
    public static Cal getCal(long fromDate) {
        return new Cal(fromDate);
    }
    public static Cal getCal() {
        return new Cal();
    }
    public static long getUnixTime() {
    	return (new Date()).getTime();
    }

    public static Date toDate(long millis) {
        return new Date(millis);
    }
    public static String friendlyComebackDate(Date lastActiveDate, Date now, long COME_BACK_TIME_IN_MILLIS) {
        if(lastActiveDate==null) {
            return "Whenever";
        } else {
            long t = (lastActiveDate.getTime()+COME_BACK_TIME_IN_MILLIS-now.getTime());
            if(t>3600000)
                return Double.valueOf(t/3600000).intValue()+"h";
            else if(t<60000)
                return Double.valueOf(t/1000).intValue()+"s";
            else
                return Double.valueOf(t/60000).intValue()+"m";
        }
    }
    public String friendlyReadDate() {
        return Cal.friendlyReadDate(this);
    }
    public static String friendlyReadDate(Cal din) {
        String frd="";
        if(din==null) {
            frd="?";
        } else {
            if(din.isWithin24hrs()) {
                //Cal now=new Cal();
                long mili = Cal.getUnixTime()-din.getTimeInMillis();
                mili=(mili/1000)/60;
                if(mili>59) {
                    mili=(mili/60);
                    if(mili<2)
                        frd=mili+"h";
                    else
                        frd=mili+"h";
                } else {
                    if(mili<1)
                        frd="now";
                    else if(mili<2)
                        frd=mili+"m";
                    else
                        frd=mili+"m";
                }

            } else {
                if(din.getDaysInPast()>430) {
                    frd = "not used";
                } else {
                    frd = din.getDaysInPast() + "d";
                }
            }
        }
        return frd;
    }
    
    public Cal()    {
        super();
        this.setTimeInMillis((new Date()).getTime());

    }
    public Cal(Date date) {
        super();
        this.setTimeInMillis(date.getTime());
    }
    public Cal(long unixtime) {
    	super();
    	this.setTimeInMillis(unixtime);
    	
    }
    public static int getAge(Cal age) {
        Cal today = new Cal();
        today.set(today.YEAR,today.get(today.YEAR) - age.get(age.YEAR));
        today.set(today.DAY_OF_YEAR,today.get(today.DAY_OF_YEAR) - age.get(age.DAY_OF_YEAR));
        return today.get(today.YEAR);
    }
    public Cal(int year, int month, int day, int hour, int min, int secs)   {
        super();
        this.set(year,month,day,hour,min,secs);
    }
    public Cal(int year, int month, int day)   {
        super();
        this.set(year,month,day);
    }
    public Cal(String date) {
        //2007-11-17 17:40:29
        super();
        //System.out.println("generating date for: "+date);
        if(date!=null) {
        String[] sp1 = date.split(" ");
        String[] sp2 = sp1[0].split("-");
        String[] sp3 = sp1[1].split(":");
        int month = Integer.parseInt(sp2[1]);
       
        this.set(Integer.parseInt(sp2[0]),--month, Integer.parseInt(sp2[2])
            , Integer.parseInt(sp3[0]), Integer.parseInt(sp3[1]));
        }
    }
    public String getMonth() {
        return getMonth(0);
    }
    public String getTimeHHMM() {
        String hour = this.get(this.HOUR_OF_DAY) < 10?"0"+this.get(this.HOUR_OF_DAY):""+this.get(this.HOUR_OF_DAY);
        String mins = this.get(this.MINUTE) < 10?"0"+this.get(this.MINUTE):""+this.get(this.MINUTE);
        return hour + ":"+ mins;
    }
    public String getTimeSlotHHMM() {
        String hour = this.get(this.HOUR_OF_DAY) < 10?"0"+this.get(this.HOUR_OF_DAY):""+this.get(this.HOUR_OF_DAY);
        int useMins=0;
        if(this.get(this.MINUTE)>29)
            useMins=30;
        String mins = useMins < 10?"0"+useMins:""+useMins;
        return hour + ":"+ mins;
    }
    public String getTimeMMSS() {
        String seconds = this.get(this.SECOND) < 10?"0"+this.get(this.SECOND):""+this.get(this.SECOND);
        String mins = this.get(this.MINUTE) < 10?"0"+this.get(this.MINUTE):""+this.get(this.MINUTE);
        return mins + ":"+ seconds;
    }
    public static String getMonth(int monthNum, int style) {
        String[][] dows = {
            {"","January","February","March","April","May","June","July","August","September","October","November","December"},
            {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"},
            {"","J","F","M","A","M","J","J","A","S","O","N","D"}
        };
        return dows[style][monthNum];
    }
    public static String getDOW(int dayNum, int style) {
        String[][] dows = {
            {"","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"},
            {"","Sun","Mon","Tue","Wed","Thu","Fri","Sat"},
            {"","S","M","T","W","T","F","S"}
        };
        return dows[style][dayNum];
    }
    public String getMonth(int style) {
        String[][] dows = {
            {"January","February","March","April","May","June","July","August","September","October","November","December"},
            {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"},
            {"J","F","M","A","M","J","J","A","S","O","N","D"}
        };
        String DOW = "";
        switch(this.get(this.MONTH)) {
            case 0:
                DOW = dows[style][0]; break;
            case 1:
                DOW = dows[style][1]; break;
            case 2:
                DOW = dows[style][2]; break;
            case 3:
                DOW = dows[style][3]; break;
            case 4:
                DOW = dows[style][4]; break;
            case 5:
                DOW = dows[style][5]; break;
            case 6:
                DOW = dows[style][6]; break;
            case 7:
                DOW = dows[style][7]; break;
            case 8:
                DOW = dows[style][8]; break;
            case 9:
                DOW = dows[style][9]; break;
            case 10:
                DOW = dows[style][10]; break;
            case 11:
                DOW = dows[style][11]; break;
        }
        return DOW;
    }
    public String getDOW() {
        return getDOW(0);
    }
    public String getDOW(int style) {
        String[][] dows = {
            {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"},
            {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"},
            {"S","M","T","W","T","F","S"}
        };
        String DOW = "";
        switch(this.get(this.DAY_OF_WEEK)) {
            case 1:
                DOW = dows[style][0]; break;
            case 2:
                DOW = dows[style][1]; break;
            case 3:
                DOW = dows[style][2]; break;
            case 4:
                DOW = dows[style][3]; break;
            case 5:
                DOW = dows[style][4]; break;
            case 6:
                DOW = dows[style][5]; break;
            case 7:
                DOW = dows[style][6]; break;
        }
        return DOW;
    }
    public void setJsCalendarDate(String jsdate, String jstime) {
        if(jsdate!=null && jstime!=null) {
            String[] sd = jsdate.split("-");
            this.set(this.YEAR,Sf.toInt(sd[0]));
            this.set(this.MONTH,Sf.toInt(sd[1])-1);
            this.set(this.DAY_OF_MONTH,Sf.toInt(sd[2]));
            String[] st = jstime.split(":");
            this.set(this.HOUR_OF_DAY,Sf.toInt(st[0]));
            this.set(this.MINUTE,Sf.toInt(st[1]));
        }
    }
    public void setJsCalendarDate(String jsdate) {
        if(jsdate!=null && !jsdate.equals("")) {
            String[] sd = jsdate.split("-");
            this.set(this.YEAR,Sf.toInt(sd[0]));
            this.set(this.MONTH,Sf.toInt(sd[1])-1);
            this.set(this.DAY_OF_MONTH,Sf.toInt(sd[2]));
        }
    }
    public String getJsCalendarDate() {
        
        return this.get(this.YEAR) + "-" + df.format(this.getRealMonth()) + "-"+this.get(this.DAY_OF_MONTH);
    }
    public long getMathCalendarDate() {
        String strMathMake = this.get(this.YEAR) + "" + df.format(this.getRealMonth()) + ""+this.get(this.DAY_OF_MONTH) + ""+this.get(this.HOUR_OF_DAY) + ""+this.get(this.MINUTE) + ""+this.get(this.SECOND);
        //System.out.println("date:"+strMathMake);
        return Sf.toLong(strMathMake);
    }
    public long getMathCalendarTruncDate() {
        String strMathMake = this.get(this.YEAR) + "" + df.format(this.getRealMonth()) + ""+this.get(this.DAY_OF_MONTH);
        return Sf.toLong(strMathMake);
    }

    public boolean isToday()    {
        Cal today = new Cal(Cal.getUnixTime());
        if(today.get(today.YEAR) == this.get(this.YEAR) && today.get(today.DAY_OF_YEAR) == this.get(this.DAY_OF_YEAR))
        //if(today.getDaysInPast()==0)
            return true;
        else return false;
    }
    public boolean isWithin24hrs()    {
        Cal today = new Cal(Cal.getUnixTime());
        if(this.getTimeInMillis()>today.getTimeInMillis()-Cal.HOURS_24_IN_MILLIS)
            return true;
        else return false;
    }
    public boolean isCurrentWeek()    {
        Cal today = new Cal();
        if(today.get(today.WEEK_OF_YEAR) == this.get(this.WEEK_OF_YEAR))
            return true;
        else return false;
    }
    public boolean isCurrentMonth()    {
        Cal today = new Cal();
        if(today.get(today.MONTH) == this.get(this.MONTH))
            return true;
        else return false;
    }
    /** Creates a new instance of PtGregorianCalendar */

    public int getRealMonth()   {
        return this.get(this.MONTH)+1;
    }
    public String getStringDatePlusMonths(int plusMonths)  {
        this.set(this.MONTH,this.get(this.MONTH)+plusMonths);
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        String smonth = new Integer(month).toString();
        String sday = new Integer(day).toString();
        if(day<10)
            sday = "0"+sday;
        if(month<10)
            smonth="0"+smonth;
        
        return sday +" / "+ smonth +" / "+year;
    }
    public String getNextPaypalDate(int plusMonths)  {
        this.set(this.MONTH,this.get(this.MONTH)+plusMonths);
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        String smonth = new Integer(month).toString();
        String sday = new Integer(day).toString();
        if(day<10)
            sday = "0"+sday;
        if(month<10)
            smonth="0"+smonth;
        
        return smonth + sday +""+year;
    }
    public String getNextPaypalDate()  {
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        month++;
        if(month > 12){
            month=1;
            year++;
        }
        String smonth = new Integer(month).toString();
        String sday = new Integer(day).toString();
        if(day<10)
            sday = "0"+sday;
        if(month<10)
            smonth="0"+smonth;
        
        return smonth + sday +""+year;
    }
    public String getPaypalDate()  {
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        String smonth = new Integer(month).toString();
        String sday = new Integer(day).toString();
        if(day<10)
            sday = "0"+sday;
        if(month<10)
            smonth="0"+smonth;
        
        return smonth + sday +""+year;
    }
    public String getDatabaseTruncDate()  {
        //DecimalFormat df = new DecimalFormat( "#00" );
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        return year + "-" + df.format(month) + "-"+ df.format(day);      
    }
    public int getDaysInPast()  {
        int daysInPast = 0;
        //System.out.println("start days in past:"+this.isPastDate());
        //boolean ispastdate = this.isPastDate();
        if(this.isPastDate()) {
            //System.out.println("is past date");
            Cal today = new Cal();
            int todayDay = today.get(today.DAY_OF_YEAR);
            int thisDay = this.get(this.DAY_OF_YEAR);
            if(today.get(today.YEAR)>this.get(this.YEAR)) {
                int tmp = 365-thisDay;
                daysInPast = tmp + todayDay;
            } else {
                daysInPast = todayDay - thisDay;
            }
        }
        //System.out.println("days in past for: "+this.getStringTruncDate()+" is "+daysInPast);
        return daysInPast;
    }
    public int getMathDate()  {
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        String strDay = "00";
        String strMonth = "00";
        if(day<10)
            strDay = "0"+day; 
        else
            strDay = Integer.toString(day);
        if(month<10)
            strMonth = "0"+month;
        else
            strMonth = Integer.toString(month);
        String concat = year + "" + strMonth + ""+ strDay;
        int mathDate = 0;
        try {
            mathDate = Integer.parseInt(concat);
        } catch(NumberFormatException e)    {
            /// nothing needed
        }
        return mathDate;         
    }
    public String getTruncConcatDate()  {
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        return year + "" + month + ""+ day;         
    }
    public String getConcatDate()  {
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        int hour = this.get(this.HOUR_OF_DAY);
        int min = this.get(this.MINUTE);
        int sec = this.get(this.SECOND);
        return year + "" + month + ""+ day + "-" +hour+""+min+""+sec;         
    }
    public String getDatabaseDate()  {
        //DecimalFormat df = new DecimalFormat( "#00" );
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        int hour = this.get(this.HOUR_OF_DAY);
        int min = this.get(this.MINUTE);
        int sec = this.get(this.SECOND);
        return year + "-" + df.format(month) + "-"+ df.format(day) + " " +df.format(hour)+":"+df.format(min)+":"+df.format(sec);         
    }
    public String getDatabaseDateYYYYMMDDHH()  {
        //DecimalFormat df = new DecimalFormat( "#00" );
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        int hour = this.get(this.HOUR_OF_DAY);
        return year + "" + df.format(month) + ""+ df.format(day) + "" +df.format(hour);
    }
    public String getStringTruncDate()  {
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        String strDay = "00";
        String strMonth = "00";

        return  df.format(day)+ " / " + df.format(month) + " / "+ year;
                
    }
    public boolean isFutureDate()    {
        //Cal today = new Cal();
        // make a random call to ensure that the Calendar time is set, if not this function will sometimes not work, mad but there you go, this caused me a massive headache
        //String tmp = this.getStringTruncDate();
        if(this.getTimeInMillis() > Cal.getUnixTime())
           return true;
        else
            return false;
    }  
    public boolean isPastDate()    {
        //Cal today = new Cal();
        // make a random call to ensure that the Calendar time is set, if not this function will sometimes not work, mad but there you go, this caused me a massive headache
        //this.get(this.YEAR);
        //today.get(today.YEAR);
        //String tmp = this.getStringTruncDate();
        if(this.getTimeInMillis() < Cal.getUnixTime())
           return true;
        else
           return false;
    }
    public boolean isExpiredDate()    {
        Cal today = new Cal();
        if(this.get(this.YEAR)> today.get(today.YEAR))
            return false;
        if(this.get(this.MONTH)> today.get(today.MONTH))
            return false;
        if(this.get(this.DAY_OF_MONTH)>today.get(today.DAY_OF_MONTH))
            return false;
        if(this.get(this.HOUR)> today.get(today.HOUR))
            return false;
        if(this.get(this.MINUTE)> today.get(today.MINUTE))
            return false;
        if(this.get(this.SECOND)> today.get(today.SECOND))
            return false;
        return true;
    }
    public Cal clone()  {
        Cal cloned = new Cal(this.get(this.YEAR),this.get(this.MONTH),this.get(this.DAY_OF_MONTH)
                                ,this.get(this.HOUR_OF_DAY), this.get(this.MINUTE), this.get(this.SECOND));
        //int fdow = this.getFirstDayOfWeek();
        //cloned.setFirstDayOfWeek(fdow);
        return cloned;
    }
    public String getStringDate()  {
        int day = this.get(this.DAY_OF_MONTH);
        int month = this.getRealMonth();
        int year = this.get(this.YEAR);
        int hour = this.get(this.HOUR_OF_DAY);
        int min = this.get(this.MINUTE);
        int sec = this.get(this.SECOND);
        return df.format(day) + "/" + df.format(month) + "/"+ year + " " +df.format(hour)+":"+df.format(min)+":"+df.format(sec);         
    }
    
}
