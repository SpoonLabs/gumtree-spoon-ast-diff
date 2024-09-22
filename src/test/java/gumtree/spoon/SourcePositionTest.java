package gumtree.spoon;

import com.github.gumtreediff.tree.Tree;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import spoon.Launcher;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class SourcePositionTest {

    private File javaSourceFile;
    private String fileName;

    public SourcePositionTest(File javaSourceFile, String fileName) {
        this.javaSourceFile = javaSourceFile;
        this.fileName = fileName;
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        File directory = new File("src/test/resources/source-positions");
        return Arrays.stream(Objects.requireNonNull(directory.listFiles())).map(file -> new Object[]{file, file.getName()}).collect(Collectors.toUnmodifiableList());
    }

    private static Tree getRootNode(File javaSourceFile) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(javaSourceFile.getAbsolutePath());
        launcher.buildModel();
        CtPackage rootPackage = launcher.getModel().getRootPackage();
        return new SpoonGumTreeBuilder().getTree(rootPackage);
    }

    private static boolean ignoreNodes(Tree node) {
        // they don't have a position in the Spoon model
        CtElement element = (CtElement) node.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
        if (element instanceof CtPackage) {
            return true;
        }
        if (element instanceof CtTypeAccess) {
            return true;
        }
        return false;
    }

    private static boolean siblingsDoNotContainEachOther(List<Tree> siblings) {
        for (int i = 0; i < siblings.size(); i++) {
            Tree sibling1 = siblings.get(i);
            for (int j = i + 1; j < siblings.size(); j++) {
                Tree sibling2 = siblings.get(j);
                if (ignoreNodes(sibling1) || ignoreNodes(sibling2)) {
                    continue;
                }
                int startPosition1 = sibling1.getPos();
                int endPosition1 = startPosition1 + sibling1.getLength();
                int startPosition2 = sibling2.getPos();
                int endPosition2 = startPosition2 + sibling2.getLength();
                if (startPosition2 <= startPosition1 && endPosition2 >= endPosition1) {
                    throw new AssertionError(sibling1 + " and " + sibling2 + " are overlapping");
                }
                if (startPosition1 <= startPosition2 && endPosition2 <= endPosition1) {
                    throw new AssertionError(sibling1 + " and " + sibling2 + " are overlapping");
                }
                if (startPosition1 < startPosition2 && endPosition1 > startPosition2) {
                    throw new AssertionError(sibling1 + " and " + sibling2 + " are overlapping");
                }
                if (startPosition2 < startPosition1 && endPosition2 > startPosition1) {
                    throw new AssertionError(sibling1 + " and " + sibling2 + " are overlapping");
                }
            }
        }
        return true;
    }

    @Test
    public void childNodeMustBeEnclosedInParentNode() {
        // Arrange
        Tree rootNode = getRootNode(javaSourceFile);
        // Assert
        for (Tree child : rootNode.getDescendants()) {
            Tree parent = child.getParent();
            if (ignoreNodes(child) || ignoreNodes(parent)) {
                continue;
            }
            int startPosition = child.getPos();
            int endPosition = startPosition + child.getLength();
            assertThat(child + " must be enclosed in " + rootNode,
                    startPosition >= parent.getPos() && endPosition <= parent.getPos() + parent.getLength());
        }
    }

    @Test
    public void allNodesShouldHaveAtLeastZeroLength() {
        // Arrange
        Tree rootNode = getRootNode(javaSourceFile);
        // Assert
        for (Tree node : rootNode.getDescendants()) {
            if (ignoreNodes(node)) {
                continue;
            }
            assertTrue(node + " has negative length", node.getLength() >= 0);
        }
    }

    @Test
    public void siblingNodeShouldNotContainEachOther() {
        // Arrange
        Tree rootNode = getRootNode(javaSourceFile);
        // Assert
        Queue<Tree> roots = new ArrayDeque<>();
        roots.add(rootNode);
        while (!roots.isEmpty()) {
            List<Tree> siblings = roots.poll().getChildren();
            assertTrue(siblingsDoNotContainEachOther(siblings));
            roots.addAll(siblings);
        }
    }
}