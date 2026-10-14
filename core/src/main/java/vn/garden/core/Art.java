package vn.garden.core;

/** Original vector artwork, shared by Android Canvas and desktop Graphics2D. */
public final class Art {
    private Art() { }
    public static int rgb(String hex) { return (int)(0xff000000L | Long.parseLong(hex, 16)); }
    public static final int INK = rgb("24483D"), GREEN = rgb("37795B"), WHITE = rgb("FFFEF8");
    private static final int LEAF = rgb("529466"), LIGHT_LEAF = rgb("8DC78A");
    public static void icon(Painter p, int kind, float x, float y, float size) {
        float s = size / 48f;
        if (kind == 0 || kind == 4 || kind == 8) {
            int petal = rgb(kind == 0 ? "F4BB4F" : kind == 4 ? "B7A0D9" : "EE8C9F");
            p.line(x, y + 5*s, x - 2*s, y + 19*s, 3*s, LEAF);
            p.ellipse(x-15*s,y+9*s,13*s,7*s,LEAF); p.ellipse(x,y+8*s,14*s,7*s,LIGHT_LEAF);
            for (int i=0;i<6;i++) {
                double a=i*Math.PI/3;
                p.ellipse(x+(float)Math.cos(a)*10*s-7*s,y+(float)Math.sin(a)*10*s-10*s,14*s,15*s,petal);
            }
            p.ellipse(x-7*s,y-10*s,14*s,14*s,rgb("865E39"));
            p.ellipse(x-4*s,y-8*s,7*s,7*s,rgb("FFF0AF"));
        } else if (kind == 1) {
            p.line(x-10*s,y+2*s,x+2*s,y-18*s,2*s,LEAF); p.line(x+10*s,y+3*s,x+2*s,y-18*s,2*s,LEAF);
            p.ellipse(x+1*s,y-19*s,13*s,7*s,LIGHT_LEAF);
            p.ellipse(x-20*s,y-1*s,21*s,24*s,rgb("D95763"));p.ellipse(x+1*s,y-1*s,21*s,24*s,rgb("CB4456"));
            p.ellipse(x-16*s,y+2*s,6*s,7*s,rgb("FFB9B1"));p.ellipse(x+5*s,y+2*s,6*s,7*s,rgb("FFB9B1"));
        } else if (kind == 2) {
            p.ellipse(x-18*s,y-17*s,36*s,35*s,rgb("F1AD43"));
            p.ellipse(x-13*s,y-13*s,10*s,9*s,rgb("FFD38B"));
            p.line(x,y-19*s,x+3*s,y-23*s,3*s,LEAF);p.ellipse(x+2*s,y-24*s,15*s,8*s,LEAF);
            for(int i=0;i<4;i++)p.ellipse(x+(i%2)*7*s-3*s,y+(i/2)*7*s,2*s,2*s,rgb("D98935"));
        } else if (kind == 3) {
            p.ellipse(x-18*s,y-3*s,36*s,19*s,LEAF);
            p.polygon(new float[]{x-17*s,y-3*s,x+17*s,y-3*s,x,y-24*s},rgb("F47D80"));
            p.line(x-17*s,y-2*s,x+17*s,y-2*s,3*s,rgb("B9D38C"));
            for(int i=0;i<3;i++)p.ellipse(x+(i-1)*9*s-1*s,y-9*s,2*s,4*s,rgb("61433A"));
        } else if (kind == 5) {
            p.line(x,y-7*s,x+7*s,y-22*s,3*s,LEAF);p.ellipse(x+4*s,y-22*s,15*s,7*s,LIGHT_LEAF);
            for(int row=0;row<3;row++)for(int col=0;col<3-row;col++)p.ellipse(x+(col-(2-row)/2f)*12*s-8*s,y+(row*11-9)*s,16*s,17*s,rgb((row+col)%2==0?"8C79B8":"A38ACD"));
            p.ellipse(x-10*s,y-6*s,5*s,5*s,rgb("DACEEE"));
        } else if (kind == 6) {
            p.ellipse(x-19*s,y-9*s,38*s,25*s,rgb("F7D86D"));p.ellipse(x-23*s,y-3*s,7*s,12*s,rgb("ECC455"));p.ellipse(x+16*s,y-3*s,7*s,12*s,rgb("ECC455"));
            p.ellipse(x-12*s,y-6*s,14*s,6*s,rgb("FFF2B6"));p.ellipse(x+1*s,y-18*s,17*s,8*s,LEAF);
        } else if (kind == 7) {
            p.ellipse(x-19*s,y-14*s,22*s,34*s,rgb("EE6578"));p.ellipse(x-3*s,y-14*s,22*s,34*s,rgb("ED7B85"));
            p.polygon(new float[]{x-18*s,y-13*s,x,y-21*s,x+18*s,y-13*s,x+9*s,y-7*s,x,y-13*s,x-9*s,y-7*s},LEAF);
            for(int row=0;row<3;row++)for(int col=0;col<2;col++)p.ellipse(x+(col*14-8)*s,y+(row*8-3)*s,3*s,4*s,rgb("FFE6B2"));
        } else {
            p.ellipse(x-17*s,y-12*s,34*s,32*s,rgb("A5C873"));p.ellipse(x-11*s,y-20*s,23*s,28*s,rgb("B5D081"));
            p.line(x,y-20*s,x+3*s,y-25*s,3*s,rgb("846442"));p.ellipse(x+3*s,y-25*s,14*s,7*s,LEAF);
            p.ellipse(x-9*s,y-4*s,9*s,12*s,rgb("D4E6A2"));
        }
    }
    public static void star(Painter p,float x,float y,float r,int color) {
        float[] v=new float[20];
        for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=(i%2==0?r:r*.45f);v[2*i]=x+(float)Math.cos(a)*rr;v[2*i+1]=y+(float)Math.sin(a)*rr;}
        p.polygon(v,color);
    }
    public static void tile(Painter p,int kind,float x,float y,float size,boolean active,boolean highlighted) {
        if(highlighted)p.rect(x-4,y-4,size+8,size+13,15,rgb("F0BC55"));
        p.rect(x+1,y+6,size,size,11,rgb("C6AD85"));
        p.rect(x,y+3,size,size,11,rgb("E2CBA7"));
        p.rect(x,y,size,size,11,active?WHITE:rgb("DFE5D8"));
        p.rect(x+4,y+3,size-8,3,2,active?rgb("FFFFFF"):rgb("E9EDDF"));
        icon(p,kind,x+size/2,y+size/2,size*.73f);
        if(!active)p.rect(x,y,size,size,11,0x50607663);
    }
}
