package org.processmining.framework.plugin.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginDescriptorID;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.util.ArrayUtils;
import org.processmining.framework.util.Cast;
import org.processmining.framework.util.Pair;
import org.processmining.framework.xstream.XStreamPersistency;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

/**
 * Implements a PluginDescriptor that reads and executes .mcr files. These files
 * can be generated with the macro definition frame in the GUI context.
 * 
 * TODO: Output parameters.
 * 
 * @author bfvdonge
 * @param <edge>
 * 
 */
public class MacroPluginDescriptorImpl extends AbstractPluginDescriptor {

	private final PluginDescriptorID id;
	private final String fileName;
	private final String name;
	private final String help;
	private boolean connectionsSet;
	private final PackageDescriptor pack;

	Class<? extends PluginContext> contextType = PluginContext.class;
	final List<String> parameterNames = new ArrayList<String>();
	final List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
	final List<String> returnNames = new ArrayList<String>();
	final List<Class<?>> returnTypes = new ArrayList<Class<?>>();
	final List<OutputParameter> returnParameters = new ArrayList<OutputParameter>();

	final Set<Pair<Pair<Object, Integer>, Pair<Object, Integer>>> edges = new HashSet<Pair<Pair<Object, Integer>, Pair<Object, Integer>>>();

	final Set<Connection> connectionsOnFirstInvoke = new HashSet<Connection>();

	// Map from indices in the mcr file to ojects to reconnect edges.
	final Map<Integer, Object> index2Objects = new HashMap<Integer, Object>();
	final Map<Object, Integer> object2Rank = new HashMap<Object, Integer>();
	int maxRank;

	public MacroPluginDescriptorImpl(File file, PluginManager manager, PackageDescriptor pack) throws IOException,
			DOMException, SAXException, ParserConfigurationException, ClassNotFoundException, DependsOnUnknownException {
		this.pack = pack;
		fileName = file.getCanonicalPath();
		String n = file.getName();
		name = n.substring(0, n.length() - 4);
		help = "";
		id = new PluginDescriptorIDImpl(this);
		McrFileLoader.loadFromFile(file, this, manager);
		connectionsSet = connectionsOnFirstInvoke.isEmpty();
	}

	public PackageDescriptor getPackage() {
		return pack;
	}

	public int compareTo(PluginDescriptor plugin) {
		if (plugin.equals(this)) {
			return 0;
		}

		int c = getName().toLowerCase().compareTo(plugin.getName().toLowerCase());
		if (c == 0) {
			c = id.compareTo(plugin.getID());
		}
		return c;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	public Class<? extends PluginContext> getContextType(int methodIndex) {
		assert (methodIndex == 0);
		return contextType;
	}

	public PluginDescriptorID getID() {
		return id;
	}

	public int getIndexInMethod(int methodIndex, int globalParameterIndex) {
		assert (methodIndex == 0);
		return globalParameterIndex;
	}

	public int getIndexInParameterNames(int methodIndex, int methodParameterIndex) {
		assert (methodIndex == 0);
		return methodParameterIndex;
	}

	public String getMethodLabel(int methodIndex) {
		assert (methodIndex == 0);
		return name;
	}

	public String getName() {
		return name;
	}

	public int getNumberOfMethods() {
		return 1;
	}

	public String getPluginParameterName(int methodIndex, int parameterIndex) {
		assert (methodIndex == 0);
		return parameterNames.get(parameterIndex);
	}

	public List<String> getParameterNames() {
		return Collections.unmodifiableList(parameterNames);
	}

	public List<String> getParameterNames(int methodIndex) {
		assert (methodIndex == 0);
		return getParameterNames();
	}

	public Class<?> getPluginParameterType(int methodIndex, int parameterIndex) {
		assert (methodIndex == 0);
		return parameterTypes.get(parameterIndex);
	}

	public List<List<Class<?>>> getParameterTypes() {
		ArrayList<List<Class<?>>> result = new ArrayList<List<Class<?>>>(1);
		result.add(getParameterTypes(0));
		return Collections.unmodifiableList(result);
	}

	public List<Class<?>> getParameterTypes(int methodIndex) {
		assert (methodIndex == 0);
		return Collections.unmodifiableList(parameterTypes);
	}

	public List<String> getReturnNames() {
		return Collections.unmodifiableList(returnNames);
	}

	public List<Class<?>> getReturnTypes() {
		return Collections.unmodifiableList(returnTypes);
	}

	public Set<Class<?>> getTypesAtParameterIndex(int globalParameterIndex) {
		HashSet<Class<?>> set = new HashSet<Class<?>>(1);
		set.add(getPluginParameterType(0, globalParameterIndex));
		return Collections.unmodifiableSet(set);
	}

	public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
		return false;
	}

