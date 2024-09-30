package gumtree.spoon;

import com.github.gumtreediff.tree.Tree;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import spoon.Launcher;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SourcePositionTest {

    public static Stream<Arguments> data() {
        File directory = new File("src/test/resources/source-positions");
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .map(file -> Arguments.of(file, file.getName()));
    }

    private static Tree getRootNode(File javaSourceFile) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(javaSourceFile.getAbsolutePath());
        launcher.buildModel();
        CtPackage rootPackage = launcher.getModel().getRootPackage();
        return new SpoonGumTreeBuilder().getTree(rootPackage);
    }

    private static boolean ignoreNodes(Tree node) {
        CtElement element = (CtElement) node.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
        if (element instanceof CtPackage || element instanceof CtTypeAccess) {
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

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("data")
    public void childNodeMustBeEnclosedInParentNode(File javaSourceFile, String name) {
        Tree rootNode = getRootNode(javaSourceFile);
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

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("data")
    public void allNodesShouldHaveAtLeastZeroLength(File javaSourceFile, String name) {
        Tree rootNode = getRootNode(javaSourceFile);
        for (Tree node : rootNode.getDescendants()) {
            if (ignoreNodes(node)) {
                continue;
            }
            assertTrue(node.getLength() >= 0, node + " has negative length");
        }
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("data")
    public void siblingNodeShouldNotContainEachOther(File javaSourceFile, String name) {
        Tree rootNode = getRootNode(javaSourceFile);
        Queue<Tree> roots = new ArrayDeque<>();
        roots.add(rootNode);
        while (!roots.isEmpty()) {
            List<Tree> siblings = roots.poll().getChildren();
            assertTrue(siblingsDoNotContainEachOther(siblings));
            roots.addAll(siblings);
        }
    }
}
