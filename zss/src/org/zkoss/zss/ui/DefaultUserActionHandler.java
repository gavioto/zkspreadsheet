package org.zkoss.zss.ui;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.image.AImage;
import org.zkoss.lang.Strings;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Range.ApplyBorderType;
import org.zkoss.zss.api.Range.DeleteShift;
import org.zkoss.zss.api.Range.InsertCopyOrigin;
import org.zkoss.zss.api.Range.InsertShift;
import org.zkoss.zss.api.Range.PasteOperation;
import org.zkoss.zss.api.Range.PasteType;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.SheetAnchor;
import org.zkoss.zss.api.SheetOperationUtil;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.CellStyle.Alignment;
import org.zkoss.zss.api.model.CellStyle.BorderType;
import org.zkoss.zss.api.model.CellStyle.VerticalAlignment;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.ChartData;
import org.zkoss.zss.api.model.Font.Boldweight;
import org.zkoss.zss.api.model.Font.Underline;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.DefaultUserActionHandler.Clipboard.Type;
import org.zkoss.zss.ui.event.Events;
import org.zkoss.zss.ui.event.KeyEvent;
import org.zkoss.zul.Messagebox;

public class DefaultUserActionHandler implements UserActionHandler,EventListener<Event> {
	
	private static ThreadLocal<UserActionContext> _ctx = new ThreadLocal<UserActionContext>();
	private Clipboard _clipboard;

	
	private void checkCtx(){
		if(_ctx.get()==null){
			throw new IllegalAccessError("can't found action context");
		}
	}
	
	protected Spreadsheet getSpreadsheet(){
		checkCtx();
		return _ctx.get().spreadsheet;
	}
	
	protected Sheet getSheet(){
		checkCtx();
		return _ctx.get().sheet;
	}
	
	protected Book getBook(){
		checkCtx();
		Sheet sheet = getSheet();
		return sheet==null?null:sheet.getBook();
	}
	
	protected Rect getSelection(){
		checkCtx();
		return _ctx.get().selection;
	}
	
	protected Object getExtraData(String key){
		checkCtx();
		Map data = _ctx.get().extraData;
		if(data!=null){
			return data.get(key);
		}
		return null;
	}
	
