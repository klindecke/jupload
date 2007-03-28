package wjhk.jupload2.gui;

import java.awt.Color;
import javax.swing.JTextArea;


public class JUploadTextArea extends JTextArea {

	/** Generated serialVersionUID */
	private static final long serialVersionUID = -6037767344615468632L;
	
	public JUploadTextArea(int rows, int columns) {
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