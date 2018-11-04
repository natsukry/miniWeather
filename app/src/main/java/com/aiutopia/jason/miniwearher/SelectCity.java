package com.aiutopia.jason.miniwearher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
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
    private TextView mSelectCityTextView;
    private EditText mSelectCityEditText;
    private SearchView sv;
    ListView mlistView;
    List cityNameList;
    List cityCodeList;
    private static final String TAG = "SelectCity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        // 从MyApplication中获取List<City>
        cityNameList = new ArrayList<>();
        cityCodeList = new ArrayList<>();
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



        mlistView = (ListView)findViewById(R.id.list_view);
        // TODO test start
//        mlistView.setTextFilterEnabled(true);//设置lv可以被过虑
//        sv = (SearchView) findViewById(R.id.sv);
//        // 设置该SearchView默认是否自动缩小为图标
//        sv.setIconifiedByDefault(false);
//        // 为该SearchView组件设置事件监听器
//        sv.setOnQueryTextListener(this);
//        // 设置该SearchView显示搜索按钮
//        sv.setSubmitButtonEnabled(true);
//        // 设置该SearchView内默认显示的提示文本
//        sv.setQueryHint("请输入城市名");
        // TODO test end
        // 将cityNameList中的城市名称用ListView显示
        activeListView(cityNameList, cityCodeList);


        mSelectCityTextView = (TextView)findViewById(R.id.select_city_text_view);
        mSelectCityEditText = (EditText)findViewById(R.id.select_city_edit_text);

        mSelectCityEditText.addTextChangedListener(new TextWatcher() {
            CharSequence tempCharSeq;
            private int editStart ;
            private int editEnd ;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tempCharSeq = s;
                Log.d(TAG,"before text changed: "+s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSelectCityTextView.setText(s);
                Log.d(TAG, "on text changed: "+s);
                filterList(cityNameList, cityCodeList, s.toString());// TODO
            }

            @Override
            public void afterTextChanged(Editable s) {
                editStart = mSelectCityEditText.getSelectionStart();
                editEnd = mSelectCityEditText.getSelectionEnd();
                if (tempCharSeq.length() > 10) {
                    Toast.makeText(SelectCity.this, "你输⼊的字数已经超过了限制！", Toast.LENGTH_SHORT).show();
                    s.delete(editStart-1, editEnd);
                    int tempSelection = editStart;
                    mSelectCityEditText.setText(s);
                    mSelectCityEditText.setSelection(tempSelection);
                }
                Log.d("myapp","afterTextChanged:") ;
            }
        });
    }

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

    // TODO test start
    // 用户输入字符时激发该方法
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        Toast.makeText(SelectCity.this, "textChange--->" + newText, Toast.LENGTH_LONG).show();
//        if (TextUtils.isEmpty(newText)) {
//            // 清除ListView的过滤
//            mlistView.clearTextFilter();
//        } else {
//            // 使用用户输入的内容对ListView的列表项进行过滤
//            mlistView.setFilterText(newText);
//        }
//        return true;
//    }
//
//    // 单击搜索按钮时激发该方法
//    @Override
//    public boolean onQueryTextSubmit(String query) {
//        // 实际应用中应该在该方法内执行实际查询
//        // 此处仅使用Toast显示用户输入的查询内容
//
//        Toast.makeText(this, "您的选择是:" + query, Toast.LENGTH_SHORT).show();
//        return false;
//    }

    // TODO test end

    // 用构造好的cityNameList, cityCodeList adapt listview
    public void activeListView(final List cityNameList, final List cityCodeList) {
        ArrayAdapter<List<String>> adapter = new ArrayAdapter<>(SelectCity.this, android.R.layout.simple_list_item_1, cityNameList);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCityCode = (String) cityCodeList.get(position);
                String selectedCityName = (String) cityNameList.get(position);
                Toast.makeText(SelectCity.this, "选择城市: " + selectedCityName + ": " + selectedCityCode, Toast.LENGTH_LONG).show();
                // 构造消息并返回
                Intent i = new Intent();
                i.putExtra("cityCode", selectedCityCode);


                //TODO Test SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // 设置main_city_code为当前选择的city
                editor.putString("main_city_code", selectedCityCode);

                // 异步写入数据
                editor.apply();
                //


                setResult(RESULT_OK, i);
                // 结束当前activity
                finish();

            }
        });
    }

    // 用pattern检索cityname，构成新的list传入adapter，更新listview
    public void filterList(List originalCityNameList, List originalCityCodeList, String pattern){
        List newCityNameList = new ArrayList();
        List newCityCodeList = new ArrayList();
        for (int i = 0; i < originalCityCodeList.size(); i++ ){
            String cuCityname = (String) originalCityNameList.get(i);
            if (cuCityname.contains(pattern)){
                newCityNameList.add((String) originalCityNameList.get(i));
                newCityCodeList.add((String) originalCityCodeList.get(i));
            }
        }
        activeListView(newCityNameList,newCityCodeList);

    }

}
