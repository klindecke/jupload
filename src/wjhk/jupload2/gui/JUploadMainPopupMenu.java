package wjhk.jupload2.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import wjhk.jupload2.policies.UploadPolicy;

public class JUploadMainPopupMenu extends JPopupMenu {
    /**
     * The current upload policy.
     */
    private UploadPolicy uploadPolicy;

    /**
     * The current upload panel. Can't be retrieve by
     * uploadPolicy.getAppel().getUploadPanel(), as the manu is cosntructed from
     * JUploadPanel constructor. That is: the applet did not get the
     * JUploadPanel reference (getUploadPanel returns null);
     */
    private JUploadPanel uploadPanel;

    JUploadMainPopupMenu(UploadPolicy uploadPolicy, JUploadPanel uploadPanel) {
        this.uploadPolicy = uploadPolicy;
        this.uploadPanel = uploadPanel;

        // ////////////////////////////////////////////////////////////////////////
        // Creation of the menu items
        // ////////////////////////////////////////////////////////////////////////
        JMenuItem menuItem = new JMenuItem("Paste");
        menuItem.setActionCommand((String) TransferHandler.getPasteAction()
                .getValue(Action.NAME));

        menuItem.addActionListener(uploadPanel);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                ActionEvent.CTRL_MASK));

        add(menuItem);
    }

}
