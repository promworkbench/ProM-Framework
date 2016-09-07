package org.processmining.framework.util;

import java.lang.reflect.Array;

public class ArrayUtils {

	/**
	 * No need for instantiation. Only static methods
	 * 
	 */
	private ArrayUtils() {

	}

	/**
	 * Makes a copy of an array. If newLength is greater than the length of
	 * source, then the result is an array with the contents of source, appended
	 * with null elements. If the new array is shorter, then only a part is
	 * copied.
	 * 
	 * @param <T>
	 *            type of the array
	 * @param source
	 *            source array
	 * @param newlength
	 *            new length of the target array
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] copyOf(T[] source, int newlength) {
		Class<?> type = source.getClass().getComponentType();
		T[] target = (T[]) Array.newInstance(type, newlength);
		System.arraycopy(source, 0, target, 0, Math.min(newlength, source.length));
		for (int i = source.length; i < target.length; i++) {
			target[i] = null;
		}
		return target;

	}

	/**
	 * Concatenates arrays
	 * 
	 * @param <T>
	 *            type of the array
	 * @param first
	 *            first array
	 * @param rest
	 *            arrays to be added
	 * @return
	 */
	public static <T> T[] concatAll(T[] first, T[]... rest) {

		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

}
