package vn.zombierush.core;

import java.util.*;

/** Shared landscape presentation and controls; platform adapters supply drawing and storage. */
public final class Scene {
    public static final float WIDTH=960, HEIGHT=540, GROUND=430;
    public enum Screen { HOME, PLAY, PAUSE, OVER, SHOP, HELP }
    public Screen screen=Screen.HOME;
    public Profile profile;
    public RunGame run;
    public long eventSerial;
    public String event="";
    private final SaveStore store;
    private int bankedCoins;
    private boolean recorded, pointerHandled;
    private long seenRunEvent, toastUntil, flashUntil;
    private String toast="";
    private double visualTime;
    private static final int PANEL=Art.rgb("29273F"), BACK=Art.rgb("1D1C35");

    public Scene(SaveStore store) {
        this.store=store;
        try { profile=Profile.decode(store.read("profile")); }
        catch(RuntimeException e) { profile=new Profile(); store.write("profile",profile.encode()); }
    }
    public void save() { store.write("profile",profile.encode()); }
    public void start(long seed) {
        if(run!=null&&!recorded) recordRun();
        run=new RunGame(seed,profile);bankedCoins=0;recorded=false;seenRunEvent=0;screen=Screen.PLAY;toast="";
        emit("start");
    }
    public void start() { start(System.nanoTime()); }
    private void emit(String name) {event=name;eventSerial++;}
    private void notify(String text) {toast=text;toastUntil=System.currentTimeMillis()+2500;}
    private void bankRun() {
        if(run==null)return;
        profile.wallet+=Math.max(0,run.coins-bankedCoins);bankedCoins=run.coins;
        profile.bestMeters=Math.max(profile.bestMeters,run.meters());profile.bestScore=Math.max(profile.bestScore,run.score());
        save();
    }
    private void recordRun() {bankRun();if(run!=null&&!recorded){profile.runs++;recorded=true;save();}}
    public void pause() {if(screen==Screen.PLAY){run.jumpUp();screen=Screen.PAUSE;bankRun();}}
    public void back() {
        if(screen==Screen.PLAY)pause();
        else if(screen==Screen.PAUSE)screen=Screen.PLAY;
        else {if(screen==Screen.OVER)recordRun();screen=Screen.HOME;}
        save();
    }
    public void close() {if(run!=null)bankRun();save();}
    public void update(double seconds) {
        visualTime+=Math.min(.1,Math.max(0,seconds));
        if(screen!=Screen.PLAY)return;
        int before=run.horde.size();run.advance(seconds);
        if(run.horde.size()<before)flashUntil=System.currentTimeMillis()+180;
        if(run.eventSerial!=seenRunEvent){seenRunEvent=run.eventSerial;emit(run.event);if(!run.event.equals("coin"))notify(run.message);}
        if(run.status==RunGame.Status.OVER){recordRun();screen=Screen.OVER;emit("over");}
    }
    private boolean inside(float x,float y,float a,float b,float w,float h){return x>=a&&x<=a+w&&y>=b&&y<=b+h;}
    public void jumpDown() {
        if(screen==Screen.HOME||screen==Screen.OVER){start();return;}
        if(screen==Screen.PLAY&&run.jumpDown())emit("jump");
    }
    public void jumpUp(){if(run!=null)run.jumpUp();}
    public void pointerDown(float x,float y) {
        pointerHandled=false;
        if(screen==Screen.PLAY) {
            if(inside(x,y,882,20,48,44)){pause();pointerHandled=true;}
            else if(run.jumpDown())emit("jump");
        }
    }
    public void pointerUp(float x,float y) {
        jumpUp();if(pointerHandled){pointerHandled=false;return;}
        if(screen==Screen.PLAY)return;
        if(screen==Screen.HOME) {
            if(inside(x,y,64,273,305,60))start();
            else if(inside(x,y,64,350,144,48))screen=Screen.SHOP;
            else if(inside(x,y,225,350,144,48))screen=Screen.HELP;
            else if(inside(x,y,858,24,72,42)){profile.sound=!profile.sound;save();}
        } else if(screen==Screen.PAUSE) {
            if(inside(x,y,320,245,320,53)){screen=Screen.PLAY;emit("tap");}
            else if(inside(x,y,320,313,320,48))start();
            else if(inside(x,y,320,378,320,45)){recordRun();screen=Screen.HOME;}
        } else if(screen==Screen.OVER) {
            if(inside(x,y,290,366,380,54))start();
            else if(inside(x,y,290,433,180,44))screen=Screen.SHOP;
            else if(inside(x,y,490,433,180,44))screen=Screen.HOME;
        } else if(screen==Screen.SHOP) {
            if(inside(x,y,30,22,64,42)){screen=Screen.HOME;return;}
            for(int i=0;i<3;i++)if(inside(x,y,90+i*270,360,240,54)){
                if(profile.buy(i)){notify("Nâng cấp thành công!");emit("power");save();}
                else notify(profile.price(i)<0?"Đã đạt mức nâng cấp tối đa":"Chưa đủ xu. Chạy thêm một ván nhé!");
            }
        } else if(screen==Screen.HELP&&inside(x,y,320,437,320,54))screen=Screen.HOME;
    }
    public void draw(Painter p,long now) {
        background(p,run!=null&&screen!=Screen.HOME&&screen!=Screen.SHOP&&screen!=Screen.HELP?run.distance:visualTime*28);
        if(screen==Screen.HOME)home(p);
        else if(screen==Screen.SHOP)shop(p);
        else if(screen==Screen.HELP)help(p);
        else {world(p,now);if(screen==Screen.PAUSE)pausePanel(p);else if(screen==Screen.OVER)overPanel(p);}
        if(now<toastUntil&&screen!=Screen.OVER){float y=screen==Screen.PLAY?133:478;p.rect(230,y,500,36,12,0xFF29273F);text(p,toast,480,y+24,13,Art.WHITE,true,true);}
    }
    private void text(Painter p,String s,float x,float y,float size,int color,boolean bold,boolean center){p.text(s,x,y,size,color,bold,center);}
    private void button(Painter p,String s,float x,float y,float w,float h,boolean primary) {
        p.rect(x,y+4,w,h,14,Art.rgb("0E1525"));p.rect(x,y,w,h,14,primary?Art.MINT:PANEL);text(p,s,x+w/2,y+h/2+6,16,primary?Art.INK:Art.WHITE,true,true);
    }
    private void background(Painter p,double distance) {
        int stage=(int)(distance/5000)%3;
        int[] top={Art.rgb("24203E"),Art.rgb("263C52"),Art.rgb("482841")};
        int[] low={Art.rgb("715278"),Art.rgb("647F87"),Art.rgb("B17477")};
        for(int i=0;i<18;i++){int a=top[stage],b=low[stage];float t=i/17f;int c=0xff000000|((int)(((a>>16)&255)*(1-t)+((b>>16)&255)*t)<<16)|((int)(((a>>8)&255)*(1-t)+((b>>8)&255)*t)<<8)|(int)((a&255)*(1-t)+(b&255)*t);p.rect(0,i*24,960,25,0,c);}
        p.ellipse(765,54,88,88,Art.rgb("E8CEA8"));p.ellipse(786,45,80,80,top[stage]);
        for(int i=0;i<17;i++){float x=(float)(i*73-distance*.12%73)-70;float h=80+(i*37%120);p.rect(x,340-h,66,h,0,Art.rgb("474059"));}
        for(int i=0;i<14;i++) {
            float x=(float)(i*90-distance*.28%90)-88;float h=90+(i*53%130);p.rect(x,390-h,78,h,3,Art.rgb("302F48"));
            p.rect(x+8,381-h,62,7,1,Art.rgb("403A57"));
            for(int row=0;row<(int)h/30-1;row++)for(int col=0;col<3;col++)if((row+col+i)%3!=0)p.rect(x+12+col*20,405-h+row*27,7,11,1,(row+col)%2==0?Art.rgb("A99185"):Art.rgb("6E6A84"));
        }
        p.rect(0,393,960,37,0,Art.rgb("4C4160"));
        for(int i=0;i<5;i++){float x=(float)(i*260-distance*.55%260);p.line(x,414,x,267,5,Art.rgb("26263C"));p.line(x,268,x+38,268,5,Art.rgb("26263C"));p.ellipse(x+27,267,35,55,0x20FFD36D);p.rect(x+25,264,28,7,3,GOLD());}
        p.rect(0,GROUND,960,110,0,Art.rgb("252437"));p.rect(0,GROUND,960,5,0,Art.rgb("BB91B7"));
        for(int i=0;i<10;i++){float x=(float)(i*130-distance%130);p.rect(x,482,65,4,2,Art.rgb("5E566D"));}
    }
    private int GOLD(){return Art.GOLD;}
    private void home(Painter p) {
        p.rect(42,44,356,220,25,0x20221D3E);
        text(p,"CHẠY VÔ TẬN · CHƠI OFFLINE",64,80,12,Art.MINT,true,false);
        text(p,"ĐẠI LỘ",64,145,49,Art.WHITE,true,false);text(p,"ZOMBIE",60,211,70,Art.MINT,true,false);
        text(p,"Một cú nhảy. Cả đàn cùng lao tới.",64,244,15,Art.MUTED,false,false);
        button(p,"BẮT ĐẦU CHẠY  ›",64,273,305,60,true);button(p,"Nâng cấp",64,350,144,48,false);button(p,"Cách chơi",225,350,144,48,false);
        p.rect(64,435,305,69,14,0xE529273F);text(p,"KỶ LỤC",82,456,10,Art.MUTED,true,false);text(p,profile.bestMeters+" m",82,487,26,Art.WHITE,true,false);
        Art.coin(p,256,470,.8f);text(p,String.valueOf(profile.wallet),275,477,20,Art.GOLD,true,false);
        Art.zombie(p,567,419,1.65f,visualTime*9,1,false);Art.zombie(p,825,421,1.7f,visualTime*9+2,2,false);Art.zombie(p,704,423,3.2f,visualTime*8,0,false);
        p.rect(566,462,279,31,11,0xC029273F);text(p,"CÀNG ĐÔNG · CÀNG MẠNH",705,483,12,Art.MINT,true,true);
        button(p,profile.sound?"ÂM: BẬT":"ÂM: TẮT",858,24,72,42,false);
    }
    private void world(Painter p,long now) {
        float shake=now<flashUntil?(float)Math.sin(now*.08)*4:0;
        for(RunGame.Item item:run.items)if(!item.removed) {
            float x=(float)(item.x-run.distance)+shake;
            if(x < -190||x>1080)continue;
            switch(item.kind) {
                case GAP:
                    p.rect(x,GROUND,(float)item.width,110,0,Art.rgb("101326"));
                    p.rect(x-4,GROUND,8,12,0,Art.GOLD);p.rect(x+(float)item.width-4,GROUND,8,12,0,Art.GOLD);
                    p.line(x+10,519,x+(float)item.width-10,519,2,Art.rgb("303149"));break;
                case COIN:Art.coin(p,x,GROUND-(float)item.height,.65f);break;
                case HUMAN:Art.human(p,x,GROUND,run.elapsed*6+x);break;
                case CAR:Art.car(p,x,GROUND,(float)item.width);break;
                case BARRIER:Art.barrier(p,x,GROUND,(float)item.width);break;
                default:Art.power(p,item.kind,x,GROUND-(float)item.height-(float)Math.sin(run.elapsed*4)*4,1);break;
            }
        }
        for(int i=run.horde.size()-1;i>=0;i--) {
            RunGame.Member member=run.horde.get(i);float x=(float)(RunGame.LEADER_X-i*RunGame.SPACING)+shake;
            float foot=GROUND-(float)member.height;
            if(run.shieldSeconds>0)p.ellipse(x-24,foot-76,49,81,0x4032C7F2);
            Art.zombie(p,x,foot,run.giantSeconds>0?1.38f:.82f,member.height>0?1.1:run.elapsed*13+i,member.id,run.giantSeconds>0);
        }
        pill(p,"QUÃNG ĐƯỜNG",run.meters()+" m",26,150);pill(p,"ĐIỂM",String.valueOf(run.score()),190,132);pill(p,"ĐÀN ZOMBIE",run.horde.size()+" / 12",337,143);
        p.rect(497,20,110,55,13,0xD929273F);Art.coin(p,520,47,.65f);text(p,String.valueOf(run.coins),542,54,20,Art.GOLD,true,false);
        p.rect(882,20,48,44,12,PANEL);p.rect(898,32,5,19,1,Art.WHITE);p.rect(909,32,5,19,1,Art.WHITE);
        float px=28;
        for(int i=0;i<3;i++) {
            double value=i==0?run.shieldSeconds:i==1?run.magnetSeconds:run.giantSeconds;
            if(value>0){p.rect(px,89,139,32,10,0xDD29273F);Art.power(p,i==0?RunGame.Kind.SHIELD:i==1?RunGame.Kind.MAGNET:RunGame.Kind.GIANT,px+18,105,.47f);text(p,(int)Math.ceil(value)+" giây",px+44,111,13,Art.WHITE,true,false);px+=151;}
        }
        if(run.elapsed<9)text(p,"Chạm / giữ hoặc SPACE để nhảy. Giữ lâu = nhảy xa.",480,509,13,Art.WHITE,true,true);
        else text(p,"GIỮ ĐỂ NHẢY XA  ·  ĐÀN TỪ 4 ZOMBIE CÓ THỂ LẬT XE",480,509,11,Art.MUTED,true,true);
    }
    private void pill(Painter p,String title,String value,float x,float w){p.rect(x,20,w,55,13,0xD929273F);text(p,title,x+13,39,9,Art.MUTED,true,false);text(p,value,x+13,64,20,Art.WHITE,true,false);}
    private void overlay(Painter p){p.rect(0,0,960,540,0,0xCC131527);}
    private void pausePanel(Painter p) {
        overlay(p);p.rect(264,112,432,360,24,PANEL);text(p,"NGHỈ MỘT CHÚT",480,167,29,Art.WHITE,true,true);text(p,"Xu kiếm được đã được lưu",480,201,14,Art.MUTED,false,true);
        button(p,"Tiếp tục chạy",320,245,320,53,true);button(p,"Chạy lại",320,313,320,48,false);button(p,"Về trang chủ",320,378,320,45,false);
    }
    private void overPanel(Painter p) {
        overlay(p);p.rect(238,52,484,454,24,PANEL);text(p,"MỘT ĐƯỜNG CHẠY ĐÁNG NHỚ",480,100,12,Art.MINT,true,true);
        text(p,run.meters()+" m",480,163,51,Art.WHITE,true,true);text(p,run.score()+" điểm",480,199,19,Art.GOLD,true,true);
        p.rect(275,227,410,108,16,BACK);
        text(p,"Xu kiếm được",306,255,13,Art.MUTED,false,false);text(p,"+"+run.coins,650,255,16,Art.GOLD,true,true);
        text(p,"Đàn đông nhất",306,284,13,Art.MUTED,false,false);text(p,run.peakHorde+" zombie",640,284,16,Art.WHITE,true,true);
        text(p,"Kỷ lục của bạn",306,313,13,Art.MUTED,false,false);text(p,profile.bestMeters+" m",646,313,16,Art.MINT,true,true);
        button(p,"CHẠY LẠI  ›",290,366,380,54,true);button(p,"Nâng cấp",290,433,180,44,false);button(p,"Trang chủ",490,433,180,44,false);
    }
    private void shop(Painter p) {
        overlay(p);button(p,"‹ Về",30,22,64,42,false);text(p,"NÂNG CẤP ĐỘI HÌNH",480,74,29,Art.WHITE,true,true);text(p,"Dùng xu kiếm được trong game · Không mua bằng tiền thật",480,106,13,Art.MUTED,false,true);
        Art.coin(p,811,53,.7f);text(p,String.valueOf(profile.wallet),834,60,19,Art.GOLD,true,false);
        String[] title={"Đàn khởi đầu","Nam châm","Giá trị xu"};
        String[] detail={profile.startingHorde()+" zombie khi bắt đầu",profile.magnetDuration()+" giây mỗi lần nhặt",profile.coinValue()+" xu mỗi đồng nhặt được"};
        for(int i=0;i<3;i++) {
            float x=90+i*270;p.rect(x,147,240,283,20,PANEL);
            if(i==0)Art.zombie(p,x+120,239,1,visualTime*7,0,false);
            else if(i==1)Art.power(p,RunGame.Kind.MAGNET,x+120,205,1.6f);
            else Art.coin(p,x+120,205,1.8f);
            text(p,title[i],x+120,280,20,Art.WHITE,true,true);text(p,detail[i],x+120,310,13,Art.MUTED,false,true);
            text(p,"Cấp "+profile.level(i)+" / "+profile.limit(i),x+120,338,12,Art.MINT,true,true);
            int price=profile.price(i);button(p,price<0?"ĐÃ TỐI ĐA":price+" xu · Nâng cấp",x,360,240,54,price>=0&&profile.wallet>=price);
        }
        text(p,"Nâng cấp được lưu tự động cho lần chơi sau",480,462,12,Art.MUTED,false,true);
    }
    private void help(Painter p) {
        overlay(p);text(p,"CẢ ĐÀN CÙNG NHẢY!",480,75,30,Art.WHITE,true,true);
        String[] title={"01  Nhảy qua rào và hố","02  Rủ thêm thành viên","03  Lật xe khi đủ mạnh","04  Thu thập sức mạnh"};
        String[][] details={{"Chạm/giữ màn hình, chuột hoặc phím SPACE.","Thả sớm để nhảy thấp; giữ lâu để bay xa."},{"Chạy ngang người đi đường để thêm zombie.","Đàn tối đa 12 thành viên, nhảy nối tiếp nhau."},{"Từ 4 zombie trở lên có thể lật xe.","Đàn nhỏ hãy nhảy qua xe để không mất người."},{"Khiên chống xe/rào, nam châm hút xu.","Khổng lồ vượt mọi chướng ngại trong 8 giây."}};
        for(int i=0;i<4;i++){float x=65+(i%2)*458,y=146+(i/2)*130;p.rect(x,y-26,417,113,17,PANEL);text(p,title[i],x+19,y+1,18,Art.MINT,true,false);text(p,details[i][0],x+19,y+34,14,Art.WHITE,false,false);text(p,details[i][1],x+19,y+60,14,Art.MUTED,false,false);}
        button(p,"Đã hiểu, vào đường phố thôi!",320,437,320,54,true);
    }
}
