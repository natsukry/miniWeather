package util;

import com.aiutopia.jason.bean.Weather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jason on 15/11/2018.
 */

public class FarecastWeatherXmlParser {
    public static void processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
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
                        }else if ("fengxiang".equals(eltName)) {
                            currentPlayer.fengxiang = parser.nextText();
                        }
                    }
                    break;
            }

            eventType = parser.next();
        }

        for (Weather w : ws)
            Weather.printInfo(w);
    }

}
