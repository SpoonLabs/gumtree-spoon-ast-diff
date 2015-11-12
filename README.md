gumtree-spoon-ast-diff
======================

Computes the AST difference between two Spoon abstract syntax trees using the Gumtree algorithm [1]

Launch tests:

    mvn compile
    mvn test

[![Build Status](https://travis-ci.org/SpoonLabs/gumtree-spoon-ast-diff.svg?branch=master)](https://travis-ci.org/SpoonLabs/gumtree-spoon-ast-diff)

The main class is used this way:
fr.inria.sacha.spoon.diffSpoon.DiffSpoon <file_1> <file_2>


**Bibliography**

[1] [Fine-grained and Accurate Source Code Differencing](http://hal.archives-ouvertes.fr/hal-01054552) (Jean-Rémy Falleri, Floréal Morandat, Xavier Blanc, Matias Martinez, Martin Monperrus), In Proceedings of the International Conference on Automated Software Engineering, 2014.

