~~~~~~~~~~~~~~~~~~~
0.0	INFORMATION
~~~~~~~~~~~~~~~~~~~


WEB SITE: http://jupload.sourceforge.net/
Version : 2.2.0

This applet comes with source code. I take no responsibility for any damages caused by the usage of this applet. Use it at your own risk!

~~~~~~~~~~~~~~~~~~~
1.0	DESCRIPTION
~~~~~~~~~~~~~~~~~~~
Traditional HTML upload forms allow you to select and upload one file at a time. This restriction is unacceptable when it comes to uploading thousands of files within a single folder. 

Java Multiple File Upload Applet (JUpload) takes care of this limited by allowing users to select and upload a whole directory and the files within it with a single click.

Main evolution since V2 :
- Add of UploadPolicy. This allows developpers to easily configure the way files are uploaded (see below for details)
- Add of picture management functions. This can easily be done by using the PictureUploadPolicy, or one of its inherited classes, like CoppermineUploadPolicy (which is dedicated to the coppermine online picture gallery.
- Cookies session are kept: Upload are now done within the current navigator session. 

UploadPolicies makes it easy to configure these parameters:
- Target upload URL
- Number of files that should be uploaded. For instance: all at once (default), one by one (see FileByFileUploadPolicy), or by packet of limited number of files (see CustomizedNbFilesPerRequestUploadPolicy)
- Top part of the applet can be modified by writing a new UploadPolicy, using the UploadPolicy.createTopPanel method. The PictureUploadPolicy uses it to add a preview panel and two rotation buttons.
- And much more ...  Please see the javadoc.

 Picture management is added by the PictureUploadPolicy. (see the Démo of the picture applet). This includes the following parameters:
- Ability to set a maximum width and/or height for pictures,
- Ability to rotate pictures, by quarters of turn,
- Ability to preview pictures. A click on the small picture displays a full screen picture.




~~~~~~~~~~~~~~~~~~~~~~~
2.0 Self Sign Applet.
~~~~~~~~~~~~~~~~~~~~~~~
  The applet must be signed, to be allowed to access to files on the client side. The best way is to sign the applet with a real way.
  Here is an explanation on how to sign the with a 'test' certificate. You _must_ do that after each modification of the jar package.
    - Generate Private/Public key set.
       keytool -genkey -alias "jupload" -validity 3600 -dname "CN=JUpload, OU=Testing/Demo, O=JUpload.SourceForge.net, L=SourceForge, S=SourceForge, C=SG"
    - List key set.
       keytool -list
    - Sign the Applet with the private key.
       jarsigner wjhk.jupload.jar jupload
    - Verify the jar file have being sign properly.
       jarsigner -verify wjhk.jupload.jar


~~~~~~~~~~~~~~~~~~~~~~
3.0	Server scripts
~~~~~~~~~~~~~~~~~~~~~~
The applet upload files to the server. Then, the server must handle the uploaded file. Here is an example, in java, of the way to handle the incoming file.

Here's a simple JSP script to log the input into file (./pages/writeOut.jsp)
---------------------------------------------------------------------
<%@ page language="java" import="java.io.*, java.sql.*, java.util.*" %>
<%
   // This JSP will save the request Input Steam into a file.
   String fileOut = "c:/temp/writeOut.bin";
    try{
      ServletInputStream in = request.getInputStream();
      byte[] line = new byte[1024];
      int bytes = 0;

      FileOutputStream fileOutS = new  FileOutputStream(fileOut);

      while(0 <(bytes = in.read(line))){
        fileOutS.write(line,0, bytes);
      }

      fileOutS.close();
      fileOutS = null;
      out.println("SUCCESSFUL : Upload Stream Saved to \"" + fileOut + "\".");
    }catch(Exception e){
      out.println("ERROR : Exception \"" + e.getMessage() + "\" Occured.");
    }
%>

---------------------------------------------------------------------
A more sophisticated server FileUpload (Jakarta Commons FileUpload) package 
can be obtained from apache.org. How it works? Look at the parseRequest.jsp 
(./pages/parseRequest.jsp).

---------------------------------------------------------------------
You can also read the file in php, using the $_FILES array. The uploaded filename is controled in the applet by the UploadPolicy.getUploadFilename method. The default behaviour is to return 'FileN' where N is the number of the uploaded file (for instance 0 to 4 when 5 files are uploaded). The CoppermineUploadPolicy changes that: here, files are uploaded one by one, and the uploaded file name is userpicture. The file is then managed in php by using the $_FILES['userpicture'] array.
See php doc for all details.

~~~~~~~~~~~~
4.0	FAQ:
~~~~~~~~~~~~
1) Any question on the java applet code  ?

  Please have a look to the java doc, available in the src/doc directory.

2) If your permissions are not setup properly the following Error messages
   will appear.

    - "File Chooser Exception: access denied (java... )"

First try to sign the applet. Don't forget to close your navigator, to make sure that your new applet is loaded.

3) You create a new jar on the web server, but your navigator only display the previous version.

 Don't forget to close your navigator, to make sure that your new applet is loaded.

