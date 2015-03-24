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
package de.bund.bfr.knime.gis.views;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.Canvas;

public class LocationSettings extends GisSettings {

	private static final String CFG_NODE_LATITUDE_COLUMN = "NodeLatitudeColumn";
	private static final String CFG_NODE_LONGITUDE_COLUMN = "NodeLongitudeColumn";
	private static final String CFG_NODE_SIZE = "GisLocationSize";

	private String nodeLatitudeColumn;
	private String nodeLongitudeColumn;
	private int nodeSize;

	public LocationSettings() {
		nodeLatitudeColumn = null;
		nodeLongitudeColumn = null;
		nodeSize = 4;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			nodeLatitudeColumn = settings.getString(CFG_NODE_LATITUDE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeLongitudeColumn = settings.getString(CFG_NODE_LONGITUDE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeSize = settings.getInt(CFG_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);
		settings.addString(CFG_NODE_LATITUDE_COLUMN, nodeLatitudeColumn);
		settings.addString(CFG_NODE_LONGITUDE_COLUMN, nodeLongitudeColumn);
		settings.addInt(CFG_NODE_SIZE, nodeSize);
	}

	@Override
	public void setFromCanvas(Canvas<?> canvas, boolean resized) {
		super.setFromCanvas(canvas, resized);
		nodeSize = canvas.getNodeSize();
	}

	@Override
	public void setToCanvas(Canvas<?> canvas) {
		super.setToCanvas(canvas);
		canvas.setNodeSize(nodeSize);
	}

	public String getNodeLatitudeColumn() {
		return nodeLatitudeColumn;
	}

	public void setNodeLatitudeColumn(String nodeLatitudeColumn) {
		this.nodeLatitudeColumn = nodeLatitudeColumn;
	}

	public String getNodeLongitudeColumn() {
		return nodeLongitudeColumn;
	}

	public void setNodeLongitudeColumn(String nodeLongitudeColumn) {
		this.nodeLongitudeColumn = nodeLongitudeColumn;
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
	}
}