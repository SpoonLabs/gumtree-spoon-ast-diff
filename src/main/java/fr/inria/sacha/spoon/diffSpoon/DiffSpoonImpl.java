package fr.inria.sacha.spoon.diffSpoon;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.CompositeMatchers.ClassicGumtree;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import fr.inria.sacha.spoon.diffSpoon.utils.TreeUtil;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;

import java.io.File;
import java.util.List;

import static fr.inria.sacha.spoon.diffSpoon.utils.DiffUtil.getCtClass;
import static fr.inria.sacha.spoon.diffSpoon.utils.DiffUtil.getCtType;

/**
 * Computes the differences between two CtElements.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 */
public class DiffSpoonImpl implements DiffSpoon {
	private static final Logger logger = Logger.getLogger(DiffSpoonImpl.class);

	private Context context = new Context();
	Factory factory = null;

	static {
		// default 0.3
		// 0.1 one failing much more changes
		// 0.2 one failing much more changes
		// 0.3 OK
		// 0.4 OK
		// 0.5
		// 0.6 OK
		// 0.7 1 failing
		// 0.8 2 failing
		// 0.9 two failing tests with more changes
		System.setProperty("gumtree.match.bu.sim", "0.3");

		// default 2
		// 0 is really bad for 211903 t_224542 225391 226622
		// 1 is required for t_225262 and t_213712 to pass
		System.setProperty("gumtree.match.gt.minh", "1");

		// default 1000
		// 1 OK
		// 10 OK
		// 100 OK
		// 2000
		// 10000 OK
		//System.getProperty("gumtree.match.bu.size", "1000");
	}

	public DiffSpoonImpl() {
		this(new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment()));
	}

	public DiffSpoonImpl(Factory factory) {
		this.factory = factory;
		logger.setLevel(Level.DEBUG);
		factory.getEnvironment().setNoClasspath(true);
	}

	@Override
	public CtDiff compare(File f1, File f2) throws Exception {
		return this.compare(getCtClass(factory, f1), getCtClass(factory, f2));
	}

	@Override
	public CtDiff compare(String left, String right) {
		return compare(getCtType(factory, left), getCtType(factory, right));
	}

	@Override
	public CtDiff compare(CtElement left, CtElement right) {
		return compare(TreeUtil.getTree(context, left), TreeUtil.getTree(context, right));
	}

	private CtDiff compare(ITree rootSpoonLeft, ITree rootSpoonRight) {
		final MappingStore mappingsComp = new MappingStore();

		final Matcher matcher = new ClassicGumtree(rootSpoonLeft, rootSpoonRight, mappingsComp);
		matcher.match();

		final ActionGenerator actionGenerator = new ActionGenerator(rootSpoonLeft, rootSpoonRight, matcher.getMappings());
		actionGenerator.generate();

		final ActionClassifier actionClassifier = new ActionClassifier();
		final List<Action> rootActions = actionClassifier.getRootActions(matcher.getMappingSet(), actionGenerator.getActions());
		return new CtDiffImpl(actionGenerator.getActions(), rootActions, mappingsComp, context);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
			return;
		}

		final CtDiff result = new DiffSpoonImpl().compare(new File(args[0]), new File(args[1]));
		System.out.println(result.toString());
	}
}
