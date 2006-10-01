package wjhk.jupload2.upload;

/**
 * Second version of FileUploadThread : was used in Jupload V1, unused since V2.
 * 
 * URLConnection instance given by the URL class openConnection() function
 * can't handle uploading of large files.
 *
 * The reason being? URLConnection only does a post to the server after the
 * getInputStream() function is called. So anything you write to the Output
 * Stream before the getInputStream() is called will be written to memory.
 * For large files this will caused the JVM to throw an Out of Memory exception.
 *
 * With the above reason I have decided to replace the use of URLConnection
 * with sockets.
 * 
 * @deprecated Replaced by {@link wjhk.jupload.FileUploadThreadV3}
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import javax.swing.JProgressBar;

public class FileUploadThreadV2 extends Thread {

  //------------- INFORMATION --------------------------------------------
  public static final String TITLE = "JUpload FileUploadThreadV2";
  public static final String DESCRIPTION =
      "Java Thread to upload files into a web server.";
  public static final String AUTHOR = "William JinHua Kwong";

  public static final double VERSION = 2.2;
  public static final String LAST_MODIFIED = "15 April 2003";

  //------------- VARIABLES ----------------------------------------------
  // Files asked to be uploaded into the server.
  private File[] files;
  private String uploadURL;

  // Progress Bar.
  private JProgressBar progress;
  private long totalFilesLength;
  private long uploadedLength;

  // Stopping the thread.
  private boolean stop = false;

  // Server Output.
  private StringBuffer sb = new StringBuffer();

  // Thread Exception.
  private Exception e = null;

  //------------- CONSTRUCTOR --------------------------------------------
  public FileUploadThreadV2(File[] files, String uploadURL){
    this.files = files;
    this.uploadURL = uploadURL;
    totalFilesLength = 0;
    for(int i=0; i < this.files.length; i++){
      totalFilesLength += this.files[i].length();
    }
  }

  //------------- Public Functions ---------------------------------------

  // Setting Progress Panel.
  public void setProgressPanel(JProgressBar pgrBar){
    progress = pgrBar;
  }

  // Stopping the Thread
  public void stopUpload(){
    this.stop = true;
  }

  // Server Output.
  public StringBuffer getServerOutput(){
    return sb;
  }

  // Exceptions
  public Exception getException(){
    return e;
  }

  //------------- Private Functions --------------------------------------
  private StringBuffer getRandomString(){
    StringBuffer sbRan = new StringBuffer(11);
    StringBuffer alphaNum= new StringBuffer();
    alphaNum.append("1234567890abcdefghijklmnopqrstuvwxyz");
    int num;
    for(int i = 0; i < 11; i++){
      num = (int)(Math.random()* (alphaNum.length() - 1));
      sbRan.append(alphaNum.charAt(num));
    }
    return sbRan;
  }

  private void uploadFileStream(File f, DataOutputStream dOut)
                                throws FileNotFoundException,
                                IOException{
    byte[] byteBuff = null;
    FileInputStream fis = null;
    try{
      int numBytes = 0;
      byteBuff = new byte[1024];
      fis = new FileInputStream(f);
      while(-1 != (numBytes = fis.read(byteBuff)) && !stop){
        dOut.write(byteBuff, 0, numBytes);
        uploadedLength += numBytes;
        if(null != progress) progress.setValue((int)uploadedLength);
      }
    }finally{
      try{
        fis.close();
      }catch(Exception e){}
      byteBuff = null;
    }
  }

  private void addServerOutPut(String s){
    if(0 < sb.length() || !s.equals("")){
      sb.append(s);
    }
  }

  private StringBuffer[] setAllHead(File[] fileA, StringBuffer bound){
    StringBuffer[] sbArray = new StringBuffer[fileA.length];
    File file;
    StringBuffer sb;
    for(int i=0; i < fileA.length; i++){
        file = fileA[i];
        sbArray[i] = new StringBuffer();
        sb = sbArray[i];
        // Line 1.
        sb.append(bound.toString());sb.append("\r\n");
        // Line 2.
        sb.append("Content-Disposition: form-data; name=\"File");sb.append(i);
        sb.append("\"; filename=\"");sb.append(file.toString());
        sb.append("\"");sb.append("\r\n");
        // Line 3 & Empty Line 4.
        sb.append("Content-Type: application/octet-stream");
        sb.append("\r\n");sb.append("\r\n");
    }
    return sbArray;
  }

  private StringBuffer[] setAllTail(int fileLength, StringBuffer bound){
    StringBuffer[] sbArray = new StringBuffer[fileLength];
    for(int i=0; i < fileLength; i++){
      sbArray[i] = new StringBuffer("\r\n");
    }
    // Telling the Server we have Finished.
    sbArray[sbArray.length-1].append(bound.toString());
    sbArray[sbArray.length-1].append("--\r\n");
    return sbArray;
  }
  //------------- THE HEART OF THE PROGRAME ------------------------------

  /*
  private void setHeader(){
  }
  */
  
  public void run() {
    Socket sock = null;
    DataOutputStream dataout = null;
    BufferedReader datain = null;
    try{
      URL url = new URL(uploadURL);

      StringBuffer boundary = new StringBuffer();
      boundary.append("-----------------------------");
      boundary.append(getRandomString().toString());

      long contentLength = totalFilesLength;
      StringBuffer[] head = setAllHead(files, boundary);
      StringBuffer[] tail = setAllTail(files.length, boundary);
      for(int i = 0; i < files.length; i++){
        contentLength += head[i].length();
        contentLength += tail[i].length();
      }

      StringBuffer header = new StringBuffer();
      // Header: Request line
      header.append("POST ");header.append(url.getPath());
      if(null != url.getQuery() && !"".equals(url.getQuery())){
        header.append("?");header.append(url.getQuery());
      }
      header.append(" HTTP/1.1\r\n");
      // Header: General
      header.append("Host: ");
      header.append(url.getHost());header.append("\r\n");
      //header.append("Accept: */*\r\n");
      // Header: Request
      //header.append("User-Agent: Mozilla/4.0\r\n");
      // Header: Entity
      header.append("Content-type: multipart/form-data; boundary=");
      header.append(boundary.substring(2, boundary.length()) +"\r\n");
      header.append("Content-length: ");
      header.append(contentLength);header.append("\r\n");
      // Blank line
      header.append("\r\n");

      // If port not specified then use default http port 80.
      sock = new Socket(url.getHost(), (-1 == url.getPort())?80:url.getPort());
      dataout = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
      datain  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      //DataInputStream datain  = new DataInputStream(new BufferedInputStream(sock.getInputStream()));

      // Send http request to server
      uploadedLength = 0;
      if(null != progress) progress.setMaximum((int)totalFilesLength);
      dataout.writeBytes(header.toString());
      for(int i=0; i < files.length && !stop; i++){
        // Write to Server the head(4 Lines), a File and the tail.
        dataout.writeBytes(head[i].toString());
        uploadFileStream(files[i],dataout);
        dataout.writeBytes(tail[i].toString());
      }
      dataout.flush ();
      if(!stop && (null != progress))
        progress.setString("File(s) uploaded. Wait for server response!");

      String line;
      while ((line = datain.readLine()) != null) {
        this.addServerOutPut(line + "\n");
      }
    }catch(Exception e){
      this.e = e;
    }finally{
      try{
        // Throws java.io.IOException
        dataout.close();
      } catch(Exception e){}
      dataout = null;
      try{
        // Throws java.io.IOException
        datain.close();
      } catch(Exception e){}
      datain = null;
      try{
        // Throws java.io.IOException
        sock.close();
      } catch(Exception e){}
      sock = null;
    }
  }


  //------------- CLEAN UP -----------------------------------------------
  public void close(){
    files = null;
    e = null;
    sb = null;
  }
}
