/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package gumtree.spoon.diff;

import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.AbstractTree;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActionGeneratorDbg {

    private ITree src;

    private ITree tmpSrc;

    private ITree dst;

    private MappingStore origMappings;

    private MappingStore newMappings;

    private Set<ITree> dstInOrder;

    private Set<ITree> srcInOrder;

    private int lastId;

    private List<Action> actions;

    private TIntObjectMap<ITree> origSrcTrees;

    private TIntObjectMap<ITree> cpySrcTrees;

    public ActionGeneratorDbg(ITree src, ITree dst, MappingStore mappings) {
        this.src = src;
        this.tmpSrc = this.src.deepCopy();
        this.dst = dst;

        origSrcTrees = new TIntObjectHashMap<>();
        for (ITree t: this.src.getTrees())
            origSrcTrees.put(t.getId(), t);
        cpySrcTrees = new TIntObjectHashMap<>();
        for (ITree t: tmpSrc.getTrees())
            cpySrcTrees.put(t.getId(), t);

        origMappings = new MappingStore();
        for (Mapping m: mappings)
            this.origMappings.link(cpySrcTrees.get(m.getFirst().getId()), m.getSecond());
        this.newMappings = origMappings.copy();
    }

    public List<Action> getActions() {
        return actions;
    }

    public List<Action> generate() {
        ITree srcFakeRoot = new AbstractTree.FakeTree(tmpSrc);
        ITree dstFakeRoot = new AbstractTree.FakeTree(dst);
        tmpSrc.setParent(srcFakeRoot);
        dst.setParent(dstFakeRoot);

        actions = new ArrayList<>();
        dstInOrder = new HashSet<>();
        srcInOrder = new HashSet<>();

        lastId = tmpSrc.getSize() + 1;
        newMappings.link(srcFakeRoot, dstFakeRoot);

        List<ITree> bfsDst = TreeUtils.breadthFirst(dst);
        for (ITree destNode: bfsDst) {
            ITree sourceNode = newMappings.getSrc(destNode);
            ITree sourceNodeParent = newMappings.getSrc(destNode.getParent());

            if (!newMappings.hasDst(destNode)) {
                ITree w = null;
                int k = findPos(destNode);
                // Insertion case : insert new node.
                w = new AbstractTree.FakeTree();
                w.setId(newId());
                // In order to use the real nodes from the second tree, we
                // furnish x instead of w and fake that x has the newly
                // generated ID.
                Action ins = new Insert(destNode, origSrcTrees.get(sourceNodeParent.getId()), k);
                actions.add(ins);
                //System.out.println(ins);
                origSrcTrees.put(w.getId(), destNode);
                newMappings.link(w, destNode);
                sourceNodeParent.getChildren().add(k, w);
                w.setParent(sourceNodeParent);
            } else {
                // there is a mapping
                ITree correspondingSrcNode = null;
                correspondingSrcNode = newMappings.getSrc(destNode);
                if (!destNode.equals(dst)) { // TODO => x != dst // Case of the root
                    ITree destNodeParent = correspondingSrcNode.getParent();
                    if (!correspondingSrcNode.getLabel().equals(destNode.getLabel())) {
                        actions.add(new Update(origSrcTrees.get(correspondingSrcNode.getId()), destNode.getLabel()));
                        correspondingSrcNode.setLabel(destNode.getLabel());
                    }
                    if (true
                            //&& sourceNode.equals(destNode) // a move must be the same node
                            //&& sourceNodeParent.equals(destNodeParent) // original condition

                            // and in a different parent
                            // there are two ways to go to the corresponding parent
                            // parent->mapping ou mapping->parent
                            && newMappings.getSrc(destNode).getParent() != newMappings.getSrc(destNode.getParent())
                            ) {
                        int k = findPos(destNode);
                        Action mv = null;
                        mv = new Move(origSrcTrees.get(correspondingSrcNode.getId()), origSrcTrees.get(sourceNodeParent.getId()), k);
                        actions.add(mv);
                        //System.out.println(mv);
                        int oldk = correspondingSrcNode.positionInParent();
                        sourceNodeParent.getChildren().add(k, correspondingSrcNode);
                        correspondingSrcNode.getParent().getChildren().remove(oldk);
                        correspondingSrcNode.setParent(sourceNodeParent);
                    }
                }
            }

            //FIXME not sure why :D
            //srcInOrder.add(w);
            // dstInOrder.add(sourceNode);
            //alignChildren(w, x);
        }

        for (ITree w : tmpSrc.postOrder()) {
            if (!newMappings.hasSrc(w)) {
                actions.add(new Delete(origSrcTrees.get(w.getId())));
                //w.getParent().getChildren().remove(w);
            }
        }

        //FIXME should ensure isomorphism.
        return actions;
    }

    private void alignChildren(ITree w, ITree x) {
        srcInOrder.removeAll(w.getChildren());
        dstInOrder.removeAll(x.getChildren());

        List<ITree> s1 = new ArrayList<>();
        for (ITree c: w.getChildren())
            if (newMappings.hasSrc(c))
                if (x.getChildren().contains(newMappings.getDst(c)))
                    s1.add(c);

        List<ITree> s2 = new ArrayList<>();
        for (ITree c: x.getChildren())
            if (newMappings.hasDst(c))
                if (w.getChildren().contains(newMappings.getSrc(c)))
                    s2.add(c);

        List<Mapping> lcs = lcs(s1, s2);

        for (Mapping m : lcs) {
            srcInOrder.add(m.getFirst());
            dstInOrder.add(m.getSecond());
        }

        for (ITree a : s1) {
            for (ITree b: s2 ) {
                if (origMappings.has(a, b)) {
                    if (!lcs.contains(new Mapping(a, b))) {
                        int k = findPos(b);
                        Action mv = new Move(origSrcTrees.get(a.getId()), origSrcTrees.get(w.getId()), k);
                        actions.add(mv);
                        //System.out.println(mv);
                        int oldk = a.positionInParent();
                        w.getChildren().add(k, a);
                        if (k  < oldk ) // FIXME this is an ugly way to patch the index
                            oldk ++;
                        a.getParent().getChildren().remove(oldk);
                        a.setParent(w);
                        srcInOrder.add(a);
                        dstInOrder.add(b);
                    }
                }
            }
        }
    }

    private int findPos(ITree x) {
        ITree y = x.getParent();
        List<ITree> siblings = y.getChildren();

        for (ITree c : siblings) {
            if (dstInOrder.contains(c)) {
                if (c.equals(x)) return 0;
                else break;
            }
        }

        int xpos = x.positionInParent();
        ITree v = null;
        for (int i = 0; i < xpos; i++) {
            ITree c = siblings.get(i);
            if (dstInOrder.contains(c)) v = c;
        }

        //if (v == null) throw new RuntimeException("No rightmost sibling in order");
        if (v == null) return 0;

        ITree u = newMappings.getSrc(v);
        // siblings = u.getParent().getChildren();
        // int upos = siblings.indexOf(u);
        int upos = u.positionInParent();
        // int r = 0;
        // for (int i = 0; i <= upos; i++)
        // if (srcInOrder.contains(siblings.get(i))) r++;
        return upos + 1;
    }

    private int newId() {
        return ++lastId;
    }

    private List<Mapping> lcs(List<ITree> x, List<ITree> y) {
        int m = x.size();
        int n = y.size();
        List<Mapping> lcs = new ArrayList<>();

        int[][] opt = new int[m + 1][n + 1];
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (newMappings.getSrc(y.get(j)).equals(x.get(i))) opt[i][j] = opt[i + 1][j + 1] + 1;
                else  opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
            }
        }

        int i = 0, j = 0;
        while (i < m && j < n) {
            if (newMappings.getSrc(y.get(j)).equals(x.get(i))) {
                lcs.add(new Mapping(x.get(i), y.get(j)));
                i++;
                j++;
            } else if (opt[i + 1][j] >= opt[i][j + 1]) i++;
            else j++;
        }

        return lcs;
    }

}
