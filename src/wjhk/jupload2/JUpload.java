//
// $Id$
// 
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
// 
// Created: ?
// Creator: William JinHua Kwong
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version. This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details. You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation, Inc.,
// 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import wjhk.jupload2.gui.JUploadPanel;
import wjhk.jupload2.gui.JUploadTextArea;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;

/**
 * This class is program (contains a main class). It can be used to run the code
 * from outside any developpement tool like eclipse. <BR>
 * It's also a sample of how to use the code from a standard java aplication.
 * <BR>
 * <BR>
 * <BR>
 * It has been tested, a long time ago...
 * @author William Kwong JinHua
 * @version $Revision$
 */
public class JUpload extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -2188362223455520522L;

    // ----------------------------------------------------------------------

    /**
     * Constructs a new application instance.
     * 
     * @param uploadPolicy The policy to be used.
     */
    public JUpload(UploadPolicy uploadPolicy) {
        super("Java Multiple Upload Frame.");

        try {
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(@SuppressWarnings("unused")
                WindowEvent e) {
                    System.exit(0);
                }
            });
            this.setSize(640, 300);

            Container c = this.getContentPane();
            c.setLayout(new BorderLayout());

            JUploadTextArea statusArea = new JUploadTextArea(5, 20);
            JUploadPanel jp = new JUploadPanel(this, statusArea, uploadPolicy);

            c.add(jp, BorderLayout.CENTER);
            // this.show();
            this.setVisible(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
    }

    /**
     * The main program
     * 
     * @param args The commandline arguments.
     * @throws Exception if an error happens.
     */
    public static void main(String[] args) throws Exception {
        UploadPolicy uploadPolicy;
        if (1 == args.length) {
            // We write the system property, so that the UploadPolicy will read
            // it.
            System.setProperty(UploadPolicy.PROP_POST_URL, args[0]);
        }

        uploadPolicy = UploadPolicyFactory.getUploadPolicy(null);

        JUpload ju = new JUpload(uploadPolicy);
        if (ju == null) {
            // Just to avoid a warning on compilation time.
        }
    }

}
