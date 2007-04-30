package wjhk.jupload2.gui;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * Technical class, used to display dates. Used in
 * {@link wjhk.jupload2.gui.FilePanelJTable}.
 */
public class DateRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -7171473761133675782L;

    private SimpleDateFormat df;

    public DateRenderer(UploadPolicy uploadPolicy) {
        super();
        this.df = new SimpleDateFormat(uploadPolicy.getString("dateformat"));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (value instanceof Date)
            setValue(df.format(value));
        super.setHorizontalAlignment(RIGHT);
        return cell;
    }
}
