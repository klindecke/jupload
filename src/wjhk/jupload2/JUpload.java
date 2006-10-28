package wjhk.jupload2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import wjhk.jupload2.gui.JUploadPanel;
import wjhk.jupload2.gui.JUploadTextArea;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;

/**
 * This class is program (contains a main class). It can be used to run the code from
 * outside any developpement tool like eclipse. <BR>
 * It's also a sample of how to use the code from a standard java aplication.
 * <BR><BR><BR>
 * Not really tested.
 */
public class JUpload extends JFrame{

  /**
	 * 
	 */
	private static final long serialVersionUID = -2188362223455520522L;
//------------- INFORMATION --------------------------------------------
  public static final String TITLE = "JUpload JUpload";
  public static final String DESCRIPTION =
      "Java Frame wrapper for JUploadPanel.";
  public static final String AUTHOR = "William Kwong JinHua";

  public static final double VERSION = 1.1;
  public static final String LAST_MODIFIED = "12 April 2004";

  //----------------------------------------------------------------------

  public JUpload(UploadPolicy uploadPolicy){
    super("Java Multiple Upload Frame.");
    
    try {
	    this.addWindowListener(
	      new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	          System.exit(0);
	        }
	      }
	    );
	    this.setSize(640,300);
	
	    Container c = this.getContentPane();
	    c.setLayout(new BorderLayout());
	
	    JUploadTextArea statusArea = new JUploadTextArea(5, 20);
	    JUploadPanel jp = new JUploadPanel(this, statusArea, uploadPolicy);
	
	    c.add(jp, BorderLayout.CENTER);
	    //this.show();
	    this.setVisible(true);
	  } catch (Exception e) {
		  System.out.println(e.getMessage());
		  System.out.println(e.getStackTrace());		  
	  }
  }

  //Main method
  public static void main(String[] args) throws Exception {
	    UploadPolicy uploadPolicy;
	    if(1 == args.length){
	    	//We write the system property, so that the UploadPolicy will read it.
	    	System.setProperty(UploadPolicy.PROP_POST_URL, args[0]);
	    }
	    	
	    uploadPolicy = UploadPolicyFactory.getUploadPolicy(null);
	
	    JUpload ju = new JUpload(uploadPolicy);
	    if (ju == null) {
	    	//juste pour éviter un warning.
	    }
  }

}
