package vn.riveriq;

import java.util.*;

public final class GameState {
    public static final class Snapshot {final int mask;final boolean boatRight;Snapshot(int m,boolean b){mask=m;boatRight=b;}}
    public final Puzzle puzzle;
    private final Deque<Snapshot> history=new ArrayDeque<Snapshot>();
    public int rightMask,selected,moves;
    public boolean boatRight,won;
    public String message="Chọn người lên thuyền";

    public GameState(Puzzle puzzle){this.puzzle=puzzle;}
    public void toggle(int i){if(i<0||i>=puzzle.people.length||won)return;boolean isRight=(rightMask&(1<<i))!=0;if(isRight==boatRight)selected^=1<<i;}
    public boolean move(){
        if(won)return false;
        int count=Integer.bitCount(selected);
        if(count==0){message="Hãy chọn ít nhất một người";return false;}
        if(count>puzzle.capacity){message="Thuyền chỉ chở tối đa "+puzzle.capacity+" người";return false;}
        int available=boatRight?rightMask:(puzzle.allMask()^rightMask);
        if((selected&~available)!=0){message="Có người không đứng cùng phía với thuyền";selected&=available;return false;}
        if(!puzzle.canOperate(selected)){message="Cần ít nhất một người biết chèo thuyền";return false;}
        int next=boatRight?(rightMask&~selected):(rightMask|selected);
        if(!puzzle.isValid(next)){message="Không thể đi: luật an toàn sẽ bị vi phạm";return false;}
        history.push(new Snapshot(rightMask,boatRight));rightMask=next;boatRight=!boatRight;selected=0;moves++;
        won=rightMask==puzzle.allMask();message=won?"Xuất sắc! Mọi người đã qua sông":"Chuyến đi an toàn — tiếp tục nào!";return true;
    }
    public void undo(){if(history.isEmpty()||won&&moves==0)return;Snapshot s=history.pop();rightMask=s.mask;boatRight=s.boatRight;selected=0;moves=Math.max(0,moves-1);won=false;message="Đã quay lại một lượt";}
    public void reset(){history.clear();rightMask=selected=moves=0;boatRight=won=false;message="Bắt đầu lại — suy nghĩ thật kỹ nhé!";}
    public boolean canUndo(){return !history.isEmpty();}
}
