/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;


/**
 * A region of cells, it doesn't relates to a sheet
 * @author dennis
 * @since 3.5.0
 */
public class CellRegion implements Serializable {
	private static final long serialVersionUID = 1L;
	final public int row;
	final public int column;
	final public int lastRow;
	final public int lastColumn;
	

	public CellRegion(int row, int column) {
		this(row, column, row, column);
	}

	public CellRegion(String areaReference) {
		AreaReference ref = new AreaReference(areaReference);
		int row = ref.getFirstCell().getRow();
		int column = ref.getFirstCell().getCol();
		int lastRow = ref.getLastCell().getRow();
		int lastColumn = ref.getLastCell().getCol();
		this.row = Math.min(row, lastRow);
		this.column = Math.min(column,lastColumn);
		this.lastRow = Math.max(row, lastRow);
		this.lastColumn = Math.max(column, lastColumn);
		
		checkLegal();
	}
	
	public String getReferenceString(){
		AreaReference ref = new AreaReference(new CellReference(row,column),new CellReference(lastRow,lastColumn));
		return isSingle()?ref.getFirstCell().formatAsString():ref.formatAsString();
	}

	private void checkLegal() {
		if ((row > lastRow || column > lastColumn)
				|| (row < 0 || lastRow < 0 || column < 0 || lastColumn < 0)) {
			throw new IllegalArgumentException("the region is illegal " + this);
		}
	}

	public CellRegion(int row, int column, int lastRow, int lastColumn) {
		this.row = row;
		this.column = column;
		this.lastRow = lastRow;
		this.lastColumn = lastColumn;
		checkLegal();
	}

	public boolean isSingle() {
		return row == lastRow && column == lastColumn;
	}

	public boolean contains(int row, int column) {
		return row >= this.row && row <= this.lastRow && column >= this.column
				&& column <= this.lastColumn;
	}
	public boolean contains(CellRegion region) {
		return contains(region.row, region.column)
				&& contains(region.lastRow, region.lastColumn); 
	}

	public boolean overlaps(CellRegion region) {
		return overlaps0(this,region) || overlaps0(region,this);
	}
	
	private static boolean overlaps0(CellRegion r1,CellRegion r2) {
		return ((r1.lastColumn >= r2.column) &&
			    (r1.lastRow >= r2.row) &&
			    (r1.column <= r2.lastColumn) &&
			    (r1.row <= r2.lastRow));
	}
	
	public boolean equals(int row, int column, int lastRow, int lastColumn){
		return this.row == row && this.column==column && this.lastRow==lastRow && this.lastColumn == lastColumn;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getReferenceString()).append("[").append(row).append(",").append(column).append(",").append(lastRow)
				.append(",").append(lastColumn).append("]");

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + lastColumn;
		result = prime * result + lastRow;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellRegion other = (CellRegion) obj;
		if (column != other.column)
			return false;
		if (lastColumn != other.lastColumn)
			return false;
		if (lastRow != other.lastRow)
			return false;
		if (row != other.row)
			return false;
		return true;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	public int getLastRow() {
		return lastRow;
	}

	public int getLastColumn() {
		return lastColumn;
	}
	
	public int getRowCount(){
		return lastRow-row+1;
	}
	public int getColumnCount(){
		return lastColumn-column+1;
	}
	
	public static String convertIndexToColumnString(int columnIdx){
		return CellReference.convertNumToColString(columnIdx);
	}
	
	public static int convertColumnStringToIndex(String colRef){
		return CellReference.convertColStringToIndex(colRef);
	}
	
	public List<CellRegion> diff(CellRegion target) {
		List<CellRegion> result = new ArrayList<CellRegion>();
		
		if(!this.overlaps(target)) {
			result.add(this);
		} else {
			
			CellRegion overlapRegion = new CellRegion(
					Math.max(this.row, target.row),
					Math.max(this.column, target.column), 
					Math.min(this.lastRow, target.lastRow), 
					Math.min(this.lastColumn, target.lastColumn));
			
			if(!overlapRegion.equals(this)) {
				// Top
				if(overlapRegion.row - this.row > 0) {
					result.add(new CellRegion(this.row, this.column, overlapRegion.row - 1, this.lastColumn));
				}
				
				// Bottom
				if(this.lastRow - overlapRegion.lastRow > 0) {
					result.add(new CellRegion(overlapRegion.lastRow + 1, this.column, this.lastRow, this.lastColumn));
				}
				
				// Left
				if(overlapRegion.column - this.column > 0) {
					result.add(new CellRegion(overlapRegion.row, this.column, overlapRegion.lastRow, overlapRegion.column - 1));
				}
				
				// Right
				if(this.lastColumn - overlapRegion.lastColumn > 0) {
					result.add(new CellRegion(overlapRegion.row, this.lastColumn, overlapRegion.lastRow, this.lastColumn));
				}
			}
		}
		
		return result;
	}

}