package wjhk.jupload;

import java.io.*;
import java.net.*;
import java.util.*;

public class FileUploadThread extends Thread {

  //------------- INFORMATION --------------------------------------------
  public static final double VERSION = 1.0;
  public static final String AUTHOR = "William Kwong Jinhua";
  public static final String AUTHOREMAIL = "wjhkwong@yahoo.com";
  public static final String DESCRIPTION = "Java Thread to upload files into a web server.";
  public static final String LASTMODIFIED = "01 July 2002";

  //------------- STATUS OF THREAD ---------------------------------------

  // For synchronise.
  private boolean perChanged = false;
  // Default timeout is 1000 milliseconds (1 Second).
  public static final long DEFAULTSYNCTIMEOUT = 1000;
  private long syncTimeOut = DEFAULTSYNCTIMEOUT;

  // Files asked to be uploaded into the server.
  private File[] aTotalFiles;
  public File[] getTotalFiles(){
    return aTotalFiles;
  }
  public int getNumTotalFiles(){
    return aTotalFiles.length;
  }

  public long getTotalFilesLength(){
    long length = 0;
    for(int i=0; i < aTotalFiles.length; i++){
      length += aTotalFiles[i].length();
    }
    return length;
  }

  private String uploadURL;
  public String getUploadURL(){
    return uploadURL;
  }

  // Setting timeout variable.
  public void setSyncTimeOut(long milliseconds){
    syncTimeOut = milliseconds;
  }
  public long getSyncTimeOut(){
    return syncTimeOut;
  }

  // File Currently being uploaded.
  private File fileUploading;
  public synchronized File getFileUploading(){
    while (!perChanged && this.isAlive()) {
      try {
        // Wait for value to Change. 1000 mi
        wait(syncTimeOut);
      } catch (InterruptedException e) {}
    }
    return fileUploading;
  }

  // How many Bytes of the current file being uploaded.
  private long uploadedBytes = 0;
  private synchronized void setUploadedBytes(long bytes){
    uploadedBytes = bytes;
    perChanged = true;
    notifyAll();
  }

  public synchronized long getUploadedBytes(File f){
    while (!perChanged && this.isAlive()) {
      try {
        // Wait for value to Change.
        wait(syncTimeOut);
      } catch (InterruptedException e){
      }
    }
    long l = 0;
    if(null == f){
    }else if(f.equals(fileUploading)){
      l = uploadedBytes;
    }else if(vUploadedFiles.contains(f)){
      l = f.length();
    }
    perChanged = false;
    return l;
  }


  // Files that have already been uploaded into the server.
  private Vector vUploadedFiles = new Vector();
  private void addUploadedFiles(File f){
    vUploadedFiles.add(f);
  }
  public File[] getUploadedFiles(){
    File[] f = new File[vUploadedFiles.size()];
    vUploadedFiles.copyInto(f);
    return f;
  }
  public int getNumUploadedFiles(){
    return vUploadedFiles.size();
  }
  public long getUploadedFilesLength(){
    long length = 0;
    for(int i=0; i < vUploadedFiles.size(); i++){
      length += ((File) vUploadedFiles.get(i)).length();
    }
    return length;
  }

  // Server Output.
  private StringBuffer sb = new StringBuffer();
  private void addServerOutPut(String s){
    if(0 < sb.length() || !s.equals("")){
      sb.append(s);
    }
  }
  public StringBuffer getServerOutput(){
    return sb;
  }

  private Exception e = null;
  public Exception getException(){
    return e;
  }


  //------------- CONSTRUCTOR --------------------------------------------

  public FileUploadThread(File[] files, String uploadURL){
    aTotalFiles = files;
    this.uploadURL = uploadURL;
  }

  //------------- CLEAN UP -----------------------------------------------
  public void close(){
    aTotalFiles = null;

    e = null;

    fileUploading = null;

    sb = null;

    vUploadedFiles.clear();
    vUploadedFiles = null;
  }

  //------------- THE HEART OF THE PROGRAME ------------------------------

  public void run() {
    URL url = null;
    URLConnection urlConn = null;
    DataOutputStream dOut = null;
    BufferedReader bInp = null;
    try{
      url = new URL(uploadURL);
      // Setting up the connection for upload.
      urlConn = url.openConnection();
      urlConn.setDoInput (true);
      urlConn.setDoOutput (true);
      urlConn.setUseCaches (false);
      String boundary = "-----------------------------" + getRandomString();
      urlConn.setRequestProperty("Content-Type",
                                 "multipart/form-data; boundary=" +
                                 boundary.substring(2, boundary.length()));
      String CRLF = "\r\n";

      // Retrieve OutputStream For upload (Post).
      dOut = new DataOutputStream(urlConn.getOutputStream());

      // Actual Uploading part.
      StringBuffer sb;
      File f;
      for(int i=0; i < aTotalFiles.length; i++){
        f = aTotalFiles[i];
        fileUploading = f;
        this.setUploadedBytes(0);
        sb = new StringBuffer();
        // Line 1.
        sb.append(boundary);sb.append(CRLF);
        // Line 2.
        sb.append("Content-Disposition: form-data; name=\"File");sb.append(i);
        sb.append("\"; filename=\"");sb.append(f.toString());
        sb.append("\"");sb.append(CRLF);
        // Line 3 & Empty Line 4.
        sb.append("Content-Type: application/octet-stream");
        sb.append(CRLF);sb.append(CRLF);
        // Write to Server the 4 Lines, a File and the CRLF.
        dOut.writeBytes(sb.toString());
        uploadFileStream(f,dOut);
        vUploadedFiles.add(f);
        dOut.writeBytes(CRLF);
        this.setUploadedBytes(f.length());
      }
      // Telling the Server we have Finished.
      dOut.writeBytes(boundary);dOut.writeBytes("--");dOut.writeBytes(CRLF);
      dOut.flush ();

      // Reading input from Server.
      bInp = new BufferedReader(new InputStreamReader(urlConn.getInputStream ()));
      String str;
      while (null != ((str = bInp.readLine()))){
        this.addServerOutPut(str);
      }

    }catch(Exception e){
      this.e = e;
    }finally{
      try{
        bInp.close();
      }catch(Exception e){}
      bInp = null;
      try{
        dOut.close();
      }catch(Exception e){}
      dOut = null;
      urlConn = null;
      url = null;
    }
  }

  private String getRandomString(){
    String alphaNum="1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    StringBuffer sbRan = new StringBuffer(11);
    int num;
    for(int i = 0; i < 11; i++){
      num = (int)(Math.random()* (alphaNum.length() - 1));
      sbRan.append(alphaNum.substring(num, num+1));
    }
    return sbRan.toString();
  }

  private void uploadFileStream(File f, DataOutputStream dOut)
                                throws FileNotFoundException,
                                IOException{
    int totalBytes = 0;
    byte[] byteBuff = null;
    FileInputStream fis = null;
    try{
      int numBytes = 0;
      byteBuff = new byte[1024];
      fis = new FileInputStream(f);
      while(-1 != (numBytes = fis.read(byteBuff))){
        dOut.write(byteBuff, 0, numBytes);
        totalBytes += numBytes;
        this.setUploadedBytes(totalBytes);
      }
    }finally{
      try{
        fis.close();
      }catch(Exception e){}
      byteBuff = null;
    }
  }
}