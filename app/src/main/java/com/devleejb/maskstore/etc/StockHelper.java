package com.devleejb.maskstore.etc;

import android.graphics.Color;

public class StockHelper {
    public static int stockToColor(String remain_stat) {
        if (remain_stat == null) {
            return Color.GRAY;
        }

        switch (remain_stat) {
            case "plenty":
                return Color.GREEN;
            case "some":
                return Color.YELLOW;
            case "few":
                return Color.RED;
            case "empty":
            case "break":
                return Color.GRAY;
            default:
                return 0;
        }
    }

    public static String stockToKor(String remain_stat) {
        if (remain_stat == null) {
            return "알 수 없음";
        }

        switch (remain_stat) {
            case "plenty":
                return "100개 이상";
                case "some":
                    return "30~99개";
            case "few":
                return "2개~29개";
            case "empty":
                return "0개~1개";
            case "break":
                return "판매 중지";
            default:
                return "알 수 없음";
        }
    }

    public static String typeToKor(String type) {
        if (type == null) {
            return "알 수 없음";
        }

        switch (type) {
            case "01":
                return "약국";
            case "02":
                return "우체국";
            case "03":
                return "농협";
            default:
                return "알 수 없음";
        }
    }

    public static String timeToKor(String time) {
        if (time == null) {
            return "알 수 없음";
        } else {
            return time;
        }
    }
}
