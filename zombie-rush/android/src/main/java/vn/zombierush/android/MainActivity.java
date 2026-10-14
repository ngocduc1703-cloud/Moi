package vn.zombierush.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import vn.zombierush.core.*;

public final class MainActivity extends Activity {
    private Scene scene;
    private RunnerView view;
    private ToneGenerator tones;
    public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences preferences=getSharedPreferences("zombie",Context.MODE_PRIVATE);
        scene=new Scene(new SaveStore(){public String read(String key){return preferences.getString(key,null);}public void write(String key,String value){preferences.edit().putString(key,value).apply();}});
        try{tones=new ToneGenerator(AudioManager.STREAM_MUSIC,45);}catch(RuntimeException ignored) { }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getWindow().setStatusBarColor(0xff1D1C35);getWindow().setNavigationBarColor(0xff1D1C35);
        getWindow().getDecorView().setSystemUiVisibility(0);
        view=new RunnerView(this);setContentView(view);
        view.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30){Insets i=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());view.setPadding(i.left,i.top,i.right,i.bottom);}
            else view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        view.requestApplyInsets();
    }
    protected void onPause(){super.onPause();if(scene!=null){scene.pause();scene.close();}if(view!=null)view.lastFrame=0;}
    protected void onResume(){super.onResume();if(view!=null)view.lastFrame=0;}
    protected void onDestroy(){if(tones!=null)tones.release();super.onDestroy();}
    public void onBackPressed(){scene.back();view.invalidate();}
    final class RunnerView extends View {
        private final AndroidPainter painter=new AndroidPainter();
        private long seen,lastFrame;
        RunnerView(Context context){super(context);setFocusable(true);setContentDescription("Đại Lộ Zombie. Chạm và giữ để cả đàn nhảy qua chướng ngại.");}
        private float scale(){return Math.min((getWidth()-getPaddingLeft()-getPaddingRight())/Scene.WIDTH,(getHeight()-getPaddingTop()-getPaddingBottom())/Scene.HEIGHT);}
        private float left(float s){return getPaddingLeft()+(getWidth()-getPaddingLeft()-getPaddingRight()-Scene.WIDTH*s)/2;}
        private float top(float s){return getPaddingTop()+(getHeight()-getPaddingTop()-getPaddingBottom()-Scene.HEIGHT*s)/2;}
        protected void onDraw(Canvas canvas){super.onDraw(canvas);long now=System.nanoTime();if(lastFrame!=0)scene.update((now-lastFrame)/1e9);lastFrame=now;playSound();canvas.drawColor(0xff171929);float s=scale();if(s>0){canvas.save();canvas.translate(left(s),top(s));canvas.scale(s,s);painter.canvas=canvas;scene.draw(painter,System.currentTimeMillis());canvas.restore();}postInvalidateDelayed(20);}
        private void playSound(){if(seen!=scene.eventSerial){seen=scene.eventSerial;if(scene.profile.sound&&tones!=null)try{tones.startTone(scene.event.equals("over")||scene.event.equals("hurt")?ToneGenerator.TONE_PROP_NACK:ToneGenerator.TONE_PROP_BEEP,scene.event.equals("over")?180:45);}catch(RuntimeException ignored){}}}
        public boolean onTouchEvent(MotionEvent e){
            float s=scale();if(s<=0)return true;float x=(e.getX()-left(s))/s,y=(e.getY()-top(s))/s;
            if(e.getAction()==MotionEvent.ACTION_DOWN)scene.pointerDown(x,y);
            else if(e.getAction()==MotionEvent.ACTION_UP){scene.pointerUp(x,y);performClick();}
            else if(e.getAction()==MotionEvent.ACTION_CANCEL)scene.jumpUp();
            playSound();invalidate();return true;
        }
        public boolean performClick(){super.performClick();return true;}
    }
    static final class AndroidPainter implements Painter {
        Canvas canvas;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final RectF rect=new RectF();final Path path=new Path();
        private void color(int c){paint.setColor(c);paint.setStyle(Paint.Style.FILL);}
        public void rect(float x,float y,float w,float h,float r,int c){color(c);rect.set(x,y,x+w,y+h);canvas.drawRoundRect(rect,r,r,paint);}
        public void ellipse(float x,float y,float w,float h,int c){color(c);rect.set(x,y,x+w,y+h);canvas.drawOval(rect,paint);}
        public void line(float x1,float y1,float x2,float y2,float width,int c){color(c);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);canvas.drawLine(x1,y1,x2,y2,paint);}
        public void polygon(float[] points,int c){color(c);path.reset();path.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);path.close();canvas.drawPath(path,paint);}
        public void text(String text,float x,float y,float size,int c,boolean bold,boolean center){color(c);paint.setTypeface(bold?Typeface.create("sans-serif",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextSize(size);paint.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);canvas.drawText(text,x,y,paint);}
    }
}
