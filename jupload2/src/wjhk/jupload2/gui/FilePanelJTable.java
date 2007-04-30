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
 * This class is the JTable that display file information to the users. Data is handled
 * by the {@link wjhk.jupload2.gui.FilePanelDataModel2} class.
 * 
 */
public class FilePanelJTable extends JTable implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5422667664740339798L;
	protected int sortedColumnIndex = -1;
	protected boolean sortedColumnAscending = true;

	//The current UploadPolicy
	UploadPolicy uploadPolicy;

	//The current DataModel
	FilePanelDataModel2 filePanelDataModel;

	public FilePanelJTable(JUploadPanel jup, UploadPolicy uploadPolicy){
		this.uploadPolicy = uploadPolicy;

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setDefaultRenderer(Long.class, new SizeRenderer(uploadPolicy));
        setDefaultRenderer(Date.class, new DateRenderer(uploadPolicy));
        setDefaultRenderer(String.class, new NameRenderer());

		//setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTableHeader header = getTableHeader();
		header.setDefaultRenderer(new SortHeaderRenderer());
		//We add the mouse listener on the header (to manage column sorts) and on the main part (to manage 
		//the contextual popup menu)
		header.addMouseListener(this);
		addMouseListener(jup);
	}

	/**
	 * Set the model. Forces the model to be a FilePanelDataModel2. This method calls the
	 * {@link JTable#setModel(javax.swing.table.TableModel)} method. 
	 *  
	 * @param filePanelDataModel
	 */
	public void setModel(FilePanelDataModel2 filePanelDataModel) {
		super.setModel(filePanelDataModel);
		this.filePanelDataModel = filePanelDataModel;
	}

	public int getSortedColumnIndex() {
		return sortedColumnIndex;
	}

	public boolean isSortedColumnAscending() {
		return sortedColumnAscending;
	}

	// MouseListener implementation.
	public void mouseReleased(MouseEvent event) {
		//Displays the contextual menu ?
		uploadPolicy.getApplet().getUploadPanel().maybeOpenPopupMenu(event);
	}

	public void mousePressed(MouseEvent event) {
		//Displays the contextual menu ?
		uploadPolicy.getApplet().getUploadPanel().maybeOpenPopupMenu(event);
	}

	public void mouseClicked(MouseEvent event) {
		//Displays the contextual menu ?
		if (! uploadPolicy.getApplet().getUploadPanel().maybeOpenPopupMenu(event)) {
			TableColumnModel colModel = getColumnModel();
			int index = colModel.getColumnIndexAtX(event.getX());
			int modelIndex = colModel.getColumn(index).getModelIndex();
	
			FilePanelDataModel2 model = (FilePanelDataModel2) getModel();
			if (model.isSortable(modelIndex)) {
				if (sortedColumnIndex == index) {
					sortedColumnAscending = !sortedColumnAscending;
				}
				sortedColumnIndex = index;
	
				model.sortColumn(modelIndex, sortedColumnAscending);
			}
		}
	}

	public void mouseEntered(MouseEvent event) {
	}

	public void mouseExited(MouseEvent event) {
	}

	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
		//Ignore extra messages, and no action before initialization.
		if (e.getValueIsAdjusting() || uploadPolicy == null) return;

		//
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		if (lsm.isSelectionEmpty()) {
			uploadPolicy.onSelectFile(null);
		} else {
			int selectedRow = lsm.getMinSelectionIndex();
			//if one file is selected, we let the current upload policy reacts.
			//Otherwise, we don't do anything.
			if (selectedRow == lsm.getMaxSelectionIndex()) {
				Cursor previousCursor = getCursor();
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				uploadPolicy.onSelectFile(filePanelDataModel.getFileDataAt(selectedRow));    	
				setCursor(previousCursor);
			}
		}
	}
}
