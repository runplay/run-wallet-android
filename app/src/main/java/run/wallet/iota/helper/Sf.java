package run.wallet.iota.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
*
* @author Peter Cooper
*/
public class Sf {

    public static final String COLON_SPACE=": ";
	public static final String NEW_LINE="\n";
   static final String[] banned = {"wank ","anal ","cock ","fvk ","fuk ","fuk-","paki ","pakki "
       ,"gays ","dicks ","rape ","felch ","cum ","fkn ","tit ","nazi ","tit-","nazi-"};
   static final String[] bannedNoWhitespace = {"fuck","fvck","cunt","cvnt","tosser","wanker","yourmum","whore","slag","slut"
       ,"shithead","pussy","arsehole","nigger","mudhut"
       ,"asshole","queers","dickhead","rapist","rapeist","bastard","faggot","maggot"};
   private static final String regExpEmailValidate = "^[+_A-Za-z0-9-]+(\\.[+_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)+";
   //private static final String regExpEmailValidate = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)+";
   private static final String dotdotdot="...";

    public static int countOccourences(String in, String subString) {
        int lastIndex = 0;
        int count =0;

        while(lastIndex != -1){

            lastIndex = in.indexOf(subString,lastIndex);

            if( lastIndex != -1){
                count ++;
                lastIndex+=subString.length();
            }
        }
        return count;
    }


