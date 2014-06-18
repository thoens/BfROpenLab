/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.bund.bfr.knime.nls.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import de.bund.bfr.knime.UI;

public class ChartSelectionPanel extends JPanel implements ItemListener,
		CellEditorListener {

	private static final long serialVersionUID = 1L;

	private List<SelectionListener> listeners;
	private ColorAndShapeCreator colorAndShapes;

	private JTable selectTable;
	private JCheckBox selectAllBox;

	private int selectColumnWidth;

	public ChartSelectionPanel(List<String> ids,
			Map<String, List<String>> stringValues,
			Map<String, List<Double>> doubleValues) {
		listeners = new ArrayList<>();
		colorAndShapes = new ColorAndShapeCreator(ids.size());

		selectAllBox = new JCheckBox("Select All");
		selectAllBox.setSelected(false);
		selectAllBox.addItemListener(this);

		selectTable = new JTable(new SelectTableModel(ids, stringValues,
				doubleValues, colorAndShapes.getColorList(),
				colorAndShapes.getShapeNameList()));
		selectTable.setRowSelectionAllowed(false);
		selectTable.setColumnSelectionAllowed(false);
		selectTable.getTableHeader().setResizingAllowed(false);
		selectTable
				.setRowHeight((new JComboBox<String>()).getPreferredSize().height);
		selectTable.setRowSorter(new SelectTableRowSorter(
				(SelectTableModel) selectTable.getModel()));
		selectTable.getColumn(ChartUtilities.ID).setMinWidth(0);
		selectTable.getColumn(ChartUtilities.ID).setMaxWidth(0);
		selectTable.getColumn(ChartUtilities.ID).setPreferredWidth(0);
		selectTable.getColumn(ChartUtilities.SELECTED).setCellEditor(
				new CheckBoxEditor());
		selectTable.getColumn(ChartUtilities.SELECTED).setCellRenderer(
				new CheckBoxRenderer());
		selectTable.getColumn(ChartUtilities.SELECTED).getCellEditor()
				.addCellEditorListener(this);
		selectTable.getColumn(ChartUtilities.COLOR).setCellEditor(
				new ColorEditor());
		selectTable.getColumn(ChartUtilities.COLOR).setCellRenderer(
				new ColorRenderer());
		selectTable.getColumn(ChartUtilities.SHAPE).setCellEditor(
				new DefaultCellEditor(new JComboBox<>(
						ColorAndShapeCreator.SHAPE_NAMES)));
		selectTable.getColumn(ChartUtilities.COLOR).getCellEditor()
				.addCellEditorListener(this);
		selectTable.getColumn(ChartUtilities.SHAPE).getCellEditor()
				.addCellEditorListener(this);
		selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		for (int c = 0; c < selectTable.getColumnCount(); c++) {
			TableColumn col = selectTable.getColumnModel().getColumn(c);

			if (col.getPreferredWidth() == 0) {
				continue;
			}

			TableCellRenderer renderer = col.getHeaderRenderer();
			Component comp = selectTable
					.getTableHeader()
					.getDefaultRenderer()
					.getTableCellRendererComponent(selectTable,
							col.getHeaderValue(), false, false, 0, 0);
			int width = comp.getPreferredSize().width;

			for (int r = 0; r < selectTable.getRowCount(); r++) {
				renderer = selectTable.getCellRenderer(r, c);
				comp = renderer.getTableCellRendererComponent(selectTable,
						selectTable.getValueAt(r, c), false, false, r, c);
				width = Math.max(width, comp.getPreferredSize().width);
			}

			col.setPreferredWidth(width += 10);
		}

		selectColumnWidth = selectTable.getColumn(ChartUtilities.SELECTED)
				.getPreferredWidth();

		setLayout(new BorderLayout());
		add(UI.createWestPanel(selectAllBox), BorderLayout.NORTH);
		add(new JScrollPane(selectTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				BorderLayout.CENTER);
	}

	public boolean isSelectAll() {
		return selectAllBox.isSelected();
	}

	public void setSelectAll(boolean selectAll) {
		selectAllBox.setSelected(selectAll);
	}

	public List<String> getSelectedIds() {
		List<String> selectedIds = new ArrayList<>();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			if ((Boolean) selectTable.getValueAt(i, 1)) {
				selectedIds.add((String) selectTable.getValueAt(i, 0));
			}
		}

		return selectedIds;
	}

	public void setSelectedIds(List<String> selectedIds) {
		Set<String> idSet = new LinkedHashSet<>(selectedIds);

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			if (idSet.contains(selectTable.getValueAt(i, 0))) {
				selectTable.setValueAt(true, i, 1);
			} else {
				selectTable.setValueAt(false, i, 1);
			}
		}

		fireSelectionChanged();
	}

	public Map<String, Color> getColors() {
		Map<String, Color> paints = new LinkedHashMap<>(
				selectTable.getRowCount());

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			paints.put((String) selectTable.getValueAt(i, 0),
					(Color) selectTable.getValueAt(i, 2));
		}

		return paints;
	}

	public void setColors(Map<String, Color> colors) {
		for (int i = 0; i < selectTable.getRowCount(); i++) {
			Color color = colors.get(selectTable.getValueAt(i, 0));

			if (color != null) {
				selectTable.setValueAt(color, i, 2);
			}
		}
	}

	public Map<String, Shape> getShapes() {
		Map<String, Shape> shapes = new LinkedHashMap<>(
				selectTable.getRowCount());
		Map<String, Shape> shapeMap = colorAndShapes.getShapeByNameMap();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			shapes.put((String) selectTable.getValueAt(i, 0),
					shapeMap.get(selectTable.getValueAt(i, 3)));
		}

		return shapes;
	}

	public void setShapes(Map<String, Shape> shapes) {
		Map<Shape, String> shapeMap = colorAndShapes.getNameByShapeMap();

		for (int i = 0; i < selectTable.getRowCount(); i++) {
			Shape shape = shapes.get(selectTable.getValueAt(i, 0));

			if (shape != null) {
				selectTable.setValueAt(shapeMap.get(shape), i, 3);
			}
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	public void fireSelectionChanged() {
		for (SelectionListener listener : listeners) {
			listener.selectionChanged();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (selectAllBox.isSelected()) {
			selectTable.getColumn(ChartUtilities.SELECTED).setMinWidth(0);
			selectTable.getColumn(ChartUtilities.SELECTED).setMaxWidth(0);
			selectTable.getColumn(ChartUtilities.SELECTED).setPreferredWidth(0);
		} else {
			selectTable.getColumn(ChartUtilities.SELECTED).setMinWidth(
					selectColumnWidth);
			selectTable.getColumn(ChartUtilities.SELECTED).setMaxWidth(
					selectColumnWidth);
			selectTable.getColumn(ChartUtilities.SELECTED).setPreferredWidth(
					selectColumnWidth);
		}

		fireSelectionChanged();
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		fireSelectionChanged();
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
	}

	public static interface SelectionListener {

		public void selectionChanged();
	}

	private static class SelectTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<Boolean> selections;

		private List<String> ids;
		private Map<String, List<String>> stringColumns;
		private Map<String, List<Double>> doubleColumns;
		private List<Color> colors;
		private List<String> shapes;

		private Map<Integer, String> stringByIndex;
		private Map<Integer, String> doubleByIndex;

		public SelectTableModel(List<String> ids,
				Map<String, List<String>> stringColumns,
				Map<String, List<Double>> doubleColumns, List<Color> colors,
				List<String> shapes) {
			if (stringColumns == null) {
				stringColumns = new LinkedHashMap<>();
			}

			if (doubleColumns == null) {
				doubleColumns = new LinkedHashMap<>();
			}

			this.ids = ids;
			this.stringColumns = stringColumns;
			this.doubleColumns = doubleColumns;
			this.colors = colors;
			this.shapes = shapes;

			selections = new ArrayList<>(Collections.nCopies(ids.size(), false));
			stringByIndex = new LinkedHashMap<>();
			doubleByIndex = new LinkedHashMap<>();

			int columnIndex = 4;

			for (String column : stringColumns.keySet()) {
				stringByIndex.put(columnIndex, column);
				columnIndex++;
			}

			for (String column : doubleColumns.keySet()) {
				doubleByIndex.put(columnIndex, column);
				columnIndex++;
			}
		}

		@Override
		public int getColumnCount() {
			return stringColumns.size() + doubleColumns.size() + 4;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return ChartUtilities.ID;
			case 1:
				return ChartUtilities.SELECTED;
			case 2:
				return ChartUtilities.COLOR;
			case 3:
				return ChartUtilities.SHAPE;
			default:
				if (stringByIndex.containsKey(column)) {
					return stringByIndex.get(column);
				} else if (doubleByIndex.containsKey(column)) {
					return doubleByIndex.get(column);
				}

				return null;
			}
		}

		@Override
		public int getRowCount() {
			return ids.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:
				return ids.get(row);
			case 1:
				return selections.get(row);
			case 2:
				return colors.get(row);
			case 3:
				return shapes.get(row);
			default:
				if (stringByIndex.containsKey(column)) {
					return stringColumns.get(stringByIndex.get(column))
							.get(row);
				} else if (doubleByIndex.containsKey(column)) {
					return doubleColumns.get(doubleByIndex.get(column))
							.get(row);
				}

				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return String.class;
			case 1:
				return Boolean.class;
			case 2:
				return Color.class;
			case 3:
				return String.class;
			default:
				if (stringByIndex.containsKey(column)) {
					return String.class;
				} else if (doubleByIndex.containsKey(column)) {
					return Double.class;
				}

				return null;
			}
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			switch (column) {
			case 1:
				selections.set(row, (Boolean) value);
				break;
			case 2:
				colors.set(row, (Color) value);
				break;
			case 3:
				shapes.set(row, (String) value);
				break;
			}

			fireTableCellUpdated(row, column);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 1 || column == 2 || column == 3;
		}
	}

	private static class ColorRenderer extends JLabel implements
			TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ColorRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setBackground((Color) color);

			return this;
		}
	}

	private static class ColorEditor extends AbstractCellEditor implements
			TableCellEditor {

		private static final long serialVersionUID = 1L;

		private JButton colorButton;

		public ColorEditor() {
			colorButton = new JButton();
			colorButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Color newColor = JColorChooser.showDialog(colorButton,
							"Choose Color", colorButton.getBackground());

					if (newColor != null) {
						colorButton.setBackground(newColor);
						stopCellEditing();
					}
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			colorButton.setBackground((Color) value);

			return colorButton;
		}

		@Override
		public Object getCellEditorValue() {
			return colorButton.getBackground();
		}

	}

	private static class CheckBoxRenderer extends JCheckBox implements
			TableCellRenderer {

		private static final long serialVersionUID = -8337460338388283099L;

		public CheckBoxRenderer() {
			super();
			setHorizontalAlignment(SwingConstants.CENTER);
			setBorderPainted(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			int statusColumn = -1;

			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumnName(i).equals(ChartUtilities.STATUS)) {
					statusColumn = i;
					break;
				}
			}

			if (statusColumn != -1) {
				String statusValue = (String) table.getValueAt(row,
						statusColumn);

				if (statusValue.equals(Plotable.Status.OK.toString())) {
					if (isSelected) {
						setForeground(table.getSelectionForeground());
						setBackground(table.getSelectionBackground());
					} else {
						setForeground(table.getForeground());
						setBackground(table.getBackground());
					}
				} else if (statusValue
						.equals(Plotable.Status.FAILED.toString())) {
					setForeground(Color.RED);
					setBackground(Color.RED);
				} else if (statusValue.equals(Plotable.Status.NO_COVARIANCE
						.toString())) {
					setForeground(Color.YELLOW);
					setBackground(Color.YELLOW);
				}
			} else {
				if (isSelected) {
					setForeground(table.getSelectionForeground());
					setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getForeground());
					setBackground(table.getBackground());
				}
			}

			setSelected((value != null && ((Boolean) value).booleanValue()));

			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			} else {
				setBorder(new EmptyBorder(1, 1, 1, 1));
			}

			return this;
		}
	}

	private static class CheckBoxEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;

		public CheckBoxEditor() {
			super(new JCheckBox());
			((JCheckBox) getComponent())
					.setHorizontalAlignment(SwingConstants.CENTER);
		}
	}

	private static class SelectTableRowSorter extends
			TableRowSorter<SelectTableModel> {

		public SelectTableRowSorter(SelectTableModel model) {
			super(model);
		}

		@Override
		public void toggleSortOrder(int column) {
			List<? extends SortKey> sortKeys = getSortKeys();

			if (sortKeys.size() > 0) {
				if (sortKeys.get(0).getColumn() == column
						&& sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING) {
					setSortKeys(null);
					return;
				}
			}

			super.toggleSortOrder(column);
		}
	}
}
