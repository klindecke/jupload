package wjhk.jupload2.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * Overwriting the default Append class such that it scrolls to the bottom of
 * the text. The JFC doesn't always remember to do that. <BR>
 */

class JUploadPopupMenu extends JPopupMenu implements ActionListener, ItemListener {

	/** A generated serialVersionUID */
	private static final long serialVersionUID = -5473337111643079720L;
	
	/**
	 * Identifies the menu item that will set debug mode on or off (on means: debugLevel=100)
	 */
	JCheckBoxMenuItem cbMenuItemDebugOnOff = null;
	
	/**
	 * The current upload policy.
	 */
	private UploadPolicy uploadPolicy;

	
	JUploadPopupMenu(UploadPolicy uploadPolicy) {
		this.uploadPolicy = uploadPolicy;
		//Creation of the menu items
		cbMenuItemDebugOnOff = new JCheckBoxMenuItem("Debug on");
		add(cbMenuItemDebugOnOff);
		cbMenuItemDebugOnOff.addItemListener(this);		
	}

	/**
	 * This methods receive the event triggered by our popup menu.
	 */
	public void actionPerformed(ActionEvent action) {
		// TODO Auto-generated method stub		
	}

	public void itemStateChanged(ItemEvent e) {
		if (cbMenuItemDebugOnOff == e.getItem()) {
			uploadPolicy.setDebugLevel( (cbMenuItemDebugOnOff.isSelected() ? 100 : 0) );
		}
		// TODO Auto-generated method stub
		
	}
	
}

public class JUploadTextArea extends JTextArea implements MouseListener {

	/** Generated serialVersionUID */
	private static final long serialVersionUID = -6037767344615468632L;
	
	//private UploadPolicy uploadPolicy;
	private JUploadPopupMenu jUploadPopupMenu;

	public JUploadTextArea(int rows, int columns) {
		super(rows, columns);
		setBackground(new Color(255, 255, 203));
		setEditable(false);
		setLineWrap(true);
		setWrapStyleWord(true);
		
		addMouseListener(this);
		
	}

	public void append(String str) {
		super.append(str);
		setCaretPosition(getText().length());
	}
	
	/** 
	 * This method precise the upload policy. Now that we have an upload policy, we can construct a popup menu. Not
	 * really fine, but it works. 
	 */
	public void setUploadPolicy(UploadPolicy uploadPolicy) {
		//this.uploadPolicy = uploadPolicy;
		jUploadPopupMenu = new JUploadPopupMenu(uploadPolicy);
	}

	private void maybeOpenPopupMenu(MouseEvent mouseEvent) {
		if (mouseEvent.isPopupTrigger()  &&  ( (mouseEvent.getModifiersEx()&InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)) {
			if (jUploadPopupMenu != null) {
				jUploadPopupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////   MouseListener interface   //////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public void mouseClicked(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mouseEntered(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mouseExited(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mousePressed(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
	public void mouseReleased(MouseEvent mouseEvent) {
		maybeOpenPopupMenu(mouseEvent);
	}
}