	public PluginExecutionResult invoke(int methodIndex, final PluginContext context, final Object... args) {
		synchronized (connectionsOnFirstInvoke) {
			if (!connectionsSet) {
				connectionsSet = true;
				for (Connection c : connectionsOnFirstInvoke) {
					context.addConnection(c);
				}
				connectionsOnFirstInvoke.clear();
			}
		}
		return super.invoke(methodIndex, context, args);
	}

	protected Object[] execute(PluginContext context, int methodIndex, Object... args) throws Exception {
		// All objects are available, now any exceptions
		// are forwarded, so start the computation of
		// this plugin

		// The ranking defines the order in which the plugins should be
		// executed.
		// ranking 0 first, 1 second and so on....
		Map<PluginParameter, PluginContext> contextMap = new HashMap<PluginParameter, PluginContext>();
		List<PluginParameter> pluginList = new ArrayList<PluginParameter>();

		context.getProgress().setMaximum(pluginList.size() + 1);
		context.getProgress().setIndeterminate(false);
		context.getProgress().setCaption("Executing macro: " + getName());

		Map<PluginParameter, PluginExecutionResult> resultMap = new HashMap<PluginParameter, PluginExecutionResult>();

		buildContexts(contextMap, context);
		pluginList.addAll(contextMap.keySet());

		Collections.sort(pluginList, new Comparator<PluginParameter>() {

			public int compare(PluginParameter arg0, PluginParameter arg1) {
				if (object2Rank.get(arg0) != object2Rank.get(arg1)) {
					return object2Rank.get(arg0) - object2Rank.get(arg1);
				}
				return (arg0.getFirst().hashCode() - arg1.getFirst().hashCode());

			}
		});

		// Plugins can now be executed in the order in which they are returned by the iterator of pluginList.
		// What remains is to correctly route the objects. For this, we use the edges.
		Iterator<PluginParameter> it = pluginList.iterator();
		while (it.hasNext()) {
			PluginParameter par = it.next();
			PluginDescriptor plugin = par.getFirst();
			int method = par.getSecond();
			Object[] parameters = getParameters(par, resultMap, args);
			PluginExecutionResult result = plugin.invoke(method, contextMap.get(par), parameters);
			// do sequential execution of these plugins.
			result.synchronize();
			context.getProgress().inc();
			resultMap.put(par, result);
		}

		Object[] result = new Object[returnNames.size()];
		for (OutputParameter par : returnParameters) {
			Object r = getResult(par, resultMap, args);
			result[par.getIndex()] = r;
		}
		context.getProgress().inc();
		return result;
	}

	private Object[] getParameters(PluginParameter par, Map<PluginParameter, PluginExecutionResult> results,
			Object... args) {
		PluginDescriptor plugin = par.getFirst();
		Object[] result = new Object[plugin.getParameterNames(par.getSecond()).size()];
		List<Class<?>> types = plugin.getParameterTypes(par.getSecond());

		for (Pair<Pair<Object, Integer>, Pair<Object, Integer>> edge : edges) {
			// check if this is an incoming edge at par
			if (edge.getSecond().getFirst() != par) {
				continue;
			}
			int index = plugin.getIndexInMethod(par.getSecond(), edge.getSecond().getSecond());
			// This is an incoming edge.
			Object source = edge.getFirst().getFirst();
			if (source instanceof InputParameter) {
				/*
				 * The source is an input parameter at index getIndex() of the
				 * provided arguments
				 */
				result[index] = assignParameter(result[index], types.get(index),
						args[((InputParameter) source).getIndex() + 1]);
			} else if (source instanceof PluginParameter) {
				/*
				 * The source should be retrieved from a previously executed
				 * plugin
				 */
				PluginExecutionResult pluginResult = results.get(source);
				assert (pluginResult != null);
				result[index] = assignParameter(result[index], types.get(index),
						pluginResult.getResult(edge.getFirst().getSecond()));
			} else {
				/*
				 * 
				 * the source is a provided object that has been serialized with
				 * this macro
				 */
				result[index] = assignParameter(result[index], types.get(index), source);
			}

		}
		return result;
	}

	private Object getResult(OutputParameter par, Map<PluginParameter, PluginExecutionResult> results, Object... args) {
		Object result = null;
		Class<?> type = par.getSecond();

		for (Pair<Pair<Object, Integer>, Pair<Object, Integer>> edge : edges) {
			// check if this is an incoming edge at par
			if (edge.getSecond().getFirst() != par) {
				continue;
			}
			// This is an incoming edge.
			Object source = edge.getFirst().getFirst();
			if (source instanceof InputParameter) {
				/*
				 * The source is an input parameter at index getIndex() of the
				 * provided arguments
				 */
				result = assignParameter(null, type, args[((InputParameter) source).getIndex()]);
			} else if (source instanceof PluginParameter) {
				/*
				 * The source should be retrieved from a previously executed
				 * plugin
				 */
				PluginExecutionResult pluginResult = results.get(source);
				assert (pluginResult != null);
				result = assignParameter(result, type, pluginResult.getResult(edge.getFirst().getSecond()));
			} else {
				/*
				 * 
				 * the source is a provided object that has been serialized with
				 * this macro
				 */
				result = assignParameter(result, type, source);
			}

		}
		return result;
	}

