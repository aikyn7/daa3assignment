package kz.aitu.daa;
public final class DSU {
    private final int[] parent, size;
    private int components;
    public long findCalls, successfulUnions;
    public DSU(int n){parent=new int[n];size=new int[n];components=n;for(int i=0;i<n;i++){parent[i]=i;size[i]=1;}}
    public int find(int x){
        findCalls++;
        int r=x; while(parent[r]!=r) r=parent[r];
        while(parent[x]!=x){int next=parent[x];parent[x]=r;x=next;}
        return r;
    }
    public boolean union(int a,int b){
        int x=find(a), y=find(b); if(x==y)return false;
        if(size[x]<size[y]){int t=x;x=y;y=t;}
        parent[y]=x;size[x]+=size[y];components--;successfulUnions++;return true;
    }
    public int components(){return components;}
}
