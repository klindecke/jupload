<%@ page language="java" import="java.io.*, java.sql.*, java.util.*" %>
<%@ page import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.disk.*, org.apache.commons.fileupload.servlet.*" %>
<%
  //Initialization for chunk management.
  boolean bLastChunk = false;
  int numChunk = 0;

  response.setContentType("text/plain");
  try{
    // Get URL Parameters.
    Enumeration paraNames = request.getParameterNames();
    out.println(" ------------------------------ ");
    String pname;
    String pvalue;
    while (paraNames.hasMoreElements()) {
      pname = (String)paraNames.nextElement();
      pvalue = request.getParameter(pname);
      out.println(pname + " = " + pvalue);
      if (pname.equals("jufinal")) {
      	bLastChunk = pvalue.equals("1");
      } else if (pname.equals("jupart")) {
      	numChunk = Integer.parseInt(pvalue);
      }
    }
    out.println(" ------------------------------ ");

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
    out.println(" Let's read input files ...");
	while (iter.hasNext()) {
	    fileItem = (FileItem) iter.next();
	
	    if (fileItem.isFormField()) {
	        //This should not occur, in this example.
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
	        //If we are in chunk mode, we add ".partN" at the end of the file, where N is the chunk number.
	        String uploadedFilename = fileItem.getName() + ( numChunk>0 ? ".part"+numChunk : "") ;
	        fout = new File(ourTempDirectory + (new File(uploadedFilename)).getName());
	        out.println("File Out: " + fout.toString());
	        // write the file
	        fileItem.write(fout);	        
	        
	        //////////////////////////////////////////////////////////////////////////////////////
	        //Chunk management: if it was the last chunk, let's recover the complete file
	        //by concatenating all chunk parts.
	        //
	        if (bLastChunk) {	        
		        out.println(" Last chunk received: let's rebuild the complete file (" + fileItem.getName() + ")");
		        //First: construct the final filename.
		        FileInputStream fis;
		        FileOutputStream fos = new FileOutputStream(ourTempDirectory + fileItem.getName());
		        int nbBytes;
		        byte[] byteBuff = new byte[1024];
		        String filename;
		        for (int i=1; i<=numChunk; i+=1) {
		        	filename = fileItem.getName() + ".part" + i;
		        	out.println("  Concatenating " + filename);
		        	fis = new FileInputStream(ourTempDirectory + filename);
		        	while ( (nbBytes = fis.read(byteBuff)) >= 0) {
		        		out.println("     Nb bytes read: " + nbBytes);
		        		fos.write(byteBuff, 0, nbBytes);
		        	}
		        	fis.close();
		        }
		        fos.close();
	        }
	        // End of chunk management
	        //////////////////////////////////////////////////////////////////////////////////////
	        
	        fileItem.delete();
	    }
	    out.println("SUCCESS");
	}//while
  }catch(Exception e){
    out.println("Exception e = " + e.toString());
  }
  
  out.close();
%>