package sample.sdk.prime.com.mysamplecode.primedrone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import sample.sdk.prime.com.mysamplecode.R;
import sample.sdk.prime.com.mysamplecode.internal.controller.MainActivity;

public class DroneMenuView extends LinearLayout {

    private static TextView mDataView;
    private static TextView mStatView;
    private static Button missionStartBtn;

    String ext = Environment.getExternalStorageState();
    String rootdir = Environment.getDataDirectory().getAbsolutePath();
    String cachedir = Environment.getDownloadCacheDirectory().getAbsolutePath();
    String dbpath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/trapdb";
    String dbFilePath = "/trapdb.txt";

    phpdown taskVer;
    phpdown taskData;
    String mData = "";
    String mVersion = "";


    enum State { RUNNING, NONE, VERSION_CHECKED, SAVED, LOADED, TSP_COMPLETE };
    public static State state = State.NONE;

    public static void Load() {
        state = State.NONE;

    }

    ArrayList<Listitem> listitem = new ArrayList<Listitem>();
    Listitem item;
    final String ADDR_LOADTRAP = "http://220.149.235.139/loadTrap.php";
    final String ADDR_LOADVER = "http://220.149.235.139/loadVer.php";

    public DroneMenuView(Context context) {
        super(context);
        initView(context);
    }

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            switch(event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    this.onKeyEvent(event);
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }*/

    private void initView(Context context){

        final Context thisCon = context;

        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_drone_menu_view, this);

        mDataView = (TextView)findViewById(R.id.trapdatatext);
        mStatView = (TextView)findViewById(R.id.statustext);
        missionStartBtn = (Button)findViewById(R.id.btn_start);

        missionStartBtn.setEnabled(false);




        final Handler handler = new Handler(Looper.getMainLooper());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            ArrayList<trapData> list = new ArrayList<trapData>();
            ArrayList<Integer> tsplist = new ArrayList<Integer>();
            MyTSP tsp = new MyTSP();
            String tspRoute = "";

