package gumtree.spoon;

import com.github.gumtreediff.tree.Tree;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import spoon.Launcher;
import spoon.reflect.declaration.CtPackage;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

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
        // we currently ignore packages
        return node.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT) instanceof CtPackage;
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
}