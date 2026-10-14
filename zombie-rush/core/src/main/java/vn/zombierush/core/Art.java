package vn.zombierush.core;

/** Original vector sprites. No artwork or characters from another game are used. */
public final class Art {
    private Art() { }
    public static int rgb(String value) { return (int)(0xff000000L | Long.parseLong(value, 16)); }
    public static final int INK=rgb("171929"), MINT=rgb("A9F478"), WHITE=rgb("F7F5FF"), MUTED=rgb("B8B6D0"), GOLD=rgb("FFD36D");
    public static void coin(Painter p,float x,float y,float s) {
        p.ellipse(x-s*12,y-s*12,s*24,s*24,rgb("C58C34"));
        p.ellipse(x-s*11,y-s*13,s*22,s*22,GOLD);
        p.ellipse(x-s*7,y-s*10,s*14,s*15,rgb("F0AD41"));
        p.line(x-s*1,y-s*7,x-s*1,y+s*2,s*3,rgb("FFECA2"));
    }
    public static void zombie(Painter p,float x,float foot,float s,double phase,int variant,boolean giant) {
        int skin=rgb(variant%3==0?"A9D96E":variant%3==1?"78D6C1":"A2BFF2");
        int shirt=rgb(variant%3==0?"C283D1":variant%3==1?"F29D80":"87A8ED");
        float stride=(float)Math.sin(phase)*4*s;
        p.ellipse(x-18*s,foot-2*s,36*s,6*s,0x60491135);
        p.line(x-6*s,foot-17*s,x-8*s+stride,foot-3*s,6*s,INK);
        p.line(x+6*s,foot-17*s,x+8*s-stride,foot-3*s,6*s,INK);
        p.rect(x-17*s+stride,foot-7*s,14*s,7*s,3*s,rgb("27233B"));
        p.rect(x+3*s-stride,foot-7*s,15*s,7*s,3*s,rgb("27233B"));
        p.rect(x-13*s,foot-35*s,26*s,24*s,7*s,INK);
        p.rect(x-10*s,foot-33*s,20*s,20*s,5*s,shirt);
        p.line(x+11*s,foot-29*s,x+23*s,foot-25*s+(float)Math.sin(phase)*2*s,6*s,INK);
        p.line(x+12*s,foot-30*s,x+23*s,foot-27*s+(float)Math.sin(phase)*2*s,4*s,skin);
        p.line(x-11*s,foot-28*s,x-17*s,foot-19*s,5*s,skin);
        p.rect(x-18*s,foot-65*s,37*s,35*s,10*s,INK);
        p.rect(x-15*s,foot-62*s,31*s,29*s,8*s,skin);
        p.ellipse(x-21*s,foot-47*s,9*s,12*s,skin);
        p.ellipse(x+11*s,foot-53*s,11*s,16*s,skin);
        p.polygon(new float[]{x-15*s,foot-61*s,x-12*s,foot-67*s,x-7*s,foot-62*s,x-4*s,foot-69*s,x+1*s,foot-62*s,x+5*s,foot-65*s,x+8*s,foot-59*s},rgb("30374A"));
        p.ellipse(x-11*s,foot-55*s,13*s,14*s,WHITE);
        p.ellipse(x+2*s,foot-55*s,12*s,11*s,WHITE);
        p.ellipse(x-3*s,foot-51*s,4*s,6*s,INK);
        p.ellipse(x+9*s,foot-51*s,4*s,5*s,INK);
        p.line(x-5*s,foot-36*s,x+10*s,foot-37*s,2.5f*s,INK);
        p.rect(x+1*s,foot-38*s,4*s,5*s,1*s,WHITE);p.rect(x+7*s,foot-39*s,4*s,5*s,1*s,WHITE);
        p.rect(x-5*s,foot-29*s,5*s,5*s,1*s,rgb("F2DBF6"));
        if(giant){p.line(x-14*s,foot-69*s,x-20*s,foot-77*s,2*s,GOLD);p.line(x+18*s,foot-63*s,x+25*s,foot-72*s,2*s,GOLD);}
    }
    public static void human(Painter p,float x,float foot,double phase) {
        float stride=(float)Math.sin(phase)*4;
        p.ellipse(x-14,foot-2,28,6,0x6035113D);
        p.line(x-4,foot-17,x-7+stride,foot-2,5,rgb("514C6D"));p.line(x+4,foot-17,x+7-stride,foot-2,5,rgb("514C6D"));
        p.rect(x-11,foot-40,22,25,7,rgb("FFD36D"));
        p.line(x+10,foot-33,x+16,foot-25,4,rgb("F2BEA3"));
        p.rect(x-12,foot-62,25,24,8,rgb("F4C6AF"));p.rect(x-13,foot-64,27,9,4,rgb("DA8C6B"));
        p.ellipse(x+2,foot-54,3,4,INK);p.ellipse(x+9,foot-54,3,4,INK);
        p.rect(x+5,foot-44,5,3,1,WHITE);
    }
    public static void power(Painter p,RunGame.Kind kind,float x,float y,float scale) {
        int color=kind==RunGame.Kind.SHIELD?rgb("82D6ED"):kind==RunGame.Kind.MAGNET?rgb("F099BF"):GOLD;
        p.ellipse(x-22*scale,y-22*scale,44*scale,44*scale,0x403D456B);
        p.ellipse(x-18*scale,y-18*scale,36*scale,36*scale,color);
        if(kind==RunGame.Kind.SHIELD) {
            p.polygon(new float[]{x-10*scale,y-10*scale,x+10*scale,y-10*scale,x+8*scale,y+4*scale,x,y+13*scale,x-8*scale,y+4*scale},WHITE);
            p.line(x,y-7*scale,x,y+8*scale,2*scale,rgb("4D99C0"));
        } else if(kind==RunGame.Kind.MAGNET) {
            p.rect(x-11*scale,y-10*scale,7*scale,15*scale,2*scale,INK);p.rect(x+4*scale,y-10*scale,7*scale,15*scale,2*scale,INK);
            p.rect(x-10*scale,y+1*scale,20*scale,10*scale,5*scale,INK);p.rect(x-3*scale,y-2*scale,6*scale,7*scale,2*scale,color);
            p.rect(x-11*scale,y-11*scale,7*scale,5*scale,1*scale,WHITE);p.rect(x+4*scale,y-11*scale,7*scale,5*scale,1*scale,WHITE);
        } else {
            p.polygon(new float[]{x+2*scale,y-14*scale,x-10*scale,y+2*scale,x-1*scale,y+2*scale,x-3*scale,y+14*scale,x+11*scale,y-4*scale,x+2*scale,y-4*scale},INK);
        }
    }
    public static void car(Painter p,float x,float ground,float width) {
        p.ellipse(x-4,ground-3,width+8,8,0x8030123C);
        p.rect(x+4,ground-55,width-11,42,11,rgb("27283D"));
        p.rect(x+8,ground-51,width-19,37,8,rgb("78BBCE"));
        p.rect(x+16,ground-45,width-36,13,4,rgb("243855"));
        p.rect(x+2,ground-30,width,19,5,rgb("68A6BE"));
        p.rect(x+width-10,ground-25,10,7,2,GOLD);
        for(int i=0;i<2;i++){float cx=x+17+i*(width-35);p.ellipse(cx-10,ground-18,20,20,INK);p.ellipse(cx-5,ground-13,10,10,rgb("ADABBC"));}
    }
    public static void barrier(Painter p,float x,float ground,float width) {
        p.rect(x,ground-32,width,32,4,rgb("E98A65"));
        for(int i=0;i<3;i++){float left=x+5+i*16;p.polygon(new float[]{left,ground-29,left+9,ground-29,left-1,ground-3,left-10,ground-3},rgb("FBE2A0"));}
        p.rect(x-2,ground-4,width+4,5,2,INK);
    }
}
