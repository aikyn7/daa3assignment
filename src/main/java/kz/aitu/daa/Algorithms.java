package kz.aitu.daa;

import java.util.*;

public final class Algorithms {
    private Algorithms(){}
    public record MstResult(long weight,int components,int edges,Metrics metrics){}
    public record SccResult(List<List<Integer>> components,Metrics metrics){}
    public record TopoResult(List<Integer> order,boolean acyclic,Metrics metrics){}
    public record PathResult(long[] distance,int[] parent,Metrics metrics){}

    public static MstResult kruskal(Graph g){
        if(g.directed()) throw new IllegalArgumentException("MST requires undirected graph");
        Metrics m=new Metrics(); List<Graph.Edge> es=new ArrayList<>(g.edges());
        es.sort((a,b)->{m.comparisons++;return Integer.compare(a.weight(),b.weight());});
        DSU d=new DSU(g.size()); long total=0;int used=0;
        for(Graph.Edge e:es){m.edgeScans++;m.dsuOps+=2;
            if(d.find(e.from())!=d.find(e.to())){d.union(e.from(),e.to());m.dsuOps+=2;total+=e.weight();used++;}
        }
        return new MstResult(total,d.components(),used,m);
    }
    public static MstResult prim(Graph g){
        if(g.directed()) throw new IllegalArgumentException("MST requires undirected graph");
        record Item(int vertex,int parent,int weight){}
        Metrics m=new Metrics(); int n=g.size(); boolean[] seen=new boolean[n];
        PriorityQueue<Item> pq=new PriorityQueue<>(Comparator.comparingInt(Item::weight));
        long total=0;int comps=0,used=0;
        for(int start=0;start<n;start++) if(!seen[start]){
            comps++;pq.add(new Item(start,-1,0));
            while(!pq.isEmpty()){
                Item x=pq.poll(); if(seen[x.vertex])continue;
                seen[x.vertex]=true;if(x.parent!=-1){total+=x.weight;used++;}
                for(Graph.Arc a:g.neighbors(x.vertex)){m.edgeScans++; if(!seen[a.to()])pq.add(new Item(a.to(),x.vertex,a.weight()));}
            }
        }
        return new MstResult(total,comps,used,m);
    }
    public static SccResult kosaraju(Graph g){
        int n=g.size();Metrics m=new Metrics();boolean[] seen=new boolean[n];List<Integer> finish=new ArrayList<>(n);
        // Iterative DFS with per-frame next-neighbor index; counts actual traversed arcs.
        for(int s=0;s<n;s++)if(!seen[s]){
            ArrayDeque<int[]> st=new ArrayDeque<>();seen[s]=true;st.push(new int[]{s,0});m.maxDepth=Math.max(m.maxDepth,st.size());
            while(!st.isEmpty()){
                int[] top=st.peek();List<Graph.Arc> ns=g.neighbors(top[0]);
                if(top[1]<ns.size()){
                    Graph.Arc a=ns.get(top[1]++);m.edgeScans++;
                    if(!seen[a.to()]){seen[a.to()]=true;st.push(new int[]{a.to(),0});m.maxDepth=Math.max(m.maxDepth,st.size());}
                }else{finish.add(top[0]);st.pop();}
            }
        }
        Graph rev=g.reversed();Arrays.fill(seen,false);List<List<Integer>> out=new ArrayList<>();
        for(int i=finish.size()-1;i>=0;i--){int s=finish.get(i);if(seen[s])continue;
            List<Integer> comp=new ArrayList<>();ArrayDeque<Integer> st=new ArrayDeque<>();st.push(s);seen[s]=true;m.maxDepth=Math.max(m.maxDepth,st.size());
            while(!st.isEmpty()){int u=st.pop();comp.add(u);
                for(Graph.Arc a:rev.neighbors(u)){m.edgeScans++;if(!seen[a.to()]){seen[a.to()]=true;st.push(a.to());m.maxDepth=Math.max(m.maxDepth,st.size());}}
            }out.add(comp);
        }
        return new SccResult(out,m);
    }
    public static TopoResult kahn(Graph g){
        int n=g.size();int[] indeg=new int[n];Metrics m=new Metrics();
        for(Graph.Edge e:g.edges())indeg[e.to()]++;
        ArrayDeque<Integer> q=new ArrayDeque<>();for(int i=0;i<n;i++)if(indeg[i]==0)q.add(i);
        List<Integer> order=new ArrayList<>(n);
        while(!q.isEmpty()){int u=q.remove();order.add(u);for(Graph.Arc a:g.neighbors(u)){m.edgeScans++;if(--indeg[a.to()]==0)q.add(a.to());}}
        return new TopoResult(order,order.size()==n,m);
    }
    public static PathResult dagShortestPaths(Graph g){
        TopoResult t=kahn(g);if(!t.acyclic())throw new IllegalArgumentException("DAG shortest paths require an acyclic graph");
        int n=g.size();int[] indeg=new int[n];for(Graph.Edge e:g.edges())indeg[e.to()]++;
        long[] d=new long[n];Arrays.fill(d,Long.MAX_VALUE/4);int[] parent=new int[n];Arrays.fill(parent,-1);
        for(int i=0;i<n;i++)if(indeg[i]==0)d[i]=0;
        Metrics m=new Metrics();
        for(int u:t.order())for(Graph.Arc a:g.neighbors(u)){m.edgeScans++;if(d[u]+a.weight()<d[a.to()]){d[a.to()]=d[u]+a.weight();parent[a.to()]=u;}}
        return new PathResult(d,parent,m);
    }
    public static List<Integer> restorePath(int target,int[] parent){
        List<Integer> p=new ArrayList<>();for(int v=target;v!=-1;v=parent[v])p.add(v);Collections.reverse(p);return p;
    }
    public static List<List<Integer>> tarjan(Graph g){
        // Iterative Tarjan bonus implementation.
        int n=g.size(),time=0;int[] index=new int[n],low=new int[n],parent=new int[n];Arrays.fill(index,-1);Arrays.fill(parent,-1);
        boolean[] on=new boolean[n];ArrayDeque<Integer> active=new ArrayDeque<>();List<List<Integer>> result=new ArrayList<>();
        for(int root=0;root<n;root++)if(index[root]<0){
            ArrayDeque<int[]> dfs=new ArrayDeque<>();index[root]=low[root]=time++;active.push(root);on[root]=true;dfs.push(new int[]{root,0});
            while(!dfs.isEmpty()){
                int[] f=dfs.peek();int u=f[0];List<Graph.Arc> ns=g.neighbors(u);
                if(f[1]<ns.size()){
                    int v=ns.get(f[1]++).to();if(index[v]<0){parent[v]=u;index[v]=low[v]=time++;active.push(v);on[v]=true;dfs.push(new int[]{v,0});}
                    else if(on[v])low[u]=Math.min(low[u],index[v]);
                }else{
                    dfs.pop();if(parent[u]!=-1)low[parent[u]]=Math.min(low[parent[u]],low[u]);
                    if(low[u]==index[u]){List<Integer> c=new ArrayList<>();int v;do{v=active.pop();on[v]=false;c.add(v);}while(v!=u);result.add(c);}
                }
            }
        }return result;
    }
}