	@Override
	public boolean handleAction(Spreadsheet spreadsheet, Sheet targetSheet,
			String action, Rect selection, Map<String, Object> extraData) {
		final UserActionContext ctx = new UserActionContext(spreadsheet, targetSheet,action,selection,extraData);
		_ctx.set(ctx);
		try{
			return dispatchAction(spreadsheet,targetSheet,action,selection,extraData);
		}finally{
			_ctx.set(null);
		}
	}
	
	
	protected boolean dispatchAction(Spreadsheet spreadsheet, Sheet targetSheet,
			String action, Rect selection, Map<String, Object> extraData) {
		
		if (UserAction.HOME_PANEL.toString().equals(action)) {
			return doShowHomePanel();
		} else if (UserAction.INSERT_PANEL.equals(action)) {
			return doShowInsertPanel();
		} else if (UserAction.FORMULA_PANEL.equals(action)) {
			return doShowFormulaPanel();
		} else if (UserAction.ADD_SHEET.toString().equals(action)) {
			return doAddSheet();
		} else if (UserAction.DELETE_SHEET.equals(action)) {
			return doDeleteSheet();
		} else if (UserAction.RENAME_SHEET.equals(action)) {
			String name = (String) extraData.get("name");
			return doRenameSheet(name);
		} else if (UserAction.MOVE_SHEET_LEFT.equals(action)) {
			return doMoveSheetLeft();
		} else if (UserAction.MOVE_SHEET_RIGHT.equals(action)) {
			return doMoveSheetRight();
		} else if (UserAction.PROTECT_SHEET.equals(action)) {
			return doProtectSheet();
		} else if (UserAction.GRIDLINES.equals(action)) {
			return doGridlines();
		} else if (UserAction.NEW_BOOK.equals(action)) {
			return doNewBook();
		} else if (UserAction.SAVE_BOOK.equals(action)) {
			return doSaveBook();
		} else if (UserAction.EXPORT_PDF.equals(action)) {
			return doExportPDF();
		} else if (UserAction.PASTE.equals(action)) {
			return doPaste();
		} else if (UserAction.PASTE_FORMULA.equals(action)) {
			return doPasteFormula();
		} else if (UserAction.PASTE_VALUE.equals(action)) {
			return doPasteValue();
		} else if (UserAction.PASTE_ALL_EXPECT_BORDERS.equals(action)) {
			return doPasteAllExceptBorder();
		} else if (UserAction.PASTE_TRANSPOSE.equals(action)) {
			return doPasteTranspose();
		} else if (UserAction.PASTE_SPECIAL.equals(action)) {
			return doPasteSpecial();
		} else if (UserAction.CUT.equals(action)) {
			return doCut();	
		} else if (UserAction.COPY.equals(action)) {
			return doCopy();
		} else if (UserAction.FONT_FAMILY.equals(action)) {
			return doFontFamily((String)extraData.get("name"));
		} else if (UserAction.FONT_SIZE.equals(action)) {
			Integer fontSize = Integer.parseInt((String)extraData.get("size"));
			return doFontSize(fontSize);
		} else if (UserAction.FONT_BOLD.equals(action)) {
			return doFontBold();
		} else if (UserAction.FONT_ITALIC.equals(action)) {
			return doFontItalic();
		} else if (UserAction.FONT_UNDERLINE.equals(action)) {
			return doFontUnderline();
		} else if (UserAction.FONT_STRIKE.equals(action)) {
			return doFontStrikeout();
		} else if (UserAction.BORDER.equals(action)) {
			return doBorder(getBorderColor(extraData));
		} else if (UserAction.BORDER_BOTTOM.equals(action)) {
			return doBorderBottom(getBorderColor(extraData));
		} else if (UserAction.BORDER_TOP.equals(action)) {
			return doBoderTop(getBorderColor(extraData));
		} else if (UserAction.BORDER_LEFT.equals(action)) {
			return doBorderLeft(getBorderColor(extraData));
		} else if (UserAction.BORDER_RIGHT.equals(action)) {
			return doBorderRight(getBorderColor(extraData));
		} else if (UserAction.BORDER_NO.equals(action)) {
			return doBorderNo(getBorderColor(extraData));
		} else if (UserAction.BORDER_ALL.equals(action)) {
			return doBorderAll(getBorderColor(extraData));
		} else if (UserAction.BORDER_OUTSIDE.equals(action)) {
			return doBorderOutside(getBorderColor(extraData));
		} else if (UserAction.BORDER_INSIDE.equals(action)) {
			return doBorderInside(getBorderColor(extraData));
		} else if (UserAction.BORDER_INSIDE_HORIZONTAL.equals(action)) {
			return doBorderInsideHorizontal(getBorderColor(extraData));
		} else if (UserAction.BORDER_INSIDE_VERTICAL.equals(action)) {
			return doBorderInsideVertical(getBorderColor(extraData));
		} else if (UserAction.FONT_COLOR.equals(action)) {
			return doFontColor(getFontColor(extraData));
		} else if (UserAction.FILL_COLOR.equals(action)) {
			return doFillColor(getFillColor(extraData));
		} else if (UserAction.VERTICAL_ALIGN_TOP.equals(action)) {
			return doVerticalAlignTop();
		} else if (UserAction.VERTICAL_ALIGN_MIDDLE.equals(action)) {
			return doVerticalAlignMiddle();
		} else if (UserAction.VERTICAL_ALIGN_BOTTOM.equals(action)) {
			return doVerticalAlignBottom();
		} else if (UserAction.HORIZONTAL_ALIGN_LEFT.equals(action)) {
			return doHorizontalAlignLeft();
		} else if (UserAction.HORIZONTAL_ALIGN_CENTER.equals(action)) {
			return doHorizontalAlignCenter();
		} else if (UserAction.HORIZONTAL_ALIGN_RIGHT.equals(action)) {
			return doHorizontalAlignRight();
		} else if (UserAction.WRAP_TEXT.equals(action)) {
			return doWrapText();
		} else if (UserAction.MERGE_AND_CENTER.equals(action)) {
			return doMergeAndCenter();
		} else if (UserAction.MERGE_ACROSS.equals(action)) {
			return doMergeAcross();
		} else if (UserAction.MERGE_CELL.equals(action)) {
			return doMergeCell();
		} else if (UserAction.UNMERGE_CELL.equals(action)) {
			return doUnmergeCell();
		} else if (UserAction.INSERT_SHIFT_CELL_RIGHT.equals(action)) {
			return doShiftCellRight();
		} else if (UserAction.INSERT_SHIFT_CELL_DOWN.equals(action)) {
			return doShiftCellDown();
		} else if (UserAction.INSERT_SHEET_ROW.equals(action)) {
			return doInsertSheetRow();
		} else if (UserAction.INSERT_SHEET_COLUMN.equals(action)) {
			return doInsertSheetColumn();
		} else if (UserAction.DELETE_SHIFT_CELL_LEFT.equals(action)) {
			return doShiftCellLeft();
		} else if (UserAction.DELETE_SHIFT_CELL_UP.equals(action)) {
			return doShiftCellUp();
		} else if (UserAction.DELETE_SHEET_ROW.equals(action)) {
			return doDeleteSheetRow();
		} else if (UserAction.DELETE_SHEET_COLUMN.equals(action)) {
			return doDeleteSheetColumn();
		} else if (UserAction.SORT_ASCENDING.equals(action)) {
			return doSortAscending();
		} else if (UserAction.SORT_DESCENDING.equals(action)) {
			return doSortDescending();
		} else if (UserAction.CUSTOM_SORT.equals(action)) {
			return doCustomSort();
		} else if (UserAction.FILTER.equals(action)) {
			return doFilter();
		} else if (UserAction.CLEAR_FILTER.equals(action)) {
			return doClearFilter();
		} else if (UserAction.REAPPLY_FILTER.equals(action)) {
			return doReapplyFilter();
		} else if (UserAction.CLEAR_CONTENT.equals(action)) {
			return doClearContent();
		} else if (UserAction.CLEAR_STYLE.equals(action)) {
			return doClearStyle();
		} else if (UserAction.CLEAR_ALL.equals(action)) {
			return doClearAll();
		} else if (UserAction.COLUMN_CHART.equals(action)) {
			return doColumnChart();
		} else if (UserAction.COLUMN_CHART_3D.equals(action)) {
			return doColumnChart3D();
		} else if (UserAction.LINE_CHART.equals(action)) {
			return doLineChart();
		} else if (UserAction.LINE_CHART_3D.equals(action)) {
			return doLineChart3D();
		} else if (UserAction.PIE_CHART.equals(action)) {
			return doPieChart();
		} else if (UserAction.PIE_CHART_3D.equals(action)) {
			return doPieChart3D();
		} else if (UserAction.BAR_CHART.equals(action)) {
			return doBarChart();
		} else if (UserAction.BAR_CHART_3D.equals(action)) {
			return doBarChart3D();
		} else if (UserAction.AREA_CHART.equals(action)) {
			return doAreaChart();
		} else if (UserAction.SCATTER_CHART.equals(action)) {
			return doScatterChart();
		} else if (UserAction.DOUGHNUT_CHART.equals(action)) {
			return doDoughnutChart();
		} else if (UserAction.HYPERLINK.equals(action)) {
			return doHyperlink();
		} else if (UserAction.INSERT_PICTURE.equals(action)) {
			return doInsertPicture();
		} else if (UserAction.CLOSE_BOOK.equals(action)) {
			return doCloseBook();
		} else if (UserAction.FORMAT_CELL.equals(action)) {
			return doFormatCell();
		} else if (UserAction.COLUMN_WIDTH.equals(action)) {
			return doColumnWidth();
		} else if (UserAction.ROW_HEIGHT.equals(action)) {
			return doRowHeight();
		} else if (UserAction.HIDE_COLUMN.equals(action)) {
			return doHideColumn();
		} else if (UserAction.UNHIDE_COLUMN.equals(action)) {
			return doUnhideColumn();
		} else if (UserAction.HIDE_ROW.equals(action)) {
			return doHideRow();
		} else if (UserAction.UNHIDE_ROW.equals(action)) {
			return doUnhideRow();
		} else if (UserAction.INSERT_FUNCTION.equals(action)) {
			return doInsertFunction();
		}else{
			showNotImplement(action);
			return false;
		}
	}

