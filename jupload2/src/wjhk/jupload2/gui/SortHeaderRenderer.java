package wjhk.jupload2.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Techical class, to display the column headers, for column that may be sorted.
 */
public class SortHeaderRenderer extends DefaultTableCellRenderer {
    /**
     * 
     */
    private static final long serialVersionUID = -4104776293873798189L;

    private static final Icon NONSORTED = new SortArrowIcon(SortArrowIcon.NONE);

    private static final Icon ASCENDING = new SortArrowIcon(
            SortArrowIcon.ASCENDING);

    private static final Icon DESCENDING = new SortArrowIcon(
            SortArrowIcon.DESCENDING);

    /**
     * Creates a new instance.
     */
    public SortHeaderRenderer() {
        setHorizontalTextPosition(LEFT);
        setHorizontalAlignment(CENTER);
    }

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            @SuppressWarnings("unused")
            boolean isSelected, @SuppressWarnings("unused")
            boolean hasFocus, @SuppressWarnings("unused")
            int row, int col) {
        int index = -1;
        boolean ascending = true;
        if (table instanceof FilePanelJTable) {
            FilePanelJTable sortTable = (FilePanelJTable) table;
            index = sortTable.getSortedColumnIndex();
            ascending = sortTable.isSortedColumnAscending();
        }
        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                setForeground(header.getForeground());
                setBackground(header.getBackground());
                setFont(header.getFont());
            }
        }
        Icon icon = ascending ? ASCENDING : DESCENDING;
        setIcon(col == index ? icon : NONSORTED);
        setText((value == null) ? "" : value.toString());
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        return this;
    }
}
