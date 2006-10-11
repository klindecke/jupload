<%@ page language="java" import="java.io.*, java.sql.*, java.util.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%
  response.setContentType("text/plain");
  try{
    // Get URL Parameters.
    Enumeration paraNames = request.getParameterNames();
    while (paraNames.hasMoreElements()) {
      String pname = (String)paraNames.nextElement();
      out.println(" ------------------------------ ");
      out.println(pname + " = " + request.getParameter(pname));
    }

    // Directory to store all the uploaded files
    String directory = "C:/Temp/";

    DiskFileUpload fu = new DiskFileUpload();
    // maximum size before a FileUploadException will be thrown
    fu.setSizeMax(10000000);
    // maximum size that will be stored in memory
    fu.setSizeThreshold(4096);
    // the location for saving data that is larger than getSizeThreshold()
    fu.setRepositoryPath(directory);
    
    List fileItems = fu.parseRequest(request);
    // assume we know there are two files. The first file is a small
    // text file, the second is unknown and is written to a file on
    // the server
    Iterator i = fileItems.iterator();
    FileItem fi = null;
    File fout = null;
    while(i.hasNext()){
      fi = (FileItem)i.next();
      if (fi.isFormField()) {
        out.println(" ------------------------------ ");
        out.println(fi.getFieldName() + " = " + fi.getString());
      } else {
        out.println(" ------------------------------ ");
        out.println("FieldName: " + fi.getFieldName());
        out.println("File Name: " + fi.getName());
        out.println("ContentType: " + fi.getContentType());
        out.println("Size (Bytes): " + fi.getSize());
        fout = new File(directory + (new File(fi.getName())).getName());
        out.println("File Out: " + fout.toString());
        // write the file
        fi.write(fout);
        
      }
    }

  }catch(Exception e){
    out.println("Exception e = " + e.toString());
  }
%>