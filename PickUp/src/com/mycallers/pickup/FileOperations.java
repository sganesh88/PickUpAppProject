package com.mycallers.pickup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;
public class FileOperations {
   public FileOperations() {
      }
   public Boolean write(String fname, String fcontent){
      try {
        String fpath = Environment.getExternalStorageDirectory()+"/"+fname;
        boolean isNew=false;
        System.out.println(fpath);
        File file = new File(fpath);
        // If file does not exists, then create it
        if (!file.exists()) {
        file.getParentFile().mkdirs();
          file.createNewFile();
          isNew=true;
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
        BufferedWriter bw = new BufferedWriter(fw);
        if(!isNew)
        	bw.append("\n");
        bw.append(fcontent);
        bw.close();
        Log.d("Success","Success");
        return true;
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
   }
   
   public Boolean writeCallLogs(String fname, String fcontent){
	      try {
	        String fpath = Environment.getExternalStorageDirectory()+"/"+fname;
	        boolean isNew=false;
	        System.out.println(fpath);
	        File file = new File(fpath);
	        // If file does not exists, then create it
	        if (!file.exists()) {
	        file.getParentFile().mkdirs();
	          file.createNewFile();
	          isNew=true;
	        }
	        FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
	        BufferedWriter bw = new BufferedWriter(fw);
	        if(!isNew)
	        	bw.append("\n\n");
	        bw.append(fcontent);
	        bw.close();
	        Log.d("Success","Success");
	        return true;
	      } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	      }
	   }
   
   public boolean deleteFile(String fname){
	   String fpath = Environment.getExternalStorageDirectory()+"/"+fname;
       
       File file = new File(fpath);
       if (file.exists()) {
	       return file.delete();
       }
       return true;
   }
   
   
   public boolean fileExists(String fname){
	   String fpath = Environment.getExternalStorageDirectory()+"/"+fname;
       
       File file = new File(fpath);
       return file.exists();
   }
   public boolean createFile(String fname){
	   try{
	   String fpath = Environment.getExternalStorageDirectory()+"/"+fname;       
       File file = new File(fpath);
       if (!file.exists()) {
	       return file.createNewFile();
       }
       return true;
	   }
	   catch(Exception e){
		   return false;
	   }
   }
   
   public Boolean writeConfig(String fname, String fcontent){
	      try {
	        String fpath = Environment.getExternalStorageDirectory()+"/"+fname;
	        
	        File file = new File(fpath);
	        if (!file.exists()) {
	        file.getParentFile().mkdirs();
	          file.createNewFile();
	        }
	        FileWriter fw = new FileWriter(file.getAbsoluteFile());
	        BufferedWriter bw = new BufferedWriter(fw);
	        
	        bw.append(fcontent);
	        bw.close();	       
	        return true;
	      } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	      }
	   }
   
   
   public Boolean removeId(String fname, String fcontent){
	   BufferedReader br = null;   
	      try {
	        String fpath = Environment.getExternalStorageDirectory()+"/"+fname;
	        
	        String response = null;
	        StringBuffer output = new StringBuffer();
	        System.out.println(fpath);
	        File file = new File(fpath);
	        if(file.exists()){
	        	boolean isNew=true;
		        br = new BufferedReader(new FileReader(fpath));
		        String line = "";
		        while ((line = br.readLine()) != null) {
		        	if(!line.equalsIgnoreCase(fcontent))
		        		if(isNew){
		        			output.append(line);
		        			isNew=false;
		        		}
		        		else
		        			output.append("\n" + line);
		        		
		        }
		        response = output.toString();
		        FileWriter fw = new FileWriter(file.getAbsoluteFile());
		        BufferedWriter bw = new BufferedWriter(fw);
		        bw.append(response);
		        bw.close();
		        Log.d("Contact id deletion","Contact id " + fcontent + " deleted");
		        }
		       return true;
	      } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	      }
	      finally {
	    	    try {
	    	        br.close();
	    	    } catch (Exception e){}
	   }
   }
   
   public int noOfLines(String fname){
	   String strFileCont=read(fname);
	   int count=0;
	   if(strFileCont!=null){
		   String[] strArr=strFileCont.split("\n");
		   for(int i=0;i<strArr.length;i++){
			   if(!strArr[i].trim().equals(""))
				   count++;
		   }
		   return count;
	   }
	   else
		   return 0;
   }
   
   public String read(String fname){
     BufferedReader br = null;
     String response = null;
      try {
        StringBuffer output = new StringBuffer();
        String fpath = Environment.getExternalStorageDirectory()+"/"+fname;
        File file = new File(fpath);
        // If file does not exists, then create it
        if (file.exists()) {
         
        br = new BufferedReader(new FileReader(fpath));
        String line = "";
        while ((line = br.readLine()) != null) {
          output.append(line +"\n");
        }
        response = output.toString();
        }
        return response; 
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
   
   finally {
 	    try {
 	        br.close();
 	    } catch (Exception e){}
   }
   }
}
