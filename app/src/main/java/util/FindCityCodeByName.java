package util;

import com.aiutopia.jason.app.MyApplication;
import com.aiutopia.jason.bean.City;

import java.util.List;

/**
 * Created by Jason on 18/11/2018.
 */

public class FindCityCodeByName {
    public static String FindCityCodeByName(MyApplication myApplication, String cityName){
        List<City> mCityList = (myApplication).getCityList();
        for (int i = 0; i < mCityList.size(); i++){
            if (mCityList.get(i).getCity().equals(cityName)){
                return mCityList.get(i).getNumber();
            }
        }
        return "----";
    }
}
