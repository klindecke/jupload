package wjhk.jupload2.gui;

import java.util.Date;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * This class is the JTable that display file information to the users. Data is
 * handled by the {@link wjhk.jupload2.gui.FilePanelDataModel2} class.
 */
public class FilePanelJTable extends JTable implements MouseListener {
    /**
     * 
     */
    private static final long serialVersionUID = 5422667664740339798L;

    protected int sortedColumnIndex = -1;

    protected boolean sortedColumnAscending = true;

    // The current UploadPolicy
    UploadPolicy uploadPolicy;

    // The current DataModel
    FilePanelDataModel2 filePanelDataModel;

    /**
     * Creates a new instance.
     * 
     * @param jup The parent upload panel.
     * @param uploadPolicy The policy for retrieval of various settings.
     */
    public FilePanelJTable(JUploadPanel jup, UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setDefaultRenderer(Long.class, new SizeRenderer(uploadPolicy));
        setDefaultRenderer(Date.class, new DateRenderer(uploadPolicy));
        setDefaultRenderer(String.class, new NameRenderer());

        // setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new SortHeaderRenderer());
        // We add the mouse listener on the header (to manage column sorts) and
        // on the main part (to manage
        // the contextual popup menu)
        header.addMouseListener(this);
        addMouseListener(jup);
    }

    /**
     * Set the model. Forces the model to be a FilePanelDataModel2. This method
     * calls the {@link JTable#setModel(javax.swing.table.TableModel)} method.
     * 
     * @param filePanelDataModel
     */
    public void setModel(FilePanelDataModel2 filePanelDataModel) {
        super.setModel(filePanelDataModel);
        this.filePanelDataModel = filePanelDataModel;
    }

    /**
     * Retrieve the currently sorted column.
     * 
     * @return the index of the currently sorted column.
     */
    public int getSortedColumnIndex() {
        return this.sortedColumnIndex;
    }

    /**
     * Retrieve the current sort order.
     * 
     * @return true, if the current sort order is ascending, false otherwise.
     */
    public boolean isSortedColumnAscending() {
        return this.sortedColumnAscending;
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent event) {
        // Displays the contextual menu ?
        this.uploadPolicy.getApplet().getUploadPanel()
                .maybeOpenPopupMenu(event);
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent event) {
        // Displays the contextual menu ?
        this.uploadPolicy.getApplet().getUploadPanel()
                .maybeOpenPopupMenu(event);
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent event) {
        // Displays the contextual menu ?
        if (!this.uploadPolicy.getApplet().getUploadPanel().maybeOpenPopupMenu(
                event)) {
            TableColumnModel colModel = getColumnModel();
            int index = colModel.getColumnIndexAtX(event.getX());
            int modelIndex = colModel.getColumn(index).getModelIndex();

            FilePanelDataModel2 model = (FilePanelDataModel2) getModel();
            if (model.isSortable(modelIndex)) {
                if (this.sortedColumnIndex == index) {
                    this.sortedColumnAscending = !this.sortedColumnAscending;
                }
                this.sortedColumnIndex = index;

                model.sortColumn(modelIndex, this.sortedColumnAscending);
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(@SuppressWarnings("unused")
    MouseEvent event) {
        // Nothing to do.
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(@SuppressWarnings("unused")
    MouseEvent event) {
        // Nothing to do.
    }

    /**
     * @see javax.swing.JTable#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        // Ignore extra messages, and no action before initialization.
        if (e.getValueIsAdjusting() || this.uploadPolicy == null)
            return;

        //
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) {
            this.uploadPolicy.onSelectFile(null);
        } else {
            int selectedRow = lsm.getMinSelectionIndex();
            // if one file is selected, we let the current upload policy reacts.
            // Otherwise, we don't do anything.
            if (selectedRow == lsm.getMaxSelectionIndex()) {
                Cursor previousCursor = getCursor();
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                this.uploadPolicy.onSelectFile(this.filePanelDataModel
                        .getFileDataAt(selectedRow));
                setCursor(previousCursor);
            }
        }
    }
}
