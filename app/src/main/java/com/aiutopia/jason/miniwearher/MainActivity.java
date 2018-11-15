package com.aiutopia.jason.miniwearher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import com.aiutopia.jason.bean.TodayWeather;
import com.aiutopia.jason.bean.Weather;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import util.MyPagerAdapter;
import util.NetUtil;
import util.WeatherUtil;

/**
 * Created by Jason on 19/09/2018.
 * TODO 更新天气，污染状况图片
 */

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_FORECAST_WEATHER = 2;
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private ViewPager vpager_one;


    private ArrayList<View> aList;
    private MyPagerAdapter mAdapter;

    public LocationClient mLocationClient;

    private TextView positionText;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate = true;

    // Each Handler instance is associated with a single thread and that thread's message queue.
    private Handler mHandler = new Handler() {
        @Override
        // acquire message from queryWeather
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                case UPDATE_FORECAST_WEATHER:
                    // TODO
                    Log.d("GEEEEEEEEEEET", ((ArrayList<Weather>) msg.obj).toString());
                    updateForecastWeather((ArrayList<Weather>) msg.obj);
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        // must-having before calling onClick method
        mUpdateBtn.setOnClickListener(this);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            // 有网络时自动更新
            Log.d("myWeather", "网络OK");

            //Toast.makeText(MainActivity.this, "网络OK！", Toast.LENGTH_LONG).show();
        } else {
            // 无网络时显示"N/A"
            Log.d("myWeather", "网络挂了");

            // Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
        }
        initViewByNA();
        // TODO Update info when launch app
        // TODO TEST 启动时自动更新天气
        // Retrieve and hold the contents of the preferences file 'name', returning
        // a SharedPreferences through which you can retrieve and modify its values.
        // name "config" Desired preferences file.
        // "MODE_PRIVATE" File creation mode: the default mode, where the created file can only be
        // accessed by the calling application (or all applications sharing the same user ID)
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        // Retrieve a String value from the preferences. return the 2nd param if the preference
        // does not exist
        String cityCode = sharedPreferences.getString("main_city_code", "101010100");
        queryWeather(cityCode);

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

