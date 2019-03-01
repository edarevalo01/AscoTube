package co.com.picolab;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ContentActivity extends AppCompatActivity {

    private RelativeLayout layout_principal;
    private ConstraintLayout layout_popup;
    private VideoView video;
    private TextView textvideo;
    private TextView texttittle;

    private FileInputStream in;
    private ArrayList<Video> videos; //Videos leidos del XML
    private Map<Integer, ImageView> mapImages; //Mapa de miniaturas de videos
    public Map<Integer, Video> mapEntry; //Mapa de descripcion de videos
    private static final int RED_TAMANO_IMG = 2;
    private static final boolean FRONTIMG = false;
    private static final int RADIO = 370;
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
        texttittle = findViewById(R.id.texttittle);

        mapImages = new TreeMap<>();
        mapEntry = new TreeMap<>();
        popUpActivated = false;

        parseXML();

        for(final Integer vi : mapImages.keySet()){
            mapImages.get(vi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setVideo(vi);
                    layout_popup.setVisibility(View.VISIBLE);
                    layout_popup.setBackground(getResources().getDrawable(R.drawable.fondogif));
                    popUpActivated = true;
                }
            });
        }
    }

    /**
     * Metodo para establecer videos en el PopUp
     * @param id id del video respecto al objeto
     */
    private void setVideo(int id){
        texttittle.setText(mapEntry.get(id).getTitulo());
        textvideo.setText(mapEntry.get(id).getDescripcion());
        File videoFile;
        String videoName = mapEntry.get(id).getNombre();
        videoFile = new  File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), videoName + ".mp4");
        video.setVideoURI(Uri.fromFile(videoFile));
        video.start();
    }

    /**
     * Metodo para ubicar imagenes en el Layout
     * @param size Tamano de la imagen
     * @param left ubicacion X
     * @param top ubicacion Y
     * @param img Imagen a colocar
     */
    private void setImages(int size, int left, int top, ImageView img){
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.leftMargin = left;
        params.topMargin = top;
        layout_principal.addView(img, params);
    }

    /**
     * Metodo para ubicar miniaturas de videos en pantalla de contenido
     * @param videos
     */
    private void printInfoVideos(ArrayList<Video> videos){
        for(Video video: videos){
            ImageView img = new ImageView(this);
            File mydr = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), video.getNombre() + ".png");
            img.setImageURI(Uri.fromFile(mydr));
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

    /**
     * Metodo para calcular posiciones al mover las imagenes
     * @param x0
     * @param x1
     * @param y0
     * @param y1
     */
    private void moveImg(int x0, int x1, int y0, int y1) {
        for(final Integer i : mapImages.keySet()){
            ImageView im = mapImages.get(i);
            if(mapEntry.get(i).getFrente()) {
                int[] pos = posImg(x0, x1, y0, y1);
                if (pos[2] < 0) {
                    for (int j = pos[0]; j < 0; j++) {
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            im.setX(mapEntry.get(i).getPosx() + 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            mapEntry.get(i).setFrente(FRONTIMG);
                        }
                        else {
                            im.setX(mapEntry.get(i).getPosx() - 1);
                            mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() - 1);
                        }
                    }
                } else {
                    for (int j = 0; j < pos[0]; j++) {
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            //1020
                            im.setX(mapEntry.get(i).getPosx() - 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            mapEntry.get(i).setFrente(FRONTIMG);
                        }
                        else {
                            im.setX(mapEntry.get(i).getPosx() + 1);
                            mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() + 1);
                        }
                    }
                }
                if (pos[3] < 0) {
                    for (int j = pos[1]; j < 0; j++) {
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            //20
                            im.setY(mapEntry.get(i).getPosy() + 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            mapEntry.get(i).setFrente(FRONTIMG);
                        }
                        else {
                            im.setY(mapEntry.get(i).getPosy() - 1);
                            mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() - 1);
                        }
                    }
                } else {
                    for (int j = 0; j < pos[1]; j++) {
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            //780
                            im.setY(mapEntry.get(i).getPosy() - 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            mapEntry.get(i).setFrente(FRONTIMG);
                        }
                        else {
                            im.setY(mapEntry.get(i).getPosy() + 1);
                            mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() + 1);
                        }
                    }
                }

                int tamano = tamanoImg(mapEntry.get(i).getPosx(), mapEntry.get(i).getPosy())/ RED_TAMANO_IMG;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(tamano, tamano);
                im.setLayoutParams(params);
            }
            else{
                int[] pos = posImg(x0, x1, y0, y1);
                if (-pos[2] < 0) {
                    for (int j = 0; j < pos[0]; j++) {
                        //derecha
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            //260
                            im.setX(mapEntry.get(i).getPosx() + 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            mapEntry.get(i).setFrente(!FRONTIMG);
                        }
                        else {
                            im.setX(mapEntry.get(i).getPosx() - 1);
                            mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() - 1);
                        }
                    }
                } else {
                    for (int j = pos[0]; j < 0; j++) {
                        //izquierda
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            //1020
                            im.setX(mapEntry.get(i).getPosx() - 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            mapEntry.get(i).setFrente(!FRONTIMG);
                        }
                        else {
                            im.setX(mapEntry.get(i).getPosx() + 1);
                            mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() + 1);
                        }
                    }
                }
                if (-pos[3] < 0) {
                    for (int j = 0; j < pos[1]; j++) {
                        //abajo
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            //20
                            im.setY(mapEntry.get(i).getPosy() + 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            mapEntry.get(i).setFrente(!FRONTIMG);
                        }
                        else {
                            im.setY(mapEntry.get(i).getPosy() - 1);
                            mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() - 1);
                        }
                    }
                } else {
                    for (int j = pos[1]; j < 0; j++) {
                        //arriba
                        if (distance(640, mapEntry.get(i).getPosx(), 400, mapEntry.get(i).getPosy()) >= RADIO) {
                            //780
                            im.setY(mapEntry.get(i).getPosy() - 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            mapEntry.get(i).setFrente(!FRONTIMG);
                        }
                        else {
                            im.setY(mapEntry.get(i).getPosy() + 1);
                            mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() + 1);
                        }
                    }
                }

            int tamano = tamanoImg(mapEntry.get(i).getPosx(), mapEntry.get(i).getPosy()) / (RED_TAMANO_IMG*2);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(tamano, tamano);
                im.setLayoutParams(params);
            }
        }
    }

    /**
     * Metodo para calcular la dirección en la que se moverán los objetos
     * @param x0 XAnterior
     * @param x1 XActual
     * @param y0 YAnterior
     * @param y1 YActual
     * @return Dirección hacia donde se mueven los objetos
     */
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

    /**
     * Metodo para establecer tamaño respecto a distancia con bordes
     * @param x Posicion X
     * @param y Posicion Y
     * @return Tamano de la imagen
     */
    private int tamanoImg(int x, int y){
        int n = 0;
        if(x < 640 && y < 400){
            n = distance(x, 260, y, 20);
        }
        if(x > 640 && y < 400){
            n = distance(x, 1020, y, 20);
        }
        if(x < 640 && y > 400){
            n = distance(x, 260, y, 780);
        }
        if(x > 640 && y > 400){
            n = distance(x, 1020, y, 780);
        }
        return n;
    }

    /**
     * Metodo para calcular distancia ecuclidiana
     * @param x X actual
     * @param x0 X inicial
     * @param y Y actual
     * @param y0 Y inicial
     * @return Distancia euclidiana entre dos puntos
     */
    private int distance(int x, int x0, int y, int y0){
        return (int)Math.sqrt((Math.pow(x-x0,2)) + (Math.pow(y-y0,2)));
    }

    /** Metodo para leer XML */
    private void parseXML(){
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            //in = getAssets().open("videos.xml"); //Lectura de XML desde proyecto
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"videos.xml"); //Leer XML desde Dispositivo
            String s = storageDir.getAbsolutePath();
            in = new FileInputStream(s);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            processParsing(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo para parsear videos desde el xml y convertirlo en objetos
     * @param parser Archivo leido desde videos.xml
     * @throws IOException
     * @throws XmlPullParserException
     */
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
                        } else if("frente".equals(eltName)){
                            currentVideo.setFrente(Boolean.parseBoolean(parser.nextText().toLowerCase()));
                        }else{
                            Log.e("Error en elemento", eltName + " este elemento no existe");
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

    /** Metodo para Pantalla completa */
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
