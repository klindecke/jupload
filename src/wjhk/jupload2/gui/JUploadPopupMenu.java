package wjhk.jupload2.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * Global applet popup menu. It currently contains only the debug on/off menu
 * entry.
 */

final class JUploadPopupMenu extends JPopupMenu implements ItemListener {

    /** A generated serialVersionUID */
    private static final long serialVersionUID = -5473337111643079720L;

    /**
     * Identifies the menu item that will set debug mode on or off (on means:
     * debugLevel=100)
     */
    JCheckBoxMenuItem cbMenuItemDebugOnOff = null;

    /**
     * The current upload policy.
     */
    private UploadPolicy uploadPolicy;

    JUploadPopupMenu(UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;

        // ////////////////////////////////////////////////////////////////////////
        // Creation of the menu items
        // ////////////////////////////////////////////////////////////////////////
        // First: debug on or off
        this.cbMenuItemDebugOnOff = new JCheckBoxMenuItem("Debug on");
        this.cbMenuItemDebugOnOff
                .setState(this.uploadPolicy.getDebugLevel() == 100);
        add(this.cbMenuItemDebugOnOff);
        // ////////////////////////////////////////////////////////////////////////
        this.cbMenuItemDebugOnOff.addItemListener(this);
    }

    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e) {
        if (this.cbMenuItemDebugOnOff == e.getItem()) {
            this.uploadPolicy.setDebugLevel((this.cbMenuItemDebugOnOff
                    .isSelected() ? 100 : 0));
        }
    }
}