//        initGetLoc();
        initViewPager();
        // test XML parser
        WeatherUtil.queryWeather("101010100", mHandler);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_update_btn) {


            // add sharedPreference
            //start
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            // TODO 用sharedPreferences存储天气信息，在未刷新前展示
            //Log.d("myweather", cityCode);
            //end
            //String cityCode = "101010100";
            Log.d("myweather", cityCode);
            queryWeather(cityCode);
        }

        if (view.getId() == R.id.title_city_manager) {
            Intent i = new Intent(this, SelectCity.class);
            startActivityForResult(i, 1);
        }
    }

    // 监听startActivityForResult(i, 1)的activity返回
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取选择城市返回的 city code
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", " 选择的城市代码为： " + newCityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeather(newCityCode);
            } else {
                Log.d("myWeather", "网络异常");
                Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_LONG).show();
            }
        }


    }

    private void queryWeather(String cityCode) {
        final String adress = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWearher", adress);

        // 创建子进程
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    // Creates a new URL instance
                    URL url = new URL(adress);
                    // Returns a new connection to the resource referred to by this URL.
                    // 返回为URLConnection对象，类型转换为HttpURLConnection
                    con = (HttpURLConnection) url.openConnection();
                    // Sets the request command which will be sent to the remote HTTP server.
                    con.setRequestMethod("GET");
                    // Sets the maximum time in milliseconds to wait while connecting.
                    con.setConnectTimeout(8000);
                    // Sets the maximum time to wait for an input stream read to complete before giving up.
                    con.setReadTimeout(8000);
                    // returns an {@code InputStream} for reading data from the resource pointed by
                    // this {@code URLConnection}.
                    InputStream in = con.getInputStream();
                    // Constructs a new {@code BufferedReader}
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);


                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                    }

                    // 返回消息给主进程，由Handler mHandler下一步处理
                    Message msg = new Message();
                    //the recipient can identify what this message is about.
                    msg.what = UPDATE_TODAY_WEATHER;
                    // An arbitrary object to send to the recipient.
                    msg.obj = todayWeather;
                    // Pushes a message onto the end of the message queue after all pending messages
                    // before the current time. It will be received in {@link #handleMessage},
                    mHandler.sendMessage(msg);


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }


    // 解析xml数据并存储在TodayWeather Javabean
    private TodayWeather parseXML(String xmldata) {

        TodayWeather todayWeather = null;

        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            // Creates a new instance of a PullParserFactory that can be used to create XML pull parsers.
            XmlPullParser xmlPullParser = fac.newPullParser();
            // Construct a new {@code StringReader} with {@code str} as source.
            // Set the input source for parser to the given reader and resets the parser.
            xmlPullParser.setInput(new StringReader(xmldata));
            // Returns the type of the current event (START_TAG, END_TAG, TEXT, etc.)
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 当前位置是文档开始
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 当前位置是文档标签开始
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                // Get next parsing event
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                Log.d("myWeather", "city:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                Log.d("myWeather", "updatetime:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                Log.d("myWeather", "shidu:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                Log.d("myWeather", "wendu:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                Log.d("myWeather", "pm25:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                Log.d("myWeather", "quality:    " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                Log.d("myWeather", "fengxiang:    " + xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                Log.d("myWeather", "fengli:    " + xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                Log.d("myWeather", "date:    " + xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                Log.d("myWeather", "high:    " + xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                Log.d("myWeather", "low:    " + xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                Log.d("myWeather", "type:    " + xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }


    // 用"N/A"初始化Views
    void initViewByNA() {
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");


    }


    // use .setText() to set data in views
    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText("更新于今天 " + todayWeather.getUpdatetime());
        humidityTv.setText("湿度" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());
        WeatherUtil.setWeatherImg(todayWeather.getType(),weatherImg);
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();

    }

//    void initGetLoc(){
//        mLocationClient = new LocationClient(getApplicationContext());
//        mLocationClient.registerLocationListener(new MyLocationListener());
//        SDKInitializer.initialize(getApplicationContext());
//        mapView = (MapView) findViewById(R.id.bmapView);
//        baiduMap = mapView.getMap();
//        baiduMap.setMyLocationEnabled(true);
//        positionText = (TextView) findViewById(R.id.position_text_view);
//        List<String> permissionList = new ArrayList<>();
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        }
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.READ_PHONE_STATE);
//        }
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//        if (!permissionList.isEmpty()) {
//            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
//            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
//        } else {
//            requestLocation();
//        }
//    }

//    private void requestLocation() {
//        initLocation();
//        mLocationClient.start();
//    }
//
//    private void initLocation() {
//        LocationClientOption option = new LocationClientOption();
//        option.setScanSpan(5000);
//        option.setIsNeedAddress(true);
//        mLocationClient.setLocOption(option);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mLocationClient.stop();
//        mapView.onDestroy();
//        baiduMap.setMyLocationEnabled(false);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case 1:
//                if (grantResults.length > 0) {
//                    for (int result : grantResults) {
//                        if (result != PackageManager.PERMISSION_GRANTED) {
//                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
//                            finish();
//                            return;
//                        }
//                    }
//                    requestLocation();
//                } else {
//                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//                break;
//            default:
//        }
//    }
//
//    public class MyLocationListener implements BDLocationListener {
//
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//            StringBuilder currentPosition = new StringBuilder();
////            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
////            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
////            currentPosition.append("国家：").append(location.getCountry()).append("\n");
////            currentPosition.append("省：").append(location.getProvince()).append("\n");
////            currentPosition.append("市：").append(location.getCity()).append("\n");
//            currentPosition.append("市：").append(location.getCity());
////            currentPosition.append("区：").append(location.getDistrict()).append("\n");
////            currentPosition.append("街道：").append(location.getStreet()).append("\n");
////            currentPosition.append("定位方式：");
//            if (location.getLocType() == BDLocation.TypeGpsLocation) {
////                currentPosition.append("GPS");
//            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
////                currentPosition.append("网络");
//            }
//            positionText.setText(currentPosition);
////            if (location.getLocType() == BDLocation.TypeGpsLocation
////                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
////                navigateTo(location);
////            }
//        }
//    }

    void initViewPager() {
        vpager_one = (ViewPager) findViewById(R.id.vpager_one);

        aList = new ArrayList<View>();
        // 动态加载
        LayoutInflater li = getLayoutInflater();
        aList.add(li.inflate(R.layout.activity_vp1, null, false));
        aList.add(li.inflate(R.layout.activity_vp2, null, false));
        mAdapter = new MyPagerAdapter(aList);
        vpager_one.setAdapter(mAdapter);


    }

    void updateForecastWeather(ArrayList<Weather> ws) {
        ArrayList<ArrayList<View>> viewsLists = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            ArrayList<View> viewsList = new ArrayList<>();
            viewsList.add(findViewById(getResourceId("d" + (i + 1) + "_date")));
            viewsList.add(findViewById(getResourceId("d" + (i + 1) + "_weather_img")));
            viewsList.add(findViewById(getResourceId("d" + (i + 1) + "_temp_range")));
            viewsList.add(findViewById(getResourceId("d" + (i + 1) + "_weather_info")));
            viewsList.add(findViewById(getResourceId("d" + (i + 1) + "_wind")));
            viewsLists.add(viewsList);
        }

        for (int dIdx = 0; dIdx < 5; dIdx++) {
            ((TextView) viewsLists.get(dIdx).get(0)).setText(ws.get(dIdx).date);
            WeatherUtil.setWeatherImg(ws.get(dIdx).type,(ImageView)viewsLists.get(dIdx).get(1));
            ((TextView) viewsLists.get(dIdx).get(2)).setText(ws.get(dIdx).high.replace("高温","") + " ~ "+ws.get(dIdx).low.replace("低温",""));
            ((TextView) viewsLists.get(dIdx).get(3)).setText(ws.get(dIdx).type);
            ((TextView) viewsLists.get(dIdx).get(4)).setText(ws.get(dIdx).fengxiang);
        }

    }

    public int getResourceId(String name) {
        Resources resources = getResources();
        int id = resources.getIdentifier(name, "id", getPackageName());
        return id;
    }

}