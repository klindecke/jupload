package wjhk.jupload2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import wjhk.jupload2.gui.JUploadPanel;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;

/**
 * This class is program (contains a main class). It can be used to run the code from
 * outside any developpement tool like eclipse. <BR>
 * It's also a sample of how to use the code from a standard java aplication.
 */
public class JUpload extends JFrame{

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

    JUploadPanel jp = new JUploadPanel(this, uploadPolicy);

    c.add(jp, BorderLayout.CENTER);
    this.show();
  }

  //Main method
  public static void main(String[] args) {
    UploadPolicy uploadPolicy;
    if(1 == args.length){
    	uploadPolicy = UploadPolicyFactory.getUploadPolicy(null, null, args[0]);
    } else {
    	uploadPolicy = UploadPolicyFactory.getUploadPolicy(null, null);
    }

    JUpload ju = new JUpload(uploadPolicy);
  }

}
