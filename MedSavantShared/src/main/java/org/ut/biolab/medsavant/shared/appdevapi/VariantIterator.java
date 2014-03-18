package org.ut.biolab.medsavant.shared.appdevapi;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;


/**
 * Iterator for all variants returned 
 * 
 * @author rammar
 */
public class VariantIterator implements Iterator {	
	
	protected static List<String> header;
	
	private int index= 0;
	private List<Object[]> rows;
	private Object[] currentRow;
	private AnnotationFormat[] afs;
	
	/**
	 * Create a new VariantIterator from variant table data.
	 * @param variantRows a List of all row Object[], as returned by VariantManagerAdapter.getVariants(...)
	 * @param afs the AnnotationFormat[] to retrieve table header information
	 */
	public VariantIterator(List<Object[]> variantRows, AnnotationFormat[] afs) {
		rows= variantRows;
		this.afs= afs;
		getTableHeader();
	}
	
	
	/**
	 * Returns true if iterator has more variants.
	 * @return true if iterator has more variants
	 */
	public boolean hasNext() {
		return index < rows.size();
	}
	
	
	/**
	 * Return the next variant.
	 * @return the next Variant object from the table.
	 */
	public Variant next() {
		Variant currentVariant= new Variant(rows.get(index++));
		return currentVariant;
	}
	
	
	/**
	 * Unsupported by this iterator - not needed for variants.
	 */
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() +
			" does not support the remove() operation since it is unneccessary " +
			" for variant analysis/retrieval.");
	}
	
	
	/** 
	 * Get the table header as a String list.
	 */
	private void getTableHeader() {
		header= new LinkedList<String>();
		
		try {
			for (AnnotationFormat af : afs) {
				for (CustomField field : af.getCustomFields()) {
					header.add(field.getAlias()); // Fields are added in correct order
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
