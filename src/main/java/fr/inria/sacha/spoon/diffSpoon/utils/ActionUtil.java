package fr.inria.sacha.spoon.diffSpoon.utils;

import com.github.gumtreediff.actions.model.Action;

import java.util.ArrayList;
import java.util.List;

public final class ActionUtil {
	/**
	 * @param rootActions
	 * @param actionParent
	 * @return
	 */
	public static List<Action> getActionChildren(List<Action> rootActions, Action actionParent) {
		final List<Action> actions = new ArrayList<>();
		for (Action action : rootActions) {
			if (action.getNode().getParent().equals(actionParent)) {
				actions.add(action);
			}
		}
		return actions;
	}

	private ActionUtil() {
		throw new AssertionError("No instance.");
	}
}
