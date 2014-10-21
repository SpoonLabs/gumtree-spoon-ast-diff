package fr.inria.sacha.spoon.diffSpoon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTSnippetCompiler;
import fr.labri.gumtree.Mapping;
import fr.labri.gumtree.Mappings;
import fr.labri.gumtree.ProduceFileTree;
import fr.labri.gumtree.Tree;
import fr.labri.gumtree.actions.Action;
import fr.labri.gumtree.actions.GenerateActions;
import fr.labri.gumtree.gen.jdt.ProduceJDTTree;
import fr.labri.gumtree.matchers.GumTreeMatcher;
import fr.labri.gumtree.matchers.Matcher;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class DiffSpoon {
	
	public static final Logger logger = Logger.getLogger(DiffSpoon.class);
	protected Factory factory = null;
	
	public DiffSpoon(Factory factory){
		this.factory = factory;
	}
	
	public DiffSpoon(){
		factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		logger.setLevel(Level.DEBUG);
	}
	
	public DiffSpoon(boolean noClasspath){
		this();
		factory.getEnvironment().setNoClasspath(noClasspath);
	}
	
	public CtDiff analyze(String left, String right) {

		CtClass<?> clazz1 = factory.Code().createCodeSnippetStatement(left).compile();

		CtClass<?> clazz2 = factory.Code().createCodeSnippetStatement(right).compile();

		
		return analyze(clazz1, clazz2);
	}
	
	Set<Mapping> mappings = null;
	Mappings mappingsComp = null;
	
	
	public CtDiff analyze(CtElement left, CtElement right) {
		
		SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();

		scanner.scan(left);
		Tree rootSpoonLeft = scanner.getRoot();

		scanner.root = null;
		scanner.nodes.clear();
		scanner.init();

		scanner.scan(right);
		Tree rootSpoonRight = scanner.getRoot();

		return compare(rootSpoonLeft, rootSpoonRight);
	}

	protected CtDiff compare(Tree rootSpoonLeft, Tree rootSpoonRight) {
		ProduceFileTree gen = null;
		gen = new ProduceJDTTree();

		
		List<Action> actions = null;

		GumTreeMatcher.prepare(rootSpoonLeft);
		GumTreeMatcher.prepare(rootSpoonRight);
		
		logger.debug("left tree:"+ rootSpoonLeft.toTreeString());
		logger.debug("right tree:"+rootSpoonRight.toTreeString());
	
		//--
		Matcher matcher = new GumTreeMatcher(rootSpoonLeft, rootSpoonRight);
		mappings = matcher.getMappings();
		mappingsComp = new Mappings(mappings);

		GenerateActions gt = new GenerateActions(rootSpoonLeft, rootSpoonRight, matcher.getMappings());
		actions = gt.getActions();

		GTFacade gtfac = new GTFacade();
		List<Action> rootActions = gtfac.getRootActions(mappings, actions);
		logger.debug("Root Actions "+rootActions.size() );
		for (Action action : rootActions) {
			logger.debug("--> " + action);
		}
		
		logger.debug("All actions: "+actions.size());
		for (Action action : actions) {
			if(action.getNode().getParent() != null)
				logger.debug("--> " + action);
		}

	
		return new CtDiff(actions,rootActions);
	}
	
	/**
	 * 
	 * @param rootActions
	 * @param actionParent
	 * @return
	 */
	public List<Action> retriveActionChilds(List<Action> rootActions, Action actionParent) {
		
		List<Action> actions = new ArrayList<Action>();
		
		for (Action action : rootActions) {
			Tree t = action.getNode();
			if(t.getParent().equals(actionParent)){
				actions.add(action);
			}
			
		}

		return rootActions;
	}
	public void getCtClass(Factory factory, String contents){
		SpoonCompiler builder = new JDTSnippetCompiler(factory, contents);
		try {
			builder.build(); 
		} catch (Exception e) {
			throw new RuntimeException(
					"snippet compilation error while compiling: " + contents, e);
		}
	}

	
	public CtClass getCtClass(String contents){
		try{
			this.getCtClass(factory, contents);
		}catch(Exception e){
			//must fails
		}
		List<CtSimpleType<?>> types = factory.Type().getAll();
		CtSimpleType spt = types.get(0);
		spt.getPackage().getTypes().remove(spt);
		
		return (CtClass) spt;
		
	}
	
	public String printTree(String tab,Tree t){
	
			StringBuffer b = new StringBuffer();
			b.append(t.getTypeLabel()+":"+ t.getLabel()+ " \n");
			Iterator<Tree> cIt = t.getChildren().iterator();
			while (cIt.hasNext()) {
				Tree c = cIt.next();
				b.append(tab + " "+printTree("\t"+tab, c));
			//	if (cIt.hasNext()) b.append(" ");
			}
			//b.append(")");
			return b.toString();
		
	}
	
	public static void main(String[] args){
		//TODO
	}
	
}
