import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

/** Portable APK builder. Run with JDK 17+: java AndroidBuild.java ROOT ANDROID_SDK. */
class AndroidBuild {
    static final boolean WINDOWS=System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    static Path jdk, sdk, build, root;
    static void run(String... args) throws Exception {
        List<String> command=new ArrayList<>(Arrays.asList(args));
        if(WINDOWS&&args[0].endsWith(".bat")){command.add(0,"/c");command.add(0,"cmd.exe");}
        ProcessBuilder p=new ProcessBuilder(command).inheritIO().directory(root.toFile());
        int exit=p.start().waitFor();if(exit!=0)throw new IOException("Command failed ("+exit+"): "+args[0]);
    }
    static String javaTool(String name){return jdk.resolve("bin").resolve(name+(WINDOWS?".exe":"")).toString();}
    static String androidTool(String name){return sdk.resolve("build-tools/35.0.0").resolve(name+(WINDOWS?(name.equals("d8")||name.equals("apksigner")?".bat":".exe"):"")).toString();}
    public static void main(String[] args) throws Exception {
        if(args.length!=2)throw new IllegalArgumentException("Usage: java scripts/AndroidBuild.java ROOT ANDROID_HOME");
        root=Paths.get(args[0]).toAbsolutePath();sdk=Paths.get(args[1]).toAbsolutePath();jdk=Paths.get(System.getProperty("java.home"));
        Path platform=sdk.resolve("platforms/android-35/android.jar");
        if(!Files.isRegularFile(platform)||!Files.isDirectory(sdk.resolve("build-tools/35.0.0")))throw new IOException("Install Android SDK platform 35 and build-tools 35.0.0");
        build=root.resolve(".build/android");Path classes=build.resolve("classes"),dex=build.resolve("dex"),dist=root.resolve("dist");
        for(Path dir:Arrays.asList(classes,dex)){if(Files.exists(dir))try(Stream<Path> paths=Files.walk(dir)){for(Path file:paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))Files.delete(file);}Files.createDirectories(dir);}
        Files.createDirectories(dist);
        List<String> compile=new ArrayList<>(Arrays.asList(javaTool("javac"),"-encoding","UTF-8","--release","8","-classpath",platform.toString(),"-d",classes.toString()));
        for(String folder:new String[]{"core","android"})try(Stream<Path> paths=Files.walk(root.resolve(folder+"/src/main/java"))){paths.filter(p->p.toString().endsWith(".java")).forEach(p->compile.add(p.toString()));}
        run(compile.toArray(new String[0]));
        run(javaTool("jar"),"cf",build.resolve("classes.jar").toString(),"-C",classes.toString(),".");
        run(androidTool("d8"),"--release","--min-api","23","--lib",platform.toString(),"--output",dex.toString(),build.resolve("classes.jar").toString());
        run(androidTool("aapt2"),"compile","--dir",root.resolve("android/src/main/res").toString(),"-o",build.resolve("resources.zip").toString());
        run(androidTool("aapt2"),"link","-o",build.resolve("resources.apk").toString(),"-I",platform.toString(),"--manifest",root.resolve("android/src/main/AndroidManifest.xml").toString(),build.resolve("resources.zip").toString());
        try(ZipInputStream input=new ZipInputStream(Files.newInputStream(build.resolve("resources.apk")));ZipOutputStream output=new ZipOutputStream(Files.newOutputStream(build.resolve("unsigned.apk")))){
            ZipEntry e;while((e=input.getNextEntry())!=null){ZipEntry entry=new ZipEntry(e.getName());entry.setTime(315532800000L);output.putNextEntry(entry);input.transferTo(output);output.closeEntry();}
            try(Stream<Path> files=Files.list(dex)){for(Path file:files.filter(p->p.toString().endsWith(".dex")).collect(Collectors.toList())){ZipEntry entry=new ZipEntry(file.getFileName().toString());entry.setTime(315532800000L);output.putNextEntry(entry);Files.copy(file,output);output.closeEntry();}}
        }
        run(androidTool("zipalign"),"-f","-p","4",build.resolve("unsigned.apk").toString(),build.resolve("aligned.apk").toString());
        String custom=System.getenv("GARDEN_KEYSTORE");
        Path key=custom==null?root.resolve(".build/keys/garden-dev.p12"):Paths.get(custom);
        if(custom==null&&!Files.exists(key)){Files.createDirectories(key.getParent());run(javaTool("keytool"),"-genkeypair","-keystore",key.toString(),"-storetype","PKCS12","-alias","garden-dev","-storepass","android","-keypass","android","-keyalg","RSA","-keysize","3072","-validity","3650","-dname","CN=Garden Development,OU=Development,O=Garden,C=VN");}
        String alias=custom==null?"garden-dev":Objects.requireNonNull(System.getenv("GARDEN_KEY_ALIAS"),"GARDEN_KEY_ALIAS is required with a custom key");
        String password=custom==null?"pass:android":"env:GARDEN_STORE_PASS";
        Path apk=dist.resolve("VuonGhepBa-Android.apk");
        run(androidTool("apksigner"),"sign","--ks",key.toString(),"--ks-key-alias",alias,"--ks-pass",password,"--v4-signing-enabled","false","--out",apk.toString(),build.resolve("aligned.apk").toString());
        run(androidTool("apksigner"),"verify","--verbose",apk.toString());
        run(androidTool("aapt"),"dump","badging",apk.toString());
        System.out.println("APK ready: "+apk);
    }
}
