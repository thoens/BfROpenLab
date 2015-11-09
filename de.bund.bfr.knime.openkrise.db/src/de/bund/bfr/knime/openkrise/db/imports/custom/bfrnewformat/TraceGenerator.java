package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.MyDBI;

import de.bund.bfr.knime.openkrise.common.Station;

public class TraceGenerator {

	private JComponent parent;
	public TraceGenerator(File outputFolder, Station station, JComponent parent, boolean isForward) {
		this.parent = parent;
		try {
			int numFilesGenerated = 0;
			try {
				numFilesGenerated = isForward ? getForStationRequests(outputFolder.getAbsolutePath(), station) : getBackStationRequests(outputFolder.getAbsolutePath(), station);
			}
			catch (Exception e) {e.printStackTrace();}

			String message = "";
			if (numFilesGenerated == 0) message = "No new Templates generated. All done?";
			else message = numFilesGenerated + " new pre-filled templates generated, available in folder '" + outputFolder.getAbsolutePath() + "'";

			IWorkbenchWindow eclipseWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (eclipseWindow != null) {						
				MessageDialog.openInformation(eclipseWindow.getShell(), "Template generation",  message);
			} else {
				JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
				JDialog dialog = pane.createDialog("Template generation");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public TraceGenerator(File outputFolder, List<String> business2Trace, boolean isForward, JComponent parent) {
		this.parent = parent;
		try {
			int numFilesGenerated = 0;
			try {
				numFilesGenerated = isForward ? getFortraceRequests(outputFolder.getAbsolutePath(), business2Trace) : getBacktraceRequests(outputFolder.getAbsolutePath(), business2Trace);
			}
			catch (Exception e) {e.printStackTrace();}

			String message = "";
			if (numFilesGenerated == 0) message = "No new Templates generated. All done?";
			else message = numFilesGenerated + " new pre-filled templates generated, available in folder '" + outputFolder.getAbsolutePath() + "'";

			IWorkbenchWindow eclipseWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (eclipseWindow != null) {						
				MessageDialog.openInformation(eclipseWindow.getShell(), "Template generation",  message);
			} else {
				JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
				JDialog dialog = pane.createDialog("Template generation");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void fillStations(XSSFSheet sheetStations, FormulaEvaluator evaluator) throws SQLException {		
		LinkedHashSet<String> se = getStationExtra();
		XSSFRow row = sheetStations.getRow(0);
		int j=0;
		for (String e : se) {
			if (e != null && !e.isEmpty()) {
				XSSFCell cell = row.getCell(11+j);
				if (cell == null) cell = row.createCell(11+j);
				cell.setCellValue(e);
				j++;
			}
		}
		
		String sql = "Select * from " + MyDBI.delimitL("Station") + " ORDER BY " + MyDBI.delimitL("Serial") + " ASC";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			int rownum = 1;
			do {
				row = sheetStations.getRow(rownum);
				if (row == null) row = sheetStations.createRow(rownum);
				rownum++;
				XSSFCell cell;
				if (rs.getObject("Serial") != null) {cell = row.createCell(0); cell.setCellValue(rs.getString("Serial"));}
				else if (rs.getObject("ID") != null) {cell = row.createCell(0); cell.setCellValue(rs.getString("ID"));}
				if (rs.getObject("Name") != null) {cell = row.createCell(1); cell.setCellValue(rs.getString("Name"));}
				if (rs.getObject("Strasse") != null) {cell = row.createCell(2); cell.setCellValue(rs.getString("Strasse"));}
				if (rs.getObject("Hausnummer") != null) {cell = row.createCell(3); cell.setCellValue(rs.getString("Hausnummer"));}
				if (rs.getObject("PLZ") != null) {cell = row.createCell(4); cell.setCellValue(rs.getString("PLZ"));}
				if (rs.getObject("Ort") != null) {cell = row.createCell(5); cell.setCellValue(rs.getString("Ort"));}
				if (rs.getObject("District") != null) {cell = row.createCell(6); cell.setCellValue(rs.getString("District"));}
				if (rs.getObject("Bundesland") != null) {cell = row.createCell(7); cell.setCellValue(rs.getString("Bundesland"));}
				if (rs.getObject("Land") != null) {cell = row.createCell(8); cell.setCellValue(rs.getString("Land"));}
				if (rs.getObject("Betriebsart") != null) {cell = row.createCell(9); cell.setCellValue(rs.getString("Betriebsart"));}
				//cell = row.getCell(10); evaluator.evaluateFormulaCell(cell);

				fillExtraFields("Station", rs.getObject("ID"), row, se, 11);
				/*
				if (rs.getObject("ID") != null) {
					sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Station' AND " + MyDBI.delimitL("id") + "=" + rs.getInt("ID");
					ResultSet rs2 = DBKernel.getResultSet(sql, false);
					if (rs2 != null && rs2.first()) {
						do {
							String s = rs2.getString("attribute");
							j=0;
							for (String e : se) {
								if (s.equals(e)) {
									cell = row.getCell(11+j);
									if (cell == null) cell = row.createCell(11+j);
									cell.setCellValue(rs2.getString("value"));
									break;
								}
								j++;
							}
						} while (rs2.next());
					}	
				}
				*/
			} while (rs.next());
		}
	}
	private void fillLookup(XSSFWorkbook workbook, XSSFSheet sheetLookup) throws SQLException {
		String sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='Sampling'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		int rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(0);
				if (cell == null) cell = row.createCell(0);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		Name reference = workbook.createName();
		reference.setNameName("Sampling");
		String referenceString = sheetLookup.getSheetName() + "!$A$2:$A$" + (rownum);
		reference.setRefersToFormula(referenceString);				
		
		sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='TypeOfBusiness'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
		rs = DBKernel.getResultSet(sql, false);
		rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(1);
				if (cell == null) cell = row.createCell(1);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		reference = workbook.createName();
		reference.setNameName("ToB");
		referenceString = sheetLookup.getSheetName() + "!$B$2:$B$" + (rownum);
		reference.setRefersToFormula(referenceString);				
		
		sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='Treatment'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
		rs = DBKernel.getResultSet(sql, false);
		rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(2);
				if (cell == null) cell = row.createCell(2);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		reference = workbook.createName();
		reference.setNameName("Treatment");
		referenceString = sheetLookup.getSheetName() + "!$C$2:$C$" + (rownum);
		reference.setRefersToFormula(referenceString);				
		
		sql = "Select * from " + MyDBI.delimitL("LookUps") + " WHERE " + MyDBI.delimitL("type") + "='Units'" + " ORDER BY " + MyDBI.delimitL("value") + " ASC";
		rs = DBKernel.getResultSet(sql, false);
		rownum = 1;
		if (rs != null && rs.first()) {
			do {
				XSSFRow row = sheetLookup.getRow(rownum);
				if (row == null) row = sheetLookup.createRow(rownum);
				XSSFCell cell = row.getCell(3);
				if (cell == null) cell = row.createCell(3);
				cell.setCellValue(rs.getString("value"));
				rownum++;
			} while (rs.next());
		}
		reference = workbook.createName();
		reference.setNameName("Units");
		referenceString = sheetLookup.getSheetName() + "!$D$2:$D$" + (rownum);
		reference.setRefersToFormula(referenceString);			
	}
	private String getStationLookup(String stationID) throws SQLException {
		String sql = "Select * from " + MyDBI.delimitL("Station") + " WHERE " + MyDBI.delimitL("ID") + "=" + stationID;
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			return getStationLookup(rs);
		}
		return null;
	}
	private String getStationLookup(ResultSet rs) throws SQLException {
		String result = rs.getString("Station.Serial");// + ", ";
		/*
		if (rs.getObject(sTable + ".Name") != null) result += rs.getString(sTable + ".Name");
		result += ", ";
		if (rs.getObject(sTable + ".Strasse") != null) result += rs.getString(sTable + ".Strasse");
		result += " ";
		if (rs.getObject(sTable + ".Hausnummer") != null) result += rs.getString(sTable + ".Hausnummer");
		result += ", ";
		if (rs.getObject(sTable + ".Ort") != null) result += rs.getString(sTable + ".Ort");
		result += ", ";
		if (rs.getObject(sTable + ".Land") != null) result += rs.getString(sTable + ".Land");
		*/
		return result;
	}
	private LinkedHashSet<String> getStationExtra() throws SQLException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Station'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				result.add(rs.getString("attribute"));
			} while (rs.next());
		}	
		return result;
	}
	private LinkedHashSet<String> getLotExtra() throws SQLException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Chargen'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				String s = rs.getString("attribute");
				if (!s.equalsIgnoreCase("Production Date") && !s.equalsIgnoreCase("Best before date") && !s.equalsIgnoreCase("Treatment of product during production") && !s.equalsIgnoreCase("Sampling")) result.add(s);
			} while (rs.next());
		}	
		return result;
	}
	private LinkedHashSet<String> getDeliveryExtra() throws SQLException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Lieferungen'";
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				result.add(rs.getString("attribute"));
			} while (rs.next());
		}	
		return result;
	}

	private int getFortraceRequests(String outputFolder, List<String> business2Trace) throws SQLException, IOException {
		int result = 0;
		String tracingBusinessesSQL = "";
		for (String s : business2Trace) {
			tracingBusinessesSQL += " OR " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " = '" + s + "'";
		}
		String sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
				" LEFT JOIN " + MyDBI.delimitL("Chargen") +
				" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
				" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
				" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
				" LEFT JOIN " + MyDBI.delimitL("Station") +
				" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
				" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
				" ON " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") +
				" WHERE " + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Produkt") + " IS NULL " +
				" AND (" + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " IS NULL " + tracingBusinessesSQL + ")" +
				" ORDER BY " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " ASC," + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Bezeichnung") + " ASC";
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Fortrace_sug.xlsx");
				XSSFWorkbook workbook = new XSSFWorkbook(myxls);
				XSSFSheet sheetTracing = workbook.getSheet("FwdTracing");
				XSSFSheet sheetStations = workbook.getSheet("Stations");
				XSSFSheet sheetLookup = workbook.getSheet("LookUp");
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				fillStations(sheetStations, evaluator);
				fillLookup(workbook, sheetLookup);
				LinkedHashSet<String> le = getLotExtra();
				LinkedHashSet<String> de = getDeliveryExtra();

				// Station in Focus
				XSSFRow row = sheetTracing.getRow(4);
				XSSFCell cell;
				String sid = null;
				if (rs.getObject("Lieferungen.Empfänger") != null) {
					sid = getStationLookup(rs.getString("Lieferungen.Empfänger"));
					cell = row.getCell(1); cell.setCellValue(sid);
					cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
				}
				
				// Ingredients for Lot(s)
				row = sheetTracing.getRow(7);
				int j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}
				
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheetTracing);
				LinkedHashSet<String> deliveryNumbers = new LinkedHashSet<>();
				int rowIndex = 9;
				row = sheetTracing.getRow(rowIndex);
				String dn = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, true, null);
				deliveryNumbers.add(dn);
				
				while (rs.next()) {
					if (rs.getObject("Station.Serial") == null) break;
					String sl = getStationLookup(rs);
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 9, rowIndex);
					dn = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, true, null);
					deliveryNumbers.add(dn);
				}
				rs.previous();

				// Lot Information
				row = sheetTracing.getRow(rowIndex + 3);
				j=0;
				for (String e : le) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(17+j);
						if (cell == null) cell = row.createCell(17+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += 5;
				int i=0;
				row = sheetTracing.getRow(rowIndex);
				for (String dns : deliveryNumbers) {
					if (!dns.isEmpty()) {
						if (i > 0) row = copyRow(workbook, sheetTracing, rowIndex, rowIndex + i);
						//todo cell = row.getCell(4); cell.setCellValue(dns);
						insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 1);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 2, "=Units");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 15, "=Treatment");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 16, "=Sampling");
						i++;
					}
				}
				
				Name reference = workbook.createName();
				reference.setNameName("LotNumbers");
				String referenceString = sheetTracing.getSheetName() + "!$A$" + (rowIndex+1) + ":$A$" + (rowIndex+i);
				reference.setRefersToFormula(referenceString);				
				
				// Products Out
				row = sheetTracing.getRow(rowIndex + i + 2);
				j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += i+4;
				for (i=0;i<86;i++) {
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 3, "1", "31");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 4, "1", "12");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 5, "1900", "3000");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 6, "1", "31");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 7, "1", "12");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 8, "1900", "3000");
					insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 9);
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 10, "=Units");
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 11, "=StationIDs");
					//row = sheetTracing.getRow(rowIndex+i);
					//cell = row.getCell(12);
					//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
					//evaluator.evaluateFormulaCell(cell);
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 0, "=LotNumbers");
				}
				for (i=0;i<deliveryNumbers.size();i++) {
					insertDropBox(dvHelper, sheetTracing, 9+i, 0, "=LotNumbers");
				}
				
				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
				if (save(workbook, outputFolder + File.separator + "Forwardtrace_request_" + getValidFileName(rs.getString("Station.Serial")) + ".xlsx")) { //  + "_" + getFormattedDate()
					result++;
				}
				myxls.close();
			} while (rs.next());
		}
		return result;
	}
	private String getFormattedDate() {
		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date resultdate = new Date(yourmilliseconds);
		return sdf.format(resultdate);		
	}
	
	private int getForStationRequests(String outputFolder, Station station) throws SQLException, IOException {
		int result = 0;
		String sql = "Select * from " + MyDBI.delimitL("Station") + " AS " + MyDBI.delimitL("S") +
				" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
				" ON " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
				" LEFT JOIN " + MyDBI.delimitL("Chargen") +
				" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
				" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
				" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
				" LEFT JOIN " + MyDBI.delimitL("Station") +
				" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
				" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
				" ON " + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") +
				" WHERE " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
				" AND " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " IS NOT NULL" +
				" ORDER BY " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Bezeichnung") + " ASC";
		/*
		String sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
				" LEFT JOIN " + MyDBI.delimitL("Chargen") +
				" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
				" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
				" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
				" LEFT JOIN " + MyDBI.delimitL("Station") +
				" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
				" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
				" ON " + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") +
				" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
				" ORDER BY " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Bezeichnung") + " ASC";
				*/
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
				InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Fortrace_sug.xlsx");
				XSSFWorkbook workbook = new XSSFWorkbook(myxls);
				XSSFSheet sheetTracing = workbook.getSheet("FwdTracing");
				XSSFSheet sheetStations = workbook.getSheet("Stations");
				XSSFSheet sheetLookup = workbook.getSheet("LookUp");
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				fillStations(sheetStations, evaluator);
				fillLookup(workbook, sheetLookup);
				LinkedHashSet<String> le = getLotExtra();
				LinkedHashSet<String> de = getDeliveryExtra();

				// Station in Focus
				XSSFRow row = sheetTracing.getRow(4);
				XSSFCell cell;
				String sid = station.getId();
				if (sid != null) {
					cell = row.getCell(1); cell.setCellValue(sid);
					cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
				}
				
				// Ingredients for Lot(s)
				row = sheetTracing.getRow(7);
				int j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}
				
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheetTracing);
				LinkedHashSet<String> deliveryNumbers = new LinkedHashSet<>();
				List<Integer> dbLots = new ArrayList<>();
				int rowIndex = 9;
				row = sheetTracing.getRow(rowIndex);
				String dn = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, true, null);
				deliveryNumbers.add(dn);
				dbLots.add(rs.getInt("ChargenVerbindungen.Produkt"));
				
				while (rs.next()) {
					if (rs.getObject("Station.Serial") == null) break;
					String sl = getStationLookup(rs);
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 9, rowIndex);
					dn = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, true, null);
					deliveryNumbers.add(dn);
					dbLots.add(rs.getInt("ChargenVerbindungen.Produkt"));
				}

				// Lot Information
				row = sheetTracing.getRow(rowIndex + 3);
				j=0;
				for (String e : le) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(17+j);
						if (cell == null) cell = row.createCell(17+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += 5;
				sql = "Select * from " + MyDBI.delimitL("Station") +
						" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") + 
						" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
						" LEFT JOIN " + MyDBI.delimitL("Chargen") + 
						" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
						" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
						" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
						" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
						" ORDER BY " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ChargenNr") + " ASC";
				rs = DBKernel.getResultSet(sql, false);
				int i=0;
				row = sheetTracing.getRow(rowIndex);
				LinkedHashMap<Integer, String> lotDb2Number = new LinkedHashMap<>();
				if (rs != null && rs.first()) {
					do {
						if (rs.getObject("Chargen.ID") != null && dbLots.contains(rs.getInt("Chargen.ID")) && !lotDb2Number.containsKey(rs.getInt("Chargen.ID"))) {
							if (i > 0) row = copyRow(workbook, sheetTracing, rowIndex, rowIndex + i);
							if (rs.getObject("Chargen.ChargenNr") != null) {cell = row.getCell(0); cell.setCellValue(rs.getString("Chargen.ChargenNr"));}
							if (rs.getObject("Chargen.Menge") != null) {cell = row.getCell(1); cell.setCellValue(rs.getDouble("Chargen.Menge"));}
							if (rs.getObject("Chargen.Einheit") != null) {cell = row.getCell(2); cell.setCellValue(rs.getString("Chargen.Einheit"));}							
							if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
								cell = row.getCell(3); cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));
							}

							insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 1);
							insertDropBox(dvHelper, sheetTracing, rowIndex+i, 2, "=Units");
							insertDropBox(dvHelper, sheetTracing, rowIndex+i, 15, "=Treatment");
							insertDropBox(dvHelper, sheetTracing, rowIndex+i, 16, "=Sampling");
							i++;
							lotDb2Number.put(rs.getInt("Chargen.ID"), rs.getString("Chargen.ChargenNr"));
						}
					} while (rs.next());
				}
				if (i==0) i=1;
				
				Name reference = workbook.createName();
				reference.setNameName("LotNumbers");
				String referenceString = sheetTracing.getSheetName() + "!$A$" + (rowIndex+1) + ":$A$" + (rowIndex+i);
				reference.setRefersToFormula(referenceString);				

				for (int ii=0;ii<dbLots.size();ii++) {
					if (lotDb2Number.containsKey(dbLots.get(ii))) {
						row = sheetTracing.getRow(9+ii);
						cell = row.getCell(0);
						if (cell == null) cell = row.createCell(0);
						cell.setCellValue(lotDb2Number.get(dbLots.get(ii)));
					}
					insertDropBox(dvHelper, sheetTracing, 9+ii, 0, "=LotNumbers");
				}
						
				// Products Out
				row = sheetTracing.getRow(rowIndex + i + 2);
				j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += i+4;

				if (rs != null && rs.first() && rs.getObject("Chargen.ChargenNr") != null) {
					boolean didOnce = false;
					do {
						if (didOnce) row = copyRow(workbook, sheetTracing, rowIndex-1, rowIndex);
						else row = sheetTracing.getRow(rowIndex);
						fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, false, null);
						rowIndex++;
						didOnce = true;
					} while (rs.next());
				}

				for (i=0;i<85;i++) {
					doFormats(dvHelper, sheetTracing, rowIndex+i, evaluator);
				}
				
				if (save(workbook, outputFolder + File.separator + "StationFortrace_request_" + getValidFileName(station.getId()) + ".xlsx")) { //  + "_" + getFormattedDate()
					result++;
				}
				myxls.close();
		}
		return result;
	}
	
	private int getBackStationRequests(String outputFolder, Station station) throws SQLException, IOException {
		int result = 0;
		String sql = "Select * from " + MyDBI.delimitL("Station") +
					" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
					" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
					" LEFT JOIN " + MyDBI.delimitL("Chargen") + 
					" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
					" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
					" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
					" WHERE " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
					" ORDER BY " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ChargenNr") + " ASC";
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
				InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Backtrace_sug.xlsx");
				XSSFWorkbook workbook = new XSSFWorkbook(myxls);
				XSSFSheet sheetTracing = workbook.getSheet("BackTracing");
				XSSFSheet sheetStations = workbook.getSheet("Stations");
				XSSFSheet sheetLookup = workbook.getSheet("LookUp");
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				fillStations(sheetStations, evaluator);
				fillLookup(workbook, sheetLookup);
				LinkedHashSet<String> le = getLotExtra();
				LinkedHashSet<String> de = getDeliveryExtra();

				// Station in Focus
				XSSFRow row = sheetTracing.getRow(4);
				XSSFCell cell;
				String sid = null;
				if (rs.getObject("Station.Serial") != null) {
					sid = getStationLookup(rs);
					cell = row.getCell(1); cell.setCellValue(sid);
					cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
				}
				
				// Products Out
				row = sheetTracing.getRow(7);
				int j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}
				
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheetTracing);
				LinkedHashMap<String, Lot> lotNumbers = new LinkedHashMap<>();
				LinkedHashMap<Integer, String> lotDb2Number = new LinkedHashMap<>();
				int rowIndex = 9;
				row = sheetTracing.getRow(rowIndex);
				String ln = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, false, null);
				if (!lotNumbers.containsKey(ln)) {
					Lot l = new Lot();
					l.setNumber(ln);
					if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
					if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
					if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
						Product p = new Product();
						p.setName(rs.getString("Produktkatalog.Bezeichnung"));
						l.setProduct(p);
					}
					l.setDbId(rs.getInt("Chargen.ID"));
					lotNumbers.put(ln, l);
				}
				lotDb2Number.put(rs.getInt("Chargen.ID"), ln);
				
				while (rs.next()) {
					if (rs.getObject("Station.Serial") == null) break;
					String sl = getStationLookup(rs);
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 9, rowIndex);
					ln = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, false, null);
					if (!lotNumbers.containsKey(ln)) {
						Lot l = new Lot();
						l.setNumber(ln);
						if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
						if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
						if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
							Product p = new Product();
							p.setName(rs.getString("Produktkatalog.Bezeichnung"));
							l.setProduct(p);
						}
						l.setDbId(rs.getInt("Chargen.ID"));
						lotNumbers.put(ln, l);
					}
					lotDb2Number.put(rs.getInt("Chargen.ID"), ln);
				}
				rs.previous();
				
				// Lot Information
				row = sheetTracing.getRow(rowIndex + 3);
				j=0;
				for (String e : le) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(17+j);
						if (cell == null) cell = row.createCell(17+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += 5;
				int i=0;
				row = sheetTracing.getRow(rowIndex);
				for (Lot lot : lotNumbers.values()) {
					if (lot != null && !lot.getNumber().isEmpty()) {
						if (i > 0) row = copyRow(workbook, sheetTracing, rowIndex, rowIndex + i);
						cell = row.getCell(0); cell.setCellValue(lot.getNumber());
						if (lot.getUnitNumber() != null) {
							cell = row.getCell(1); cell.setCellValue(lot.getUnitNumber());
						}
						if (lot.getUnitUnit() != null) {
							cell = row.getCell(2); cell.setCellValue(lot.getUnitUnit());							
						}
						if (lot.getProduct() != null && lot.getProduct().getName() != null) {
							cell = row.getCell(3); cell.setCellValue(lot.getProduct().getName());
						}
						LinkedHashSet<String> le0 = new LinkedHashSet<>();
						le0.add("Production Date");
						le0.add("Best before date");
						le0.add("Treatment of product during production");
						le0.add("Sampling");
						le0.addAll(le);
						fillExtraFields("Chargen", lot.getDbId(), row, le0, 13);
						insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 1);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 2, "=Units");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 15, "=Treatment");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 16, "=Sampling");
						i++;
					}
				}
				
				Name reference = workbook.createName();
				reference.setNameName("LotNumbers");
				String referenceString = sheetTracing.getSheetName() + "!$A$" + (rowIndex+1) + ":$A$" + (rowIndex+i);
				reference.setRefersToFormula(referenceString);				
				
				String sif = getValidFileName(rs.getString("Station.Serial")); //  + "_" + getFormattedDate()

				// Ingredients for Lot(s)
				row = sheetTracing.getRow(rowIndex + i + 2);
				j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}
				rowIndex += i+4;

				sql = "Select * from " + MyDBI.delimitL("Station") + " AS " + MyDBI.delimitL("S") +
						" LEFT JOIN " + MyDBI.delimitL("Lieferungen") +
						" ON " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Empfänger") +
						" LEFT JOIN " + MyDBI.delimitL("Chargen") +
						" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
						" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
						" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
						" LEFT JOIN " + MyDBI.delimitL("Station") +
						" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
						" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
						" ON " + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("ID") +
						" WHERE " + MyDBI.delimitL("S") + "." + MyDBI.delimitL("Serial") + " = '" + station.getId() + "'" +
						" AND " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " IS NOT NULL" +			
						" ORDER BY " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Bezeichnung") + " ASC";
				//System.out.println(sql);
				rs = DBKernel.getResultSet(sql, false);
				if (rs != null && rs.first()) {
					LinkedHashSet<String> deliveryNumbers = new LinkedHashSet<>();
					row = sheetTracing.getRow(rowIndex);
					String dn = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, null, lotDb2Number);
					doFormats(dvHelper, sheetTracing, rowIndex, evaluator);
					deliveryNumbers.add(dn);
					
					boolean didOnce = false;
					while (rs.next()) {
						if (rs.getObject("Station.Serial") == null) break;
						String sl = getStationLookup(rs);
						if (!sl.equals(sid)) break;
						rowIndex++;
						if (didOnce) row = copyRow(workbook, sheetTracing, rowIndex-1, rowIndex);
						else row = sheetTracing.getRow(rowIndex);
						dn = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, null, lotDb2Number);
						doFormats(dvHelper, sheetTracing, rowIndex, evaluator);
						deliveryNumbers.add(dn);		
						didOnce = true;
					}
					rowIndex++;
				}
				for (i=0;i<84;i++) {
					doFormats(dvHelper, sheetTracing, rowIndex+i, evaluator);
				}
				
				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
				if (save(workbook, outputFolder + File.separator + "StationBacktrace_request_" + sif + ".xlsx")) {
					result++;
				}
				myxls.close();
		}
		return result;
	}
	private void doFormats(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int rowIndex, FormulaEvaluator evaluator) {
		insertCondition(dvHelper, sheetTracing, rowIndex, 3, "1", "31");
		insertCondition(dvHelper, sheetTracing, rowIndex, 4, "1", "12");
		insertCondition(dvHelper, sheetTracing, rowIndex, 5, "1900", "3000");
		insertCondition(dvHelper, sheetTracing, rowIndex, 6, "1", "31");
		insertCondition(dvHelper, sheetTracing, rowIndex, 7, "1", "12");
		insertCondition(dvHelper, sheetTracing, rowIndex, 8, "1900", "3000");
		insertDecCondition(dvHelper, sheetTracing, rowIndex, 9);
		insertDropBox(dvHelper, sheetTracing, rowIndex, 10, "=Units");
		insertDropBox(dvHelper, sheetTracing, rowIndex, 11, "=StationIDs");
		//XSSFRow row = sheetTracing.getRow(rowIndex);
		//XSSFCell cell = row.getCell(12);
		//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
		//evaluator.evaluateFormulaCell(cell);
		insertDropBox(dvHelper, sheetTracing, rowIndex, 0, "=LotNumbers");		
	}
	private int getBacktraceRequests(String outputFolder, List<String> business2Backtrace) throws SQLException, IOException {
		int result = 0;
		String sql;
			String backtracingBusinessesSQL = "";
			for (String s : business2Backtrace) {
				backtracingBusinessesSQL += " OR " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " = '" + s + "'";
			}
			sql = "Select * from " + MyDBI.delimitL("Lieferungen") +
					" LEFT JOIN " + MyDBI.delimitL("Chargen") +
					" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Lieferungen") + "." + MyDBI.delimitL("Charge") +
					" LEFT JOIN " + MyDBI.delimitL("ChargenVerbindungen") +
					" ON " + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Produkt") +
					" LEFT JOIN " + MyDBI.delimitL("Produktkatalog") +
					" ON " + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("Artikel") +
					" LEFT JOIN " + MyDBI.delimitL("Station") +
					" ON " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + "=" + MyDBI.delimitL("Produktkatalog") + "." + MyDBI.delimitL("Station") +
					" WHERE " + MyDBI.delimitL("ChargenVerbindungen") + "." + MyDBI.delimitL("Zutat") + " IS NULL " +
					" AND (" + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("Betriebsart") + " IS NULL " + backtracingBusinessesSQL + ")" +
					" ORDER BY " + MyDBI.delimitL("Station") + "." + MyDBI.delimitL("ID") + " ASC," + MyDBI.delimitL("Chargen") + "." + MyDBI.delimitL("ChargenNr") + " ASC";
		//System.err.println(sql);
		ResultSet rs = DBKernel.getResultSet(sql, false);
		if (rs != null && rs.first()) {
			do {
				InputStream myxls = this.getClass().getResourceAsStream("/de/bund/bfr/knime/openkrise/db/imports/custom/bfrnewformat/BfR_Format_Backtrace_sug.xlsx");
				XSSFWorkbook workbook = new XSSFWorkbook(myxls);
				XSSFSheet sheetTracing = workbook.getSheet("BackTracing");
				XSSFSheet sheetStations = workbook.getSheet("Stations");
				XSSFSheet sheetLookup = workbook.getSheet("LookUp");
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				fillStations(sheetStations, evaluator);
				fillLookup(workbook, sheetLookup);
				LinkedHashSet<String> le = getLotExtra();
				LinkedHashSet<String> de = getDeliveryExtra();

				// Station in Focus
				XSSFRow row = sheetTracing.getRow(4);
				XSSFCell cell;
				String sid = null;
				if (rs.getObject("Station.Serial") != null) {
					sid = getStationLookup(rs);
					cell = row.getCell(1); cell.setCellValue(sid);
					cell = row.getCell(2); evaluator.evaluateFormulaCell(cell);
				}
				
				// Products Out
				row = sheetTracing.getRow(7);
				int j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}
				
				XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheetTracing);
				LinkedHashMap<String, Lot> lotNumbers = new LinkedHashMap<>();
				int rowIndex = 9;
				row = sheetTracing.getRow(rowIndex);
				String ln = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, false, null);
				if (!lotNumbers.containsKey(ln)) {
					Lot l = new Lot();
					l.setNumber(ln);
					if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
					if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
					if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
						Product p = new Product();
						p.setName(rs.getString("Produktkatalog.Bezeichnung"));
						l.setProduct(p);
					}
					l.setDbId(rs.getInt("Chargen.ID"));
					lotNumbers.put(ln, l);
				}
				
				while (rs.next()) {
					if (rs.getObject("Station.Serial") == null) break;
					String sl = getStationLookup(rs);
					if (!sl.equals(sid)) break;
					rowIndex++;
					row = copyRow(workbook, sheetTracing, 9, rowIndex);
					ln = fillRow(dvHelper, sheetTracing, rs, row, evaluator, de, false, null);
					if (!lotNumbers.containsKey(ln)) {
						Lot l = new Lot();
						l.setNumber(ln);
						if (rs.getObject("Chargen.Menge") != null) l.setUnitNumber(rs.getDouble("Chargen.Menge"));
						if (rs.getObject("Chargen.Einheit") != null) l.setUnitUnit(rs.getString("Chargen.Einheit"));
						if (rs.getObject("Produktkatalog.Bezeichnung") != null) {
							Product p = new Product();
							p.setName(rs.getString("Produktkatalog.Bezeichnung"));
							l.setProduct(p);
						}
						l.setDbId(rs.getInt("Chargen.ID"));
						lotNumbers.put(ln, l);
					}
				}
				rs.previous();

				// Lot Information
				row = sheetTracing.getRow(rowIndex + 3);
				j=0;
				for (String e : le) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(17+j);
						if (cell == null) cell = row.createCell(17+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += 5;
				int i=0;
				row = sheetTracing.getRow(rowIndex);
				for (Lot lot : lotNumbers.values()) {
					if (lot != null && !lot.getNumber().isEmpty()) {
						if (i > 0) row = copyRow(workbook, sheetTracing, rowIndex, rowIndex + i);
						cell = row.getCell(0); cell.setCellValue(lot.getNumber());
						if (lot.getUnitNumber() != null) {
							cell = row.getCell(1); cell.setCellValue(lot.getUnitNumber());
						}
						if (lot.getUnitUnit() != null) {
							cell = row.getCell(2); cell.setCellValue(lot.getUnitUnit());							
						}
						if (lot.getProduct() != null && lot.getProduct().getName() != null) {
							cell = row.getCell(3); cell.setCellValue(lot.getProduct().getName());
						}
						LinkedHashSet<String> le0 = new LinkedHashSet<>();
						le0.add("Production Date");
						le0.add("Best before date");
						le0.add("Treatment of product during production");
						le0.add("Sampling");
						le0.addAll(le);
						fillExtraFields("Chargen", lot.getDbId(), row, le0, 13);
						insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 1);
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 2, "=Units");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 15, "=Treatment");
						insertDropBox(dvHelper, sheetTracing, rowIndex+i, 16, "=Sampling");
						i++;
					}
				}
				
				Name reference = workbook.createName();
				reference.setNameName("LotNumbers");
				String referenceString = sheetTracing.getSheetName() + "!$A$" + (rowIndex+1) + ":$A$" + (rowIndex+i);
				reference.setRefersToFormula(referenceString);				
				
				// Ingredients for Lot(s)
				row = sheetTracing.getRow(rowIndex + i + 2);
				j=0;
				for (String e : de) {
					if (e != null && !e.isEmpty()) {
						cell = row.getCell(13+j);
						if (cell == null) cell = row.createCell(13+j);
						cell.setCellValue(e);
						j++;
					}
				}

				rowIndex += i+4;
				for (i=0;i<86;i++) {
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 3, "1", "31");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 4, "1", "12");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 5, "1900", "3000");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 6, "1", "31");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 7, "1", "12");
					insertCondition(dvHelper, sheetTracing, rowIndex+i, 8, "1900", "3000");
					insertDecCondition(dvHelper, sheetTracing, rowIndex+i, 9);
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 10, "=Units");
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 11, "=StationIDs");
					//row = sheetTracing.getRow(rowIndex+i);
					//cell = row.getCell(12);
					//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
					//evaluator.evaluateFormulaCell(cell);
					insertDropBox(dvHelper, sheetTracing, rowIndex+i, 0, "=LotNumbers");
				}
				
				//System.err.println(rs.getInt("Lieferungen.ID") + "\t" + rs.getInt("Chargen.ID"));
				if (save(workbook, outputFolder + File.separator + "Backtrace_request_" + getValidFileName(rs.getString("Station.Serial")) + ".xlsx")) { //  + "_" + getFormattedDate()
					result++;
				}
				myxls.close();
			} while (rs.next());
		}
		return result;
	}
	
	private void insertDropBox(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col, String ref) {
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(ref);
		CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
		XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.setSuppressDropDownArrow(true);
		validation.setShowPromptBox(true);
		sheetTracing.addValidationData(validation);
	}
	private void insertCondition(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col, String min, String max) {
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createIntegerConstraint(OperatorType.BETWEEN, min, max);
		// dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"0000011", "0000021", "0000031"});
		CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
		XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.setSuppressDropDownArrow(true);
		validation.setShowPromptBox(true);
		sheetTracing.addValidationData(validation);
	}
	private void insertDecCondition(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, int row, int col) {
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createDecimalConstraint(OperatorType.GREATER_OR_EQUAL, "0", "");
		// dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(new String[]{"0000011", "0000021", "0000031"});
		CellRangeAddressList addressList = new CellRangeAddressList(row, row, col, col);
		XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.setSuppressDropDownArrow(true);
		validation.setShowPromptBox(true);
		sheetTracing.addValidationData(validation);
	}
	
	private String fillRow(XSSFDataValidationHelper dvHelper, XSSFSheet sheetTracing, ResultSet rs, XSSFRow row, FormulaEvaluator evaluator, LinkedHashSet<String> de, Boolean isForward, LinkedHashMap<Integer, String> lotDb2Number) throws SQLException {
		String result = null;
		
		XSSFCell cell;
		if (isForward == null || isForward) {
			cell = row.getCell(1);
			if (rs.getObject("Produktkatalog.Bezeichnung") != null) cell.setCellValue(rs.getString("Produktkatalog.Bezeichnung"));
			else cell.setCellValue("");
			cell = row.getCell(2);
			if (rs.getObject("Chargen.ChargenNr") != null) cell.setCellValue(rs.getString("Chargen.ChargenNr"));
			else cell.setCellValue("(autoLot" + row.getRowNum() + ")");
			result = cell.getStringCellValue();
		}
		else {
			cell = row.getCell(0);
			if (rs.getObject("Chargen.ChargenNr") != null) cell.setCellValue(rs.getString("Chargen.ChargenNr"));
			else cell.setCellValue("(autoLot" + row.getRowNum() + ")");
			result = cell.getStringCellValue();
		}
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 3, "1", "31");
		cell = row.getCell(3);
		if (rs.getObject("Lieferungen.dd_day") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_day"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 4, "1", "12");
		cell = row.getCell(4);
		if (rs.getObject("Lieferungen.dd_month") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_month"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 5, "1900", "3000");
		cell = row.getCell(5);
		if (rs.getObject("Lieferungen.dd_year") != null) cell.setCellValue(rs.getInt("Lieferungen.dd_year"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 6, "1", "31");
		cell = row.getCell(6);
		if (rs.getObject("Lieferungen.ad_day") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_day"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 7, "1", "12");
		cell = row.getCell(7);
		if (rs.getObject("Lieferungen.ad_month") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_month"));
		else cell.setCellValue("");
		insertCondition(dvHelper, sheetTracing, row.getRowNum(), 8, "1900", "3000");
		cell = row.getCell(8);
		if (rs.getObject("Lieferungen.ad_year") != null) cell.setCellValue(rs.getInt("Lieferungen.ad_year"));
		else cell.setCellValue("");
		insertDecCondition(dvHelper, sheetTracing, row.getRowNum(), 9);
		cell = row.getCell(9);
		if (rs.getObject("Lieferungen.numPU") != null) cell.setCellValue(rs.getDouble("Lieferungen.numPU"));
		else cell.setCellValue("");
		insertDropBox(dvHelper, sheetTracing, row.getRowNum(), 10, "=Units");
		cell = row.getCell(10);
		if (rs.getObject("Lieferungen.typePU") != null) cell.setCellValue(rs.getString("Lieferungen.typePU"));
		else cell.setCellValue("");
		
		cell = row.getCell(11);
		String stationBez = "Lieferungen.Empfänger";
		if (isForward == null || isForward) stationBez = "Produktkatalog.Station";
		if (rs.getObject(stationBez) != null) cell.setCellValue(getStationLookup(rs.getString(stationBez)));
		else cell.setCellValue("");
		//cell = row.getCell(12);
		//cell.setCellFormula("INDEX(Companies,MATCH(L" + (row.getRowNum() + 1) + ",StationIDs,0),1)");
		//evaluator.evaluateFormulaCell(cell);
		
		if (isForward == null) {
			cell = row.getCell(0);
			if (rs.getObject("ChargenVerbindungen.Produkt") != null && lotDb2Number != null && lotDb2Number.containsKey(rs.getInt("ChargenVerbindungen.Produkt"))) cell.setCellValue(lotDb2Number.get(rs.getInt("ChargenVerbindungen.Produkt")));
			else cell.setCellValue("");
		}
		// DeliveryID
		cell = row.getCell(12);
		if (rs.getObject("Lieferungen.Serial") != null) cell.setCellValue(rs.getString("Lieferungen.Serial"));
		else if (rs.getObject("Lieferungen.ID") != null) cell.setCellValue(rs.getString("Lieferungen.ID"));
		else cell.setCellValue("");

		if (isForward == null || isForward) result = cell.getStringCellValue();
		
		fillExtraFields("Lieferungen", rs.getObject("Lieferungen.ID"), row, de, 13);
		/*
		// ExtraFields
		if (rs.getObject("Lieferungen.ID") != null) {
			String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='Lieferungen' AND " + MyDBI.delimitL("id") + "=" + rs.getInt("Lieferungen.ID");
			ResultSet rs2 = DBKernel.getResultSet(sql, false);
			if (rs2 != null && rs2.first()) {
				do {
					String s = rs2.getString("attribute");
					int j=0;
					for (String e : de) {
						if (s.equals(e)) {
							cell = row.getCell(13+j);
							if (cell == null) cell = row.createCell(13+j);
							cell.setCellValue(rs2.getString("value"));
							break;
						}
						j++;
					}
				} while (rs2.next());
			}	
		}
		*/
		
		return result;
	}
	private void fillExtraFields(String tablename, Object id, XSSFRow row, LinkedHashSet<String> de, int startCol) throws SQLException {
		// ExtraFields
		if (id != null) {
			String sql = "Select * from " + MyDBI.delimitL("ExtraFields") + " WHERE " + MyDBI.delimitL("tablename") + "='" + tablename  + "' AND " + MyDBI.delimitL("id") + "=" + id;
			ResultSet rs2 = DBKernel.getResultSet(sql, false);
			if (rs2 != null && rs2.first()) {
				do {
					String s = rs2.getString("attribute");
					int j=0;
					for (String e : de) {
						if (s.equalsIgnoreCase(e)) {
							XSSFCell cell = row.getCell(startCol+j);
							if (cell == null) cell = row.createCell(startCol+j);
							cell.setCellValue(rs2.getString("value"));
							break;
						}
						j++;
					}
				} while (rs2.next());
			}	
		}		
	}
	   private XSSFRow copyRow(XSSFWorkbook workbook, XSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {
	        XSSFRow sourceRow = worksheet.getRow(sourceRowNum);
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1, true, false);
            XSSFRow newRow = worksheet.createRow(destinationRowNum);

	        // Loop through source columns to add to new row
	        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
	            // Grab a copy of the old/new cell
	            XSSFCell oldCell = sourceRow.getCell(i);
	            XSSFCell newCell = newRow.createCell(i);

	            // If the old cell is null jump to next cell
	            if (oldCell == null) {
	                newCell = null;
	                continue;
	            }

	            // Copy style from old cell and apply to new cell
	            XSSFCellStyle newCellStyle = workbook.createCellStyle();
	            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
	            
	            newCell.setCellStyle(newCellStyle);

	            // Set the cell data type
	            newCell.setCellType(oldCell.getCellType());

	        }

	        // If there are are any merged regions in the source row, copy to new row
	        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
	            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
	            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
	                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
	                        (newRow.getRowNum() +
	                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
	                                        )),
	                        cellRangeAddress.getFirstColumn(),
	                        cellRangeAddress.getLastColumn());
	                worksheet.addMergedRegion(newCellRangeAddress);
	            }
	        }
	        
	        newRow.setHeight(sourceRow.getHeight());
	        
	        return newRow;
	    }
	   private boolean save(XSSFWorkbook workbook, String filename) {
		try {
			File f = new File(filename);
			if (f.exists()) {
				int returnVal = JOptionPane.showConfirmDialog(parent, "Replace file '" + filename + "'?", "Excel file '" + filename + "' exists already", JOptionPane.YES_NO_OPTION);
				if (returnVal == JOptionPane.NO_OPTION) return false;
				else if (returnVal == JOptionPane.YES_OPTION) ;
				else return false;
			}
			POIXMLProperties.CoreProperties coreProp = workbook.getProperties().getCoreProperties();
			coreProp.setCreator("FoodChain-Lab");
			coreProp.setCreated(new Nullable<Date>(new Date(System.currentTimeMillis())));
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(f);
			workbook.write(out);
			out.close();
			System.out.println(filename + " written successfully on disk.");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	   private String getValidFileName(String fileName) {
		    String newFileName = fileName.replaceAll("[:\\\\/*?|<>]", "_");
		    if (newFileName.length()==0)
		        throw new IllegalStateException(
		                "File Name " + fileName + " results in an empty fileName!");
		    return newFileName;
		}	   
}
