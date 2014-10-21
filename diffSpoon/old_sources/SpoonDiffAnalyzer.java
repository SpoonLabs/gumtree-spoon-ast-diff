package fr.adam.scanner.spoon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import spoon.compiler.SpoonCompiler;
import spoon.reflect.Factory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtSimpleType;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import spoon.support.compiler.jdt.JDTSnippetCompiler;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtIfImpl;
import fr.adam.scanner.GTFacade;
import fr.adam.scanner.ifexperiments.IfFineGrainChangeCommitAnalyzer;
import fr.adam.scanner.pattern.IfPattern;
import fr.adam.scanner.pattern.IfPatternFactory;
import fr.adam.scanner.pattern.IfPatternInstance;
import fr.labri.gumtree.Mapping;
import fr.labri.gumtree.Mappings;
import fr.labri.gumtree.ProduceFileTree;
import fr.labri.gumtree.Tree;
import fr.labri.gumtree.actions.Action;
import fr.labri.gumtree.actions.GenerateActions;
import fr.labri.gumtree.actions.Insert;
import fr.labri.gumtree.actions.Update;
import fr.labri.gumtree.gen.jdt.ProduceJDTTree;
import fr.labri.gumtree.matchers.GumTreeMatcher;
import fr.labri.gumtree.matchers.Matcher;

