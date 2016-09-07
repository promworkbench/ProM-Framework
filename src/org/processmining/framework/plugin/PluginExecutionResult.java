package org.processmining.framework.plugin;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.processmining.framework.providedobjects.ProvidedObjectID;

/**
 * This class represents the result of a plugin execution. It can be used to
 * obtain all details after executing a plugin.
 * 
 * Typically, plugins are executed a-synchronously, but by calling synchronize()
 * on this object, the current thread is suspended untill all results are in (or
 * exceptions are thrown)
 * 
 * @author bfvdonge
 * 
 */
public interface PluginExecutionResult {

	/**
	 * Returns the size of this result, i.e. how many objects were returned by
	 * the plugin
	 * 
	 * @return
	 */
	public int getSize();

	/**
	 * Synchronizes on any futures that might still exist in the result. If no
	 * futures exist, this method does terminate normally
	 * 
	 * @throws CancellationException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void synchronize() throws CancellationException, ExecutionException, InterruptedException;

	/**
	 * returns the results of this plugin in an array. If synchronize() was
	 * called, this method returns the actual objects. Otherwise, it might
	 * return ProMFutures on the actual objects (or any combination thereof).
	 * 
	 * @return
	 */
	public Object[] getResults();

	/**
	 * returns the result at the given index, casted to the given returntype. If
	 * synchronize() was called, this method returns the actual object.
	 * Otherwise, it might return a ProMFuture<T> on the actual object.
	 * 
	 * Note that 0<= resultIndex < getSize()
	 * 
	 * @param <T>
	 * @param resultIndex
	 * @return
	 */
	public <T> T getResult(int resultIndex) throws ClassCastException;

	/**
	 * This method returns the names of the results
	 * 
	 * @return
	 */
	public String[] getResultNames();

	/**
	 * this method returns the name of the result at the given index.
	 * 
	 * Note that 0<= resultIndex < getSize()
	 * 
	 * @param resultIndex
	 * @return
	 */
	public String getResultName(int resultIndex);

	/**
	 * Tells this result under which provided object ID the object at index i is
	 * known to the framework
	 * 
	 * @param i
	 *            index of the object
	 * @param id
	 *            the id of the provided object refering to the objet at index i
	 */
	void setProvidedObjectID(int i, ProvidedObjectID id);

	/**
	 * Returns the ProvidedObjectID under which the object at index i is known
	 * to the framework's ProvidedObject manager
	 * 
	 * @param i
	 *            index of the object
	 */
	ProvidedObjectID getProvidedObjectID(int i);

	/**
	 * Returns the type of the object at index i, as declared by the plugin
	 * 
	 * @param i
	 * @return
	 */
	public <T> Class<? super T> getType(int i);

	/**
	 * Returns the plugin used to generate this result
	 * 
	 * @return
	 */
	public PluginDescriptor getPlugin();

}
