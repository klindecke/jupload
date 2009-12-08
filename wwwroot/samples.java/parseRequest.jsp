<%@ page language="java" import="java.io.*, java.sql.*, java.util.*" %>
<%@ page import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.disk.*, org.apache.commons.fileupload.servlet.*" %>
<%

/**
 * This pages gives a sample of java upload management. It needs the commons FileUpload library, which can be found on apache.org.
 *
 * Note:
 * - putting error=true as a parameter on the URL will generate an error. Allows test of upload error management in the applet. 
 * 
 * 
 * 
 * 
 * 
 * 
 */


	byte[] cr = {13}; 
	byte[] lf = {10}; 
	String CR = new String(cr);
	String LF = new String(lf);
	String CRLF = CR + LF;
	out.println("Before a LF=chr(10)" + LF 
		+ "Before a CR=chr(13)" + CR 
		+ "Before a CRLF" + CRLF); 


  //Initialization for chunk management.
  boolean bLastChunk = false;
  int numChunk = 0;
  
  //CAN BE OVERRIDEN BY THE postURL PARAMETER: if error=true is passed as a parameter on the URL
  boolean generateError = false;  

  response.setContentType("text/plain");
  try{
    // Get URL Parameters.
    Enumeration paraNames = request.getParameterNames();
    out.println("[parseRequest.jsp]  ------------------------------ ");
    out.println("[parseRequest.jsp]  Parameters: ");
    String pname;
    String pvalue;
    while (paraNames.hasMoreElements()) {
      pname = (String)paraNames.nextElement();
      pvalue = request.getParameter(pname);
      out.println("[parseRequest.jsp] " + pname + " = " + pvalue);
      if (pname.equals("jufinal")) {
      	bLastChunk = pvalue.equals("1");
      } else if (pname.equals("jupart")) {
      	numChunk = Integer.parseInt(pvalue);
      }
      //For debug convenience, putting error=true as a URL parameter, will generate an error
      //in this class.
      if (pname.equals("error") && pvalue.equals("true")) {
      	generateError = true;
      }
       
    }
    out.println("[parseRequest.jsp]  ------------------------------ ");

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
	if (! request.getContentType().startsWith("multipart/form-data")) {
		out.println("[parseRequest.jsp] No parsing of uploaded file: content type is " + request.getContentType()); 
	} else { 
		List /* FileItem */ items = upload.parseRequest(request);
		// Process the uploaded items
		Iterator iter = items.iterator();
		FileItem fileItem;
	    File fout;
	    out.println("[parseRequest.jsp]  Let's read input files ...");
		while (iter.hasNext()) {
		    fileItem = (FileItem) iter.next();
		
		    if (fileItem.isFormField()) {
		        out.println("[parseRequest.jsp] (form field) " + fileItem.getFieldName() + " = " + fileItem.getString());
		        
		        //If we receive the md5sum parameter, we've read finished to read the current file. It's not
		        //a very good (end of file) signal. Will be better in the future ... probably !
		        //Let's put a separator, to make output easier to read.
		        if (fileItem.getFieldName().equals("md5sum[]")) { 
					out.println("[parseRequest.jsp]  ------------------------------ ");
				}
		    } else {
		        //Ok, we've got a file. Let's process it.
		        //Again, for all informations of what is exactly a FileItem, please
		        //have a look to http://jakarta.apache.org/commons/fileupload/
		        //
		        out.println("[parseRequest.jsp] FieldName: " + fileItem.getFieldName());
		        out.println("[parseRequest.jsp] File Name: " + fileItem.getName());
		        out.println("[parseRequest.jsp] ContentType: " + fileItem.getContentType());
		        out.println("[parseRequest.jsp] Size (Bytes): " + fileItem.getSize());
		        //If we are in chunk mode, we add ".partN" at the end of the file, where N is the chunk number.
		        String uploadedFilename = fileItem.getName() + ( numChunk>0 ? ".part"+numChunk : "") ;
		        fout = new File(ourTempDirectory + (new File(uploadedFilename)).getName());
		        out.println("[parseRequest.jsp] File Out: " + fout.toString());
		        // write the file
		        fileItem.write(fout);	        
		        
		        //////////////////////////////////////////////////////////////////////////////////////
		        //Chunk management: if it was the last chunk, let's recover the complete file
		        //by concatenating all chunk parts.
		        //
		        if (bLastChunk) {	        
			        out.println("[parseRequest.jsp]  Last chunk received: let's rebuild the complete file (" + fileItem.getName() + ")");
			        //First: construct the final filename.
			        FileInputStream fis;
			        FileOutputStream fos = new FileOutputStream(ourTempDirectory + fileItem.getName());
			        int nbBytes;
			        byte[] byteBuff = new byte[1024];
			        String filename;
			        for (int i=1; i<=numChunk; i+=1) {
			        	filename = fileItem.getName() + ".part" + i;
			        	out.println("[parseRequest.jsp] " + "  Concatenating " + filename);
			        	fis = new FileInputStream(ourTempDirectory + filename);
			        	while ( (nbBytes = fis.read(byteBuff)) >= 0) {
			        		//out.println("[parseRequest.jsp] " + "     Nb bytes read: " + nbBytes);
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
		}//while
	}
	
  	out.println("[parseRequest.jsp] " + "Let's write a status, to finish the server response :");
    //out.println("WARNING: just a warning message.\\nOn two lines!");
    
    //Let's wait a little, to simulate the server time to manage the file.
    Thread.sleep(500);
    
    //Do you want to test a successful upload, or the way the applet reacts to an error ?
    if (generateError) { 
    	out.println("ERROR: this is a test error (forced in /wwwroot/pages/parseRequest.jsp).\\nHere is a second line!");
    } else {
    	out.println("SUCCESS");
    }

  	out.println("[parseRequest.jsp] " + "End of server treatment ");

  }catch(Exception e){
    out.println("");
    out.println("ERROR: Exception e = " + e.toString());
    out.println("");
  }
  
%>