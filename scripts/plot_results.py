import csv
from pathlib import Path
import matplotlib.pyplot as plt
ROOT=Path(__file__).resolve().parents[1]
rows=list(csv.DictReader((ROOT/'results/results.csv').open()))
rows=[r for r in rows if r.get('time_ms') and int(r['time_ms']) >= 0]
out=ROOT/'results/plots'; out.mkdir(parents=True,exist_ok=True)
algs=sorted(set(r['algorithm'] for r in rows))
plt.figure()
for alg in algs:
 for density in ('sparse','dense'):
  data=[]
  for r in rows:
   n,m=int(r['n']),int(r['m'])
   if r['algorithm']==alg and ((m/n)<=5 if density=='sparse' else (m/n)>5):
    count=int(r['dsu_ops'])+int(r['edge_scans'])+int(r['comparisons'])
    if count>0:data.append((n,count))
  data.sort()
  if data:plt.loglog([x for x,y in data],[y for x,y in data],marker='o',label=f'{alg} {density}')
plt.xlabel('Vertices (n)');plt.ylabel('Counted operations');plt.title('Operations vs n');plt.legend(fontsize=7);plt.tight_layout();plt.savefig(out/'operations_vs_n.png',dpi=160);plt.close()
plt.figure()
for alg in ('Kosaraju','Kahn','DagShortestPath','Kruskal','Prim'):
 data=sorted((int(r['n']),int(r['edge_scans'])/max(1,int(r['m']))) for r in rows if r['algorithm']==alg)
 if data:plt.semilogx([x for x,y in data],[y for x,y in data],marker='o',label=alg)
plt.xlabel('Vertices (n)');plt.ylabel('edge_scans / m');plt.title('Scans per edge');plt.legend(fontsize=8);plt.tight_layout();plt.savefig(out/'scans_per_edge.png',dpi=160);plt.close()
plt.figure()
for density in ('sparse','dense'):
 data=sorted((int(r['n']),int(r['edge_scans'])/max(1,int(r['m']))) for r in rows if r['algorithm']=='Kruskal' and ((int(r['m'])/int(r['n']))<=5 if density=='sparse' else (int(r['m'])/int(r['n']))>5))
 if data:plt.semilogx([x for x,y in data],[y for x,y in data],marker='o',label=density)
plt.xlabel('Vertices (n)');plt.ylabel('edge_scans / m');plt.title('Share of edges examined by Kruskal');plt.legend();plt.tight_layout();plt.savefig(out/'kruskal_edge_share.png',dpi=160);plt.close()
print('Plots written to',out)
