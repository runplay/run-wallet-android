/*
 * TextFile.java
 *
 * Created on 18 January 2008, 14:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package run.wallet.common;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Peter Cooper
 */
public class TextFile {
    
    public TextFile() {
    }
    
    public static void deleteFile(String filePath) {
        File f = new File(filePath);
        if(f.exists())
            f.delete();
    }
    
    public static void writeToFile(String filePath, String content) {
        try {
            File f=new File(filePath);
            if(!f.exists()) {
                //f.mkdirs();
                f.createNewFile();
            }
            FileWriter fw=new FileWriter(filePath);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(content);
            out.flush();
            out.close();
            fw.flush();
            fw.close();
        } catch (IOException e) {
        }
    }
    public static String getClasspathFileContent(String file)   {

        StringBuffer contents = new StringBuffer();

        //declared here only to make visible to finally clause
        BufferedReader input = null;
        try {
          input = new BufferedReader( new InputStreamReader(TextFile.class.getResourceAsStream(file)) );
          String line = null;
          while (( line = input.readLine()) != null){
            contents.append(line);
            contents.append(System.getProperty("line.separator"));
          }
        }
        catch (FileNotFoundException ex) {
          ex.printStackTrace();
        }
        catch (IOException ex){
          ex.printStackTrace();
        }
        finally {
          try {
            if (input!= null) {
              //flush and close both "input" and its underlying FileReader
              input.close();
            }
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
        return contents.toString();
    }
  static public String getFileContent(String file) {

	  StringBuilder contents = new StringBuilder();

    //declared here only to make visible to finally clause
    BufferedReader input = null;
    try {
      input = new BufferedReader( new FileReader(file) );
      String line = null; 
      while (( line = input.readLine()) != null){
        contents.append(line);
        contents.append(System.getProperty("line.separator"));
      }
    }
    catch (FileNotFoundException ex) {
      //ex.printStackTrace();
    }
    catch (IOException ex){
      //ex.printStackTrace();
    }
    finally {
      try {
        if (input!= null) {
          //flush and close both "input" and its underlying FileReader
          input.close();
        }
      }
      catch (IOException ex) {
        //ex.printStackTrace();
      }
    }
    return contents.toString();
  }
  public static String getAssetFileContent(Context context, String filename) {
	  BufferedReader reader = null;
	  StringBuilder sb=new StringBuilder();
	  try {
	      reader = new BufferedReader(
	          new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

	      // do reading, usually loop until end of file reading  
	      String mLine = reader.readLine();
	      while (mLine != null) {
	         sb.append(mLine);
	    	  mLine = reader.readLine();
	      }
	  } catch (IOException e) {
	      //log the exception
	  } finally {
	      if (reader != null) {
	           try {
	               reader.close();
	           } catch (IOException e) {
	               //log the exception
	           }
	      }
	  }
	  return sb.toString();
  }
}
