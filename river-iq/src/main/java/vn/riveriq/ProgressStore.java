package vn.riveriq;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

public final class ProgressStore {
    private final Properties data=new Properties();
    private final Path file;
    public ProgressStore(){this(Paths.get(System.getProperty("user.home"),".qua-song-iq","progress.properties"));}
    ProgressStore(Path file){this.file=file;load();}
    private void load(){try{if(Files.exists(file))try(Reader r=Files.newBufferedReader(file,StandardCharsets.UTF_8)){data.load(r);}}catch(IOException ignored){}}
    public int unlocked(){return number("unlocked",1);}
    public int best(int level){return number("best."+level,0);}
    private int number(String key,int fallback){try{return Integer.parseInt(data.getProperty(key));}catch(Exception e){return fallback;}}
    public synchronized void complete(int level,int moves){
        int best=best(level);if(best==0||moves<best)data.setProperty("best."+level,String.valueOf(moves));
        data.setProperty("unlocked",String.valueOf(Math.max(unlocked(),level+2)));save();
    }
    private void save(){try{
        Files.createDirectories(file.getParent());Path temp=file.resolveSibling("progress.tmp");
        try(Writer w=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){data.store(w,"Qua Song IQ progress");}
        try{Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
    }catch(IOException ignored){}}
}
