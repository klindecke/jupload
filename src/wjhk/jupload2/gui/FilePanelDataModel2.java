/*
 * Created on 21 avr. 2006
 */
package wjhk.jupload2.gui;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import wjhk.jupload2.filedata.FileData;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.policies.UploadPolicyFactory;

/**
 *
 * This class replaces FilePanelDataModel. The data for each row is now contained in an 
 * instance of FileData (or one of its subclasses, like {@link wjhk.jupload2.filedata.PictureFileData}).
 * This allow easy add of new functionalites, during upload, by adding attributes or methods to 
 * these classes, or create new ones.
 * <BR>
 * Some ides of improvements :
 * <UL>
 * <LI> Compression of picture before Upload (see {@link wjhk.jupload2.filedata.PictureFileData})
 * <LI> Could be XML validation before sending to the server
 * <LI> Up to your imagination...
 * </UL>
 *  
 */
class FilePanelDataModel2 extends AbstractTableModel {

	
	/**
	 * The uploadPolicy contains all current parameter, including the FileDataParam
	 */
	private UploadPolicy uploadPolicy = UploadPolicyFactory.getCurrentUploadPolicy();

	/**
	 * The column names, as displayed on the applet.
	 */
	private String COL_NAME = uploadPolicy.getString("colName");
	private String COL_SIZE = uploadPolicy.getString("colSize");
	private String COL_DIRECTORY = uploadPolicy.getString("colDirectory");
	private String COL_MODIFIED = uploadPolicy.getString("colModified");
	private String COL_READABLE = uploadPolicy.getString("colReadable");
	
	protected String[] columnNames = new String[] {
	 COL_NAME, COL_SIZE, COL_DIRECTORY, COL_MODIFIED, COL_READABLE
	};
	
	protected int[] columnSize = new int[] {
	    150, 75, 199, 130, 75
	};
	
	protected Class[] columnClasses = new Class[] {
	    String.class, Long.class, String.class, Date.class, Boolean.class
	};
	 
	/**
	 * This Vector contains all FileData.
	 */
	private Vector rows = new Vector();

	/**
	 * 
	 */
	public FilePanelDataModel2(UploadPolicy uploadPolicy) {
		//Property initialization is done ... for each property. Nothing to do here. 
		super();
		//
		this.uploadPolicy = uploadPolicy;
	}

	/**
	 * Does this table contain this file ?
	 * 
	 * @param file : the file that could be contained...
	 * @return true if the table contains this file.
	 */
	public boolean contains(File file) {
		Iterator i = rows.iterator();
		while (i.hasNext()) {
			if (file.equals(  ((FileData)i.next()) .getFile())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add a file to the panel (at the end of the list)
	 * 
	 * @param file
	 */
	public void addFile (File file) {
		rows.add(uploadPolicy.createFileData(file));
		fireTableDataChanged();
	}
	
	/**
	 * Ask for the file contained at specified row number.
	 * 
	 * @param row The row number
	 * @return The return instance of File.
	 */
	public File getFileAt(int row) {
		return ( (FileData) rows.get(row)).getFile();
	}
	
	/**
	 * Ask for the file contained at specified row number.
	 * 
	 * @param row The row number
	 * @return The return instance of File.
	 */
	public FileData getFileDataAt(int row) {
		return ( (FileData) rows.get(row));
	}

	/**
	 * Remove a specified row.
	 * 
	 * @param row The row to remove.
	 */
	public void removeRow(int row) {
		rows.remove(row);
		fireTableDataChanged();
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
	    return columnNames.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return rows.size();
	}

	/**
	 * Always return false here : no editable cell.
	 * 
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int arg0, int arg1) {
		//No editable columns.
		return false;
	}
	
	/**
	 * Sort the rows, according to one column.
	 *  
	 * @param col The index of the column to sort
	 * @param ascending true if ascending, false if descending.
	 */
	public void sortColumn(int col, boolean ascending) {
		Collections.sort(rows, new ColumnComparator(col, ascending));
		fireTableDataChanged();
	}

	/**
	 * Return true if this column can be sorted.
	 * 
	 * @param col The index of the column which can sortable or not.
	 * @return true if the column can be sorted. false otherwise.
	 */
	public boolean isSortable(int col) {
		return (Boolean.class != getColumnClass(col));
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int arg0) {
	    return columnClasses[arg0];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		FileData fileData = getFileDataAt(row);
		String colName = getColumnName(col); 
		//Don't if it will be useful, but the switch below allows the column
		//to be in any order.
		if (colName.equals(COL_NAME)) {
			return fileData.getFileName();
		} else if (colName.equals(COL_SIZE)) {
			return new Long(fileData.getFileLength());
		} else if (colName.equals(COL_DIRECTORY)) {
			return fileData.getDirectory();
		} else if (colName.equals(COL_MODIFIED)) {
			return fileData.getLastModified();
		} else if (colName.equals(COL_READABLE)) {
			return new Boolean(fileData.canRead());
		} else {
			uploadPolicy.displayErr("Unknown column in " + this.getClass().getName() + ": " + colName);
			return null;
		}
	}//getValueAt

	/**
	 * This method doesn't do anything : no changeable value.
	 * 
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object arg0, int arg1, int arg2) {
		uploadPolicy.displayWarn(this.getClass().getName() + ".setValueAt: no action");
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int arg0) {
	    return columnNames[arg0];
	}


	public int getColumnSize(int col) {
		return columnSize[col];
	}
}