	protected boolean doMoveSheetRight() {
		Book book = getBook();
		Sheet sheet = getSheet();
		
		int max = book.getNumberOfSheets();
		int i = book.getSheetIndex(sheet);
		
		if(i<max){
			i ++;
			Range range = Ranges.range(sheet);
			SheetOperationUtil.setSheetOrder(range,i);
		}
		return true;
	}

	protected boolean doMoveSheetLeft() {
		Book book = getBook();
		Sheet sheet = getSheet();
		
		if(book.getSheetIndex(sheet)>0){
			Range range = Ranges.range(sheet);
			SheetOperationUtil.setSheetOrder(range,0);
		}
		return true;
	}

	protected boolean doRenameSheet(String newname) {
		Book book = getBook();
		Sheet sheet = getSheet();

		if(book.getSheet(newname)!=null){
			showWarnMessage("Canot rename a sheet to the same as another.");
			return false;
		}
		
		Range range = Ranges.range(sheet);
		SheetOperationUtil.renameSheet(range,newname);
		return true;
	}

	protected boolean doDeleteSheet() {
		Book book = getBook();
		Sheet sheet = getSheet();
		
		int num = book.getNumberOfSheets();
		if(num<=1){
			showWarnMessage("Canot delete last sheet.");
			return false;
		}
		
		int index = book.getSheetIndex(sheet);
		
		Range range = Ranges.range(sheet);
		SheetOperationUtil.deleteSheet(range);
		
		if(index==num-1){
			index--;
		}
		
		getSpreadsheet().setSelectedSheet(book.getSheetAt(index).getSheetName());
		
		return true;
	}

