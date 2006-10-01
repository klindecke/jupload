package wjhk.jupload2.gui;

import java.text.DateFormat;
import java.util.Date;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Technical class, used to display dates. 
 * Used in {@link wjhk.jupload2.gui.FilePanelJTable}.
 */
public class DateRenderer extends DefaultTableCellRenderer {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7171473761133675782L;

public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    Component cell = super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

    if (value instanceof Date) {
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
          DateFormat.SHORT);
      setValue(df.format(value));
    }
    return cell;
  }
}
