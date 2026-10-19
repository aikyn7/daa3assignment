package kz.aitu.daa;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class Graph {
    public record Edge(int from, int to, int weight) {}
    public record Arc(int to, int weight) {}
    private final int n;
    private final boolean directed;
    private final List<List<Arc>> adj;
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        this.n=n; this.directed=directed; adj=new ArrayList<>(n);
        for(int i=0;i<n;i++) adj.add(new ArrayList<>());
    }
    public int size(){return n;}
    public boolean directed(){return directed;}
    public List<Edge> edges(){return Collections.unmodifiableList(edges);}
    public List<Arc> neighbors(int u){return Collections.unmodifiableList(adj.get(u));}
    public void addEdge(int u,int v,int w){
        check(u); check(v);
        edges.add(new Edge(u,v,w)); adj.get(u).add(new Arc(v,w));
        if(!directed && u!=v) adj.get(v).add(new Arc(u,w));
    }
    private void check(int x){if(x<0||x>=n) throw new IndexOutOfBoundsException("vertex "+x);}
    public Graph reversed(){
        Graph r=new Graph(n,true);
        for(Edge e:edges) r.addEdge(e.to,e.from,e.weight);
        return r;
    }
    public static Graph read(Path path) throws IOException {
        try(BufferedReader br=Files.newBufferedReader(path)){
            String[] h=br.readLine().trim().split("\\s+");
            int n=Integer.parseInt(h[0]), m=Integer.parseInt(h[1]);
            boolean d=h[2].equalsIgnoreCase("directed");
            Graph g=new Graph(n,d);
            for(int i=0;i<m;i++){
                String line=br.readLine(); if(line==null) throw new IOException("Missing edge line "+i);
                String[] p=line.trim().split("\\s+");
                g.addEdge(Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2]));
            }
            return g;
        }
    }
}
