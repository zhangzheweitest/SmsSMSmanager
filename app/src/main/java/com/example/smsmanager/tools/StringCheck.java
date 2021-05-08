package com.example.smsmanager.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串校验工具类
 */
public class StringCheck {
    /**
     * 正则方法手机号号段校验，
        第1位：1；
        第2位：{3、4、5、6、7、8}任意数字；
        第3—11位：0—9任意数字
     * @param value 被检查字符串
     * @return
     * */
    public static boolean isTelPhoneNumber(String value) {
        if (value != null && value.length() == 11)
        {
            Pattern pattern = Pattern.compile("^1[3|4|5|6|7|8][0-9]\\d{8}$");
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        }
        return false;
    }
}
