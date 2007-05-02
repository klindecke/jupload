package wjhk.jupload2.gui;

import java.util.Comparator;
import wjhk.jupload2.filedata.DefaultFileData;

/**
 * Technical class, used to sort rows in the
 * {@link wjhk.jupload2.gui.FilePanelDataModel2} class.
 */
public class ColumnComparator implements Comparator {
    protected int index;

    protected boolean ascending;

    public ColumnComparator(int index, boolean ascending) {
        this.index = index;
        this.ascending = ascending;
    }

    @SuppressWarnings("unchecked")
    public int compare(Object one, Object two) {
        if (one instanceof DefaultFileData && two instanceof DefaultFileData) {
            Object oOne;
            Object oTwo;
            switch (this.index) {
                case FilePanelDataModel2.COLINDEX_NAME:
                    oOne = ((DefaultFileData) one).getFileName();
                    oTwo = ((DefaultFileData) two).getFileName();
                    break;
                case FilePanelDataModel2.COLINDEX_SIZE:
                    oOne = new Long(((DefaultFileData) one).getFileLength());
                    oTwo = new Long(((DefaultFileData) two).getFileLength());
                    break;
                case FilePanelDataModel2.COLINDEX_DIRECTORY:
                    oOne = ((DefaultFileData) one).getDirectory();
                    oTwo = ((DefaultFileData) two).getDirectory();
                    break;
                case FilePanelDataModel2.COLINDEX_MODIFIED:
                    oOne = ((DefaultFileData) one).getLastModified();
                    oTwo = ((DefaultFileData) two).getLastModified();
                    break;
                default:
                    return 0;
            }
            if (oOne instanceof Comparable && oTwo instanceof Comparable) {
                return ((Comparable) oOne).compareTo(oTwo)
                        * (this.ascending ? 1 : -1);
            }
        }
        return 0;
    }
}
