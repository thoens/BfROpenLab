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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.SinglePropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeStrokeTransformer;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

/**
 * @author Christian Thoens
 */
public class GraphCanvas extends Canvas<GraphNode> {

	private static final long serialVersionUID = 1L;

	private static final String IS_META_NODE = "IsMetaNode";

	private List<GraphNode> allNodes;
	private List<Edge<GraphNode>> allEdges;
	private Set<GraphNode> nodes;
	private Set<Edge<GraphNode>> edges;
	private Map<Edge<GraphNode>, Set<Edge<GraphNode>>> joinMap;

	private Map<String, Map<String, Point2D>> collapsedNodes;
	private Map<String, GraphNode> nodeSaveMap;
	private Map<String, Edge<GraphNode>> edgeSaveMap;

	private String metaNodeProperty;

	public GraphCanvas() {
		this(new ArrayList<GraphNode>(), new ArrayList<Edge<GraphNode>>(),
				new LinkedHashMap<String, Class<?>>(),
				new LinkedHashMap<String, Class<?>>(), null, null, null, null);
	}

	public GraphCanvas(List<GraphNode> nodes, List<Edge<GraphNode>> edges,
			Map<String, Class<?>> nodeProperties,
			Map<String, Class<?>> edgeProperties, String nodeIdProperty,
			String edgeIdProperty, String edgeFromProperty,
			String edgeToProperty) {
		super(nodeProperties, edgeProperties, nodeIdProperty, edgeIdProperty,
				edgeFromProperty, edgeToProperty);
		setAllowEdges(true);
		setAllowLayout(true);
		this.allNodes = nodes;
		this.allEdges = edges;
		this.nodes = new LinkedHashSet<GraphNode>();
		this.edges = new LinkedHashSet<Edge<GraphNode>>();

		Map<String, GraphNode> nodesById = new LinkedHashMap<String, GraphNode>();

		for (GraphNode node : allNodes) {
			GraphNode newNode = new GraphNode(node.getId(),
					new LinkedHashMap<String, Object>(node.getProperties()),
					node.getRegion());

			nodesById.put(node.getId(), newNode);
			this.nodes.add(newNode);
		}

		for (Edge<GraphNode> edge : allEdges) {
			this.edges.add(new Edge<GraphNode>(edge.getId(),
					new LinkedHashMap<String, Object>(edge.getProperties()),
					nodesById.get(edge.getFrom().getId()), nodesById.get(edge
							.getTo().getId())));
		}

		nodeSaveMap = CanvasUtilities.getElementsById(nodes);
		edgeSaveMap = CanvasUtilities.getElementsById(edges);

		joinMap = new LinkedHashMap<Edge<GraphNode>, Set<Edge<GraphNode>>>();
		collapsedNodes = new LinkedHashMap<String, Map<String, Point2D>>();
		metaNodeProperty = KnimeUtilities.createNewValue(IS_META_NODE,
				getNodeProperties().keySet());
		getNodeProperties().put(metaNodeProperty, Boolean.class);

		getViewer().getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<GraphNode>(getNodeSize(),
						new LinkedHashMap<GraphNode, Double>()));
		getViewer().getGraphLayout().setGraph(
				CanvasUtilities.createGraph(this.nodes, this.edges));
		applyLayout(LayoutType.FR_LAYOUT, null);
	}

	public Set<GraphNode> getNodes() {
		return nodes;
	}

	public Set<Edge<GraphNode>> getEdges() {
		return edges;
	}

	public Map<String, GraphNode> getNodeSaveMap() {
		return nodeSaveMap;
	}

	public Map<String, Edge<GraphNode>> getEdgeSaveMap() {
		return edgeSaveMap;
	}

	public Map<String, Point2D> getNodePositions() {
		return getNodePositions(nodes);
	}

	public void setNodePositions(Map<String, Point2D> nodePositions) {
		if (nodePositions.isEmpty()) {
			return;
		}

		int n = 0;

		for (GraphNode node : nodes) {
			if (nodePositions.get(node.getId()) == null) {
				n++;
			}
		}

		Layout<GraphNode, Edge<GraphNode>> layout = new StaticLayout<GraphNode, Edge<GraphNode>>(
				getViewer().getGraphLayout().getGraph());
		Point2D upperLeft = toGraphCoordinates(0, 0);
		Point2D upperRight = toGraphCoordinates(
				getViewer().getPreferredSize().width, 0);
		double x1 = upperLeft.getX();
		double x2 = upperRight.getX();
		double y = upperLeft.getY();
		int i = 0;

		for (GraphNode node : nodes) {
			Point2D pos = nodePositions.get(node.getId());

			if (pos != null) {
				layout.setLocation(node, pos);
			} else {
				double x = x1 + (double) i / (double) n * (x2 - x1);

				layout.setLocation(node, new Point2D.Double(x, y));
				i++;
			}
		}

		layout.setSize(getViewer().getSize());
		getViewer().setGraphLayout(layout);
	}

	public Map<String, Map<String, Point2D>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(
			Map<String, Map<String, Point2D>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
		applyChanges();
	}

	@Override
	public void resetLayoutItemClicked() {
		setTransform(1.0, 1.0, 0.0, 0.0);
	}

	@Override
	public void layoutItemClicked(LayoutType layoutType) {
		applyLayout(layoutType, getSelectedNodes());
	}

	@Override
	public void collapseToNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : collapsedNodes.keySet()) {
			if (selectedIds.contains(id)) {
				JOptionPane.showMessageDialog(this,
						"Some of the selected nodes are already collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		String newId = null;

		while (true) {
			newId = (String) JOptionPane.showInputDialog(this,
					"Specify ID for Meta Node", "Node ID",
					JOptionPane.QUESTION_MESSAGE, null, null, "");

			if (newId == null) {
				return;
			} else if (nodeSaveMap.containsKey(newId)) {
				JOptionPane.showMessageDialog(this,
						"ID already exists, please specify different ID",
						"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				break;
			}
		}

		Map<String, Point2D> absPos = getNodePositions(CanvasUtilities
				.getElementsById(getViewer().getGraphLayout().getGraph()
						.getVertices(), selectedIds));
		Map<String, Point2D> relPos = new LinkedHashMap<String, Point2D>();
		Point2D center = CanvasUtilities.getCenter(absPos.values());

		for (String id : absPos.keySet()) {
			relPos.put(id,
					CanvasUtilities.substractPoints(absPos.get(id), center));
		}

		collapsedNodes.put(newId, relPos);
		applyChanges();
		setSelectedNodeIds(new LinkedHashSet<String>(Arrays.asList(newId)));
	}

	@Override
	public void expandFromNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (!collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this,
						"Some of the selected nodes are not collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Set<String> newIds = new LinkedHashSet<String>();

		for (String id : selectedIds) {
			Map<String, Point2D> removed = collapsedNodes.remove(id);
			Point2D center = getViewer().getGraphLayout().transform(
					nodeSaveMap.remove(id));

			newIds.addAll(removed.keySet());

			for (String newId : removed.keySet()) {
				getViewer().getGraphLayout().setLocation(
						nodeSaveMap.get(newId),
						CanvasUtilities.addPoints(removed.get(newId), center));
			}
		}

		applyChanges();
		setSelectedNodeIds(newIds);
	}

	@Override
	public void collapseByPropertyItemClicked() {
		String[] properties = getNodeProperties().keySet().toArray(
				new String[0]);
		String result = (String) JOptionPane.showInputDialog(this,
				"Select Property for Collapse?", "Collapse by Property",
				JOptionPane.QUESTION_MESSAGE, null, properties, properties[0]);

		if (result == null) {
			return;
		}

		for (String id : collapsedNodes.keySet()) {
			nodeSaveMap.remove(id);
		}

		collapsedNodes.clear();

		Map<String, Set<GraphNode>> nodesByProperty = new LinkedHashMap<String, Set<GraphNode>>();

		for (GraphNode node : allNodes) {
			Object value = node.getProperties().get(result);

			if (value == null) {
				continue;
			}

			String stringValue = value.toString();

			if (!nodesByProperty.containsKey(stringValue)) {
				nodesByProperty
						.put(stringValue, new LinkedHashSet<GraphNode>());
			}

			nodesByProperty.get(stringValue).add(node);
		}

		for (String value : nodesByProperty.keySet()) {
			String newId = KnimeUtilities.createNewValue(value,
					nodeSaveMap.keySet());
			Map<String, Point2D> absPos = getNodePositions(nodesByProperty
					.get(value));
			Map<String, Point2D> relPos = new LinkedHashMap<String, Point2D>();
			Point2D center = CanvasUtilities.getCenter(absPos.values());

			for (String id : absPos.keySet()) {
				relPos.put(id,
						CanvasUtilities.substractPoints(absPos.get(id), center));
			}

			collapsedNodes.put(newId, relPos);
		}

		applyChanges();
		getViewer().getPickedVertexState().clear();
	}

	@Override
	public void clearCollapsedNodesItemClicked() {
		for (String id : collapsedNodes.keySet()) {
			Map<String, Point2D> removed = collapsedNodes.get(id);
			Point2D center = getViewer().getGraphLayout().transform(
					nodeSaveMap.remove(id));

			for (String newId : removed.keySet()) {
				getViewer().getGraphLayout().setLocation(
						nodeSaveMap.get(newId),
						CanvasUtilities.addPoints(removed.get(newId), center));
			}
		}

		collapsedNodes.clear();
		applyChanges();
		getViewer().getPickedVertexState().clear();
	}

	@Override
	public void borderAlphaChanged() {
	}

	@Override
	protected void applyTransform() {
	}

	@Override
	protected GraphMouse<GraphNode, Edge<GraphNode>> createMouseModel() {
		return new GraphMouse<GraphNode, Edge<GraphNode>>(
				new PickingGraphMousePlugin<GraphNode, Edge<GraphNode>>() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
							GraphNode node = getViewer().getPickSupport()
									.getVertex(getViewer().getGraphLayout(),
											e.getX(), e.getY());
							Edge<GraphNode> edge = getViewer().getPickSupport()
									.getEdge(getViewer().getGraphLayout(),
											e.getX(), e.getY());

							if (node != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), node,
										getNodeProperties());

								dialog.setVisible(true);
							} else if (edge != null) {
								SinglePropertiesDialog dialog = new SinglePropertiesDialog(
										e.getComponent(), edge,
										getEdgeProperties());

								dialog.setVisible(true);
							}
						}
					}
				}, getEditingMode());
	}

	@Override
	protected void applyChanges() {
		Set<String> selectedNodeIds = getSelectedNodeIds();
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		applyNodeCollapse();
		applyHighlights();

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		getViewer().repaint();
	}

	protected void applyNodeCollapse() {
		nodes = new LinkedHashSet<GraphNode>();
		edges = new LinkedHashSet<Edge<GraphNode>>();

		Map<String, String> collapseTo = new LinkedHashMap<String, String>();

		for (String to : collapsedNodes.keySet()) {
			for (String from : collapsedNodes.get(to).keySet()) {
				collapseTo.put(from, to);
			}
		}

		Map<String, GraphNode> nodesById = new LinkedHashMap<String, GraphNode>();

		for (GraphNode node : allNodes) {
			if (!collapseTo.keySet().contains(node.getId())) {
				GraphNode newNode = nodeSaveMap.get(node.getId());

				if (newNode == null) {
					newNode = new GraphNode(node.getId(),
							new LinkedHashMap<String, Object>(
									node.getProperties()), node.getRegion());
					getViewer().getGraphLayout().setLocation(newNode,
							getViewer().getGraphLayout().transform(node));
				}

				nodes.add(newNode);
				nodesById.put(node.getId(), newNode);
			}
		}

		Set<GraphNode> metaNodes = new LinkedHashSet<GraphNode>();

		for (String newId : collapsedNodes.keySet()) {
			GraphNode newNode = nodeSaveMap.get(newId);

			if (newNode == null) {
				Set<GraphNode> nodes = CanvasUtilities.getElementsById(
						nodeSaveMap, collapsedNodes.get(newId).keySet());
				Point2D pos = CanvasUtilities.getCenter(getNodePositions(nodes)
						.values());

				newNode = combineNodes(newId, nodes);
				getViewer().getGraphLayout().setLocation(newNode, pos);
			}

			nodes.add(newNode);
			nodesById.put(newNode.getId(), newNode);
			metaNodes.add(newNode);
		}

		for (Edge<GraphNode> edge : allEdges) {
			GraphNode from = nodesById.get(edge.getFrom().getId());
			GraphNode to = nodesById.get(edge.getTo().getId());

			if (from == null) {
				from = nodesById.get(collapseTo.get(edge.getFrom().getId()));
			}

			if (to == null) {
				to = nodesById.get(collapseTo.get(edge.getTo().getId()));
			}

			Edge<GraphNode> newEdge = edgeSaveMap.get(edge.getId());

			if (newEdge == null) {
				Map<String, Object> properties = new LinkedHashMap<String, Object>(
						edge.getProperties());

				properties.put(getEdgeFromProperty(), from.getId());
				properties.put(getEdgeToProperty(), to.getId());
				newEdge = new Edge<GraphNode>(edge.getId(), properties, from,
						to);
			} else if (!newEdge.getFrom().equals(from)
					|| !newEdge.getTo().equals(to)) {
				newEdge.getProperties()
						.put(getEdgeFromProperty(), from.getId());
				newEdge.getProperties().put(getEdgeToProperty(), to.getId());
				newEdge = new Edge<GraphNode>(newEdge.getId(),
						newEdge.getProperties(), from, to);
			}

			if (newEdge.getFrom().equals(newEdge.getTo())) {
				if (!metaNodes.contains(newEdge.getFrom())) {
					edges.add(newEdge);
				}
			} else {
				edges.add(newEdge);
			}
		}

		if (isJoinEdges()) {
			edges = CanvasUtilities.removeInvisibleElements(edges,
					getEdgeHighlightConditions());
			joinMap = CanvasUtilities.joinEdges(edges, getEdgeProperties(),
					getEdgeIdProperty(), getEdgeFromProperty(),
					getEdgeToProperty(),
					CanvasUtilities.getElementIds(allEdges));
			edges = joinMap.keySet();
		} else {
			joinMap = new LinkedHashMap<Edge<GraphNode>, Set<Edge<GraphNode>>>();
		}

		nodeSaveMap.putAll(CanvasUtilities.getElementsById(nodes));
		edgeSaveMap.putAll(CanvasUtilities.getElementsById(edges));
		getViewer().getGraphLayout().setGraph(
				CanvasUtilities.createGraph(nodes, edges));
		getViewer().getRenderContext().setVertexStrokeTransformer(
				new NodeStrokeTransformer<GraphNode>(metaNodes));
		getViewer().getPickedVertexState().clear();
	}

	protected void applyHighlights() {
		CanvasUtilities.applyNodeHighlights(getViewer(), nodes,
				getNodeHighlightConditions(), getNodeSize(), false);

		if (!isJoinEdges()) {
			CanvasUtilities.applyEdgeHighlights(getViewer(), edges,
					getEdgeHighlightConditions());
		} else {
			HighlightConditionList conditions = CanvasUtilities
					.removeInvisibleConditions(getEdgeHighlightConditions());

			CanvasUtilities.applyEdgeHighlights(getViewer(), edges, conditions);
		}

		CanvasUtilities.applyEdgelessNodes(getViewer(), isSkipEdgelessNodes());
	}

	@Override
	protected Map<Edge<GraphNode>, Set<Edge<GraphNode>>> getJoinMap() {
		return joinMap;
	}

	@Override
	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, getNodeProperties(), true, true,
				true, getNodeHighlightConditions(), null);
	}

	@Override
	protected HighlightListDialog openEdgeHighlightDialog() {
		return new HighlightListDialog(this, getEdgeProperties(), true, true,
				true, getEdgeHighlightConditions(), null);
	}

	private void applyLayout(LayoutType layoutType, Set<GraphNode> selectedNodes) {
		Graph<GraphNode, Edge<GraphNode>> graph = getViewer().getGraphLayout()
				.getGraph();
		Layout<GraphNode, Edge<GraphNode>> layout = null;
		boolean nodesSelected = selectedNodes != null
				&& !selectedNodes.isEmpty();

		if (nodesSelected && layoutType == LayoutType.ISOM_LAYOUT) {
			if (JOptionPane.showConfirmDialog(this, layoutType
					+ " cannot be applied on a subset of nodes. Apply "
					+ layoutType + " on all nodes?", "Confirm",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				nodesSelected = false;
			} else {
				return;
			}
		}

		switch (layoutType) {
		case CIRCLE_LAYOUT:
			layout = new CircleLayout<GraphNode, Edge<GraphNode>>(graph);
			break;
		case FR_LAYOUT:
			layout = new FRLayout<GraphNode, Edge<GraphNode>>(graph);
			break;
		case FR_LAYOUT_2:
			layout = new FRLayout2<GraphNode, Edge<GraphNode>>(graph);
			break;
		case ISOM_LAYOUT:
			layout = new ISOMLayout<GraphNode, Edge<GraphNode>>(graph);
			break;
		case KK_LAYOUT:
			layout = new KKLayout<GraphNode, Edge<GraphNode>>(graph);
			break;
		case SPRING_LAYOUT:
			layout = new SpringLayout<GraphNode, Edge<GraphNode>>(graph);
			break;
		case SPRING_LAYOUT_2:
			layout = new SpringLayout2<GraphNode, Edge<GraphNode>>(graph);
			break;
		}

		if (nodesSelected) {
			Point2D move = new Point2D.Double(getTranslationX() / getScaleX(),
					getTranslationY() / getScaleY());

			layout.setSize(new Dimension(
					(int) (getViewer().getSize().width / getScaleX()),
					(int) (getViewer().getSize().height / getScaleY())));

			for (GraphNode node : nodes) {
				if (!selectedNodes.contains(node)) {
					layout.setLocation(node, CanvasUtilities.addPoints(
							getViewer().getGraphLayout().transform(node), move));
					layout.lock(node, true);
				}
			}

			setTransform(getScaleX(), getScaleY(), 0.0, 0.0);
		} else {
			layout.setSize(getViewer().getSize());
			setTransform(1.0, 1.0, 0.0, 0.0);
		}

		getViewer().setGraphLayout(layout);
	}

	private Map<String, Point2D> getNodePositions(Collection<GraphNode> nodes) {
		Map<String, Point2D> map = new LinkedHashMap<String, Point2D>();
		Layout<GraphNode, Edge<GraphNode>> layout = getViewer()
				.getGraphLayout();

		for (GraphNode node : nodes) {
			Point2D pos = layout.transform(node);

			if (pos != null) {
				map.put(node.getId(), pos);
			}
		}

		return map;
	}

	private GraphNode combineNodes(String id, Collection<GraphNode> nodes) {
		Map<String, Object> properties = new LinkedHashMap<String, Object>();

		for (GraphNode node : nodes) {
			CanvasUtilities.addMapToMap(properties, getNodeProperties(),
					node.getProperties());
		}

		if (getNodeIdProperty() != null) {
			properties.put(getNodeIdProperty(), id);
		}

		properties.put(metaNodeProperty, true);

		return new GraphNode(id, properties, null);
	}
}
