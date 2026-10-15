package vn.riveriq;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class RiverIQApp {
    public static void main(String[] args) throws Exception {
        if(args.length>=2&&"--screenshot".equals(args[0])){
            GamePanel panel=new GamePanel(new MemoryProgress());panel.setSize(GamePanel.W,GamePanel.H);
            if(args.length>2&&"game".equals(args[2]))panel.startLevel(Math.min(4,PuzzleCatalog.all().size()-1));
            BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);panel.paint(image.getGraphics());ImageIO.write(image,"png",new File(args[1]));panel.stop();return;
        }
        if(args.length>0&&"--smoke-test".equals(args[0])){
            SwingUtilities.invokeAndWait(new Runnable(){public void run(){
                JFrame frame=new JFrame("Qua Song IQ smoke test");GamePanel panel=new GamePanel(new MemoryProgress());frame.setContentPane(panel);frame.setSize(1100,700);frame.setVisible(true);panel.startLevel(0);
                BufferedImage image=new BufferedImage(1100,700,BufferedImage.TYPE_INT_RGB);panel.paint(image.getGraphics());Rectangle target=panel.peopleBounds[0];
                float s=Math.min(panel.getWidth()/(float)GamePanel.W,panel.getHeight()/(float)GamePanel.H);int ox=Math.round((panel.getWidth()-GamePanel.W*s)/2);int oy=Math.round((panel.getHeight()-GamePanel.H*s)/2);
                int x=ox+Math.round((target.x+target.width/2)*s),y=oy+Math.round((target.y+target.height/2)*s);panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_RELEASED,System.currentTimeMillis(),0,x,y,1,false));
                if(panel.game.selected==0)throw new AssertionError("Mouse did not select a passenger");panel.paint(image.getGraphics());panel.stop();frame.dispose();System.out.println("River IQ window, rendering and mouse input: PASS");
            }});return;
        }
        SwingUtilities.invokeLater(new Runnable(){public void run(){
            try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}
            JFrame frame=new JFrame("Qua Sông IQ");frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);GamePanel panel=new GamePanel(new ProgressStore());frame.setContentPane(panel);frame.pack();frame.setMinimumSize(new Dimension(900,580));frame.setLocationRelativeTo(null);frame.setVisible(true);
        }});
    }

    interface Progress {int unlocked();int best(int level);void complete(int level,int moves);}
    static final class MemoryProgress implements Progress {int unlocked=99;public int unlocked(){return unlocked;}public int best(int l){return 0;}public void complete(int l,int m){}}

    static final class GamePanel extends JPanel {
        static final int W=1280,H=760;
        final List<Puzzle> levels=PuzzleCatalog.all();final Progress progress;final javax.swing.Timer timer;
        final Rectangle[] peopleBounds=new Rectangle[16];final Rectangle[] levelBounds=new Rectangle[12];
        final Rectangle homeButton=new Rectangle(30,26,118,46),moveButton=new Rectangle(530,626,220,70),undoButton=new Rectangle(900,670,100,44),resetButton=new Rectangle(1014,670,100,44),hintButton=new Rectangle(1128,670,120,44);
        GameState game;int level=-1;boolean home=true;long noticeUntil;String notice="";
        final Font titleFont=new Font("SansSerif",Font.BOLD,46),headingFont=new Font("SansSerif",Font.BOLD,25),bodyFont=new Font("SansSerif",Font.PLAIN,17),smallFont=new Font("SansSerif",Font.BOLD,14);

        GamePanel(final ProgressStore store){this(new Progress(){public int unlocked(){return store.unlocked();}public int best(int l){return store.best(l);}public void complete(int l,int m){store.complete(l,m);}});}
        GamePanel(Progress progress){
            this.progress=progress;setPreferredSize(new Dimension(W,H));setBackground(new Color(0x081D35));setFocusable(true);
            addMouseListener(new MouseAdapter(){public void mouseReleased(MouseEvent e){Point p=virtualPoint(e.getX(),e.getY());handleClick(p.x,p.y);requestFocusInWindow();}});
            bind(KeyEvent.VK_ESCAPE,"home",new Runnable(){public void run(){home=true;repaint();}});bind(KeyEvent.VK_ENTER,"move",new Runnable(){public void run(){move();}});bind(KeyEvent.VK_Z,"undo",new Runnable(){public void run(){if(game!=null){game.undo();repaint();}}});bind(KeyEvent.VK_R,"reset",new Runnable(){public void run(){if(game!=null){game.reset();repaint();}}});bind(KeyEvent.VK_H,"hint",new Runnable(){public void run(){hint();}});
            timer=new javax.swing.Timer(33,new ActionListener(){public void actionPerformed(ActionEvent e){repaint();}});timer.start();
        }
        void bind(int key,String name,final Runnable action){getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key,0),name);getActionMap().put(name,new AbstractAction(){public void actionPerformed(ActionEvent e){action.run();}});}
        void stop(){timer.stop();}
        void startLevel(int index){level=index;game=new GameState(levels.get(index));home=false;repaint();}
        Point virtualPoint(int x,int y){float s=Math.min(getWidth()/(float)W,getHeight()/(float)H);return new Point(Math.round((x-(getWidth()-W*s)/2)/s),Math.round((y-(getHeight()-H*s)/2)/s));}
        void handleClick(int x,int y){
            if(home){for(int i=0;i<levels.size();i++)if(levelBounds[i]!=null&&levelBounds[i].contains(x,y)&&i<progress.unlocked()){startLevel(i);return;}return;}
            if(homeButton.contains(x,y)){home=true;repaint();return;}
            if(game.won){if(new Rectangle(500,545,280,60).contains(x,y)){if(level+1<levels.size())startLevel(level+1);else home=true;}return;}
            for(int i=0;i<game.puzzle.people.length;i++)if(peopleBounds[i]!=null&&peopleBounds[i].contains(x,y)){game.toggle(i);repaint();return;}
            if(moveButton.contains(x,y))move();else if(undoButton.contains(x,y)){game.undo();repaint();}else if(resetButton.contains(x,y)){game.reset();repaint();}else if(hintButton.contains(x,y))hint();
        }
        void move(){if(game==null||home)return;boolean wasWon=game.won;if(game.move()&&!wasWon&&game.won){progress.complete(level,game.moves);notice="Đã mở khóa màn tiếp theo!";noticeUntil=System.currentTimeMillis()+2600;}repaint();}
        void hint(){if(game==null||game.won)return;List<Integer> path=Solver.solve(game.puzzle,game.rightMask,game.boatRight);if(path.isEmpty()){game.message="Không còn lời giải từ trạng thái này — hãy hoàn tác";}else{game.selected=path.get(0);game.message="Gợi ý đã chọn người cho chuyến thuyền kế tiếp";}repaint();}

        protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);float s=Math.min(getWidth()/(float)W,getHeight()/(float)H);g.translate((getWidth()-W*s)/2,(getHeight()-H*s)/2);g.scale(s,s);if(home)paintHome(g);else paintGame(g);g.dispose();
        }
        void paintHome(Graphics2D g){
            gradient(g,0,0,W,H,new Color(0x071B33),new Color(0x0B4162));drawStars(g);drawMoon(g,1080,86,58);
            g.setColor(new Color(0xEAF9FF));g.setFont(titleFont);center(g,"QUA SÔNG IQ",W/2,82);g.setFont(bodyFont);g.setColor(new Color(0xA9DCE8));center(g,"Mỗi chuyến thuyền là một bài toán. Bạn có đưa tất cả sang bờ an toàn?",W/2,118);
            int cardW=350,cardH=205,gap=24,startX=91,startY=158;
            for(int i=0;i<levels.size();i++){int col=i%3,row=i/3,x=startX+col*(cardW+gap),y=startY+row*(cardH+gap);Rectangle r=new Rectangle(x,y,cardW,cardH);levelBounds[i]=r;boolean open=i<progress.unlocked();
                shadow(g,r,24);g.setColor(new Color(open?0xF8FCFB:0x17354A));g.fillRoundRect(x,y,cardW,cardH,24,24);g.setColor(new Color(open?0xD5ECE8:0x31536A));g.setStroke(new BasicStroke(2));g.drawRoundRect(x,y,cardW,cardH,24,24);
                g.setColor(new Color(open?0x16A085:0x547386));g.fillRoundRect(x+22,y+20,52,34,14,14);g.setFont(smallFont);g.setColor(Color.WHITE);center(g,String.format("%02d",i+1),x+48,y+43);
                g.setFont(headingFont);g.setColor(new Color(open?0x12324A:0x6A8798));drawFit(g,levels.get(i).title,x+22,y+88,cardW-44);
                g.setFont(bodyFont);g.setColor(new Color(open?0x527183:0x49697C));g.drawString(levels.get(i).subtitle,x+22,y+120);
                if(open){int best=progress.best(i);g.setFont(smallFont);g.setColor(new Color(0x15967F));g.drawString(best>0?"★ Kỷ lục: "+best+" chuyến":"Sẵn sàng thử thách",x+22,y+170);drawMiniBoat(g,x+276,y+154,0.85f);}else{drawLock(g,x+291,y+25);g.setFont(smallFont);g.drawString("Hoàn thành màn trước",x+22,y+170);}
            }
            g.setFont(new Font("SansSerif",Font.PLAIN,14));g.setColor(new Color(0x79B6C7));center(g,"Java • Chơi offline • Tiến độ tự động lưu",W/2,728);
        }
        void paintGame(Graphics2D g){
            gradient(g,0,0,W,H,new Color(0xBCE8F2),new Color(0xEAF8F6));drawSun(g,1130,90,46);drawCloud(g,180,95,1.0f);drawCloud(g,880,138,0.72f);
            g.setColor(new Color(0x527386));g.setFont(smallFont);g.drawString("MÀN "+(level+1)+" / "+levels.size(),174,52);g.setColor(new Color(0x183F55));g.setFont(headingFont);g.drawString(game.puzzle.title,174,82);
            button(g,homeButton,"← Chọn màn",false,new Color(0xFFFFFF));
            g.setColor(new Color(255,255,255,218));g.fillRoundRect(840,24,408,94,20,20);g.setColor(new Color(0x446779));g.setFont(new Font("SansSerif",Font.PLAIN,14));drawWrapped(g,game.puzzle.ruleText,860,48,370,21,3);
            // river and banks
            gradient(g,0,170,W,410,new Color(0x5EC4D5),new Color(0x238EBA));drawWater(g);
            g.setColor(new Color(0x68A950));g.fill(new RoundRectangle2D.Float(-45,150,470,470,80,80));g.setColor(new Color(0x84C765));g.fill(new RoundRectangle2D.Float(-60,142,465,80,70,70));
            g.setColor(new Color(0x68A950));g.fill(new RoundRectangle2D.Float(855,150,470,470,80,80));g.setColor(new Color(0x84C765));g.fill(new RoundRectangle2D.Float(875,142,465,80,70,70));
            g.setFont(smallFont);g.setColor(new Color(0x315C40));g.drawString("BỜ KHỞI HÀNH",34,196);g.drawString("BỜ ĐÍCH",1138,196);
            layoutPeople(g,false);layoutPeople(g,true);drawBoat(g);
            g.setColor(new Color(0xF7FCFD));g.fillRoundRect(24,610,1232,126,26,26);g.setColor(new Color(0xD6EAED));g.drawRoundRect(24,610,1232,126,26,26);
            g.setColor(new Color(0x35596B));g.setFont(bodyFont);g.drawString(game.message,48,650);g.setFont(smallFont);g.setColor(new Color(0x77909D));g.drawString("Phím tắt: Enter đi thuyền  •  H gợi ý  •  Z hoàn tác  •  R chơi lại",48,686);
            g.setColor(new Color(0x173F55));g.setFont(headingFont);g.drawString(String.format("%02d",game.moves),792,654);g.setFont(smallFont);g.setColor(new Color(0x76919E));g.drawString("CHUYẾN",788,678);
            button(g,moveButton,game.boatRight?"←  QUA BỜ TRÁI":"QUA BỜ PHẢI  →",true,new Color(0x12A389));button(g,undoButton,"Hoàn tác",false,new Color(0xEAF3F3));button(g,resetButton,"Chơi lại",false,new Color(0xEAF3F3));button(g,hintButton,"Gợi ý",false,new Color(0xFFE7A8));
            if(game.won)paintWin(g);if(System.currentTimeMillis()<noticeUntil){g.setColor(new Color(0x123A4D));g.fillRoundRect(460,132,360,48,18,18);g.setColor(Color.WHITE);g.setFont(smallFont);center(g,notice,640,162);}
        }
        void layoutPeople(Graphics2D g,boolean right){
            Puzzle p=game.puzzle;int count=0;for(int i=0;i<p.people.length;i++)if(((game.rightMask>>i)&1)==(right?1:0))count++;
            int cols=count>6?3:2,index=0;for(int i=0;i<p.people.length;i++)if(((game.rightMask>>i)&1)==(right?1:0)){
                int x=(right?915:30)+(index%cols)*(right?105:118),y=225+(index/cols)*108;Rectangle r=new Rectangle(x,y,92,88);peopleBounds[i]=r;drawPerson(g,p.people[i],r,(game.selected&(1<<i))!=0);index++;
            }
        }
        void drawPerson(Graphics2D g,Person person,Rectangle r,boolean selected){
            if(selected){g.setColor(new Color(255,220,89,100));g.fillOval(r.x-7,r.y-8,r.width+14,r.height+14);g.setColor(new Color(0xFFD34E));g.setStroke(new BasicStroke(3));g.drawOval(r.x-5,r.y-6,r.width+10,r.height+10);}
            Color c=new Color(person.color);g.setColor(new Color(0,0,0,28));g.fillOval(r.x+17,r.y+69,58,12);g.setColor(c);g.fillRoundRect(r.x+20,r.y+35,52,38,18,18);g.setColor(bright(c,1.16f));g.fillOval(r.x+25,r.y+4,42,42);g.setColor(new Color(0x153A4D));g.fillOval(r.x+37,r.y+20,4,5);g.fillOval(r.x+52,r.y+20,4,5);g.setStroke(new BasicStroke(2));g.drawArc(r.x+40,r.y+27,13,8,190,160);
            g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,12));center(g,person.shortName,r.x+46,r.y+60);g.setFont(new Font("SansSerif",Font.BOLD,12));g.setColor(new Color(0x244B5D));center(g,person.name,r.x+46,r.y+86);
        }
        void drawBoat(Graphics2D g){
            int x=game.boatRight?725:425,y=500;double bob=Math.sin(System.currentTimeMillis()/260.0)*3;g.translate(0,bob);g.setColor(new Color(0,61,88,60));g.fillOval(x-10,y+46,150,18);g.setColor(new Color(0x8A4F32));Path2D hull=new Path2D.Float();hull.moveTo(x,y);hull.lineTo(x+130,y);hull.lineTo(x+105,y+45);hull.lineTo(x+25,y+45);hull.closePath();g.fill(hull);g.setColor(new Color(0xD8864E));g.fillRoundRect(x+8,y-8,114,18,9,9);g.setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(new Color(0x6A402B));g.drawLine(x+65,y,x+100,y-58);g.setColor(new Color(0xF6E4B4));Path2D sail=new Path2D.Float();sail.moveTo(x+95,y-55);sail.lineTo(x+95,y-8);sail.lineTo(x+47,y-10);sail.closePath();g.fill(sail);g.translate(0,-bob);
        }
        void paintWin(Graphics2D g){
            g.setColor(new Color(5,27,43,190));g.fillRect(0,0,W,H);shadow(g,new Rectangle(390,160,500,450),30);g.setColor(new Color(0xF8FCFA));g.fillRoundRect(390,160,500,450,30,30);g.setColor(new Color(0xFFD45E));g.fillOval(570,195,140,140);g.setColor(new Color(0xFFFFFF));g.setFont(new Font("SansSerif",Font.BOLD,72));center(g,"★",640,292);g.setColor(new Color(0x173F55));g.setFont(titleFont);center(g,"HOÀN THÀNH!",640,388);g.setFont(bodyFont);g.setColor(new Color(0x587585));center(g,"Bạn đã đưa mọi người qua sông an toàn",640,425);
            String stars=game.moves<=game.puzzle.parMoves?"★★★":game.moves<=game.puzzle.parMoves+3?"★★☆":"★☆☆";g.setFont(new Font("SansSerif",Font.BOLD,34));g.setColor(new Color(0xF4B835));center(g,stars,640,480);g.setFont(smallFont);g.setColor(new Color(0x668391));center(g,game.moves+" chuyến • Mục tiêu "+game.puzzle.parMoves+" chuyến",640,510);button(g,new Rectangle(500,545,280,60),level+1<levels.size()?"MÀN TIẾP THEO  →":"VỀ CHỌN MÀN",true,new Color(0x12A389));
        }
        void button(Graphics2D g,Rectangle r,String text,boolean primary,Color fill){g.setColor(new Color(0,0,0,primary?32:15));g.fillRoundRect(r.x+2,r.y+4,r.width,r.height,16,16);g.setColor(fill);g.fillRoundRect(r.x,r.y,r.width,r.height,16,16);g.setColor(primary?Color.WHITE:new Color(0x35596B));g.setFont(smallFont);center(g,text,r.x+r.width/2,r.y+r.height/2+5);}
        void drawWater(Graphics2D g){g.setStroke(new BasicStroke(2));long t=System.currentTimeMillis()/25;for(int row=0;row<8;row++){g.setColor(new Color(255,255,255,35+row*3));for(int x=-80;x<W;x+=90){int xx=(int)(x+(t*(row%2==0?1:-1))%90);g.drawArc(xx,215+row*45,60,12,8,164);}}}
        void drawStars(Graphics2D g){Random r=new Random(7);for(int i=0;i<70;i++){int x=r.nextInt(W),y=r.nextInt(150);g.setColor(new Color(255,255,255,80+r.nextInt(150)));g.fillOval(x,y,2+r.nextInt(3),2+r.nextInt(3));}}
        void drawMoon(Graphics2D g,int x,int y,int radius){g.setColor(new Color(255,239,181,35));g.fillOval(x-radius-18,y-radius-18,(radius+18)*2,(radius+18)*2);g.setColor(new Color(0xFFF0B8));g.fillOval(x-radius,y-radius,radius*2,radius*2);}
        void drawSun(Graphics2D g,int x,int y,int radius){g.setColor(new Color(255,202,76,45));g.fillOval(x-radius-18,y-radius-18,(radius+18)*2,(radius+18)*2);g.setColor(new Color(0xFFD468));g.fillOval(x-radius,y-radius,radius*2,radius*2);}
        void drawCloud(Graphics2D g,int x,int y,float scale){g.setColor(new Color(255,255,255,175));g.fillOval(x,y,(int)(100*scale),(int)(38*scale));g.fillOval(x+(int)(22*scale),y-(int)(22*scale),(int)(55*scale),(int)(55*scale));g.fillOval(x+(int)(58*scale),y-(int)(10*scale),(int)(62*scale),(int)(48*scale));}
        void drawMiniBoat(Graphics2D g,int x,int y,float s){g.setColor(new Color(0xD9824A));Path2D p=new Path2D.Float();p.moveTo(x,y);p.lineTo(x+68*s,y);p.lineTo(x+56*s,y+20*s);p.lineTo(x+12*s,y+20*s);p.closePath();g.fill(p);g.setColor(new Color(0x7D4B31));g.setStroke(new BasicStroke(3));g.drawLine(x+(int)(34*s),y,x+(int)(48*s),y-(int)(35*s));}
        void drawLock(Graphics2D g,int x,int y){g.setColor(new Color(0x648295));g.setStroke(new BasicStroke(4));g.drawArc(x+5,y,24,26,0,180);g.fillRoundRect(x,y+12,34,28,8,8);}
        void gradient(Graphics2D g,int x,int y,int w,int h,Color top,Color bottom){g.setPaint(new GradientPaint(x,y,top,x,y+h,bottom));g.fillRect(x,y,w,h);}
        void shadow(Graphics2D g,Rectangle r,int radius){g.setColor(new Color(0,0,0,24));g.fillRoundRect(r.x+5,r.y+9,r.width,r.height,radius,radius);}
        static Color bright(Color c,float amount){return new Color(Math.min(255,(int)(c.getRed()*amount)),Math.min(255,(int)(c.getGreen()*amount)),Math.min(255,(int)(c.getBlue()*amount)));}
        void center(Graphics2D g,String text,int x,int baseline){FontMetrics fm=g.getFontMetrics();g.drawString(text,x-fm.stringWidth(text)/2,baseline);}
        void drawFit(Graphics2D g,String text,int x,int y,int width){Font old=g.getFont();while(g.getFontMetrics().stringWidth(text)>width&&g.getFont().getSize()>15)g.setFont(g.getFont().deriveFont((float)g.getFont().getSize()-1));g.drawString(text,x,y);g.setFont(old);}
        void drawWrapped(Graphics2D g,String text,int x,int y,int width,int lineHeight,int maxLines){String[] words=text.split(" ");String line="";int lineNo=0;for(String word:words){String next=line.length()==0?word:line+" "+word;if(g.getFontMetrics().stringWidth(next)>width){g.drawString(line,x,y+lineNo*lineHeight);line=word;if(++lineNo>=maxLines)return;}else line=next;}if(lineNo<maxLines)g.drawString(line,x,y+lineNo*lineHeight);}
    }
}
