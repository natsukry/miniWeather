package com.aiutopia.jason.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.aiutopia.jason.bean.City;
import com.aiutopia.jason.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 17/10/2018.
 *
 */
// Application : Base class for those who need to maintain global application state.
public class MyApplication extends Application{

    private static final String TAG = "MyAPP";
    private static MyApplication mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MyApplication->onCreate");
        mApplication = this;
        mCityDB = openCityDB();
        initCityList();
    }

    public void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    public boolean prepareCityList(){
        mCityList = mCityDB.getAllCity();
        int i = 0;
        for (City city:mCityList){
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i+"+i);
        return true;
    }

    public List<City> getCityList() {
        return  mCityList;
    }

    public static MyApplication getInstance() {
        return  mApplication;
    }

    private CityDB openCityDB() {
        // 从原始数据库city.db读取数据复制到新的databases1
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath ()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

}
