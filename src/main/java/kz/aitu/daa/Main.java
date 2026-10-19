package kz.aitu.daa;
import java.nio.file.*;
public final class Main {
    public static void main(String[] args) throws Exception {
        if(args.length>0 && args[0].equals("benchmark")){
            Path p=Paths.get(args.length>1?args[1]:"results/results.csv");Benchmark.run(p);System.out.println("Benchmark complete: "+p);return;
        }
        if(args.length>1 && args[0].equals("graph")){
            Graph g=Graph.read(Paths.get(args[1]));System.out.println("vertices="+g.size()+", edges="+g.edges().size());
            if(g.directed()){var s=Algorithms.kosaraju(g);System.out.println("SCC count="+s.components().size());var t=Algorithms.kahn(g);System.out.println("acyclic="+t.acyclic()+", topological order size="+t.order().size());}
            else {var k=Algorithms.kruskal(g);var p=Algorithms.prim(g);System.out.println("Kruskal weight="+k.weight()+", Prim weight="+p.weight()+", components="+k.components());}
            return;
        }
        System.out.println("DAA Assignment 3 — Graph algorithms");
        System.out.println("Run tests: mvn test");
        System.out.println("Run full benchmark: mvn -q exec:java -Dexec.args=\"benchmark results/results.csv\"");
        System.out.println("Inspect graph: mvn -q exec:java -Dexec.args=\"graph data/disconnected.txt\"");
    }
}