	private Object assignParameter(Object currentlyAssigned, Class<?> reqType, Object toAssign) {
		assert ((currentlyAssigned == null) || reqType.isArray());

		if (reqType.isArray()) {
			Object[] existing;
			if (currentlyAssigned == null) {
				// Create a new array
				existing = new Object[0];
			} else {
				// There is an array in there, so get it
				existing = (Object[]) currentlyAssigned;
			}
			Object[] newArray;
			if (toAssign.getClass().isArray()) {
				newArray = ArrayUtils.copyOf(existing, ((Object[]) toAssign).length + existing.length);
				for (int k = 0; k < ((Object[]) toAssign).length; k++) {
					newArray[existing.length + k] = ((Object[]) toAssign)[k];
				}
			} else {
				newArray = ArrayUtils.copyOf(existing, 1 + existing.length);
				newArray[existing.length] = toAssign;
			}
			return newArray;
		} else {
			return toAssign;
		}
	}

	private void buildContexts(Map<PluginParameter, PluginContext> contextMap, PluginContext parent) {

		for (Map.Entry<Object, Integer> entry : object2Rank.entrySet()) {
			if (entry.getKey() instanceof PluginParameter) {
				PluginParameter par = (PluginParameter) entry.getKey();
				PluginContext childContext = parent.createChildContext(entry.getKey().toString());
				parent.getPluginLifeCycleEventListeners().firePluginCreated(childContext);
				contextMap.put(par, childContext);
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isUserAccessible() {
		return true;
	}

	public boolean handlesCancel() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.PluginDescriptor#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass, int methodIndex) {
		return null;
	}

	public boolean hasAnnotation(Class<? extends Annotation> annotationClass, int methodIndex) {
		return false;
	}

	public int getMostSignificantResult() {
		// TODO: Most significant result is not specified in macro. 
		// this should be added.
		return 0;
	}

	public String getHelp() {
		return help;
	}

	public String getMethodHelp(int methodIndex) {
		assert (methodIndex == 0);
		return help;
	}

	public String[] getKeywords() {
		return new String[0];
	}

	public String[] getCategories() {
		return new String[0];
	}
	
	public boolean meetsQualityThreshold() {
		return false;
	}

	public boolean meetsLevelThreshold() {
		return false;
	}

}

class InputParameter extends Pair<String, Class<?>> {

	private final int index;

	public InputParameter(String first, Class<?> second, int index) {
		super(first, second);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}

class OutputParameter extends Pair<String, Class<?>> {

	private final int index;

	public OutputParameter(String first, Class<?> second, int index) {
		super(first, second);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}

class PluginParameter extends Pair<PluginDescriptor, Integer> {

	public PluginParameter(PluginDescriptor first, Integer second) {
		super(first, second);
	}

}

class McrFileLoader {

	public static void loadFromFile(File f, MacroPluginDescriptorImpl macroPlugin, PluginManager manager)
			throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, DOMException,
			ClassNotFoundException, DependsOnUnknownException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new GZIPInputStream(new BufferedInputStream(new FileInputStream(f))));

		XStream stream = new XStream();
		stream.autodetectAnnotations(true);

		XStreamPersistency.register(stream);

		NodeList nodes = doc.getElementsByTagName("SerializedTypes");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Class<?> type;
			type = Class.forName(node.getAttributes().getNamedItem("type").getNodeValue());
			stream.processAnnotations(type);
		}

		nodes = doc.getElementsByTagName("SerializedObjects");
		assert (nodes.getLength() == 1);
		List<Object> serializedObjects = Cast.<List<Object>>cast(stream.fromXML(nodes.item(0).getTextContent()));

		nodes = doc.getElementsByTagName("PluginCell");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			int index = Integer.parseInt(node.getAttributes().getNamedItem("index").getNodeValue());
			String pluginID = node.getAttributes().getNamedItem("pluginID").getNodeValue();

			PluginDescriptor plugin = manager.getPlugin(pluginID);
			if (plugin == null) {
				throw new DependsOnUnknownException(macroPlugin.getFileName() + " depends on: " + pluginID);
			}

			int selIndex = Integer.parseInt(node.getAttributes().getNamedItem("selectedMethodIndex").getNodeValue());
			String methods = node.getAttributes().getNamedItem("methods").getNodeValue();
			StringTokenizer st = new StringTokenizer(methods, ",");
			List<Integer> enabled = new ArrayList<Integer>();
			while (st.hasMoreTokens()) {
				enabled.add(Integer.parseInt(st.nextToken()));
			}
			PluginParameter selectedPlugin = new PluginParameter(plugin, selIndex);
			macroPlugin.index2Objects.put(index, selectedPlugin);

			if (plugin.getParameterNames(selIndex).isEmpty()) {
				macroPlugin.object2Rank.put(selectedPlugin, 0);
			}

			Class<? extends PluginContext> reqContextType = plugin.getContextType(selIndex);
			if (!(reqContextType.isAssignableFrom(macroPlugin.contextType))) {
				macroPlugin.contextType = reqContextType;
			}
		}

		nodes = doc.getElementsByTagName("ProvidedObjectCell");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			int index = Integer.parseInt(node.getAttributes().getNamedItem("index").getNodeValue());
			int objectIndex = Integer.parseInt(node.getAttributes().getNamedItem("objectIndex").getNodeValue());
			Object object = serializedObjects.get(objectIndex);
			macroPlugin.index2Objects.put(index, object);
			macroPlugin.object2Rank.put(object, 0);

			// Set a strong reference to the object, not to loose possible connections for as long as this
			// plugin exists.
		}

