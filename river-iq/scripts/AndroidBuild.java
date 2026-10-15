import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

/** Portable APK builder. Run with JDK 17+: java AndroidBuild.java ROOT ANDROID_SDK. */
class AndroidBuild {
    static final boolean WINDOWS=System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    static Path jdk,sdk,build,root;
    static void run(String... args)throws Exception{List<String> c=new ArrayList<String>(Arrays.asList(args));if(WINDOWS&&args[0].endsWith(".bat")){c.add(0,"/c");c.add(0,"cmd.exe");}int exit=new ProcessBuilder(c).inheritIO().directory(root.toFile()).start().waitFor();if(exit!=0)throw new IOException("Command failed ("+exit+"): "+args[0]);}
    static String jt(String n){return jdk.resolve("bin").resolve(n+(WINDOWS?".exe":"")).toString();}
    static String at(String n){return sdk.resolve("build-tools/35.0.0").resolve(n+(WINDOWS?(n.equals("d8")||n.equals("apksigner")?".bat":".exe"):"")).toString();}
    static void clean(Path dir)throws Exception{if(Files.exists(dir))try(Stream<Path> s=Files.walk(dir)){for(Path p:s.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))Files.delete(p);}Files.createDirectories(dir);}
    public static void main(String[] args)throws Exception{
        if(args.length!=2)throw new IllegalArgumentException("Usage: java scripts/AndroidBuild.java ROOT ANDROID_HOME");root=Paths.get(args[0]).toAbsolutePath();sdk=Paths.get(args[1]).toAbsolutePath();jdk=Paths.get(System.getProperty("java.home"));Path platform=sdk.resolve("platforms/android-35/android.jar");if(!Files.isRegularFile(platform))throw new IOException("Android platform 35 is missing");
        build=root.resolve(".build/android");Path classes=build.resolve("classes"),dex=build.resolve("dex"),dist=root.resolve("dist");clean(classes);clean(dex);Files.createDirectories(dist);
        List<String> compile=new ArrayList<String>(Arrays.asList(jt("javac"),"-encoding","UTF-8","--release","8","-classpath",platform.toString(),"-d",classes.toString()));
        String[] core={"Person.java","Puzzle.java","PuzzleCatalog.java","GameState.java","Solver.java"};for(String file:core)compile.add(root.resolve("src/main/java/vn/riveriq/"+file).toString());
        try(Stream<Path> s=Files.walk(root.resolve("android/src/main/java"))){s.filter(p->p.toString().endsWith(".java")).forEach(p->compile.add(p.toString()));}run(compile.toArray(new String[0]));
        run(jt("jar"),"cf",build.resolve("classes.jar").toString(),"-C",classes.toString(),".");run(at("d8"),"--release","--min-api","23","--lib",platform.toString(),"--output",dex.toString(),build.resolve("classes.jar").toString());
        run(at("aapt2"),"compile","--dir",root.resolve("android/src/main/res").toString(),"-o",build.resolve("resources.zip").toString());run(at("aapt2"),"link","-o",build.resolve("resources.apk").toString(),"-I",platform.toString(),"--manifest",root.resolve("android/src/main/AndroidManifest.xml").toString(),build.resolve("resources.zip").toString());
        try(ZipInputStream in=new ZipInputStream(Files.newInputStream(build.resolve("resources.apk")));ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(build.resolve("unsigned.apk")))){ZipEntry e;while((e=in.getNextEntry())!=null){ZipEntry n=new ZipEntry(e.getName());n.setTime(315532800000L);out.putNextEntry(n);in.transferTo(out);out.closeEntry();}try(Stream<Path> files=Files.list(dex)){for(Path p:files.filter(x->x.toString().endsWith(".dex")).collect(Collectors.toList())){ZipEntry n=new ZipEntry(p.getFileName().toString());n.setTime(315532800000L);out.putNextEntry(n);Files.copy(p,out);out.closeEntry();}}}
        run(at("zipalign"),"-f","-p","4",build.resolve("unsigned.apk").toString(),build.resolve("aligned.apk").toString());Path key=root.resolve(".build/keys/river-iq-dev.p12");if(!Files.exists(key)){Files.createDirectories(key.getParent());run(jt("keytool"),"-genkeypair","-keystore",key.toString(),"-storetype","PKCS12","-alias","river-iq-dev","-storepass","android","-keypass","android","-keyalg","RSA","-keysize","3072","-validity","3650","-dname","CN=River IQ Development,OU=Development,O=River IQ,C=VN");}
        Path apk=dist.resolve("QuaSongIQ-Android.apk");run(at("apksigner"),"sign","--ks",key.toString(),"--ks-key-alias","river-iq-dev","--ks-pass","pass:android","--v4-signing-enabled","false","--out",apk.toString(),build.resolve("aligned.apk").toString());run(at("apksigner"),"verify","--verbose",apk.toString());run(at("aapt"),"dump","badging",apk.toString());System.out.println("APK ready: "+apk);
    }
}
