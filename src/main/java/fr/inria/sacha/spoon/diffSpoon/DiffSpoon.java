package fr.inria.sacha.spoon.diffSpoon;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import spoon.compiler.SpoonCompiler;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import spoon.support.compiler.jdt.JDTSnippetCompiler;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.CompositeMatchers.ClassicGumtree;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;

/**
 * Computes the differences between two CtElements.
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class DiffSpoon {

	public static final Logger logger = Logger.getLogger(DiffSpoon.class);
	protected Factory factory = null;
	protected SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
	protected Set<Mapping> mappings = null;
	
	public DiffSpoon(boolean noClasspath) {
		this();
		factory.getEnvironment().setNoClasspath(noClasspath);
	}
	
	public DiffSpoon(boolean noClasspath,boolean decorate ) {
		this();
		factory.getEnvironment().setNoClasspath(noClasspath);
	}
	
	
	public DiffSpoon(Factory factory, boolean decorate) {
		this(factory);
	}
	
	public DiffSpoon(Factory factory) {
		this.factory = factory;
		logger.setLevel(Level.DEBUG);
		factory.getEnvironment().setNoClasspath(true);
	}

	public DiffSpoon() {
		factory = new FactoryImpl(new DefaultCoreFactory(),
				new StandardEnvironment());
		logger.setLevel(Level.DEBUG);
		factory.getEnvironment().setNoClasspath(true);
	}

	

	@Deprecated
	public CtDiff compare(String left, String right) {

		CtType<?> clazz1;
		try {
			clazz1 = getCtType(left);
	

			CtType<?> clazz2 = getCtType(right);
		//factory.Code().createCodeSnippetStatement(right)
		//		.compile();

			
		return analyze(clazz1, clazz2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
			
	}
	
	public CtClass getCtClass(File f) throws Exception{
		SpoonResource sr1 = SpoonResourceHelper .createResource(f) ;
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
		compiler.getFactory().getEnvironment().setLevel("OFF");
		compiler.addInputSource(sr1);
		compiler.build();
		CtClass<?> clazz1 = (CtClass<?>) factory.Type().getAll().get(0);
		return clazz1;
	}

	
	public  CtType<?> getCtType(String content) throws Exception{
				
		SpoonCompiler compiler = new JDTSnippetCompiler(factory, content);//new JDTBasedSpoonCompiler(factory);
		//compiler.addInputSource(new VirtualFile(content,""));
		compiler.build();
		CtClass<?> clazz1 = (CtClass<?>) factory.Type().getAll().get(0);
		return clazz1;
	}
	
	public  CtType<?> getCtType2(String content) throws Exception{
		
/*	factory.Package().getAllRoots().clear();
	factory.Type().getAll().clear();*/
	SpoonCompiler builder = new JDTSnippetCompiler(factory, content);

	builder.addInputSource(new VirtualFile(content,""));
	
		try {
			builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		CtType<?> ret =  factory.Type().getAll().get(0);
		factory.Package().getAllRoots().clear();
		return ret;
	}
	
	public CtDiff compare(URL f1, URL f2) throws Exception {
		return this.compare(new File(f1.getFile()), new File(f1.getFile()));
	}
	
	public CtDiff compare(File f1, File f2) throws Exception {
		 
		CtClass<?> clazz1 = getCtClass(f1);
			
		CtClass<?> clazz2 = getCtClass(f2);
		
		CtDiff result = this.analyze(clazz1,clazz2);
		
		return result;
	}


		
	
	public ITree getTree(CtElement element){
		scanner.init();
		scanner.scan(element);
		ITree tree = scanner.getRoot();
		prepare(tree);
		
		scanner.root = null;
		scanner.nodes.clear();
		return tree;
	}

	public CtDiff analyze(CtElement left, CtElement right) {

		ITree rootSpoonLeft = getTree(left);

		ITree rootSpoonRight = getTree(right);

		return compare(rootSpoonLeft, rootSpoonRight);
	}

	public CtDiff compare(ITree rootSpoonLeft, ITree rootSpoonRight) {
	
		List<Action> actions = null;

	//	GumTreeMatcher.prepare(rootSpoonLeft);
	//	GumTreeMatcher.prepare(rootSpoonRight);
		

		
		prepare(rootSpoonLeft);
		prepare(rootSpoonRight);
		
		//---
		/*logger.debug("-----Trees:----");
		logger.debug("left tree:  " + rootSpoonLeft.toTreeString());
		logger.debug("right tree: " + rootSpoonRight.toTreeString());
*/
		// --
		//Matcher matcher = new GumTreeMatcher(rootSpoonLeft, rootSpoonRight);
		//MatcherFactory f = new CompositeMatchers.GumTreeMatcherFactory();
		// matcher = f.newMatcher(rootSpoonLeft, rootSpoonRight);
		Matcher matcher;
		MappingStore mappingsComp = null;
		mappingsComp = new MappingStore();
		matcher=new ClassicGumtree(rootSpoonLeft, rootSpoonRight, mappingsComp);
		//new 
		matcher.match();
		//
		mappings = matcher.getMappingSet();

		ActionGenerator gt = new ActionGenerator(rootSpoonLeft, rootSpoonRight,	matcher.getMappings());
		gt.generate();
		actions = gt.getActions();

		ActionClassifier gtfac = new ActionClassifier();
		List<Action> rootActions = gtfac.getRootActions(mappings, actions);
	
		return new CtDiff(actions, rootActions, mappingsComp);
	}

	/**
	 * 
	 * @param rootActions
	 * @param actionParent
	 * @return
	 */
	public List<Action> retriveActionChilds(List<Action> rootActions,
			Action actionParent) {

		List<Action> actions = new ArrayList<Action>();

		for (Action action : rootActions) {
			ITree t = action.getNode();
			if (t.getParent().equals(actionParent)) {
				actions.add(action);
			}

		}

		return rootActions;
	}

	public void getCtClass(Factory factory, String contents) {
		SpoonCompiler builder = new JDTSnippetCompiler(factory, contents);
		try {
			builder.build();
		} catch (Exception e) {
			throw new RuntimeException(
					"snippet compilation error while compiling: " + contents, e);
		}
	}

	public CtType getSpoonType(String contents) throws Exception {
		try {
			this.getCtClass(factory, contents);
		} catch (Exception e) {
			// must fails
			//System.out.println(" e:  "+e.getCause());
		}
		List<CtType<?>> types = factory.Type().getAll();
		if(types.isEmpty())
		{
			//System.err.println("No Type was created by spoon");
			throw new Exception("No Type was created by spoon");
		}
		CtType spt = types.get(0);
		spt.getPackage().getTypes().remove(spt);

		return spt;

	}

	public String printTree(String tab, ITree t) {

		StringBuffer b = new StringBuffer();
		b.append(t.getType() + ":" + t.getLabel() + " \n");
		Iterator<ITree> cIt = t.getChildren().iterator();
		while (cIt.hasNext()) {
			ITree c = cIt.next();
			b.append(tab + " " + printTree("\t" + tab, c));
			// if (cIt.hasNext()) b.append(" ");
		}
		// b.append(")");
		return b.toString();

	}
	
	public void prepare(ITree node){
		node.refresh();
		TreeUtils.postOrderNumbering(node);
		TreeUtils.computeHeight(node);
		//TreeUtils.computeDigest(node);
	}
	
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
			return;
		}

		File f1 = new File(args[0]);
		File f2 = new File(args[1]);

	

		DiffSpoon ds = new DiffSpoon(true);
		CtDiff result = ds.compare(f1, f2);
		System.out.println(result.toString());
	}

	public static String readFile(File f) throws IOException {
		FileReader reader = new FileReader(f);
		char[] chars = new char[(int) f.length()];
		reader.read(chars);
		String content = new String(chars);
		reader.close();
		return content;
	}

	// Action.getName is private
	private String workAroundVisibility(String actionKind) {
		if ("INS".equals(actionKind)) {
			actionKind = "Insert";
		}
		if ("DEL".equals(actionKind)) {
			actionKind = "Delete";
		}
		if ("UPD".equals(actionKind)) {
			actionKind = "Update";
		}
		return actionKind;
	}
	
	public boolean containsAction(List<Action> actions, String actionKind, String nodeKind){
		actionKind = workAroundVisibility(actionKind);
		for (Action action : actions) {
			if(action.getClass().getSimpleName().equals(actionKind)){
				if (scanner.gtContext.getTypeLabel(action.getNode()).equals(nodeKind)) {
					return true;
				}
			}
		}
		return false;
	}

	
	public boolean containsAction(List<Action> actions, String actionKind, String nodeKind, String nodeLabel){
		actionKind = workAroundVisibility(actionKind);
		for (Action action : actions) {
			if(action.getClass().getSimpleName().equals(actionKind)){
				if (scanner.gtContext.getTypeLabel(action.getNode()).equals(nodeKind)) {
					if (action.getNode().getLabel().equals(nodeLabel)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
