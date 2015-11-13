gumtree-spoon-ast-diff
======================

Computes the AST difference between two Spoon abstract syntax trees using the Gumtree algorithm.

If you use this, please cite:

[Fine-grained and Accurate Source Code Differencing](http://hal.archives-ouvertes.fr/hal-01054552) (Jean-Rémy Falleri, Floréal Morandat, Xavier Blanc, Matias Martinez, Martin Monperrus), In Proceedings of the International Conference on Automated Software Engineering, 2014.


Launch tests:

    mvn compile
    mvn test

[![Build Status](https://travis-ci.org/SpoonLabs/gumtree-spoon-ast-diff.svg?branch=master)](https://travis-ci.org/SpoonLabs/gumtree-spoon-ast-diff)

The main class is used this way:
fr.inria.sacha.spoon.diffSpoon.DiffSpoon <file_1> <file_2>

**Testing AST differencing **

gumtree-spoon-ast-diff is heavily tested. The testing of AST tree differencing is quite interesting.

There are cases where the oracle is pretty clear, for instance for the deletion of a node.

```
// there is only one deletion at line 442
assertEquals(actions.size(), 1);
assertTrue(diff.containsAction(actions, "DEL", "Literal", "\"UTF-8\""));
assertEquals(442, result.changedNode().getPosition().getLine());

```

or for the addition of a single node
```
assertEquals(actions.size(), 1);
assertTrue(diff.containsAction(actions, "Insert", "Invocation", "append"));

```

However, with the presence of moves, the answer is less clear. For instance, an insert+delete instead of a move is correct, although not optimal. However, in this case, there are still definitive oracles: for instance, you are sure that the change happened within a certain node

```
// the change happened in System.out.println() at line 334
CtElement ancestor = result.commonAncestor();		
assertTrue(ancestor instanceof CtInvocation);
assertEquals("println", ((CtInvocation)ancestor).getExecutable().getSimpleName());
assertEquals(344,ancestor.getPosition().getLine());

``` 

To conclude, for testing AST differencing, there is not always a unique and complete, it is a blend of assertions on:

* the number of changes
* the presence of certain changes
* the location of the change (node type and content)
* the location of the change (line number)



