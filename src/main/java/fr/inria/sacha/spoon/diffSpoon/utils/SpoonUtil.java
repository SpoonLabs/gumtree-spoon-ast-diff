package fr.inria.sacha.spoon.diffSpoon.utils;

public final class SpoonUtil {
	/**
	 * Removes the "Ct" at the beginning and the "Impl" at the end
	 *
	 * @param simpleName
	 * @return
	 */
	public static String getTypeName(String simpleName) {
		return simpleName.substring(2, simpleName.length() - 4);
	}

	private SpoonUtil() {
		throw new AssertionError("No instance.");
	}
}