	protected boolean doAddSheet() {
		String prefix = Labels.getLabel(UserAction.SHEET.getLabelKey());
		if (Strings.isEmpty(prefix))
			prefix = "Sheet";
		Sheet sheet = getSheet();

		Range range = Ranges.range(sheet);
		SheetOperationUtil.addSheet(range,prefix);
		
		return true;
	}

	/**
	 * Returns the border color
	 * @return
	 */
	protected String getDefaultBorderColor() {
		return "#000000";
	}
	
	
	/**
	 * Returns the border color
	 * @return
	 */
	protected String getDefaultFontColor() {
		return "#000000";
	}
	
	
	/**
	 * Returns the border color
	 * @return
	 */
	protected String getDefaultFillColor() {
		return "#FFFFFF";
	}
	
	private String getBorderColor(Map extraData){
		String color = (String)extraData.get("color");
		if (Strings.isEmpty(color)) {//CE version won't provide color
			color = getDefaultBorderColor();
		}
		return color;
	}
	
	private String getFontColor(Map extraData){
		String color = (String)extraData.get("color");
		if (Strings.isEmpty(color)) {//CE version won't provide color
			color = getDefaultFontColor();
		}
		return color;
	}
	
	private String getFillColor(Map extraData){
		String color = (String)extraData.get("color");
		if (Strings.isEmpty(color)) {//CE version won't provide color
			color = getDefaultFillColor();
		}
		return color;
	}
	
