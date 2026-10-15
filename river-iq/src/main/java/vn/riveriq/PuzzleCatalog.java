package vn.riveriq;

import java.util.*;

public final class PuzzleCatalog {
    private PuzzleCatalog(){}
    private static Person p(String n,String s,int c,boolean row){return new Person(n,s,c,row);}
    private static boolean together(int mask,int a,int b,boolean right){return ((mask>>a)&1)==(right?1:0)&&((mask>>b)&1)==(right?1:0);}
    private static boolean sideHas(int mask,int i,boolean right){return ((mask>>i)&1)==(right?1:0);}

    public static List<Puzzle> all(){
        List<Puzzle> levels=new ArrayList<Puzzle>();
        levels.add(new Puzzle("Sói, Dê & Bắp cải","Khởi động kinh điển","Thuyền chở tối đa 2. Bác nông dân phải chèo. Không để Sói với Dê hoặc Dê với Bắp cải khi thiếu bác.",2,7,new Person[]{
            p("Bác nông dân","BN",0xE6A15A,true),p("Sói","S",0x667080,false),p("Dê","D",0xF2EEE2,false),p("Bắp cải","BC",0x64B96A,false)
        },new Puzzle.StateRule(){public boolean isValid(int m){
            for(boolean r:new boolean[]{false,true})if(!sideHas(m,0,r)&&(together(m,1,2,r)||together(m,2,3,r)))return false;return true;
        }}));
        levels.add(new Puzzle("Mèo, Chim & Cá","Người giữ thú cưng","Cô chủ phải chèo. Khi cô vắng mặt, Mèo không được ở cùng Chim hoặc Cá.",2,7,new Person[]{
            p("Cô chủ","CC",0xE782A9,true),p("Mèo","M",0xF0A35E,false),p("Chim","CH",0x58A6D8,false),p("Cá","C",0x4FC5B5,false)
        },new Puzzle.StateRule(){public boolean isValid(int m){
            for(boolean r:new boolean[]{false,true})if(!sideHas(m,0,r)&&(together(m,1,2,r)||together(m,1,3,r)))return false;return true;
        }}));
        levels.add(missionaries("Ba nhà thám hiểm",3,2,11));
        levels.add(new Puzzle("Ba cặp đôi","Đừng làm ai ghen!","Không người vợ nào được ở cùng người chồng khác nếu chồng mình không có mặt. Ai cũng biết chèo.",2,11,new Person[]{
            p("Anh An","A",0x3978C5,true),p("Chị An","a",0x76A9E0,true),p("Anh Bình","B",0xD05C59,true),p("Chị Bình","b",0xEA9693,true),p("Anh Cường","C",0x6B59B5,true),p("Chị Cường","c",0xA495DF,true)
        },new Puzzle.StateRule(){public boolean isValid(int m){
            for(boolean r:new boolean[]{false,true})for(int wife=0;wife<3;wife++)if(sideHas(m,wife*2+1,r)&&!sideHas(m,wife*2,r))for(int man=0;man<3;man++)if(sideHas(m,man*2,r))return false;return true;
        }}));
        levels.add(new Puzzle("Gia đình & Cảnh sát","Thử thách chuyên gia","Chỉ bố, mẹ và cảnh sát chèo. Kẻ trộm cần cảnh sát giám sát; bố không ở riêng với con gái, mẹ không ở riêng với con trai.",2,17,new Person[]{
            p("Bố","B",0x3479C5,true),p("Mẹ","M",0xD76791,true),p("Con trai 1","T1",0x60A4E5,false),p("Con trai 2","T2",0x79B6EB,false),p("Con gái 1","G1",0xE798B5,false),p("Con gái 2","G2",0xF0A8C2,false),p("Cảnh sát","CS",0x455263,true),p("Kẻ trộm","KT",0x8B6F47,false)
        },new Puzzle.StateRule(){public boolean isValid(int m){
            for(boolean r:new boolean[]{false,true}){
                boolean police=sideHas(m,6,r), thief=sideHas(m,7,r);
                if(thief&&!police)for(int i=0;i<6;i++)if(sideHas(m,i,r))return false;
                if(sideHas(m,0,r)&&!sideHas(m,1,r)&&(sideHas(m,4,r)||sideHas(m,5,r)))return false;
                if(sideHas(m,1,r)&&!sideHas(m,0,r)&&(sideHas(m,2,r)||sideHas(m,3,r)))return false;
            }return true;
        }}));
        levels.add(missionaries("Bốn học giả & Robot",4,3,9));
        return Collections.unmodifiableList(levels);
    }

    private static Puzzle missionaries(String title,final int count,int capacity,int par){
        Person[] people=new Person[count*2];
        for(int i=0;i<count;i++)people[i]=p("Học giả "+(i+1),"H"+(i+1),0x3E88C5,true);
        for(int i=0;i<count;i++)people[count+i]=p("Robot "+(i+1),"R"+(i+1),0xE46C5C,true);
        return new Puzzle(title,count==3?"Giữ cân bằng hai bờ":"Thuyền lớn, bài toán lớn","Ở mỗi bờ, nếu có học giả thì số Robot không được nhiều hơn số học giả.",capacity,par,people,new Puzzle.StateRule(){public boolean isValid(int m){
            for(boolean right:new boolean[]{false,true}){int h=0,r=0;for(int i=0;i<count;i++){if(sideHas(m,i,right))h++;if(sideHas(m,count+i,right))r++;}if(h>0&&r>h)return false;}return true;
        }});
    }
}