    public static String getDomainName(String url) {
        String domain="#";
        try {
            URI uri = new URI(url);
            domain = uri.getHost();
            domain= domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch(Exception e) {}
        return domain;
    }

   public static String shortenText(String txttoshort, int maxLength) {
	   if(maxLength<dotdotdot.length()+2)
		   return txttoshort;
	   if(txttoshort.length()<=maxLength)
		   return txttoshort;
	   
	   int cutout= Double.valueOf((maxLength-3)/2).intValue();
	   //Log.e("CUTOUT", "value: "+cutout);
	   StringBuilder sb=new StringBuilder(txttoshort.substring(0,cutout));
	   sb.append(dotdotdot);
	   sb.append(txttoshort.substring(txttoshort.length()-cutout,txttoshort.length()));
	   
	   return sb.toString();
	   
   }
   
   public static String javascriptSafe(String text) {
       if(text!=null) {
           text=Sf.replaceLineBreaks(text);
           text=text.replaceAll("'", "\\\\'");
       } else {
           return "";
       }
       return text;
   }
	public static boolean isValidEmail(String emailAddress)   {
        
        if(emailAddress!=null && emailAddress.matches(regExpEmailValidate))
            return true;
        else
            return false;
    }
	public static String stripdownToEmail(String fullEmailWithName) {
		if(fullEmailWithName!=null) {
			if(fullEmailWithName.indexOf("<")!=-1) {
				try {
				String[] sp1=fullEmailWithName.split("<");
				fullEmailWithName=sp1[1].substring(0,sp1[1].indexOf(">"));
				} catch(Exception e) {}
			}
				
			fullEmailWithName=fullEmailWithName.trim();
			//Log.e("GCONTACT", "get with email: "+spl);
			if(Sf.isValidEmail(fullEmailWithName)) {
				return fullEmailWithName;
			} 
		}
		return "";

	}

	
   public static String getIfBannedWords(String fromText) {
       List<String> bwords=getBannedWords(fromText);
       if(!bwords.isEmpty()) {
           StringBuilder words=new StringBuilder();
           String app=", ";
           boolean first=false;
           for(String w:bwords) {
               words.append(w);
               if(first)
                   first=false;
               else
                   words.append(app);
           }
           return words.toString();
       }
       return null;
   }

   public static List<String> getBannedWords(String fromText) {
       ArrayList<String> ban=new ArrayList();

       if(fromText!=null) {
           String useText=fromText;
           // this is only designed to process short messages, so if it's longer than 140 char then ignore all after 140 char and cover the first part of message.
           if(useText.length()>140)
               useText=useText.substring(0,139);

           useText=useText.toLowerCase()+" ";
           String nowhite=useText.replaceAll("[^A-Za-z0-9]", "");

           for(int i=0; i<banned.length; i++) {
               if(useText.indexOf(banned[i])!=-1) {
                   ban.add(banned[i]);
               }
           }

           for(int i=0; i<bannedNoWhitespace.length; i++) {
               if(nowhite.indexOf(bannedNoWhitespace[i])!=-1) {
                   ban.add(bannedNoWhitespace[i]);
               }
           }
       }
       return ban;
   }

   /** Creates a new instance of StringFunctions */
   public Sf() {
   }

   public static String convertStreamToString(InputStream is)
           throws IOException {
       /*
        * To convert the InputStream to String we use the
        * Reader.read(char[] buffer) method. We iterate until the
        * Reader return -1 which means there's no more data to
        * read. We use the StringWriter class to produce the string.
        */
       if (is != null) {
           Writer writer = new StringWriter();

           char[] buffer = new char[1024];
           try {
               Reader reader = new BufferedReader(
                       new InputStreamReader(is, "UTF-8"));
               int n;
               while ((n = reader.read(buffer)) != -1) {
                   writer.write(buffer, 0, n);
                                               }
           } catch(Exception e) {
               //System.out.println("convert exception: "+e.getMessage());
           } finally {
               is.close();
           }
           return writer.toString();
       } else {
           return "";
       }
   }

   public static String getPositionNumber(int position) {
       String pos="th";
       if(position%10==1 && position!=11) pos="st";
       if(position%10==2 && position!=12) pos="nd";
       else if(position%10==3 && position!=13) pos="rd";
       return position+pos;
   }
   public static String getFirstWord(String in) {
       if(in!=null && in.indexOf(" ")!=-1) {
           return in.split(" ")[0];
       } else {
           return in;
       }
   }

	public static String htmlWrap(String message) {
		StringBuilder head=new StringBuilder();
		StringBuilder foot=new StringBuilder();
		if(message.indexOf("<html")==-1) {
			head.append("<html>");
			foot.append("</html>");
		}
		if(message.indexOf("<body")==-1) {
			head.append("<body style=\"margin:0px,padding:0px\">");
			foot.append("</body>");
		} else {
			message.replace("</body>", "<style type=\"text/css\">html body{margin:0px,padding:0px}</style></body>");
		}
		return head+message+foot;
	}
   public static String htmlSafe(String in) {
       if(in!=null) {
           in=in.replaceAll("<", "&lt;");
           in=in.replaceAll(">", "&gt;");
           return in;
       } else
           return "";
   }
   public static String urlSafe(String in) {
       if(in==null)
           return "";
       else
           return in.replaceFirst(" ", "%20");
   }

    public static boolean isHtml(String message) {
        String use=message.toString();
        if(use.length()-use.replace("</", "").length()>0)
            return true;
        else if(use.length()-use.replace("&amp;", "").length()>0)
            return true;
        return false;
    }

    public static String cleanEmailText(String in) {

        in =  in.replaceAll("\n<br/>", "\n")
                .replaceAll("<br/>\n", "\n")
                .replaceAll("<br/>", "\n")
                .replaceAll("<p>", "")
                .replaceAll("</p>", "\n\n");

        return in;
    }

    public static String cleanHtml(String in) {
        in=in.replaceAll("<!\\[CDATA\\[", "")
                .replaceAll("\\]\\]>", "")
                .replaceAll("\\t", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\n", "");

        in=in.replaceAll("&amp;", "&");
        in = in.replaceAll("&#039;", "");
        in =  in.replaceAll("&quot;", "\"")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&nbsp;", ">");



        in = in.replaceAll("<!.*?>", "");
        in = in.replaceAll("<style.*?>.*?</style>", "");
        in = in.replaceAll("<script.*?>.*?</script>", "");
        in = in.replaceAll("style=\".*?\"", "");
        return in;
    }

    public static String stripIfHtml(String in) {
        if(in!=null) {
            if(isHtml(in)) {
                return stripHtml(in);
            }


            return in;
        } else {
            return "";
        }
    }

   public static String stripHtml(String in) {
       if(in!=null) {
           if(isHtml(in)) {

               in=in.replaceAll("<!\\[CDATA\\[", "")
                       .replaceAll("\\]\\]>", "")
                       .replaceAll("\\t", "")
                       .replaceAll("\\s+", " ");

               in=in.replaceAll("&amp;", "&");
               in = in.replaceAll("&#039;", "");
               in = in.replaceAll("&#160;", " ");
               in =  in.replaceAll("&quot;", "\"")
                       .replaceAll("&lt;", "<")
                       .replaceAll("&gt;", ">")
                       .replaceAll("&nbsp;", ">");


               in = in.replaceAll("<!.*?>", "")
                       .replaceAll("<head.*?>", "")
                       .replaceAll("<meta.*?>", "")
                       .replaceAll("<html.*?>", "")
                       .replaceAll("<title.*?>", "")
                       .replaceAll("</head>", "")
                       .replaceAll("</html>", "")
                       .replaceAll("</title>", "")
                       .replaceAll("<body.*?>", "")
                       .replaceAll("</body>", "")
                       .replaceAll("<ul.*?>", "")
                       .replaceAll("<li.*?>", "")
                       .replaceAll("</ul>", "\n")
                       .replaceAll("</li>", "\n")
                       .replaceAll("<b.*?>", "")
                       .replaceAll("</b>", "")
                       .replaceAll("<a.*?>", "")
                       .replaceAll("</a>", "")
                       .replaceAll("<img.*?/>", "")
                       .replaceAll("<img.*?>", "")
                       .replaceAll("<div.*?>", "")
                       .replaceAll("</div>", "")
                       .replaceAll("<font.*?>", "")
                       .replaceAll("</font>", "")
                       .replaceAll("<span.*?>", "")
                       .replaceAll("</span>", "")
                       .replaceAll("<p.*?>", "")
                       .replaceAll("</p>", "\n")
                       .replaceAll("<table.*?>", "")
                       .replaceAll("</table>", "")
                       .replaceAll("<tr.*?>", "")
                       .replaceAll("</tr>", "")
                       .replaceAll("<td.*?>", "")
                       .replaceAll("</td>", "")
                       .replaceAll("<br.*?>", "\n")
                       .replaceAll("<em.*?>", "")
                       .replaceAll("</em>", "")
                       .replaceAll("<script.*?>.*?</script>", "")
                       .replaceAll("<script.*>", "")
                       .replaceAll("</script>", "")
                       .replaceAll("<style.*?>.*?</style>", "")
                       .replaceAll("<style.*>", "")
                       .replaceAll("</style>", "")
                       .replaceAll("<link.*>", "")
                       .replaceAll("<strong.*>", "")
                       .replaceAll("</strong>", "")
                       .replaceAll("<time.*?>", "")
                       .replaceAll("</time>", "")
                       .replaceAll("<inset.*?>", "")
                       .replaceAll("</inset>", "")
                       .replaceAll("<iframe.*?>", "")
                       .replaceAll("</iframe>", "")
                       .replaceAll("&nbsp;", " ")
                       .replaceAll("&hellip;", "...");

               in.trim();
           }

           
           return in;
       } else {
           return "";
       }
   }
   public static String notNull(String in) {
       if(in==null)
           return "";
       return in;
   }
   public static String replaceLineBreaks(String instr) {
       instr = instr.replaceAll("\r\n","<br/>");
       instr = instr.replaceAll("\n","<br/>");
       instr = instr.replaceAll("\r","<br/>");
       instr = instr.replaceAll("<br/><br/>","<br/>");
       instr = instr.replaceAll("<br/><br/>","<br/>");
       return instr;
       
   }
   public static String reverseDbSafe(String makeSafe)    {
       if(makeSafe!=null)  {
           makeSafe=makeSafe.replaceAll("&#27;","'");
           return makeSafe.replaceAll("&#26;","&");
       } else  {
           return "";
       }
   }
   public static String makeDbSafe(String makeSafe)    {
       if(makeSafe!=null)  {
           //makeSafe=makeSafe.replaceAll("'","");
           //makeSafe.replaceAll(";","&#59;");
           makeSafe=makeSafe.replaceAll("&","&#26;");
           makeSafe=makeSafe.replaceAll("'","&#27;");
           return makeSafe;
       } else  {
           return "";
       }
   }
   public static String makeDbSafe(String makeSafe, int maxLength)    {
       if(makeSafe!=null)  {
           makeSafe=makeSafe.replaceAll("&","&#26;");
           makeSafe=makeSafe.replaceAll("'","&#27;");
           return restrictLength(makeSafe,maxLength);
       } else  {
           return "";
       }
   }
   public static String restrictLength(String content, int length) {
       if(content==null)
           return "";
       if(content.length()>=length)
           return content.substring(0,length);
       else
           return content;
   }
   public static String getParam(String parameter) {
       if(parameter==null)
           parameter = "";
       return parameter;
   }
   public static boolean toBoolean(String in) {
       if(in==null || in.equals("0") || in.equals("") || in.equals("false"))
           return false;
       else
           return true;
   }
   public static float toFloat(String in)  {
       float out = 0;
       try {
           out= Float.parseFloat(in);
       } catch(Exception e)    {

       }
       return out;
   }
   public static long toLong(String in)  {
       long out = 0;
       try {
           out= Long.valueOf(in).longValue();
       } catch(Exception e)    {
           
       }
       return out;
   }
   public static double toDouble(String in)  {
       double out = 0;
       try {
           out= Double.valueOf(in).doubleValue();
       } catch(Exception e)    {
           
       }
       return out;
   }
   public static int toInt(String in)  {
       int out = 0;
       try {
           out= Integer.valueOf(in).intValue();
       } catch(Exception e)    {
           
       }
       return out;
   }
   public static String removeNewLines(String content) {
       if(content!=null) {
           content = content.replaceAll("\r\n","");
           content = content.replaceAll("\n","");
           content = content.replaceAll("\r","");
       }
       return content;
   }
   public static String makeFileNameSafe(String fileName) {
       fileName = fileName.replaceAll("\n","");
       //fileName = fileName.replaceAll("[ ]+"," ");
       //fileName = fileName.replaceAll("\\\\w","_");
       fileName = fileName.replaceAll("[/\\\\ ]+","_");
       //fileName = fileName.replaceAll("\\\\W","_");
       //System.out.println("---------made safe: "+fileName);
       return fileName;
   }
}
