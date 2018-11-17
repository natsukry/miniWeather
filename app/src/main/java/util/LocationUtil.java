package util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.aiutopia.jason.app.MyApplication;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 17/11/2018.
 */

public class LocationUtil {
    public static LocationClient mLocationClient;
    private static String locCityName = new String();
    public static Context appContext;
    public static MyApplication myApplication;

    public static void initLocationUtil(Context applicationContext, MyApplication application) {
        appContext = applicationContext;
        myApplication  = application;
        mLocationClient = new LocationClient(applicationContext);
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(applicationContext);
//        mapView = (MapView) findViewById(R.id.bmapView);
//        baiduMap = mapView.getMap();
//        baiduMap.setMyLocationEnabled(true);
//        positionText = (TextView) findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();
        requestLocation();
    }

    // only can be called after initLocationUtil()


    public static void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private static void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    public static class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
//            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
//            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
//            currentPosition.append("国家：").append(location.getCountry()).append("\n");
//            currentPosition.append("省：").append(location.getProvince()).append("\n");
//            currentPosition.append("市：").append(location.getCity()).append("\n");
//            currentPosition.append("市：").append(location.getCity());
            currentPosition.append(location.getCity());
//            currentPosition.append("区：").append(location.getDistrict()).append("\n");
//            currentPosition.append("街道：").append(location.getStreet()).append("\n");
//            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
//                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
//                currentPosition.append("网络");
            }
//            positionText.setText(currentPosition);

            locCityName = currentPosition.toString();
            locCityName = locCityName.substring(0, locCityName.length()-1);
            Log.d("LOC", locCityName);
//            if (location.getLocType() == BDLocation.TypeGpsLocation
//                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
//                navigateTo(location);
//            }
            mLocationClient.stop();

            SharedPreferences sharedPreferences = appContext.getSharedPreferences("config", appContext.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("loc_city_name", locCityName);
            String locCityCode = FindCityCodeByName.FindCityCodeByName(myApplication, locCityName);
            editor.putString("loc_city_code", locCityCode);
            editor.apply();
        }

    }
}