	protected boolean doHideRow() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		range = range.getRowRange();
		CellOperationUtil.hide(range);
		return true;
	}

	protected boolean doUnhideRow() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		range = range.getRowRange();
		CellOperationUtil.unHide(range);
		return true;
	}

	protected boolean doUnhideColumn() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		range = range.getColumnRange();
		CellOperationUtil.hide(range);
		return true;
		
	}

	protected boolean doHideColumn() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		range = range.getColumnRange();
		CellOperationUtil.unHide(range);
		return true;
	}
	
	protected boolean doCloseBook() {
		Spreadsheet zss = getSpreadsheet();
		if(zss.getSrc()!=null){
			zss.setSrc(null);
		}
		if(zss.getBook()!=null){
			zss.setBook(null);
		}
		
		clearClipboard();
		return true;
	}
	
	
	protected boolean doInsertPicture(){
		final Spreadsheet spreadsheet = getSpreadsheet();
		final Sheet sheet = getSheet();
		final Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		askUploadFile(new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				if(org.zkoss.zk.ui.event.Events.ON_UPLOAD.equals(event.getName())){
					Media media = ((UploadEvent)event).getMedia();
					doInsertPicture(spreadsheet,sheet,selection,media);
				}
			}
		});
		return true;
	}
	
	protected boolean doInsertPicture(Spreadsheet spreadsheet,Sheet sheet,Rect selection,Media media) {
		if(media==null){
			showWarnMessage("Can't get the uploaded file");
			return true;
		}
		
		if(!(media instanceof AImage) || SheetOperationUtil.getPictureFormat((AImage)media)==null){
			showWarnMessage("Can't support the uploaded file");
			return true;
		}
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		
		SheetOperationUtil.addPicture(range,(AImage)media);
		
		clearClipboard();
		return true;
	}
		
	/**
	 * Execute when user press key
	 * @param event
	 */
	protected boolean doKeystroke(int keyCode,boolean ctrlKey, boolean shiftKey, boolean altKey) {
		if (46 == keyCode) {
			if (ctrlKey)
				return doClearStyle();
			else
				return doClearContent();
		}
		if (!ctrlKey)
			return false;
		
		switch (keyCode) {
		case 'X':
			return doCut();
		case 'C':
			return doCopy();
		case 'V':
			return doPaste();
		case 'D':
			return doClearContent();
		case 'B':
			return doFontBold();
		case 'I':
			return doFontItalic();
		case 'U':
			return doFontUnderline();
		}
		return false;
	}
	
	protected Clipboard getClipboard() {
		return _clipboard;
	}
	
	protected void setClipboard(Clipboard clipboard){
		_clipboard = clipboard;
		if(_clipboard!=null)
			getSpreadsheet().setHighlight(_clipboard.sourceRect);
	}
	
	protected void clearClipboard() {
		_clipboard = null;
		getSpreadsheet().setHighlight(null);
		//TODO: shall also clear client side clipboard if possible
	}
	
	protected boolean doCopy() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		setClipboard(new Clipboard(Clipboard.Type.COPY, sheet.getSheetName(), selection));
		return true;
	}
	
	protected boolean doPaste(PasteType pasteType, PasteOperation pasteOperation, boolean skipBlank, boolean transpose) {
		Clipboard cb = getClipboard();
		if(cb==null)
			return false;
		
		Book book = getBook();
		Sheet destSheet = getSheet();
		Sheet srcSheet = book.getSheet(cb.sourceSheetName);
		if(srcSheet==null){
			//TODO message;
			clearClipboard();
			return true;
		}
		Rect src = cb.sourceRect;
		
		Rect selection = getSelection();
		
		Range srcRange = Ranges.range(srcSheet, src.getTop(),
				src.getLeft(), src.getBottom(),src.getRight());

		Range destRange = Ranges.range(destSheet, selection.getTop(),
				selection.getLeft(), selection.getBottom(), selection.getRight());
		
		if (destRange.isProtected()) {
			showProtectMessage();
			return true;
		} else if (cb.type == Type.CUT && srcRange.isProtected()) {
			showProtectMessage();
			return true;
		}
		
		if(cb.type==Type.CUT){
			CellOperationUtil.cut(srcRange,destRange);
			clearClipboard();
		}else{
			CellOperationUtil.pasteSpecial(srcRange, destRange, pasteType, pasteOperation, skipBlank, transpose);
		}
		return true;
	}
	
	protected void showProtectMessage() {
		Messagebox.show("The cell that you are trying to change is protected and locked.", "ZK Spreadsheet", Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	protected void showWarnMessage(String message) {
		Messagebox.show(message, "ZK Spreadsheet", Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	protected void showNotImplement(String action) {
		Messagebox.show("This action "+Labels.getLabel(action,action)+" doesn't be implemented yet", "ZK Spreadsheet", Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	protected boolean doPaste() {
		return doPaste(PasteType.PASTE_ALL,PasteOperation.PASTEOP_NONE,false,false);
	}
	
	protected boolean doPasteFormula() {
		return doPaste(PasteType.PASTE_FORMULAS,PasteOperation.PASTEOP_NONE,false,false);
	}
	
	protected boolean doPasteValue() {
		return doPaste(PasteType.PASTE_VALUES,PasteOperation.PASTEOP_NONE,false,false);
	}
	
	protected boolean doPasteAllExceptBorder() {
		return doPaste(PasteType.PASTE_ALL_EXCEPT_BORDERS,PasteOperation.PASTEOP_NONE,false,false);
	}
	
	protected boolean doPasteTranspose() {
		return doPaste(PasteType.PASTE_ALL, PasteOperation.PASTEOP_NONE, false, true);
	}
	
	protected boolean doCut() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		setClipboard(new Clipboard(Clipboard.Type.CUT, sheet.getSheetName(), selection));
		return true;
	}
	
	protected boolean doFontFamily(String fontFamily) {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyFontName(range, fontFamily);
		return true;
	}
	
	protected boolean doFontSize(int fontSize) {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyFontSize(range, (short)fontSize);
		return true;
	}
	
	protected boolean doFontBold() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}

		//toggle and apply bold of first cell to dest
		Boldweight bw = range.getCellStyle().getFont().getBoldweight();
		if(Boldweight.BOLD.equals(bw)){
			bw = Boldweight.NORMAL;
		}else{
			bw = Boldweight.BOLD;
		}
		
		CellOperationUtil.applyFontBoldweight(range, bw);
		return true;
	}
	
	protected boolean doFontItalic() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}

		//toggle and apply bold of first cell to dest
		boolean italic = !range.getCellStyle().getFont().isItalic();
		CellOperationUtil.applyFontItalic(range, italic);
		return true;
	}
	
	protected boolean doFontStrikeout() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}

		//toggle and apply bold of first cell to dest
		boolean strikeout = !range.getCellStyle().getFont().isStrikeout();
		CellOperationUtil.applyFontStrikeout(range, strikeout);
		return true;
	}
	
	protected boolean doFontUnderline() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}

		//toggle and apply bold of first cell to dest
		Underline underline = range.getCellStyle().getFont().getUnderline();
		if(Underline.NONE.equals(underline)){
			underline = Underline.SINGLE;
		}else{
			underline = Underline.NONE;
		}
		
		CellOperationUtil.applyFontUnderline(range, underline);	
		return true;
	}

	protected boolean doBorder(ApplyBorderType type,BorderType borderTYpe, String color){
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyBorder(range,type, borderTYpe, color);
		return true;
	}
	
	protected boolean doBorder(String color) {
		return doBorder(ApplyBorderType.EDGE_BOTTOM,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderBottom(String color) {
		return doBorder(ApplyBorderType.EDGE_BOTTOM,BorderType.MEDIUM,color);
	}
	
	protected boolean doBoderTop(String color) {
		return doBorder(ApplyBorderType.EDGE_TOP,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderLeft(String color) {
		return doBorder(ApplyBorderType.EDGE_LEFT,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderRight(String color) {
		return doBorder(ApplyBorderType.EDGE_RIGHT,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderNo(String color) {
		return doBorder(ApplyBorderType.FULL,BorderType.NONE,color);
	}
	
	protected boolean doBorderAll(String color) {
		return doBorder(ApplyBorderType.FULL,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderOutside(String color) {
		return doBorder(ApplyBorderType.OUTLINE,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderInside(String color) {
		return doBorder(ApplyBorderType.INSIDE,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderInsideHorizontal(String color) {
		return doBorder(ApplyBorderType.INSIDE_HORIZONTAL,BorderType.MEDIUM,color);
	}
	
	protected boolean doBorderInsideVertical(String color) {
		return doBorder(ApplyBorderType.INSIDE_VERTICAL,BorderType.MEDIUM,color);
	}
	
	protected boolean doFontColor(String color) {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyFontColor(range, color);
		return true;
	}
	
	protected boolean doFillColor(String color) {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyCellColor(range,color);
		return true;
	}
	
	protected boolean doVerticalAlignTop() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyCellVerticalAlignment(range, VerticalAlignment.TOP);
		return true;
	}
	
	protected boolean doVerticalAlignMiddle() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyCellVerticalAlignment(range, VerticalAlignment.CENTER);
		return true;
	}

	protected boolean doVerticalAlignBottom() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyCellVerticalAlignment(range, VerticalAlignment.BOTTOM);
		return true;
	}
	
	protected boolean doHorizontalAlignLeft() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyCellAlignment(range, Alignment.LEFT);
		return true;
	}
	
	protected boolean doHorizontalAlignCenter() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyCellAlignment(range, Alignment.CENTER);
		return true;
	}
	
	protected boolean doHorizontalAlignRight() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.applyCellAlignment(range, Alignment.RIGHT);
		return true;
	}
	
	protected boolean doWrapText() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		boolean wrapped = !range.getCellStyle().isWrapText();
		CellOperationUtil.applyCellWrapText(range, wrapped);
		return true;
	}
	
	protected boolean doMergeAndCenter() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.toggleMergeCenter(range);
		clearClipboard();
		return true;
	}
	
	protected boolean doMergeAcross() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.merge(range, true);
		clearClipboard();
		return true;
	}
	
	protected boolean doMergeCell() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.merge(range, false);
		clearClipboard();
		return true;
	}
	
	protected boolean doUnmergeCell() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.unMerge(range);
		clearClipboard();
		return true;
	}
	
	protected boolean doShiftCellRight() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.insert(range,InsertShift.RIGHT, InsertCopyOrigin.RIGHT_BELOW);
		clearClipboard();
		return true;
	}
	
	protected boolean doShiftCellDown() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.insert(range,InsertShift.DOWN, InsertCopyOrigin.LEFT_ABOVE);
		clearClipboard();
		return true;
	}
	
	protected boolean doInsertSheetRow() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		if(range.isWholeColumn()){
			showWarnMessage("don't allow to inser row when select whole column");
			return true;
		}
		
		range = range.getRowRange();
		CellOperationUtil.insert(range,InsertShift.DOWN, InsertCopyOrigin.LEFT_ABOVE);
		clearClipboard();
		return true;
	}
	
	protected boolean doInsertSheetColumn() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		if(range.isWholeRow()){
			showWarnMessage("don't allow to inser column when select whole row");
			return true;
		}
		
		range = range.getColumnRange();
		CellOperationUtil.insert(range,InsertShift.RIGHT, InsertCopyOrigin.RIGHT_BELOW);
		clearClipboard();
		return true;
	}
	
	protected boolean doShiftCellLeft() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		CellOperationUtil.delete(range,DeleteShift.LEFT);
		clearClipboard();
		return true;
	}
	
	protected boolean doShiftCellUp() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		CellOperationUtil.delete(range,DeleteShift.UP);
		clearClipboard();
		return true;
	}
	
	protected boolean doDeleteSheetRow() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		if(range.isWholeColumn()){
			showWarnMessage("don't allow to delete all rows");
			return true;
		}
		
		range = range.getRowRange();
		CellOperationUtil.delete(range, DeleteShift.UP);
		clearClipboard();
		return true;
	}
	
	protected boolean doDeleteSheetColumn() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		if(range.isWholeRow()){
			showWarnMessage("don't allow to delete all column");
			return true;
		}
		
		range = range.getColumnRange();
		CellOperationUtil.delete(range, DeleteShift.LEFT);
		clearClipboard();
		return true;
	}

	protected boolean doClearStyle() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.clearStyles(range);
		return true;
	}
	
	protected boolean doClearContent() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.clearContents(range);
		return true;
	}
	
	protected boolean doClearAll() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.clearAll(range);
		return true;
	}
	
	protected boolean doSortAscending() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.sort(range,false);
		clearClipboard();
		return true;
	}
	
	protected boolean doSortDescending() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		CellOperationUtil.sort(range,true);
		clearClipboard();
		return true;
	}
	
	protected boolean doFilter() {
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		SheetOperationUtil.toggleAutoFilter(range);
		clearClipboard();
		return true;
	}
	
	protected boolean doClearFilter() {
		Sheet sheet = getSheet();
		
		Range range = Ranges.range(sheet);
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		SheetOperationUtil.resetAutoFilter(range);
		clearClipboard();
		return true;
	}
	
	protected boolean doReapplyFilter() {
		Sheet sheet = getSheet();
		
		Range range = Ranges.range(sheet);
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		SheetOperationUtil.applyAutoFilter(range);
		clearClipboard();
		return true;
	}
	
	protected boolean doProtectSheet() {
		
		Sheet sheet = getSheet();
		
		Range range = Ranges.range(sheet);
		
		String newpassword = "1234";//TODO, make it meaningful
		if(range.isProtected()){
			SheetOperationUtil.protectSheet(range,null,null);
		}else{
			SheetOperationUtil.protectSheet(range,null,newpassword);
		}
		
		boolean p = range.isProtected();
		
		//TODO re-factor action bar
//		for (Action action : _defaultDisabledActionOnSheetProtected) {
//			getSpreadsheet().setActionDisabled(p, action);
//		}
		

		return true;
	}
	
	protected boolean doGridlines() {
		Sheet sheet = getSheet();
		
		Range range = Ranges.range(sheet);
		
		SheetOperationUtil.displaySheetGridlines(range,!range.isDisplaySheetGridlines());
		return true;
	}
	
	
	protected boolean doChart(Chart.Type type, Chart.Grouping grouping, Chart.LegendPosition pos){
		Sheet sheet = getSheet();
		Rect selection = getSelection();
		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
		if(range.isProtected()){
			showProtectMessage();
			return true;
		}
		
		SheetAnchor anchor = SheetOperationUtil.toChartAnchor(range);
		
		ChartData data = org.zkoss.zss.api.ChartDataUtil.getChartData(sheet,selection, type);
		SheetOperationUtil.addChart(range,anchor,data,type,grouping,pos);
		clearClipboard();
		return true;
		
	}

	protected boolean doColumnChart() {
		return doChart(Chart.Type.Column,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}
	
	protected boolean doColumnChart3D() {
		return doChart(Chart.Type.Column3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doLineChart() {
		return doChart(Chart.Type.Line,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doLineChart3D() {
		return doChart(Chart.Type.Line3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doPieChart() {
		return doChart(Chart.Type.Pie,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doPieChart3D() {
		return doChart(Chart.Type.Pie3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doBarChart() {
		return doChart(Chart.Type.Bar,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doBarChart3D() {
		return doChart(Chart.Type.Bar3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doAreaChart() {
		return doChart(Chart.Type.Area,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doScatterChart() {
		return doChart(Chart.Type.Scatter,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}

	protected boolean doDoughnutChart() {
		return doChart(Chart.Type.Doughnut,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}
	
	private static <T> T checkNotNull(String message, T t) {
		if (t == null) {
			throw new NullPointerException(message);
		}
		return t;
	}

	private static class UserActionContext {

		final Spreadsheet spreadsheet;
		final Sheet sheet;
		final String action;
		final Rect selection;
		final Map extraData;
		
		public UserActionContext(Spreadsheet spreadsheet, Sheet sheet,String action,
				Rect selection, Map extraData) {
			this.spreadsheet = spreadsheet;
			this.sheet = sheet;
			this.action = action;
			this.selection = selection;
			this.extraData = extraData;
		}
	}
	
	/**
	 * Used for copy & paste function
	 * 
	 * @author sam
	 */
	public static class Clipboard {
		public enum Type {
			COPY,
			CUT
		}
		
		public final Type type;
		public final Rect sourceRect;
		public final String sourceSheetName;
		
		public Clipboard(Type type, String sourceSheetName,Rect sourceRect) {
			this.type = checkNotNull("Clipboard's type cannot be null", type);
			this.sourceSheetName = checkNotNull("Clipboard's sourceSheetName cannot be null", sourceSheetName);
			this.sourceRect = checkNotNull("Clipboard's sourceRect cannot be null", sourceRect);
		}
	}
	
	
	
	// non-implemented action
	
	protected boolean doRowHeight() {
		showNotImplement(UserAction.ROW_HEIGHT.toString());
		return true;
	}

	protected boolean doColumnWidth() {
		showNotImplement(UserAction.COLUMN_WIDTH.toString());
		return true;
	}

	protected boolean doFormatCell() {
		showNotImplement(UserAction.FORMAT_CELL.toString());
		return true;
	}

	protected boolean doHyperlink() {
		showNotImplement(UserAction.HYPERLINK.toString());
		return true;
	}

	protected boolean doCustomSort() {
		showNotImplement(UserAction.CUSTOM_SORT.toString());
		return true;
	}

	protected boolean doPasteSpecial() {
		showNotImplement(UserAction.PASTE_SPECIAL.toString());
		return true;
	}

	protected boolean doExportPDF() {
		showNotImplement(UserAction.EXPORT_PDF.toString());
		return true;
	}

	protected boolean doSaveBook() {
		showNotImplement(UserAction.SAVE_BOOK.toString());
		return true;
	}

	protected boolean doNewBook() {
		showNotImplement(UserAction.NEW_BOOK.toString());
		return true;
	}

	protected boolean doShowFormulaPanel() {
		showNotImplement(UserAction.FORMULA_PANEL.toString());
		return true;
	}

	protected boolean doShowInsertPanel() {
		showNotImplement(UserAction.INSERT_PANEL.toString());
		return true;
	}

	protected boolean doShowHomePanel() {
		showNotImplement(UserAction.HOME_PANEL.toString());
		return true;
	}

	protected boolean doInsertFunction() {
		showNotImplement(UserAction.INSERT_FUNCTION.toString());
		return true;
	}
	
	protected void askUploadFile(EventListener l){
		//TODO Need ZK's new feature support
		
	}


	@Override
	public String[] getInterestedEvents() {
		return new String[] { Events.ON_SHEET_SELECT, Events.ON_CTRL_KEY,org.zkoss.zk.ui.event.Events.ON_CANCEL,
				Events.ON_CELL_DOUBLE_CLICK, Events.ON_START_EDITING };
	}
	
	@Override
	public void onEvent(Event event) throws Exception {
		Component comp = event.getTarget();
		if(!(comp instanceof Spreadsheet)) return;
		
		Spreadsheet spreadsheet = (Spreadsheet)comp;
		Rect selection;
		if(event instanceof KeyEvent){
			//respect zss key-even't selection 
			//(could consider to remove this extra spec some day)
			selection = ((KeyEvent)event).getSelection();
		}else{
			selection = spreadsheet.getSelection();
		}
		final UserActionContext ctx = new UserActionContext(spreadsheet, spreadsheet.getSelectedSheet(),"",selection,new HashMap());
		_ctx.set(ctx);
		try{
			onEvent0(event);
		}finally{
			_ctx.set(null);
		}
	}

	private void onEvent0(Event event) throws Exception {

		String nm = event.getName();
		if(Events.ON_SHEET_SELECT.equals(nm)){
			
			updateClipboardHighlightEffect();
			//TODO
			//syncAutoFilter();
			
			//TODO this should be spreadsheet's job
			//toggleActionOnSheetSelected() 
		}else if(Events.ON_CTRL_KEY.equals(nm)){
			KeyEvent kevt = (KeyEvent)event;
			boolean r = doKeystroke(kevt.getKeyCode(), kevt.isCtrlKey(), kevt.isShiftKey(), kevt.isAltKey());
			if(r){
				//to disable client copy/paste feature if there is any server side copy/paste
				if(kevt.isCtrlKey() && kevt.getKeyCode()=='V'){
					getSpreadsheet().smartUpdate("doPasteFromServer", true);
				}
			}
		}else if(Events.ON_CELL_DOUBLE_CLICK.equals(nm)){//TODO check if we need it still
			clearClipboard();
		}else if(Events.ON_START_EDITING.equals(nm)){
			clearClipboard();
		}else if(org.zkoss.zk.ui.event.Events.ON_CANCEL.equals(nm)){
			clearClipboard();
		}
	}
	
	private void updateClipboardHighlightEffect() {
		//to sync the 
		Clipboard cb = getClipboard();
		if (cb != null) {
			//TODO a way to know the book is different already?
			
			final Sheet current = getSheet();
			final Book book = current.getBook();
			final Sheet src = book.getSheet(cb.sourceSheetName);
			if(src==null){
				clearClipboard();
			}else{
				if(current.equals(src)){
					getSpreadsheet().setHighlight(cb.sourceRect);
				}else{
					getSpreadsheet().setHighlight(null);
				}
			}
		}
	}

}
