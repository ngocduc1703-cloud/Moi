package vn.garden.desktop;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import vn.garden.core.*;

public final class DesktopLauncher {
    public static void main(String[] args) throws Exception {
        if(args.length>=2&&args[0].equals("--screenshot")) {
            Scene scene=new Scene(new MemoryStore());
            if(args.length>2&&args[2].equals("game"))scene.startLevel(12);
            BufferedImage img=new BufferedImage(960,1640,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=img.createGraphics();g.scale(2,2);scene.draw(new AwtPainter(g),System.currentTimeMillis());g.dispose();
            ImageIO.write(img,"png",new File(args[1]));return;
        }
        if(args.length>=1&&args[0].equals("--smoke-test")) {
            SwingUtilities.invokeAndWait(()->{
                JFrame frame=new JFrame("Garden smoke test");
                GamePanel panel=new GamePanel(new Scene(new MemoryStore()));frame.setContentPane(panel);frame.setSize(500,880);frame.setVisible(true);
                panel.scene.click(240,526);
                Tile t=panel.scene.game.tiles.get(panel.scene.game.availableTiles().get(0));
                float scale=Math.min(panel.getWidth()/Scene.WIDTH,panel.getHeight()/Scene.HEIGHT);
                int x=Math.round((panel.getWidth()-Scene.WIDTH*scale)/2+(Scene.tileX(t)+27)*scale);
                int y=Math.round((panel.getHeight()-Scene.HEIGHT*scale)/2+(Scene.tileY(t)+27)*scale);
                panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_RELEASED,System.currentTimeMillis(),0,x,y,1,false));
                if(panel.scene.game.moves!=1)throw new AssertionError("Mouse input did not select a tile");
                BufferedImage image=new BufferedImage(panel.getWidth(),panel.getHeight(),BufferedImage.TYPE_INT_RGB);panel.paint(image.getGraphics());
                panel.timer.stop();frame.dispose();System.out.println("Desktop window, scene rendering and mouse input: PASS");
            });return;
        }
        SwingUtilities.invokeLater(()->{
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored) { }
            Scene scene=new Scene(new FileStore());
            JFrame frame=new JFrame("Vườn Ghép Ba");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){scene.save();frame.dispose();System.exit(0);}});
            BufferedImage icon=new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);Graphics2D ig=icon.createGraphics();
            AwtPainter painter=new AwtPainter(ig);painter.rect(0,0,128,128,24,Art.rgb("E4EDDE"));Art.tile(painter,0,22,20,84,true,false);ig.dispose();frame.setIconImage(icon);
            frame.setContentPane(new GamePanel(scene));frame.pack();frame.setMinimumSize(new Dimension(380,650));frame.setLocationRelativeTo(null);frame.setVisible(true);
        });
    }
    static final class GamePanel extends JPanel {
        final Scene scene;
        final Timer timer;
        final Sounds sounds=new Sounds();
        long seen;
        GamePanel(Scene scene) {
            this.scene=scene;setPreferredSize(new Dimension(480,820));setBackground(new Color(0xDDE6D4));setFocusable(true);
            addMouseListener(new MouseAdapter(){public void mouseReleased(MouseEvent e){
                float s=Math.min(getWidth()/Scene.WIDTH,getHeight()/Scene.HEIGHT);
                scene.click((e.getX()-(getWidth()-Scene.WIDTH*s)/2)/s,(e.getY()-(getHeight()-Scene.HEIGHT*s)/2)/s);
                if(seen!=scene.eventSerial){seen=scene.eventSerial;if(scene.sound)sounds.play(scene.event);}repaint();requestFocusInWindow();
            }});
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"back");
            getActionMap().put("back",new AbstractAction(){public void actionPerformed(ActionEvent e){scene.back();repaint();}});
            timer=new Timer(33,e->repaint());timer.start();
        }
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
            float s=Math.min(getWidth()/Scene.WIDTH,getHeight()/Scene.HEIGHT);
            g.translate((getWidth()-Scene.WIDTH*s)/2,(getHeight()-Scene.HEIGHT*s)/2);g.scale(s,s);
            scene.draw(new AwtPainter(g),System.currentTimeMillis());g.dispose();
        }
    }
    static final class FileStore implements SaveStore {
        private final Properties data=new Properties();
        private final Path file=Paths.get(System.getProperty("user.home"),".vuon-ghep-ba","save.properties");
        FileStore(){try{if(Files.exists(file))try(Reader r=Files.newBufferedReader(file,StandardCharsets.UTF_8)){data.load(r);}}catch(IOException e){System.err.println("Cannot read save: "+e.getMessage());}}
        public String read(String key){return data.getProperty(key);}
        public synchronized void write(String key,String value){data.setProperty(key,value);try{
            Files.createDirectories(file.getParent());Path temp=file.resolveSibling("save.tmp");
            try(Writer w=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){data.store(w,"Vuon Ghep Ba local progress");}
            try{Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
        }catch(IOException e){System.err.println("Cannot save progress: "+e.getMessage());}}
    }
    static final class MemoryStore implements SaveStore {
        final Properties p=new Properties();public String read(String k){return p.getProperty(k);}public void write(String k,String v){p.setProperty(k,v);}
    }
    static final class Sounds {
        void play(String event){
            Thread thread=new Thread(()->{
                float frequency=event.equals("match")?660:event.equals("win")?880:event.equals("lose")?220:440;
                int samples=event.equals("tap")?1200:6000;byte[] pcm=new byte[samples*2];
                for(int i=0;i<samples;i++){short value=(short)(Math.sin(i*frequency*2*Math.PI/22050)*4500*(1f-i/(float)samples));pcm[i*2]=(byte)value;pcm[i*2+1]=(byte)(value>>8);}
                try{AudioFormat format=new AudioFormat(22050,16,1,true,false);SourceDataLine line=AudioSystem.getSourceDataLine(format);line.open(format);line.start();line.write(pcm,0,pcm.length);line.drain();line.close();}catch(LineUnavailableException|IllegalArgumentException ignored) { }
            },"garden-audio");thread.setDaemon(true);thread.start();
        }
    }
}
