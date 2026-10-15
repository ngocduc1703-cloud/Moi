package vn.riveriq;

public final class Puzzle {
    public interface StateRule { boolean isValid(int rightMask); }
    public final String title,subtitle,ruleText;
    public final Person[] people;
    public final int capacity,parMoves;
    private final StateRule rule;

    public Puzzle(String title,String subtitle,String ruleText,int capacity,int parMoves,Person[] people,StateRule rule) {
        this.title=title;this.subtitle=subtitle;this.ruleText=ruleText;this.capacity=capacity;this.parMoves=parMoves;this.people=people;this.rule=rule;
    }
    public int allMask(){return (1<<people.length)-1;}
    public boolean isValid(int rightMask){return rule.isValid(rightMask);}
    public boolean canOperate(int passengers){
        for(int i=0;i<people.length;i++)if((passengers&(1<<i))!=0&&people[i].canRow)return true;
        return false;
    }
}
