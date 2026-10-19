package kz.aitu.daa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class AlgorithmsTest {
 @Test void dsuComponentsAndUnions(){DSU d=new DSU(5);assertTrue(d.union(0,1));assertFalse(d.union(1,0));d.union(3,4);assertEquals(3,d.components());assertEquals(d.find(0),d.find(1));}
 @Test void mstAgreementAndDisconnectedForest(){Graph g=new Graph(5,false);g.addEdge(0,1,4);g.addEdge(0,2,2);g.addEdge(1,2,1);g.addEdge(3,4,7);var a=Algorithms.kruskal(g);var b=Algorithms.prim(g);assertEquals(a.weight(),b.weight());assertEquals(2,a.components());assertEquals(3,a.edges());}
 @Test void sccAndTarjanAgree(){Graph g=new Graph(6,true);g.addEdge(0,1,1);g.addEdge(1,2,1);g.addEdge(2,0,1);g.addEdge(2,3,1);g.addEdge(3,4,1);g.addEdge(4,3,1);assertEquals(canonical(Algorithms.kosaraju(g).components()),canonical(Algorithms.tarjan(g)));}
 @Test void kahnDetectsCycleAndValidatesDag(){Graph g=new Graph(4,true);g.addEdge(0,1,1);g.addEdge(0,2,1);g.addEdge(2,3,1);var t=Algorithms.kahn(g);assertTrue(t.acyclic());int[] p=new int[4];for(int i=0;i<4;i++)p[t.order().get(i)]=i;for(var e:g.edges())assertTrue(p[e.from()]<p[e.to()]);g.addEdge(3,0,1);assertFalse(Algorithms.kahn(g).acyclic());}
 @Test void dagPathsAllowNegativeWeightsAndRestorePath(){Graph g=new Graph(4,true);g.addEdge(0,1,2);g.addEdge(0,2,5);g.addEdge(1,2,-4);g.addEdge(2,3,1);var p=Algorithms.dagShortestPaths(g);assertEquals(-1,p.distance()[3]);assertEquals(List.of(0,1,2,3),Algorithms.restorePath(3,p.parent()));}
 @Test void iterativeSccHandlesHundredThousandVertices(){Graph g=new Graph(100000,true);for(int i=0;i<99999;i++)g.addEdge(i,i+1,1);assertEquals(100000,Algorithms.kosaraju(g).components().size());}
 @Test void loopsAndParallelEdgesDoNotCrash(){Graph g=new Graph(3,false);g.addEdge(0,0,2);g.addEdge(0,1,4);g.addEdge(0,1,1);assertEquals(1,Algorithms.kruskal(g).weight());}
 private Set<Set<Integer>> canonical(List<List<Integer>> cs){Set<Set<Integer>> s=new HashSet<>();for(var c:cs)s.add(new HashSet<>(c));return s;}
}