/**
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class SpoonDiffAnalyzer {

	Factory factory = new Factory(new DefaultCoreFactory(), new StandardEnvironment());

	public List<Action>[] analyze(String left, String right) {

		CtClass<?> clazz1 = factory.Code().createCodeSnippetStatement(left).compile();

		CtClass<?> clazz2 = factory.Code().createCodeSnippetStatement(right).compile();

		
		return analyze(left, right);
	}
	
	public List<IfPatternInstance> getPatterns(String cl, String cr){
		
		List<IfPatternInstance> patternsRet = new ArrayList<IfPatternInstance>();
		List<Action>[] actions = this.analyze(cl, cr);
		Map<Tree,List<Action>> actionsIf =  this.searchIfConditionChanges(actions[0]);
		for (Tree tree : actionsIf.keySet()) {
			patternsRet.addAll(detectPattern(actionsIf.get(tree)));
		}
		return patternsRet;
	}
	
	public List<IfPatternInstance> resolvePatterns(Map<Tree,List<Action>> actionsIf ){
		List<IfPatternInstance> patternsRet = new ArrayList<IfPatternInstance>();
		for (Tree tree : actionsIf.keySet()) {
			List<Action> actions = actionsIf.get(tree);
			resolvePatterns(patternsRet, actions);
		}
		return patternsRet;
	}
	public List<IfPatternInstance> resolvePatterns( List<Action> actions) {
		List<IfPatternInstance> patternsRet = new ArrayList<IfPatternInstance>();
		this.resolvePatterns( patternsRet, actions);
		return patternsRet;
	}	
	protected void resolvePatterns(List<IfPatternInstance> patternsRet, List<Action> actions) {
		if(actions.size() < IfFineGrainChangeCommitAnalyzer.MAX_SPOON_AST_CHANGES_ACTION){
			List<IfPatternInstance> l = detectPattern(actions); 
			patternsRet.addAll(l);
		}
	}
	
	private List<IfPatternInstance> detectPattern(List<Action> list) {
		
		List<IfPatternInstance> patternsRet = new ArrayList<IfPatternInstance>();
		
		List<IfPattern> patterns = IfPatternFactory.patterns;
		for (IfPattern ifPattern : patterns) {
			IfPatternInstance instance = ifPattern.match(list);
			if(instance != null){
				patternsRet.add(instance);
				System.out.println(ifPattern.getName()+" present "+instance.toString());
			}
		}
		return patternsRet;
		
	}

	public Map<Tree, List<Action>> searchIfConditionChanges(List<Action> actions){
		Map<Tree, List<Action>> ifs = new HashMap<Tree, List<Action>>();
		System.out.println();
		for (Action action : actions) {
			boolean foundStopNode = false;

			Tree current_tree = action.getNode().getParent();
			
			while(!foundStopNode && current_tree != null){
				//If we found a Block we stop
				if(current_tree.getTypeLabel().equals(CtBlockImpl.class.getSimpleName())){
					//We discart the change action that are applied in one element which has a parent Block.
					foundStopNode = true;
				}
				else{
					//else, if is an if, we analyze it.
					if(current_tree.getTypeLabel().equals(CtIfImpl.class.getSimpleName())){
						foundStopNode = true;
						List<Action> actionsif = null;
						//The action is inside the If, we store it.
						if(action instanceof Insert){
							//An insert is never mapped!
							Tree map = mappingsComp.getSrc(current_tree);
							if(map != null){
								current_tree = map;
							}
							
						}
						//We store
						if(ifs.containsKey(current_tree)){
							actionsif = ifs.get(current_tree);
						}else{
							 actionsif = new ArrayList<Action>();
							 ifs.put(current_tree, actionsif);
						}
						actionsif.add(action);
					}	
				}
				current_tree = current_tree.getParent();	
			}
		}
		System.out.println("Total if roots: "+ifs.keySet().size());
		
		for(Tree key: ifs.keySet()){
			System.out.println("Tree"+key + " - "+key.getId());
			List<Action> l = ifs.get(key);
			for (Action action : l) {
				System.out.println("--> "+action + " parent if? "+action.getNode().getParent().equals(key));
			}
		}
		//Now, we take only updates in IF
		for (Action action : actions) {
			if(ifs.containsKey(action.getNode())){
				if(!(action instanceof Update)){
					ifs.remove(action.getNode());
					System.out.println("Removing "+action.getNode() + " - "+action.getNode().getId());
					System.out.println("Due to: "+action);
				}
			}
		}
		return ifs;
	}
	Set<Mapping> mappings = null;
	Mappings mappingsComp = null;
	public List<Action>[] analyze(CtElement left, CtElement right) {
		//all, roots
		List[] r = new List[2];
		
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

	protected List<Action>[] compare(Tree rootSpoonLeft, Tree rootSpoonRight) {
		ProduceFileTree gen = null;
		gen = new ProduceJDTTree();

		
		List<Action> actions = null;

		GumTreeMatcher.prepare(rootSpoonLeft);
		GumTreeMatcher.prepare(rootSpoonRight);
		
		System.out.println(rootSpoonLeft.toTreeString());
	//	System.out.println(printTree("", rootSpoonLeft));
		System.out.println(rootSpoonRight.toTreeString());
	//	System.out.println(printTree("", rootSpoonRight));
		//--
		Matcher matcher = new GumTreeMatcher(rootSpoonLeft, rootSpoonRight);
		mappings = matcher.getMappings();
		mappingsComp = new Mappings(mappings);

		GenerateActions gt = new GenerateActions(rootSpoonLeft, rootSpoonRight, matcher.getMappings());
		actions = gt.getActions();


		System.out.println(actions.size() + " - Row");
	/*	for (Action action : actions) {
			if(action.getNode().getParent() != null)
				System.out.println("--> " + action);
			else
				System.err.println("Action in root");
		}*/

		GTFacade gtfac = new GTFacade();
		List<Action> rootActions = gtfac.getRootActions(mappings, actions);
		System.out.println(rootActions.size() + " - Root");
		/*for (Action action : rootActions) {
			System.out.println("--> " + action);
		}*/
		return new List[]{actions,rootActions};
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
	public void getCtClass(Factory f, String contents){
		SpoonCompiler builder = new JDTSnippetCompiler(f, contents);
		try {
			builder.build(); 
		} catch (Exception e) {
			throw new RuntimeException(
					"snippet compilation error while compiling: " + contents, e);
		}
	}

	public CtClass getCtClass(String content,String projectPath, String fileName){
		
		// We compile all the model

		File f = new File(projectPath+"/src/java");
		if(!f.exists()){
			f =  new File(projectPath+"/src/main/java");
			if(!f.exists())
				return null;
			
		}
		
		
		//SpoonCompiler builder = new JDTSnippetCompiler(factory,(new File(path)).getPath() );
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
		String classpath = "";
		classpath += "C:\\Personal\\develop\\repositoryResearch\\math-version\\141217\\target\\lib\\commons-beanutils-1.6.1.jar;";
		classpath += "C:\\Personal\\develop\\repositoryResearch\\math-version\\141217\\target\\lib\\commons-collections-3.0.jar;";
		classpath += "C:\\Personal\\develop\\repositoryResearch\\math-version\\141217\\target\\lib\\commons-discovery-0.2.jar;";
		classpath += "C:\\Personal\\develop\\repositoryResearch\\math-version\\141217\\target\\lib\\commons-lang-2.0.jar;";
		classpath += "C:\\Personal\\develop\\repositoryResearch\\math-version\\141217\\target\\lib\\commons-logging-1.0.3.jar;";
		
		compiler.setSourceClasspath(classpath);
		
		boolean result = false;
		try {
		compiler.addInputSource(f);
		
		result = 	compiler.build();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String qn = fileName.replace("src/java/", "").replace("src/main/java/", "").replace("/", ".").replace(".java", "");
		if(!qn.startsWith("org")){
			System.err.println("error- not org package");
		}
		
		List<CtSimpleType<?>> types = factory.Type().getAll();
		//System.out.println("types "+types);
		
		
		CtClass cl = (CtClass) factory.Type().get(qn);
		
		/*boolean removedOriginal = cl.getPackage().getTypes().remove(cl);
		if(!removedOriginal){
			System.err.println("Remove original false");
		}*/
		return cl;
	
	}
	
	public CtClass getCtClass( String contents){
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
}
