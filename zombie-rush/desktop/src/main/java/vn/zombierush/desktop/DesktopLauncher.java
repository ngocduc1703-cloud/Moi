package vn.zombierush.desktop;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import vn.zombierush.core.*;

public final class DesktopLauncher {
    public static void main(String[] args) throws Exception {
        if(args.length>=2&&args[0].equals("--screenshot")) {
            Scene scene=new Scene(new MemoryStore());
            if(args.length>2&&args[2].equals("game")){scene.start(2026);for(int i=0;i<180;i++)scene.update(1.0/60);}
            BufferedImage image=new BufferedImage(1920,1080,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=image.createGraphics();g.scale(2,2);scene.draw(new AwtPainter(g),System.currentTimeMillis());g.dispose();
            ImageIO.write(image,"png",new File(args[1]));return;
        }
        // Load MouseEvent before a visible X11 window can initialize it concurrently.
        Class.forName("java.awt.event.MouseEvent");
        if(args.length>0&&args[0].equals("--smoke-test")) {
            SwingUtilities.invokeAndWait(()->{
                Scene scene=new Scene(new MemoryStore());GamePanel panel=new GamePanel(scene);panel.timer.stop();
                JFrame frame=new JFrame("Zombie runner smoke test");frame.setContentPane(panel);frame.pack();frame.setVisible(true);
                scene.start(123);
                panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_PRESSED,System.currentTimeMillis(),0,600,300,1,false));
                scene.update(.1);
                if(scene.run.horde.get(0).height<=0)throw new AssertionError("Mouse press did not trigger jump");
                panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_RELEASED,System.currentTimeMillis(),0,600,300,1,false));
                BufferedImage image=new BufferedImage(panel.getWidth(),panel.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();panel.paint(g);g.dispose();
                if(panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed SPACE"))==null)throw new AssertionError("SPACE binding missing");
                for(int i=0;i<100;i++)scene.update(RunGame.STEP);
                panel.getActionMap().get("jump").actionPerformed(new ActionEvent(panel,ActionEvent.ACTION_PERFORMED,"SPACE"));scene.update(.02);
                if(scene.run.horde.get(0).height<=0)throw new AssertionError("SPACE action did not jump");
                panel.getActionMap().get("release").actionPerformed(new ActionEvent(panel,ActionEvent.ACTION_PERFORMED,"SPACE_RELEASE"));
                frame.dispose();System.out.println("Desktop window, mouse jump/release, render and SPACE binding: PASS");
            });return;
        }
        SwingUtilities.invokeLater(()->{
            try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored) { }
            Scene scene=new Scene(new FileStore());JFrame frame=new JFrame("Đại Lộ Zombie");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){scene.close();frame.dispose();System.exit(0);}});
            frame.addWindowFocusListener(new WindowAdapter(){public void windowLostFocus(WindowEvent e){scene.pause();}});
            BufferedImage icon=new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);Graphics2D g=icon.createGraphics();AwtPainter p=new AwtPainter(g);p.rect(0,0,128,128,24,Art.rgb("29273F"));Art.zombie(p,65,116,1.65f,1.0,0,false);g.dispose();frame.setIconImage(icon);
            frame.setContentPane(new GamePanel(scene));frame.pack();frame.setMinimumSize(new Dimension(760,450));frame.setLocationRelativeTo(null);frame.setVisible(true);
        });
    }
    static final class GamePanel extends JPanel {
        final Scene scene;final Timer timer;final Sounds sounds=new Sounds();long last=System.nanoTime(),seen;
        GamePanel(Scene scene) {
            this.scene=scene;setPreferredSize(new Dimension(960,540));setBackground(new Color(0x171929));setFocusable(true);
            addMouseListener(new MouseAdapter(){public void mousePressed(MouseEvent e){float[] point=logical(e);scene.pointerDown(point[0],point[1]);sound();requestFocusInWindow();}public void mouseReleased(MouseEvent e){float[] point=logical(e);scene.pointerUp(point[0],point[1]);sound();repaint();}});
            bind("pressed SPACE","jump",()->{scene.jumpDown();sound();});bind("released SPACE","release",scene::jumpUp);bind("ESCAPE","pause",scene::back);bind("P","pauseP",scene::back);
            timer=new Timer(20,e->{long now=System.nanoTime();scene.update((now-last)/1e9);last=now;sound();repaint();});timer.start();
        }
        private void bind(String key,String action,Runnable work){getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key),action);getActionMap().put(action,new AbstractAction(){public void actionPerformed(ActionEvent e){work.run();repaint();}});}
        private void sound(){if(seen!=scene.eventSerial){seen=scene.eventSerial;if(scene.profile.sound)sounds.play(scene.event);}}
        private float[] logical(MouseEvent e){float s=Math.min(getWidth()/Scene.WIDTH,getHeight()/Scene.HEIGHT);return new float[]{(e.getX()-(getWidth()-Scene.WIDTH*s)/2)/s,(e.getY()-(getHeight()-Scene.HEIGHT*s)/2)/s};}
        protected void paintComponent(Graphics graphics){super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();float s=Math.min(getWidth()/Scene.WIDTH,getHeight()/Scene.HEIGHT);g.translate((getWidth()-Scene.WIDTH*s)/2,(getHeight()-Scene.HEIGHT*s)/2);g.scale(s,s);scene.draw(new AwtPainter(g),System.currentTimeMillis());g.dispose();}
    }
    static final class FileStore implements SaveStore {
        final Properties data=new Properties();final Path file=Paths.get(System.getProperty("user.home"),".dai-lo-zombie","progress.properties");
        FileStore(){try{if(Files.exists(file))try(Reader r=Files.newBufferedReader(file,StandardCharsets.UTF_8)){data.load(r);}}catch(IOException e){System.err.println("Cannot read progress: "+e.getMessage());}}
        public String read(String key){return data.getProperty(key);}
        public synchronized void write(String key,String value){data.setProperty(key,value);try{Files.createDirectories(file.getParent());Path temp=file.resolveSibling("progress.tmp");try(Writer writer=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){data.store(writer,"Dai Lo Zombie offline progress");}try{Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}}catch(IOException e){System.err.println("Cannot save progress: "+e.getMessage());}}
    }
    static final class MemoryStore implements SaveStore {final Properties values=new Properties();public String read(String key){return values.getProperty(key);}public void write(String key,String value){values.setProperty(key,value);}}
    static final class Sounds {
        final Executor executor=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(4),task->{Thread t=new Thread(task,"zombie-audio");t.setDaemon(true);return t;},new ThreadPoolExecutor.DiscardPolicy());
        void play(String event){executor.execute(()->{float frequency=event.equals("coin")?880:event.equals("jump")?440:event.equals("recruit")?620:event.equals("hurt")||event.equals("over")?150:720;int count=event.equals("over")?6500:1600;byte[] bytes=new byte[count*2];for(int i=0;i<count;i++){double sweep=event.equals("jump")?1+i/(double)count*.8:1;short sample=(short)(Math.sin(i*frequency*sweep*Math.PI*2/22050)*4000*(1-i/(double)count));bytes[2*i]=(byte)sample;bytes[2*i+1]=(byte)(sample>>8);}try{AudioFormat format=new AudioFormat(22050,16,1,true,false);SourceDataLine line=AudioSystem.getSourceDataLine(format);line.open(format);line.start();line.write(bytes,0,bytes.length);line.drain();line.close();}catch(LineUnavailableException|IllegalArgumentException ignored) { }});}
    }
}
