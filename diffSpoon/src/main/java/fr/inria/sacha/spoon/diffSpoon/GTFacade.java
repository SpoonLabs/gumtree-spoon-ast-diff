package fr.inria.sacha.spoon.diffSpoon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.labri.gumtree.Mapping;
import fr.labri.gumtree.Mappings;
import fr.labri.gumtree.ProduceFileTree;
import fr.labri.gumtree.Tree;
import fr.labri.gumtree.actions.Action;
import fr.labri.gumtree.actions.Delete;
import fr.labri.gumtree.actions.GenerateActions;
import fr.labri.gumtree.actions.Insert;
import fr.labri.gumtree.actions.Move;
import fr.labri.gumtree.actions.Permute;
import fr.labri.gumtree.actions.Update;
import fr.labri.gumtree.gen.jdt.ProduceJDTTree;
import fr.labri.gumtree.gen.jdt.cd.ProduceCDJDTTree;
import fr.labri.gumtree.matchers.GumTreeMatcher;
import fr.labri.gumtree.matchers.Matcher;

/**
 * Fine granularity comparison between two files according to a granularity. GT
 * Matching
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class GTFacade {

	public boolean defaultOnlyRoots = true;

	Tree tl = null;
	Tree tr = null;
	public Set<Mapping> mappings = null;

	public List<Action> analyzeFiles(File fjaval, File fjavar, GranuralityType granularity) {
		return this.analyzeFiles(fjaval, fjavar, granularity, defaultOnlyRoots);
	}

	public List<Action> analyzeFiles(File fjaval, File fjavar, GranuralityType granularity, boolean onlyRoot) {

		List<Action> actions = null;
		try {
			ProduceFileTree gen = null;
			if (granularity.equals(GranuralityType.CD))
				gen = new ProduceCDJDTTree();
			else if (granularity.equals(GranuralityType.JDT))
				gen = new ProduceJDTTree();

			/* Tree */tl = gen.generate(fjaval.getAbsolutePath());

			/* Tree */tr = gen.generate(fjavar.getAbsolutePath());

			// Matcher matcher = new ChangeDistillerMatcher(tl, tr);
			actions = getActions(tl,tr,onlyRoot);
			return actions;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public List<Action> getActions(Tree tl, Tree tr, boolean onlyRoot) {
		List<Action> actions;
		Matcher matcher = new GumTreeMatcher(tl, tr);
		mappings = matcher.getMappings();

		GenerateActions gt = new GenerateActions(tl, tr, matcher.getMappings());
		actions = gt.getActions();

		if (onlyRoot) {
			actions = getRootActions(matcher.getMappings(), actions);
		}
		actions.removeAll(Collections.singleton(null));
		return actions;
	}

	public List<Action> analyzeContent(String contentL, String contentR, GranuralityType granularity)
			throws IOException {
		File fileL = createTempFile("filetempl", contentL);
		File fileR = createTempFile("filetempr", contentR);
		return analyzeFiles(fileL, fileR, granularity);

	};

	public List<Action> analyzeContent(String contentL, String contentR, GranuralityType granularity, boolean onlyroots)
			throws IOException {
		File fileL = createTempFile("filetempl", contentL);
		File fileR = createTempFile("filetempr", contentR);
		return analyzeFiles(fileL, fileR, granularity,onlyroots);

	};
	
	public File createTempFile(String name, String content) throws IOException {

		File temp = File.createTempFile(name, ".tmp");

		BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
		bw.write(content);
		bw.close();

		return temp;

	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2)
			throw new IllegalArgumentException("wrong # of parameters");

		GTFacade c = new GTFacade();
		List<Action> actions = c.analyzeFiles(new File(args[0]/* "c:/tmp/Launcher_l.java" */),
				new File(args[1]/* "c:/tmp/Launcher_r.java" */), GranuralityType.CD);

		for (Action action : actions) {
			System.out.println("-->" + action);
		}

	}

	// /
	// ROOT CLASSIFIER
	// /
	protected Set<Tree> srcUpdTrees = new HashSet<Tree>();

	protected Set<Tree> dstUpdTrees = new HashSet<Tree>();

	protected Set<Tree> srcMvTrees = new HashSet<Tree>();

	protected Set<Tree> dstMvTrees = new HashSet<Tree>();

	protected Set<Tree> srcDelTrees = new HashSet<Tree>();

	protected Set<Tree> dstAddTrees = new HashSet<Tree>();

	protected Map<Tree, Action> originalActionsSrc = new HashMap<Tree, Action>();
	protected Map<Tree, Action> originalActionsDst = new HashMap<Tree, Action>();

	/**
	 * @return
	 * 
	 */
	public List<Action> getRootActions(Set<Mapping> rawMappings, List<Action> actions) {
		clean();
		Mappings mappings = new Mappings(rawMappings);

		for (Action a : actions) {

			if (a instanceof Delete) {
				srcDelTrees.add(a.getNode());
				originalActionsSrc.put(a.getNode(), a);
			} else if (a instanceof Insert) {
				dstAddTrees.add(a.getNode());
				originalActionsDst.put(a.getNode(), a);
			} else if (a instanceof Update) {
				srcUpdTrees.add(a.getNode());
				dstUpdTrees.add(mappings.getDst(a.getNode()));
				Tree dest = mappings.getDst(a.getNode());
				//dstMvTrees.add(dest);
				//originalActionsDst.put(dest, a);
				//New
				originalActionsSrc.put(a.getNode(), a);

			} else if (a instanceof Move || a instanceof Permute) {
				srcMvTrees.add(a.getNode());
				Tree dest = mappings.getDst(a.getNode());
				dstMvTrees.add(dest);
				//Bugfix? 
				//originalActionsDst.put(a.getNode(), a);
				originalActionsDst.put(dest, a);
			}
		}
		return retrieveRootActionsFromTreeNodes();
	}

	/**
	 * This method retrieves ONLY the ROOT actions
	 * 
	 * @return
	 */
	private List<Action> retrieveRootActionsFromTreeNodes() {

		List<Action> rootActions = new ArrayList<Action>();

/*		for (Tree t : dstUpdTrees) {
			// inc("UPD " + t.getTypeLabel() + " IN " +
			// t.getParent().getTypeLabel(), summary);
			Action a = originalActionsDst.get(t);
			rootActions.add(a);
		}*/
		
		//New: iterates source instead of dest
		for (Tree t : srcUpdTrees) {
			// inc("UPD " + t.getTypeLabel() + " IN " +
			// t.getParent().getTypeLabel(), summary);
			Action a = originalActionsSrc.get(t);
			rootActions.add(a);
		}
		for (Tree t : srcDelTrees) {
			if (!srcDelTrees.contains(t.getParent())) {
				Action a = originalActionsSrc.get(t);
				rootActions.add(a);

			}
		}
		for (Tree t : dstAddTrees) {
			if (!dstAddTrees.contains(t.getParent())) {
				Action a = originalActionsDst.get(t);
				rootActions.add(a);
			}
		}
		//Due to the change in getRootActions
	/*	for (Tree t : srcMvTrees) {
			if (!srcMvTrees.contains(t.getParent())) {
				Action a = originalActionsSrc.get(t);
				rootActions.add(a);
			}
		}*/
		for (Tree t : dstMvTrees) {
			if (!dstMvTrees.contains(t.getParent())) {
				Action a = originalActionsDst.get(t);
				rootActions.add(a);
			}
		}
		rootActions.removeAll(Collections.singleton(null));
		return rootActions;
	}


	
	
	private void clean() {
		srcUpdTrees.clear();

		dstUpdTrees.clear();

		srcMvTrees.clear();

		dstMvTrees.clear();

		srcDelTrees.clear();

		dstAddTrees.clear();

		originalActionsSrc.clear();

		originalActionsDst.clear();
	}
}
