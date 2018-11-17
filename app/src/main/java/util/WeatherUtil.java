package util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.aiutopia.jason.bean.Weather;
import com.aiutopia.jason.miniwearher.R;

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

/**
 * Created by Jason on 15/11/2018.
 */

public class WeatherUtil {
    private static final int UPDATE_FORECAST_WEATHER = 2;
    public static void queryWeather(String cityCode, final Handler mHandler) {
        final String adress = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWearher", adress);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
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


                    ArrayList<Weather> ws = parseXML(responseStr);

                    // 返回消息给主进程，由Handler mHandler下一步处理
                    Message msg =new Message();
                    //the recipient can identify what this message is about.
                    msg.what = UPDATE_FORECAST_WEATHER;
                    // An arbitrary object to send to the recipient.
                    msg.obj=ws;
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


    // 解析xml数据并存储在Weather Javabean
    public static ArrayList<Weather> parseXML(String xmldata) {

        ArrayList<Weather> ws = null;


        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            // Creates a new instance of a PullParserFactory that can be used to create XML pull parsers.
            XmlPullParser xmlPullParser = fac.newPullParser();
            // Construct a new {@code StringReader} with {@code str} as source.
            // Set the input source for parser to the given reader and resets the parser.
            xmlPullParser.setInput(new StringReader(xmldata));
            // Returns the type of the current event (START_TAG, END_TAG, TEXT, etc.)
            int eventType = xmlPullParser.getEventType();

            ws =  processParsing(xmlPullParser);

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ws;
    }

    private static ArrayList<Weather> processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<Weather> ws = new ArrayList<>();
        int eventType = parser.getEventType();
        Weather currentPlayer = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();

                    if ("weather".equals(eltName)) {
                        currentPlayer = new Weather();
                        ws.add(currentPlayer);
                    } else if (currentPlayer != null) {
                        if ("date".equals(eltName)) {
                            currentPlayer.date = parser.nextText();
                        } else if ("high".equals(eltName)) {
                            currentPlayer.high = parser.nextText();
                        } else if ("low".equals(eltName)) {
                            currentPlayer.low = parser.nextText();
                        } else if ("type".equals(eltName)) {
                            currentPlayer.type = parser.nextText();
                        } else if ("fengxiang".equals(eltName)) {
                            currentPlayer.fengxiang = parser.nextText();
                        }
                    }
                    break;
            }

            eventType = parser.next();
        }

        for (Weather w : ws)
            Weather.printInfo(w);
        return ws;
    }

    public static void setWeatherImg(String wInfo, ImageView iv){
        switch (wInfo){
            case  "晴":
                iv.setImageResource(R.drawable.sunny);
                break;
            case "阴":
                iv.setImageResource(R.drawable.overcast);
                break;
            case "多云":
                iv.setImageResource(R.drawable.cloudy);
                break;
            case "小雨":
                iv.setImageResource(R.drawable.lightrain);
                break;
            case "中雨":
                iv.setImageResource(R.drawable.zrain);
                break;
            case "大雨":
                iv.setImageResource(R.drawable.drain);
                break;
            case "小雪":
                iv.setImageResource(R.drawable.xsnow);
                break;
            case "中雪":
                iv.setImageResource(R.drawable.zsnow);
                break;
            case "大雪":
                iv.setImageResource(R.drawable.dsnow);
                break;
            default:
                iv.setImageResource(R.drawable.sunny);
                break;
        }
    }


}
