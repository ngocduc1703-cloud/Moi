package vn.riveriq;

import java.util.*;

public final class Solver {
    private Solver(){}
    private static final class Node {final int mask;final boolean boat;final List<Integer> path;Node(int m,boolean b,List<Integer> p){mask=m;boat=b;path=p;}}
    public static List<Integer> solve(Puzzle p,int startMask,boolean boatRight){
        ArrayDeque<Node> q=new ArrayDeque<Node>();Set<Integer> seen=new HashSet<Integer>();q.add(new Node(startMask,boatRight,new ArrayList<Integer>()));
        while(!q.isEmpty()){
            Node n=q.remove();int key=n.mask*2+(n.boat?1:0);if(!seen.add(key))continue;if(n.mask==p.allMask())return n.path;
            int available=n.boat?n.mask:(p.allMask()^n.mask);
            for(int subset=available;subset>0;subset=(subset-1)&available){
                int count=Integer.bitCount(subset);if(count>p.capacity||!p.canOperate(subset))continue;
                int next=n.boat?(n.mask&~subset):(n.mask|subset);if(!p.isValid(next))continue;
                List<Integer> path=new ArrayList<Integer>(n.path);path.add(subset);q.add(new Node(next,!n.boat,path));
            }
        }return Collections.emptyList();
    }
}
