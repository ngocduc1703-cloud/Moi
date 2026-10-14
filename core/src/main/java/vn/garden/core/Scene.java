package vn.garden.core;

import java.util.*;

/** One responsive scene and input controller for both platforms. */
public final class Scene {
    public static final float WIDTH=480, HEIGHT=820;
    public enum Screen { HOME, GAME, LEVELS, HELP, PAUSE }
    public Screen screen=Screen.HOME;
    public Game game;
    public int unlocked=1, bestScore=0;
    public boolean sound=true;
    public long eventSerial;
    public String event="";
    private final SaveStore store;
    private final int[] stars=new int[Game.MAX_LEVEL];
    private int hintId=-1, page=0, lastRemaining;
    private long hintUntil, messageUntil;
    private String message="";
    private static final int BG=Art.rgb("F5F3E8"), MUTED=Art.rgb("7D9184"), LILAC=Art.rgb("E9E3F1");
    public Scene(SaveStore store) {
        this.store=store;
        try {
            unlocked=Math.max(1,Math.min(Game.MAX_LEVEL,Integer.parseInt(store.read("unlocked"))));
            bestScore=Math.max(0,Integer.parseInt(store.read("best")));
        } catch(RuntimeException ignored) { }
        sound=!"off".equals(store.read("sound"));
        try {
            String[] values=store.read("stars").split(",");
            for(int i=0;i<Math.min(values.length,stars.length);i++)stars[i]=Math.max(0,Math.min(3,Integer.parseInt(values[i])));
        } catch(RuntimeException ignored) { }
        try { String saved=store.read("game"); if(saved!=null&&!saved.isEmpty())game=Game.decode(saved); }
        catch(RuntimeException ignored) { store.write("game", ""); }
    }
    public void startLevel(int level) { game=new Game(level,0x6A09E667L+level*7919L); screen=Screen.GAME; hintId=-1; message=""; save(); }
    public void save() {
        store.write("unlocked",String.valueOf(unlocked));store.write("best",String.valueOf(bestScore));store.write("sound",sound?"on":"off");
        StringBuilder b=new StringBuilder();for(int n:stars)b.append(n).append(',');store.write("stars",b.toString());
        store.write("game",game==null?"":game.encode());
    }
    private void notify(String text) { message=text;messageUntil=System.currentTimeMillis()+2600; }
    private void emit(String e) {event=e;eventSerial++;}
    private boolean inside(float x,float y,float left,float top,float w,float h) {return x>=left&&x<=left+w&&y>=top&&y<=top+h;}
    public void back() {
        if(screen==Screen.GAME&&game.status==Game.Status.PLAYING)screen=Screen.PAUSE;
        else if(screen==Screen.PAUSE)screen=Screen.GAME;
        else screen=Screen.HOME;
        save();
    }
    public void click(float x,float y) {
        if(screen==Screen.HOME) {
            if(inside(x,y,60,495,360,62)){if(game!=null&&game.status==Game.Status.PLAYING)screen=Screen.GAME;else startLevel(unlocked);emit("tap");}
            else if(inside(x,y,60,573,360,56)){screen=Screen.LEVELS;page=(unlocked-1)/15;}
            else if(inside(x,y,60,645,170,48))screen=Screen.HELP;
            else if(inside(x,y,250,645,170,48)){sound=!sound;save();}
            return;
        }
        if(screen==Screen.HELP){if(inside(x,y,60,703,360,54))screen=Screen.HOME;return;}
        if(screen==Screen.LEVELS) {
            if(inside(x,y,28,35,48,44)){screen=Screen.HOME;return;}
            for(int i=0;i<15;i++){
                int level=page*15+i+1;
                if(inside(x,y,48+(i%3)*132,170+(i/3)*100,120,84)){
                    if(level<=unlocked)startLevel(level);else notify("Hoàn thành màn trước để mở khóa");return;
                }
            }
            if(inside(x,y,70,713,140,48))page=0;
            if(inside(x,y,270,713,140,48))page=1;
            return;
        }
        if(screen==Screen.PAUSE) {
            if(inside(x,y,80,365,320,54))screen=Screen.GAME;
            else if(inside(x,y,80,435,320,54))startLevel(game.level);
            else if(inside(x,y,80,505,320,54)){screen=Screen.HOME;save();}
            return;
        }
        if(game.status==Game.Status.WON) {
            if(inside(x,y,80,474,320,56)){startLevel(game.level<Game.MAX_LEVEL?game.level+1:1);emit("tap");}
            else if(inside(x,y,80,544,320,48))screen=Screen.LEVELS;
            return;
        }
        if(game.status==Game.Status.LOST) {
            if(inside(x,y,80,453,320,56)){if(game.undo()){notify("Đã hoàn tác. Hãy thử cách ghép khác!");emit("tap");save();}else startLevel(game.level);}
            else if(inside(x,y,80,523,320,48))startLevel(game.level);
            else if(inside(x,y,80,585,320,40))screen=Screen.HOME;
            return;
        }
        if(inside(x,y,28,35,48,44)){screen=Screen.PAUSE;save();return;}
        if(inside(x,y,36,697,128,66)) {
            hintId=game.hint();hintUntil=System.currentTimeMillis()+4500;
            notify(hintId>=0?"Chọn ô có viền vàng":"Bạn đã dùng hết gợi ý");save();return;
        }
        if(inside(x,y,176,697,128,66)){
            if(game.undo()){hintId=-1;notify("Đã hoàn tác một lượt");emit("tap");save();}else notify("Không thể hoàn tác lúc này");return;
        }
        if(inside(x,y,316,697,128,66)){
            if(game.shuffle()){hintId=-1;notify("Đã xáo trộn. Ô trong khay được giữ lại");emit("tap");save();}else notify("Bạn đã dùng hết xáo trộn");return;
        }
        for(int i=game.tiles.size()-1;i>=0;i--) {
            Tile t=game.tiles.get(i);
            if(!t.removed&&inside(x,y,tileX(t),tileY(t),54,54)) {
                if(!game.available(t.id)){notify("Ô này còn bị che. Chọn ô sáng phía trên");return;}
                lastRemaining=game.remaining();int matches=game.matches;
                game.pick(t.id);hintId=-1;emit(game.matches>matches?"match":"tap");
                if(game.matches>matches)notify("Ghép ba! +"+(100+game.level*10)+" điểm");
                if(game.status==Game.Status.WON) {
                    unlocked=Math.max(unlocked,Math.min(Game.MAX_LEVEL,game.level+1));bestScore=Math.max(bestScore,game.score);
                    int used=(3-game.hints)+(3-game.undos)+(2-game.shuffles);
                    stars[game.level-1]=Math.max(stars[game.level-1],used==0?3:used<4?2:1);emit("win");
                } else if(game.status==Game.Status.LOST)emit("lose");
                save();return;
            }
        }
    }
    public static float tileX(Tile t){return 42+t.x*64;}
    public static float tileY(Tile t){return 280+t.y*61;}
    public void draw(Painter p,long now) {
        p.rect(0,0,WIDTH,HEIGHT,0,BG);
        p.ellipse(-130,-110,420,350,Art.rgb("E4EDDE"));p.ellipse(365,635,280,300,Art.rgb("E6EBDE"));
        if(screen==Screen.HOME)home(p);
        else if(screen==Screen.HELP)help(p);
        else if(screen==Screen.LEVELS)levels(p);
        else {board(p,now);if(screen==Screen.PAUSE)pause(p);else if(game.status==Game.Status.WON)win(p);else if(game.status==Game.Status.LOST)lose(p);}
        if(now<messageUntil) {
            p.rect(30,771,420,32,12,Art.rgb("24483D"));p.text(message,240,792,12,Art.WHITE,false,true);
        }
    }
    private void label(Painter p,String s,float x,float y,float size,int c,boolean bold) {p.text(s,x,y,size,c,bold,true);}
    private void button(Painter p,String s,float x,float y,float w,float h,boolean primary) {
        p.rect(x,y+4,w,h,17,primary?Art.rgb("255F45"):Art.rgb("DCDCCE"));
        p.rect(x,y,w,h,17,primary?Art.GREEN:Art.WHITE);label(p,s,x+w/2,y+h/2+6,17,primary?Art.WHITE:Art.INK,true);
    }
    private void home(Painter p) {
        label(p,"MỘT CHÚT THƯ GIÃN, MỖI NGÀY",240,65,11,MUTED,true);
        label(p,"Vườn Ghép Ba",240,116,37,Art.INK,true);
        label(p,"Chạm nhẹ. Ghép ba. Nở hoa.",240,146,15,MUTED,false);
        p.ellipse(85,235,310,190,Art.rgb("DFE8D7"));
        p.ellipse(104,388,275,24,Art.rgb("CCD8BE"));
        int[][] stack={{0,0,0},{1,0,1},{2,0,2},{0,1,7},{1,1,4},{2,1,6}};
        for(int[] t:stack)Art.tile(p,t[2],122+t[0]*72,250+t[1]*72,64,true,false);
        Art.tile(p,0,161,224,64,true,false);Art.tile(p,4,230,215,64,true,false);Art.tile(p,7,197,280,64,true,false);
        Art.star(p,99,239,12,Art.rgb("E9B954"));Art.star(p,379,308,10,Art.rgb("BCA4D4"));
        label(p,"30 màn chơi · Offline · Không quảng cáo",240,456,13,MUTED,false);
        button(p,game!=null&&game.status==Game.Status.PLAYING?"Tiếp tục · Màn "+game.level:"Chơi ngay · Màn "+unlocked,60,495,360,62,true);
        button(p,"Khu vườn của bạn",60,573,360,56,false);
        button(p,"Cách chơi",60,645,170,48,false);button(p,sound?"Âm thanh: Bật":"Âm thanh: Tắt",250,645,170,48,false);
        label(p,"KỶ LỤC CỦA BẠN",240,744,10,MUTED,true);label(p,String.format(Locale.US,"%,d điểm",bestScore),240,771,21,Art.INK,true);
    }
    private void board(Painter p,long now) {
        p.rect(28,35,48,44,14,Art.WHITE);p.line(42,48,61,48,2.5f,Art.INK);p.line(42,56,61,56,2.5f,Art.INK);p.line(42,64,61,64,2.5f,Art.INK);
        label(p,"Vườn Ghép Ba",240,58,23,Art.INK,true);label(p,"MÀN "+String.format(Locale.US,"%02d",game.level)+" / 30",240,83,10,MUTED,true);
        p.rect(357,36,95,42,14,Art.WHITE);Art.star(p,375,56,8,Art.rgb("E9B954"));label(p,String.valueOf(game.score),413,62,16,Art.INK,true);
        p.rect(36,110,408,8,4,Art.rgb("DFE6D8"));p.rect(36,110,408*(game.tiles.size()-game.remaining())/game.tiles.size(),8,4,Art.GREEN);
        p.text("GHÉP 3 Ô CÙNG LOẠI",38,146,11,MUTED,true,false);p.text(game.remaining()+" ô còn lại",440,146,11,MUTED,false,true);
        p.rect(27,164,426,85,20,Art.rgb("D5DFCA"));p.rect(31,167,418,74,17,Art.rgb("C5D1B9"));
        for(int i=0;i<7;i++) {
            float x=38+i*58;
            p.rect(x,179,54,53,9,Art.rgb("B4C3A9"));
            if(i<game.tray.size())Art.tile(p,game.tiles.get(game.tray.get(i)).kind,x,174,54,true,false);
        }
        p.ellipse(45,628,392,26,Art.rgb("DFE4D5"));
        for(Tile t:game.tiles)if(!t.removed)Art.tile(p,t.kind,tileX(t),tileY(t),54,game.available(t.id),t.id==hintId&&now<hintUntil);
        if(game.level==1&&game.moves==0)label(p,"Chọn các ô sáng để đưa vào khay phía trên",240,670,12,MUTED,false);
        else label(p,game.tray.size()>=5?"Khay sắp đầy! Ưu tiên ghép các ô đã có":"Ba ô giống nhau sẽ tự biến mất",240,670,12,game.tray.size()>=5?Art.rgb("B06454"):MUTED,false);
        booster(p,"Gợi ý",game.hints,36,0);booster(p,"Hoàn tác",game.undos,176,1);booster(p,"Xáo trộn",game.shuffles,316,2);
        label(p,"Không vội vàng. Mỗi nước đi là một lựa chọn.",240,797,11,MUTED,false);
    }
    private void booster(Painter p,String s,int n,float x,int icon) {
        p.rect(x,701,128,64,16,Art.rgb("DCDCCE"));p.rect(x,697,128,64,16,Art.WHITE);
        if(icon==0){p.ellipse(x+15,711,18,18,Art.rgb("F0C866"));p.rect(x+20,729,8,4,2,Art.INK);}
        else if(icon==1){p.line(x+16,719,x+35,719,2.5f,Art.GREEN);p.line(x+16,719,x+23,712,2.5f,Art.GREEN);p.line(x+16,719,x+23,726,2.5f,Art.GREEN);p.line(x+35,719,x+35,731,2.5f,Art.GREEN);}
        else{p.line(x+16,714,x+34,730,2.5f,Art.GREEN);p.line(x+16,730,x+34,714,2.5f,Art.GREEN);p.line(x+26,714,x+34,714,2.5f,Art.GREEN);p.line(x+34,714,x+34,722,2.5f,Art.GREEN);}
        p.rect(x+89,707,25,25,9,LILAC);label(p,String.valueOf(n),x+101,725,12,Art.INK,true);label(p,s,x+64,749,13,Art.INK,true);
    }
    private void modal(Painter p,String title,String subtitle) {
        p.rect(0,0,480,820,0,0xA02B4435);p.rect(52,221,376,435,26,BG);
        Art.icon(p,0,240,270,52);label(p,title,240,325,27,Art.INK,true);label(p,subtitle,240,350,13,MUTED,false);
    }
    private void pause(Painter p) {modal(p,"Nghỉ một chút nhé", "Tiến độ của bạn đã được lưu");button(p,"Tiếp tục chơi",80,365,320,54,true);button(p,"Chơi lại màn này",80,435,320,54,false);button(p,"Về khu vườn",80,505,320,54,false);}
    private void win(Painter p) {
        modal(p,game.level==30?"Khu vườn trọn vẹn!":"Khu vườn nở hoa!","Bạn đã hoàn thành màn "+game.level);
        for(int i=0;i<3;i++)Art.star(p,188+i*52,390,i==1?24:20,Art.rgb(i<stars[game.level-1]?"E9B954":"D8DCCF"));
        label(p,game.score+" điểm · "+game.matches+" bộ ba",240,443,17,Art.INK,true);
        button(p,game.level==30?"Chơi lại từ đầu":"Đến màn "+(game.level+1),80,474,320,56,true);button(p,"Chọn màn chơi",80,544,320,48,false);
    }
    private void lose(Painter p) {
        modal(p,"Khay đã đầy rồi", "Một nước đi khác, một cơ hội mới");
        label(p,"Ghép ba ô trước khi khay chứa đủ 7 ô.",240,400,14,Art.INK,false);
        button(p,game.canUndo()?"Hoàn tác & tiếp tục":"Thử lại màn này",80,453,320,56,true);button(p,"Bắt đầu lại",80,523,320,48,false);label(p,"Về khu vườn",240,610,14,MUTED,true);
    }
    private void levels(Painter p) {
        p.rect(28,35,48,44,14,Art.WHITE);label(p,"‹",52,67,29,Art.INK,true);
        label(p,"Khu vườn của bạn",240,105,29,Art.INK,true);label(p,"Mỗi màn chơi, một bông hoa mới",240,135,14,MUTED,false);
        for(int i=0;i<15;i++) {
            int level=page*15+i+1;float x=48+(i%3)*132,y=170+(i/3)*100;
            p.rect(x,y+4,120,84,17,Art.rgb("DCDCCE"));p.rect(x,y,120,84,17,level<=unlocked?Art.WHITE:Art.rgb("E4E6DC"));
            label(p,level<=unlocked?String.valueOf(level):"· "+level+" ·",x+60,y+37,24,level<=unlocked?Art.INK:MUTED,true);
            for(int j=0;j<3;j++)Art.star(p,x+42+j*18,y+62,7,Art.rgb(j<stars[level-1]?"E9B954":"DADDD1"));
        }
        button(p,"01 — 15",70,713,140,48,page==0);button(p,"16 — 30",270,713,140,48,page==1);
    }
    private void help(Painter p) {
        label(p,"Một chút hướng dẫn",240,105,29,Art.INK,true);label(p,"Dễ bắt đầu. Vui khi chinh phục.",240,138,14,MUTED,false);
        for(int i=0;i<3;i++)Art.tile(p,0,130+i*76,190,62,true,false);
        String[] title={"01   Chọn ô không bị che","02   Ghép ba ô giống nhau","03   Đừng để khay đầy","04   Tận dụng trợ giúp"};
        String[][] desc={{"Chạm hoặc nhấp vào ô sáng trên bàn.","Ô được chọn sẽ chuyển xuống khay."},{"Ba ô cùng loại trong khay tự biến mất.","Dọn sạch cả bàn để qua màn tiếp theo."},{"Khay có 7 chỗ. Đủ 7 ô là thua.","Ưu tiên ghép những loại đang có trong khay."},{"Mỗi màn: 3 gợi ý, 3 hoàn tác, 2 xáo trộn.","Tiến độ tự lưu. Không cần kết nối mạng."}};
        for(int i=0;i<4;i++){float y=305+i*91;p.text(title[i],58,y,17,Art.INK,true,false);p.text(desc[i][0],58,y+28,13,MUTED,false,false);p.text(desc[i][1],58,y+48,13,MUTED,false,false);}
        button(p,"Đã hiểu, vào vườn thôi!",60,703,360,54,true);
    }
}
