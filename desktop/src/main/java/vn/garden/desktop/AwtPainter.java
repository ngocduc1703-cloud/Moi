package vn.garden.desktop;

import java.awt.*;
import java.awt.geom.*;
import vn.garden.core.Painter;

public final class AwtPainter implements Painter {
    private final Graphics2D g;
    public AwtPainter(Graphics2D g) {
        this.g=g;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    private void color(int argb){g.setColor(new Color(argb,true));}
    public void rect(float x,float y,float w,float h,float r,int c){color(c);g.fill(new RoundRectangle2D.Float(x,y,w,h,r*2,r*2));}
    public void ellipse(float x,float y,float w,float h,int c){color(c);g.fill(new Ellipse2D.Float(x,y,w,h));}
    public void line(float x1,float y1,float x2,float y2,float width,int c){color(c);g.setStroke(new BasicStroke(width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.draw(new Line2D.Float(x1,y1,x2,y2));}
    public void polygon(float[] v,int c){color(c);Path2D.Float path=new Path2D.Float();path.moveTo(v[0],v[1]);for(int i=2;i<v.length;i+=2)path.lineTo(v[i],v[i+1]);path.closePath();g.fill(path);}
    public void text(String s,float x,float y,float size,int c,boolean bold,boolean center){color(c);g.setFont(new Font(Font.SANS_SERIF,bold?Font.BOLD:Font.PLAIN,Math.round(size)));if(center)x-=g.getFontMetrics().stringWidth(s)/2f;g.drawString(s,x,y);}
}
