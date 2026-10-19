package kz.aitu.daa;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class Benchmark {
    private static final int[] SIZES={1000,3162,10000,31623,100000};
    private static final long[] SPARSE_M={4000,12648,40000,126492,400000};
    private static final long[] DENSE_M={20000,63240,200000,632460,2000000};
    private static final long[] MST_REF={147970,30363,468911,97589,1486139,309550,4715515,968517,15012094,3056681};
    private static final int[] SCC_REF={27,1,137,1,373,1,1188,1,3923,1};
    private static final long[] DAG_REF={396274,252411,1271911,723157,3941828,2381342,12551891,7376612,40096539,23512087};
    private Benchmark(){}
    public static Graph generate(int n,int m,String kind){
        Random rnd=new Random(42); boolean directed=!kind.equals("undirected"); Graph g=new Graph(n,directed);
        int[] order=new int[n];for(int i=0;i<n;i++)order[i]=i;
        if(kind.equals("undirected")||kind.equals("dag"))shuffle(order,rnd);
        int added=0;
        if(kind.equals("undirected")){
            for(int i=1;i<n;i++){int a=order[rnd.nextInt(i)],b=order[i];g.addEdge(a,b,1+rnd.nextInt(1000));added++;}
        }
        while(added<m){
            int a=rnd.nextInt(n),b=rnd.nextInt(n);if(a==b)continue;
            if(kind.equals("dag")&&order[a]>order[b]){int t=a;a=b;b=t;}
            g.addEdge(a,b,1+rnd.nextInt(1000));added++;
        }
        return g;
    }
    private static void shuffle(int[] a,Random r){for(int i=a.length-1;i>0;i--){int j=r.nextInt(i+1),t=a[i];a[i]=a[j];a[j]=t;}}
    public static void run(Path output) throws Exception{
        Files.createDirectories(output.getParent());
        try(BufferedWriter w=Files.newBufferedWriter(output)){
            w.write("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result\n");
            int cfg=0;
            for(int si=0;si<SIZES.length;si++)for(int density=0;density<2;density++){
                int n=SIZES[si],m=(int)(density==0?SPARSE_M[si]:DENSE_M[si]);String label=n+"_"+m;
                Graph und=generate(n,m,"undirected");
                long start=System.nanoTime();Algorithms.MstResult kr=null;
                for(int r=0;r<5;r++){long t=System.nanoTime();kr=Algorithms.kruskal(und);if(r==4)start=System.nanoTime()-t;}
                long kt=medianKruskal(und);
                long pt=medianPrim(und);Algorithms.MstResult pr=Algorithms.prim(und);
                if(kr.weight()!=pr.weight()||kr.components()!=pr.components())throw new IllegalStateException("MST mismatch: "+label);
                row(w,"Kruskal",label,n,m,kt,kr.metrics(),kr.weight());
                row(w,"Prim",label,n,m,pt,pr.metrics(),pr.weight());

                Graph dir=generate(n,m,"directed");long st=System.nanoTime();Algorithms.SccResult sr=null;
                long[] times=new long[5];for(int r=0;r<5;r++){long t=System.nanoTime();sr=Algorithms.kosaraju(dir);times[r]=System.nanoTime()-t;}
                row(w,"Kosaraju",label,n,m,median(times),copyMetrics(sr.metrics()),sr.components().size());
                Graph dag=generate(n,m,"dag");Algorithms.TopoResult tr=Algorithms.kahn(dag);
                if(!tr.acyclic()||!validOrder(dag,tr.order()))throw new IllegalStateException("Invalid topo order: "+label);
                long[] ktimes=new long[5],dtimes=new long[5];for(int r=0;r<5;r++){long t=System.nanoTime();tr=Algorithms.kahn(dag);ktimes[r]=System.nanoTime()-t;t=System.nanoTime();Algorithms.PathResult x=Algorithms.dagShortestPaths(dag);dtimes[r]=System.nanoTime()-t;}
                Algorithms.PathResult dp=Algorithms.dagShortestPaths(dag);long sum=0;for(long x:dp.distance())sum+=x;
                row(w,"Kahn",label,n,m,median(ktimes),tr.metrics(),tr.order().size());
                row(w,"DagShortestPath",label,n,m,median(dtimes),dp.metrics(),sum);
                w.flush();cfg++;
            }
        }
    }
    private static Metrics copyMetrics(Metrics a){Metrics b=new Metrics();b.dsuOps=a.dsuOps;b.edgeScans=a.edgeScans;b.comparisons=a.comparisons;b.maxDepth=a.maxDepth;return b;}
    private static long medianKruskal(Graph g){long[] a=new long[5];for(int i=0;i<5;i++){long t=System.nanoTime();Algorithms.kruskal(g);a[i]=System.nanoTime()-t;}return median(a);}
    private static long medianPrim(Graph g){long[] a=new long[5];for(int i=0;i<5;i++){long t=System.nanoTime();Algorithms.prim(g);a[i]=System.nanoTime()-t;}return median(a);}
    private static long median(long[] a){Arrays.sort(a);return a[a.length/2]/1_000_000;}
    private static boolean validOrder(Graph g,List<Integer> order){if(order.size()!=g.size())return false;int[] p=new int[g.size()];for(int i=0;i<order.size();i++)p[order.get(i)]=i;for(Graph.Edge e:g.edges())if(p[e.from()]>=p[e.to()])return false;return true;}
    private static void row(BufferedWriter w,String alg,String graph,int n,int m,long ms,Metrics x,long result)throws IOException{
        w.write(String.format(Locale.ROOT,"%s,%s,%d,%d,%d,%d,%d,%d,%d,%d%n",alg,graph,n,m,ms,x.dsuOps,x.edgeScans,x.comparisons,x.maxDepth,result));
    }
}
