/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.transformer;

import java.awt.Color;
import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.gis.views.canvas.CanvasUtilities;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class EdgeDrawTransformer<E> implements Transformer<E, Paint> {

	private VisualizationViewer<?, E> viewer;
	private Map<E, Paint> edgeColors;

	public EdgeDrawTransformer(VisualizationViewer<?, E> viewer,
			Map<E, List<Double>> alphaValues, List<Color> colors) {
		this.viewer = viewer;
		edgeColors = new LinkedHashMap<>();

		for (E edge : alphaValues.keySet()) {
			List<Double> alphas = alphaValues.get(edge);

			edgeColors.put(edge,
					CanvasUtilities.mixColors(Color.BLACK, colors, alphas));
		}
	}

	@Override
	public Paint transform(E edge) {
		if (viewer.getPickedEdgeState().getPicked().contains(edge)) {
			return Color.GREEN;
		} else if (edgeColors.containsKey(edge)) {
			return edgeColors.get(edge);
		} else {
			return Color.BLACK;
		}
	}

}