            @Override
            public void run() {
                switch (state) {
                    case NONE:
                        state = State.RUNNING;
                        Log.e("Test", "0");
                        taskVer = new phpdown();
                        taskVer.execute(ADDR_LOADVER);
                        Log.e("Test", "1");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mStatView.setText("버전 확인 중입니다...");
                            }
                        });
                        break;
                    case VERSION_CHECKED:
                        state = State.RUNNING;
                        Log.e("Test", "2");
                        taskData = new phpdown();
                        taskData.execute(ADDR_LOADTRAP);
                        Log.e("Test", "3");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mStatView.setText("새로운 데이터를 불러옵니다...");
                            }
                        });
                        break;
                    case SAVED:
                        state = State.RUNNING;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDataView.setText(loadDataFile(dbpath+dbFilePath));
                                mStatView.setText("데이터를 로드중입니다...");
                            }
                        });
                        break;
                    case LOADED:
                        state = State.RUNNING;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("loaded","running");
                                list = parsingData(new File(dbpath+dbFilePath));
                                Log.e("parsingdata",""+list.get(0).getAltitude());
                                tsplist = tsp.TSP(list);
                                tspRoute = tsp.returnList(tsplist);
                                Log.e("tsplist","tsp : "+tspRoute);
                                mDataView.setText(tspRoute);
                                mStatView.setText("데이터 불러오기에 성공하였습니다.");
                                missionStartBtn.setEnabled(true);
                                missionStartBtn.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(thisCon,missionView.class);
                                        intent.putExtra("tspRoute",tspRoute);
                                        thisCon.startActivity(intent);
                                    }
                                });
                            }
                        });
                        this.cancel();
                        break;
                }
            }
        }, 1000, 1000);
    }

    public ArrayList<trapData> parsingData(File file){
        ArrayList<trapData> dataList = new ArrayList<trapData>();
        String text = DroneMenuView.loadDataFile(file.getAbsolutePath());

        String[] data = text.split("\\r?\\n");

        dataList.add(new trapData(0,37.0,127.0,0));

        for(int i = 1;i<data.length;i++){
            String[] trap = data[i].split(",");
            if(trap.length!=4){
                continue;
            }
            dataList.add(new trapData(Integer.parseInt(trap[0]),Double.parseDouble(trap[1]),Double.parseDouble(trap[2]),Double.parseDouble(trap[3])));
        }

        return dataList;
    }

    public void SaveDataFile(String data, String ver) {
        //BufferedOutputStream bos = null;

        String str = ver+data;

        File dir = new File(dbpath);
        if (!dir.exists())
            dir.mkdir();

        try {
            File newData = new File(dir.getAbsolutePath()+"/trapdb.txt");
            FileOutputStream fos = new FileOutputStream(newData);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(str);
            bw.flush();
            bw.close();
            fos.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public boolean isNewVersion(String newVer, String preVerPath){
        String text = "";

        try{
            File preDbFile = new File(preVerPath);
            if(!preDbFile.exists())
                return  true;
            FileInputStream fis = new FileInputStream(preDbFile);
            Reader reader = new InputStreamReader(fis);
            int size = fis.available();
            char[] buffer = new char[size];
            reader.read(buffer);
            reader.close();
            text = new String(buffer);
        }catch(Exception e){
            e.getStackTrace();
            return false;
        }
        String preVer = text.split("\\r?\\n")[0];
        newVer = newVer.split("\\r?\\n")[0];
        Log.e("preVer", preVer+"ver");
        Log.e("newVer",newVer+"ver");
        if(!newVer.equals(preVer)){
            Log.e("isNewVersion","it's new version!");
            return true;
        }
        return false;
    }

    public static String loadDataFile(String path){
        //File dataPath = new File(path);
        String text = "";
        try{
            FileInputStream fis = new FileInputStream(path);
            Reader reader = new InputStreamReader(fis);
            int size = fis.available();
            char[] buffer = new char[size];
            reader.read(buffer);
            reader.close();
            text = new String(buffer);
            state = State.LOADED;
        }catch(Exception e){
            e.getStackTrace();
        }
        return text;
    }

    private class phpdown extends AsyncTask<String, Integer, String> {

        String container = "";
        String version="";


        //String jsonStr = "0";
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder jsonHtml = new StringBuilder();

            try{
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                if(conn != null){
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                        for(;;){
                            String line = br.readLine();
                            if(line == null) break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            }catch(Exception e){
                //Toast.makeText(, "서버 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return jsonHtml.toString();
        }

        protected void onPostExecute(String str){
            int id;
            double latitude;
            double longitude;
            double altitude;
            Log.e("test","onPostExecute: " + str);

            String preVersion = "";

            if(str.contains("latitude")) {
                String jsonString;
                //jsonStr = str;

                //mDataView.setText(container);
                File loadDbPath = new File(dbpath + "/trapdb.txt");

                if (isNewVersion(mVersion, loadDbPath.getAbsolutePath())) {
                    try {
                        JSONObject root = new JSONObject(str);
                        JSONArray ja = root.getJSONArray("result");
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jo = ja.getJSONObject(i);
                            id = jo.getInt("id");
                            latitude = jo.getDouble("latitude");
                            longitude = jo.getDouble("longitude");
                            altitude = jo.getDouble("altitude");
                            listitem.add(new Listitem(id, latitude, longitude,altitude));
                            container += id + "," + latitude + "," + longitude + "," + altitude +"\n";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mData = container;
                    Log.e("test", "Updatedata: " + mData);
                    SaveDataFile(mData, mVersion);
                    //Toast.makeText(, "데이터가 업데이트 되었습니다. 새로운 데이터를 불러옵니다.", Toast.LENGTH_LONG).show();
                    state = State.SAVED;
                    //mDataView.setText("version : "+mVersion+mData);

                }

            }
            else{
                mVersion = str;
                if(isNewVersion(mVersion,dbpath+dbFilePath)){
                    state = State.VERSION_CHECKED;
                }
                else
                    state = State.SAVED;

                Log.e("test","version: " + mVersion);

               // taskData = new phpdown();
                //taskData.execute(ADDR_LOADTRAP);
            }
        }
    }
}
