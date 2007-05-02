package wjhk.jupload2.gui;

import java.awt.Color;

import javax.swing.JTextArea;

/**
 * This class represents the text area for debug output.
 */
public class JUploadTextArea extends JTextArea {

    /** Generated serialVersionUID */
    private static final long serialVersionUID = -6037767344615468632L;

    /**
     * Constructs a new empty TextArea with the specified number of rows and
     * columns.
     * 
     * @param rows The desired number of text rows (lines).
     * @param columns The desired number of columns.
     */
    public JUploadTextArea(int rows, int columns) {
        super(rows, columns);
        setBackground(new Color(255, 255, 203));
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    /**
     * @see javax.swing.JTextArea#append(java.lang.String)
     */
    @Override
    public final void append(String str) {
        super.append(str);
        setCaretPosition(getText().length());
    }
}