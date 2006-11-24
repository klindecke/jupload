<%@ page language="java" import="java.io.*, java.sql.*, java.util.*" %>
<%@ page import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.disk.*, org.apache.commons.fileupload.servlet.*" %>
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
    String ourTempDirectory = "/tmp/";
    int ourMaxMemorySize  = 10000000;
    int ourMaxRequestSize = 2000000000;

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//The code below is directly taken from the jakarta fileupload common classes
	//All informations, and download, available here : http://jakarta.apache.org/commons/fileupload/
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// Create a factory for disk-based file items
	DiskFileItemFactory factory = new DiskFileItemFactory();
	
	// Set factory constraints
	factory.setSizeThreshold(ourMaxMemorySize);
	factory.setRepository(new File(ourTempDirectory));
	
	// Create a new file upload handler
	ServletFileUpload upload = new ServletFileUpload(factory);
	
	// Set overall request size constraint
	upload.setSizeMax(ourMaxRequestSize);
	
	// Parse the request
	List /* FileItem */ items = upload.parseRequest(request);
	// Process the uploaded items
	Iterator iter = items.iterator();
	FileItem fileItem;
    File fout;
	while (iter.hasNext()) {
	    fileItem = (FileItem) iter.next();
	
	    if (fileItem.isFormField()) {
	        //This should not occur, here.
	        out.println(" ------------------------------ ");
	        out.println(fileItem.getFieldName() + " = " + fileItem.getString());
	    } else {
	        //Ok, we've got a file. Let's process it.
	        //Again, for all informations of what is exactly a FileItem, please
	        //have a look to http://jakarta.apache.org/commons/fileupload/
	        //
	        out.println(" ------------------------------ ");
	        out.println("FieldName: " + fileItem.getFieldName());
	        out.println("File Name: " + fileItem.getName());
	        out.println("ContentType: " + fileItem.getContentType());
	        out.println("Size (Bytes): " + fileItem.getSize());
	        fout = new File(ourTempDirectory + (new File(fileItem.getName())).getName());
	        out.println("File Out: " + fout.toString());
	        // write the file
	        fileItem.write(fout);	        
	    }
	    out.println("SUCCESS");
	}//while
  }catch(Exception e){
    out.println("Exception e = " + e.toString());
  }
%>