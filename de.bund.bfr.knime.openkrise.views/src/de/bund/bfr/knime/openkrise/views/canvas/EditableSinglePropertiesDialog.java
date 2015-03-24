/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Element;
import de.bund.bfr.knime.openkrise.TracingColumns;

public class EditableSinglePropertiesDialog extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private Element element;

	private JButton okButton;
	private JButton cancelButton;

	private JTextField caseField;
	private JCheckBox contaminationBox;
	private JCheckBox observedBox;

	private boolean approved;

	public EditableSinglePropertiesDialog(Component parent, Element element,
			Map<String, Class<?>> properties) {
		super(SwingUtilities.getWindowAncestor(parent), "Properties",
				DEFAULT_MODALITY_TYPE);
		this.element = element;

		double weight = 0.0;
		boolean crossContamination = false;
		boolean observed = false;

		if (element.getProperties().get(TracingColumns.WEIGHT) != null) {
			weight = (Double) element.getProperties()
					.get(TracingColumns.WEIGHT);
		}

		if (element.getProperties().get(TracingColumns.CROSS_CONTAMINATION) != null) {
			crossContamination = (Boolean) element.getProperties().get(
					TracingColumns.CROSS_CONTAMINATION);
		}

		if (element.getProperties().get(TracingColumns.OBSERVED) != null) {
			observed = (Boolean) element.getProperties().get(
					TracingColumns.OBSERVED);
		}

		caseField = new JTextField(String.valueOf(weight));
		contaminationBox = new JCheckBox("", crossContamination);
		observedBox = new JCheckBox("", observed);

		JPanel inputPanel = UI.createOptionsPanel("Input", Arrays.asList(
				new JLabel(TracingColumns.WEIGHT + ":"), new JLabel(
						TracingColumns.CROSS_CONTAMINATION + ":"), new JLabel(
						TracingColumns.OBSERVED + ":")), Arrays.asList(
				caseField, contaminationBox, observedBox));
		List<JLabel> tracingLabels = Arrays.asList(new JLabel(
				TracingColumns.SCORE + ":"), new JLabel(TracingColumns.BACKWARD
				+ ":"), new JLabel(TracingColumns.FORWARD + ":"));
		List<JTextField> tracingFields = Arrays.asList(createField(element
				.getProperties().get(TracingColumns.SCORE)),
				createField(element.getProperties()
						.get(TracingColumns.BACKWARD)), createField(element
						.getProperties().get(TracingColumns.FORWARD)));
		JPanel tracingPanel = UI.createOptionsPanel("Tracing", tracingLabels,
				tracingFields);
		JPanel northPanel = new JPanel();

		northPanel.setLayout(new BorderLayout());
		northPanel.add(inputPanel, BorderLayout.CENTER);
		northPanel.add(tracingPanel, BorderLayout.SOUTH);

		Map<String, Class<?>> otherProperties = new LinkedHashMap<>(properties);

		otherProperties.remove(TracingColumns.WEIGHT);
		otherProperties.remove(TracingColumns.CROSS_CONTAMINATION);
		otherProperties.remove(TracingColumns.OBSERVED);
		otherProperties.remove(TracingColumns.SCORE);
		otherProperties.remove(TracingColumns.BACKWARD);
		otherProperties.remove(TracingColumns.FORWARD);

		JPanel leftCenterPanel = new JPanel();
		JPanel rightCenterPanel = new JPanel();

		leftCenterPanel.setLayout(new GridLayout(otherProperties.size(), 1, 5,
				5));
		rightCenterPanel.setLayout(new GridLayout(otherProperties.size(), 1, 5,
				5));

		for (String property : otherProperties.keySet()) {
			Object value = element.getProperties().get(property);

			leftCenterPanel.add(new JLabel(property + ":"));
			rightCenterPanel.add(createField(value));
		}

		JPanel centerPanel = new JPanel();

		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.setLayout(new BorderLayout(5, 5));
		centerPanel.add(leftCenterPanel, BorderLayout.WEST);
		centerPanel.add(rightCenterPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		setLayout(new BorderLayout());
		add(northPanel, BorderLayout.NORTH);
		add(new JScrollPane(UI.createNorthPanel(centerPanel)),
				BorderLayout.CENTER);
		add(UI.createEastPanel(UI.createHorizontalPanel(okButton, cancelButton)),
				BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		UI.adjustDialog(this, 0.5, 1.0);
	}

	public boolean isApproved() {
		return approved;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			if (caseField.getText().isEmpty()) {
				element.getProperties().put(TracingColumns.WEIGHT, 0.0);
			} else {
				try {
					element.getProperties().put(TracingColumns.WEIGHT,
							Double.parseDouble(caseField.getText()));
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this,
							"Please enter valid number for "
									+ TracingColumns.WEIGHT, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

			element.getProperties().put(TracingColumns.CROSS_CONTAMINATION,
					contaminationBox.isSelected());
			element.getProperties().put(TracingColumns.OBSERVED,
					observedBox.isSelected());
			approved = true;
			dispose();
		} else if (e.getSource() == cancelButton) {
			approved = false;
			dispose();
		}
	}

	private static JTextField createField(Object obj) {
		JTextField field = new JTextField(obj != null ? obj.toString() : "");

		field.setPreferredSize(new Dimension(
				field.getPreferredSize().width + 5,
				field.getPreferredSize().height));
		field.setEditable(false);

		return field;
	}
}