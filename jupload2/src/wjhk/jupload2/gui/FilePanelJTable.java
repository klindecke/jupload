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
import wjhk.jupload2.policies.UploadPolicyFactory;

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

  public FilePanelJTable(){
  	uploadPolicy = UploadPolicyFactory.getCurrentUploadPolicy();
	
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setDefaultRenderer(Date.class, new DateRenderer());
    
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    JTableHeader header = getTableHeader();
    header.setDefaultRenderer(new SortHeaderRenderer());
    header.addMouseListener(this);
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
  }

  public void mousePressed(MouseEvent event) {
  }

  public void mouseClicked(MouseEvent event) {
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
        Cursor previousCursor = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	uploadPolicy.onSelectFile(filePanelDataModel.getFileDataAt(selectedRow));    	
        setCursor(previousCursor);
    }
  }
}
