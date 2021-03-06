package co.com.picolab;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
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
    private ImageButton btn_expand;
    private ImageButton btn_close;

    private FileInputStream in;
    private ArrayList<Video> videos; //Videos leidos del XML
    private Map<Integer, ImageView> mapImages; //Mapa de miniaturas de videos
    public Map<Integer, Video> mapEntry; //Mapa de descripcion de videos
    private static final int RED_TAMANO_IMG = 2;
    private static final boolean FRONTIMG = false;
    private static final int RADIO = 370;
    private static final int CENTERX = 585;
    private static final int CENTERY = 335;
    private int actx = -1, acty = -1; //Variables para mover objetos
    boolean popUpActivated;
    boolean isExpanded = false;
    //MovAsincrono movimiento;

    private static final long START_TIME = 3*60*1000; //Tiempo de inactividad (los minutos se representan en el primer valor)
    private static final long INTERVAL = 1*1000;
    private MyCountDownTimer countDownTimer;

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
        btn_expand = findViewById(R.id.btn_expand);
        btn_close = findViewById(R.id.btn_close);

        mapImages = new TreeMap<>();
        mapEntry = new TreeMap<>();
        popUpActivated = false;

        parseXML();
        countDownTimer = new MyCountDownTimer(START_TIME, INTERVAL);


        //new MovAsincrono().execute();
        //for (int i = 0; i < 10; i++){
        //    movimiento = new MovAsincrono();
        //    moveImg(585+(i*10),685+(i*10),335+(i*10),435+(i*10));
        //    movimiento.execute();

            //movimiento.onCancelled();
        //}


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
     * @param id del video respecto al objeto
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
     * @param left ubicacion X (posicion)
     * @param top ubicacion Y (posicion)
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
     * @param videos Arreglo de videos
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
        layout_popup.setMaxWidth(491);
        layout_popup.setMaxHeight(601);
        texttittle.setTextColor(getResources().getColor(R.color.white));
        textvideo.setTextColor(getResources().getColor(R.color.white));
        texttittle.setEnabled(true);
        textvideo.setEnabled(true);
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
                        //Movimiento hacia izquierda en eje x
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            im.setX(mapEntry.get(i).getPosx() + 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            if(im.getY() < CENTERY){
                                im.setY(mapEntry.get(i).getPosy() + 3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
                            else if(im.getY() > CENTERY){
                                im.setY(mapEntry.get(i).getPosy() - 3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
                            mapEntry.get(i).setFrente(FRONTIMG);
                        }
                        else {
                            im.setX(mapEntry.get(i).getPosx() - 1);
                            mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() - 1);
                        }
                    }
                } else {
                    for (int j = 0; j < pos[0]; j++) {
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            //Movimiento hacia derecha en eje x
                            im.setX(mapEntry.get(i).getPosx() - 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            if(im.getY() < CENTERY){
                                im.setY(mapEntry.get(i).getPosy() + 3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
                            else if(im.getY() > CENTERY){
                                im.setY(mapEntry.get(i).getPosy() - 3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
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
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            //Movimiento hacia arriba en eje y
                            im.setY(mapEntry.get(i).getPosy() + 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            if(im.getX() < CENTERX){
                                im.setX(mapEntry.get(i).getPosx() + 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
                            else if(im.getX() > CENTERX){
                                im.setX(mapEntry.get(i).getPosx() - 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
                            mapEntry.get(i).setFrente(FRONTIMG);
                        }
                        else {
                            im.setY(mapEntry.get(i).getPosy() - 1);
                            mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() - 1);
                        }
                    }
                } else {
                    for (int j = 0; j < pos[1]; j++) {
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            //Movimiento hacia abajo en eje y
                            im.setY(mapEntry.get(i).getPosy() - 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            if(im.getX() < CENTERX){
                                im.setX(mapEntry.get(i).getPosx() + 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
                            else if(im.getX() > CENTERX){
                                im.setX(mapEntry.get(i).getPosx() - 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
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
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            //Movimiento hacia derecha eje x imagen invertida
                            im.setX(mapEntry.get(i).getPosx() + 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            if(im.getY() < CENTERY){
                                im.setY(mapEntry.get(i).getPosy() + 3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
                            else if(im.getY() > CENTERY){
                                im.setY(mapEntry.get(i).getPosy() -3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
                            mapEntry.get(i).setFrente(!FRONTIMG);
                        }
                        else {
                            im.setX(mapEntry.get(i).getPosx() - 1);
                            mapEntry.get(i).setPosx(mapEntry.get(i).getPosx() - 1);
                        }
                    }
                } else {
                    for (int j = pos[0]; j < 0; j++) {
                        //Movimiento hacia izquierda en eje x imagen invertida
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            //1020
                            im.setX(mapEntry.get(i).getPosx() - 3);
                            mapEntry.get(i).setPosx((int)im.getX());
                            if(im.getY() < CENTERY){
                                im.setY(mapEntry.get(i).getPosy() + 3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
                            else if(im.getY() > CENTERY){
                                im.setY(mapEntry.get(i).getPosy() -3);
                                mapEntry.get(i).setPosy((int)im.getY());
                            }
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
                        //Movimiento hacia abajo en eje Y imagen invertida
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            //20
                            im.setY(mapEntry.get(i).getPosy() + 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            if(im.getX() < CENTERX){
                                im.setX(mapEntry.get(i).getPosx() + 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
                            else if(im.getX() > CENTERX){
                                im.setX(mapEntry.get(i).getPosx() - 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
                            mapEntry.get(i).setFrente(!FRONTIMG);
                        }
                        else {
                            im.setY(mapEntry.get(i).getPosy() - 1);
                            mapEntry.get(i).setPosy(mapEntry.get(i).getPosy() - 1);
                        }
                    }
                } else {
                    for (int j = pos[1]; j < 0; j++) {
                        //Movimiento hacia arriba en eje Y imagen invertida
                        if (distance(CENTERX, mapEntry.get(i).getPosx(), CENTERY, mapEntry.get(i).getPosy()) >= RADIO) {
                            //780
                            im.setY(mapEntry.get(i).getPosy() - 3);
                            mapEntry.get(i).setPosy((int)im.getY());
                            if(im.getX() < CENTERX){
                                im.setX(mapEntry.get(i).getPosx() + 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
                            else if(im.getX() > CENTERX){
                                im.setX(mapEntry.get(i).getPosx() - 3);
                                mapEntry.get(i).setPosx((int)im.getX());
                            }
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

    /**
     * Metodo para Expandir el video
     */
    public void expandVideo(View view){
        if(!isExpanded) {
            layout_popup.setMaxWidth(1280);
            layout_popup.setMaxHeight(800);
            texttittle.setTextColor(getResources().getColor(R.color.transparent));
            textvideo.setTextColor(getResources().getColor(R.color.transparent));
            texttittle.setEnabled(false);
            textvideo.setEnabled(false);
            isExpanded = true;
        }
        else{
            layout_popup.setMaxWidth(491);
            layout_popup.setMaxHeight(601);
            texttittle.setTextColor(getResources().getColor(R.color.white));
            textvideo.setTextColor(getResources().getColor(R.color.white));
            texttittle.setEnabled(true);
            textvideo.setEnabled(true);
            isExpanded = false;
        }
    }

    /**
     *Metodo para cerrar el video
     */
    public void closeVideo(View view){
        layout_popup.setVisibility(View.INVISIBLE);
        popUpActivated = false;
        video.stopPlayback();
        layout_popup.setMaxWidth(491);
        layout_popup.setMaxHeight(601);
        texttittle.setTextColor(getResources().getColor(R.color.white));
        textvideo.setTextColor(getResources().getColor(R.color.white));
        texttittle.setEnabled(true);
        textvideo.setEnabled(true);
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

    /** Metodo volver pantalla de inicio despues de incatividad */
    @Override
    public void onUserInteraction(){

        super.onUserInteraction();
        countDownTimer.cancel();
        countDownTimer.start();
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    /*public class MovAsincrono extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            for(int i = 0; i < 10; i++) {
                try {Thread.sleep(100000);
                    /*moveImg(585, 595, 335, 345);
                    Thread.sleep(10000000);
                    for (int j = 0; j < 10; j++) {
                        moveImg(585+(j*10), 685+(j*10), 335+(j*10), 435+(j*10));
                    }
                }
                catch (InterruptedException e){
                }

                if(isCancelled()) break;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            /*while (true) {
                moveImg(585, 595, 335, 345);
            }
        }

        @Override
        protected void onPreExecute() {
            //moveImg(585,585,335,335);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Toast.makeText(ContentActivity.this, "Movimiento finalizado", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(ContentActivity.this, "Movimiento cancelado", Toast.LENGTH_SHORT).show();
        }
    }

    private void tareaLarga(){
        try {
            //moveImg(585, 595, 335, 345);
            Thread.sleep(1000);

        }catch (InterruptedException e){}
    }*/
}
