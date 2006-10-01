package wjhk.jupload2.gui;

import java.awt.Color;
import javax.swing.JTextArea;

/**
 * Overwriting the default Append class such that it scrolls to the bottom of
 * the text. The JFC doesn't always remember to do that. <BR>
 */

public class StatusArea extends JTextArea {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6037767344615468632L;

	public StatusArea(int rows, int columns) {
		super(rows, columns);
		setBackground(new Color(255, 255, 203));
		setEditable(false);
		setLineWrap(true);
		setWrapStyleWord(true);
	}

	public void append(String str) {
		super.append(str);
		setCaretPosition(getText().length());
	}
}