		nodes = doc.getElementsByTagName("InputPortCell");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			int index = Integer.parseInt(node.getAttributes().getNamedItem("index").getNodeValue());
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			Class<?> type = Class.forName(node.getAttributes().getNamedItem("type").getNodeValue());
			// For an input port, store the index of this parameter in the list of parameters
			// in the plugin
			InputParameter input = new InputParameter(name, type, macroPlugin.parameterNames.size());
			macroPlugin.index2Objects.put(index, input);
			// For each input port, register one parameter to the macro plugin
			macroPlugin.parameterNames.add(name);
			macroPlugin.parameterTypes.add(type);
			macroPlugin.object2Rank.put(input, 0);
		}

		nodes = doc.getElementsByTagName("OutputPortCell");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			int index = Integer.parseInt(node.getAttributes().getNamedItem("index").getNodeValue());
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			Class<?> type = Class.forName(node.getAttributes().getNamedItem("type").getNodeValue());
			// For an output port, store the index of this parameter in the list of parameters
			// in the plugin
			OutputParameter par = new OutputParameter(name, type, macroPlugin.returnNames.size());
			macroPlugin.index2Objects.put(index, par);
			// For each input port, register one parameter to the macro plugin
			macroPlugin.returnNames.add(name);
			macroPlugin.returnTypes.add(type);
			macroPlugin.returnParameters.add(par);
		}

		nodes = doc.getElementsByTagName("Connection");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			int objectIndex = Integer.parseInt(node.getAttributes().getNamedItem("objectIndex").getNodeValue());
			macroPlugin.connectionsOnFirstInvoke.add((Connection) serializedObjects.get(objectIndex));
			// Connections are registered, but no local record is kept.
		}

		nodes = doc.getElementsByTagName("Edge");
		ArrayList<Node> toProcess = new ArrayList<Node>(nodes.getLength());

		for (int i = 0; i < nodes.getLength(); i++) {
			toProcess.add(nodes.item(i));
		}

		int rank = 0;
		while (!toProcess.isEmpty()) {
			Iterator<Node> it = toProcess.iterator();
			while (it.hasNext()) {
				Node node = it.next();

				int sourceObjectIndex = Integer.parseInt(node.getAttributes().getNamedItem("sourceCellIndex")
						.getNodeValue());
				int targetObjectIndex = Integer.parseInt(node.getAttributes().getNamedItem("targetCellIndex")
						.getNodeValue());
				int sourceParameterIndex = Integer.parseInt(node.getAttributes().getNamedItem("sourcePortIndex")
						.getNodeValue());
				int targetParameterIndex = Integer.parseInt(node.getAttributes().getNamedItem("targetPortIndex")
						.getNodeValue());

				Object sourceObject = macroPlugin.index2Objects.get(sourceObjectIndex);

				assert (sourceObject != null);
				if (macroPlugin.object2Rank.get(sourceObject) == null) {
					continue;
				}

				Object targetObject = macroPlugin.index2Objects.get(targetObjectIndex);
				assert (targetObject != null);

				int predRank = macroPlugin.object2Rank.get(sourceObject);
				if (predRank < rank) {
					// This predecessor is fully ranked.
					Pair<Object, Integer> source = new Pair<Object, Integer>(sourceObject, sourceParameterIndex);
					Pair<Object, Integer> target = new Pair<Object, Integer>(targetObject, targetParameterIndex);
					macroPlugin.edges.add(new Pair<Pair<Object, Integer>, Pair<Object, Integer>>(source, target));
					it.remove();
					continue;
				}

				macroPlugin.object2Rank.put(targetObject, rank + 1);

			}
			rank++;
		}
		macroPlugin.maxRank = rank;

	}

}