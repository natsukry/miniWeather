package com.aiutopia.jason.miniwearher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.ListView;
import android.widget.Toast;

import com.aiutopia.jason.app.MyApplication;
import com.aiutopia.jason.bean.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 17/10/2018.
 */

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        // 从MyApplication中获取List<City>
        final List cityNameList = new ArrayList<>();
        final List cityCodeList = new ArrayList<>();
        // getApplication()获取到Application类
        List<City> mCityList = ((MyApplication)getApplication()).getCityList();
        for (City city:mCityList){
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            cityNameList.add(cityName);
            cityCodeList.add(cityCode);
            //Log.d(TAG,cityCode+":"+cityName);
        }
        //Log.d(TAG,"i+"+i);
        //


//        //String[] data = {"a","b"};
//        List cityList = new ArrayList<>();
//        cityList.add("深圳");
//        cityList.add("上海");
//        final List cityCodeList = new ArrayList<>();
//        cityCodeList.add("101280601");
//        cityCodeList.add("101020100");

        ListView mlistView = (ListView)findViewById(R.id.list_view);
        // 将cityNameList中的城市名称用ListView显示
        ArrayAdapter<List<String>> adapter = new ArrayAdapter<>(SelectCity.this, android.R.layout.simple_list_item_1, cityNameList);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCityCode = (String)cityCodeList.get(position);
                String selectedCityName = (String)cityNameList.get(position);
                Toast.makeText(SelectCity.this, "选择城市: "+selectedCityName+": "+selectedCityCode,Toast.LENGTH_LONG).show();
                // 构造消息并返回
                Intent i = new Intent();
                i.putExtra("cityCode",selectedCityCode);


                //TODO Test SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // 设置main_city_code为当前选择的city
                editor.putString("main_city_code",selectedCityCode);

                // 异步写入数据
                editor.apply();
                //


                setResult(RESULT_OK, i);
                // 结束当前activity
                finish();

            }
        });
    }



//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.title_back:
//                Intent i = new Intent();
//                i.putExtra("cityCode", "101160101");
//                setResult(RESULT_OK, i);
//                finish();
//                break;
//            default:
//                break;
//        }
//    }

    // 点击返回，结束当前activity
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_back:
                finish();
                break;
            default:
                break;
        }
    }

}
