package co.com.picolab;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ContentActivity extends AppCompatActivity {

    private RelativeLayout layout_principal;
    private ConstraintLayout layout_popup;
    private VideoView video;
    private TextView textvideo;

    private FileInputStream in;
    private ArrayList<Video> videos; //Videos leidos del XML
    private Map<Integer, ImageView> mapImages; //Mapa de miniaturas de videos
    public Map<Integer, Video> mapEntry; //Mapa de descripcion de videos
    private static final int RED_TAMANO_IMG = 3;
    private int actx = -1, acty = -1; //Variables para mover objetos
    boolean popUpActivated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        layout_principal = findViewById(R.id.layout_principal);
        layout_popup = findViewById(R.id.layout_popup);
        layout_popup.setVisibility(View.INVISIBLE);
        video = findViewById(R.id.video);
        textvideo = findViewById(R.id.textvideo);

        mapImages = new TreeMap<>();
        mapEntry = new TreeMap<>();
        popUpActivated = false;

        parseXML();

        for(final Integer vi : mapImages.keySet()){
            mapImages.get(vi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setGif(vi);
                    //CAMBIAR TAMAÑO DE LA PUTA IMAGEN :)
                    layout_popup.setVisibility(View.VISIBLE);
                    layout_popup.setBackground(getResources().getDrawable(R.drawable.fondogif));
                    popUpActivated = true;
                    //Hacer lo del pop-up
                }
            });
        }

    }
    private void setGif(int id){
        textvideo.setText(mapEntry.get(id).getDescripcion());
        File videoFile;
        switch (id){
            case 1:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zelda.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 2:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zeldados.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 3:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zelda.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 4:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zeldados.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 5:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zelda.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 6:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zelda.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 7:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zelda.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 8:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zelda.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
            case 9:
                videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "zelda.mp4");
                video.setVideoURI(Uri.fromFile(videoFile));
                video.start();
                break;
        }
    }

    private void setImages(int size, int left, int top, ImageView img){
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.leftMargin = left;
        params.topMargin = top;
        layout_principal.addView(img, params);
    }

    private void printInfoVideos(ArrayList<Video> videos){
        for(Video video: videos){
            ImageView img = new ImageView(this);
            Drawable mydr = setDrawable(video.getId());
            img.setImageDrawable(mydr);
            int tamano = tamanoImg(video.getPosx(), video.getPosy())/RED_TAMANO_IMG;
            setImages(tamano, video.getPosx(), video.getPosy(), img);
            mapImages.put(video.id, img);
            mapEntry.put(video.id, video);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        video.stopPlayback();
        if(popUpActivated) {
            layout_popup.setVisibility(View.INVISIBLE);
            popUpActivated = false;
        }
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actx = -1; acty = -1; break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        if(actx == -1 && acty == -1){
            actx = x; acty = y;
        }
        else{
            int oldx = actx, oldy = acty;
            actx = x; acty = y;
            moveImg(oldx, actx, oldy, acty);
        }
        return false;
    }

    private void moveImg(int x0, int x1, int y0, int y1){
        for(final Integer i : mapImages.keySet()){
            ImageView im = mapImages.get(i);
            int[] pos = posImg(x0, x1, y0, y1);

            if(pos[2] < 0){
                for(int j = pos[0]; j < 0; j++){
                    if(im.getX() <= 0){
                        im.setX(1279);
                        mapEntry.get(i).setPosx(1279);
                    }
                    im.setX(mapEntry.get(i).getPosx() - 1);
                    mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() - 1);
                }
            }
            else{
                for (int j = 0; j < pos[0]; j++){
                    if(im.getX() + im.getWidth() > 1279){
                        im.setX(0);
                        mapEntry.get(i).setPosx(0);
                    }
                    im.setX(mapEntry.get(i).getPosx() + 1);
                    mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() + 1);
                }
            }
            if(pos[3] < 0){
                for(int j = pos[1]; j < 0; j++){
                    if(im.getY() <= 0){
                        im.setY(799);
                        mapEntry.get(i).setPosy(799);
                    }
                    im.setY(mapEntry.get(i).getPosy() - 1);
                    mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() - 1);
                }
            }
            else {
                for(int j = 0; j < pos[1]; j++){
                    if(im.getY() + im.getHeight() > 799){
                        im.setY(0);
                        mapEntry.get(i).setPosy(0);
                    }
                    im.setY(mapEntry.get(i).getPosy() + 1);
                    mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() + 1);
                }
            }

            int tamano = tamanoImg(mapEntry.get(i).getPosx(), mapEntry.get(i).getPosy()) / RED_TAMANO_IMG;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(tamano, tamano);
            im.setLayoutParams(params);
        }
    }

    private int[] posImg(int x0, int x1, int y0, int y1) {
        int difx = x1 - x0;
        int dify = y1 - y0;
        int sigx = 0, sigy = 0;

        if(difx < 0) sigx = -1;
        else sigx = +1;

        if(dify < 0) sigy = -1;
        else sigy = +1;

        return new int[]{difx, dify, sigx, sigy};
    }

    private int tamanoImg(int x, int y){
        int n = 0;
        if(x < 640 && y < 400){
            n = distance(x, 0, y, 0);
        }
        if(x > 640 && y < 400){
            n = distance(x, 1280, y, 0);
        }
        if(x < 640 && y > 400){
            n = distance(x, 0, y, 800);
        }
        if(x > 640 && y > 400){
            n = distance(x, 1280, y, 800);
        }
        return n;
    }

    private int distance(int x, int x0, int y, int y0){
        return (int)Math.sqrt((Math.pow(x-x0,2)) + (Math.pow(y-y0,2)));
    }

    private Drawable setDrawable(int id){
        switch (id){
            case 1: return getResources().getDrawable(R.drawable.link);
            case 2: return getResources().getDrawable(R.drawable.link);
            case 3: return getResources().getDrawable(R.drawable.link);
            case 4: return getResources().getDrawable(R.drawable.link);
            case 5: return getResources().getDrawable(R.drawable.link);
            case 6: return getResources().getDrawable(R.drawable.link);
            case 7: return getResources().getDrawable(R.drawable.link);
        }
        return null;
    }

    private void parseXML(){
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            //in = getAssets().open("videos.xml");
            //Nuevo de aca
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"videos.xml");
            String s = storageDir.getAbsolutePath();
            in = new FileInputStream(s);
            //A aca
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            processParsing(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        videos = new ArrayList<>();
        int eventType = parser.getEventType();
        Video currentVideo = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if ("video".equals(eltName)) {
                        currentVideo = new Video();
                        videos.add(currentVideo);
                    } else if (currentVideo != null) {
                        if("id".equals(eltName)){
                            currentVideo.setId(Integer.parseInt(parser.nextText()));
                        } else if ("nombre".equals(eltName)) {
                            currentVideo.setNombre(parser.nextText());
                        } else if ("titulo".equals(eltName)) {
                            currentVideo.setTitulo(parser.nextText());
                        } else if ("descripcion".equals(eltName)) {
                            currentVideo.setDescripcion(parser.nextText());
                        } else if("posx".equals(eltName)){
                            currentVideo.setPosx(Integer.parseInt(parser.nextText()));
                        } else if("posy".equals(eltName)){
                            currentVideo.setPosy(Integer.parseInt(parser.nextText()));
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        printInfoVideos(videos);
        layout_principal.removeViewInLayout(layout_popup);
        layout_principal.addView(layout_popup);
    }

    //Metodo para tener pantalla completa
    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;// | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
