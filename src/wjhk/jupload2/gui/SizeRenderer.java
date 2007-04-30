package wjhk.jupload2.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * Technical class, used to display file sizes. Used in
 * {@link wjhk.jupload2.gui.FilePanelJTable}.
 */
public class SizeRenderer extends DefaultTableCellRenderer {
    /**
     * 
     */
    private static final long serialVersionUID = -2029129064667754146L;
    
    private static final double gB = 1024L * 1024L * 1024L;
    private static final double mB = 1024L * 1024L;
    private static final double kB = 1024L;

    private String sizeunit_gigabytes;
    private String sizeunit_megabytes;
    private String sizeunit_kilobytes;
    private String sizeunit_bytes;

    public SizeRenderer(UploadPolicy uploadPolicy) {
        super();
        this.sizeunit_gigabytes = uploadPolicy.getString("unitGigabytes");
        this.sizeunit_megabytes = uploadPolicy.getString("unitMegabytes");
        this.sizeunit_kilobytes = uploadPolicy.getString("unitKilobytes");
        this.sizeunit_bytes = uploadPolicy.getString("unitBytes");
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (value instanceof Long) {
            double d = ((Long)value).doubleValue();
            String unit = this.sizeunit_bytes;
            if (d >= gB) {
                d /= gB;
                unit = this.sizeunit_gigabytes;
            } else if (d >= mB) {
                d /= mB;
                unit = this.sizeunit_megabytes;
            } else if (d >= kB) {
                d /= kB;
                unit = this.sizeunit_kilobytes;
            }
            setValue(String.format("%1$,3.2f %2$s", d, unit));
            super.setHorizontalAlignment(RIGHT);
        }
        return cell;
    }
}
