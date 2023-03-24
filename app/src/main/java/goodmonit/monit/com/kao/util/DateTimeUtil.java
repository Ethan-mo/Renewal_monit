package goodmonit.monit.com.kao.util;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import goodmonit.monit.com.kao.R;

public class DateTimeUtil {
    public static final long ONE_HOUR_MILLIS	= 60 * 60 * 1000L;
    public static final long ONE_DAY_MILLIS		= ONE_HOUR_MILLIS * 24;
    public static final long ONE_WEEK_MILLIS	= ONE_DAY_MILLIS * 7;
    public static final long ONE_MONTH_MILLIS	= ONE_DAY_MILLIS * 30;

    public static final int TYPE_COLON		= 1;
    public static final int TYPE_CHARACTER	= 2;

    public static final int DATE_NOTATION_TYPE_YYMMDD	= 1;
    public static final int DATE_NOTATION_TYPE_MMDDYY	= 2;
    public static final int DATE_NOTATION_TYPE_DDMMYY	= 3;

    public static int HOUR_AM = 0;
    public static int HOUR_PM = 1;

    public static Context mContext;

    public static void setContext(Context context) {
        if (mContext == null) {
            mContext = context;
        }
    }

    public static String getFilenameDateTime(long time) {
        return DateFormat.format("yyMMddHHmmss", time).toString();
    }

    public static String getDebuggingDateTimeString(long time) {
        return DateFormat.format("yyyy-MM-dd HH:mm:ss", time).toString();
    }

    public static String getDateStringForServer(long time) {
        return DateFormat.format("yyyyMMdd", time).toString();
    }

    public static String getDateStringForServer(String dateString, String locale) {
        if (dateString == null || dateString.length() < 8 || locale == null) {
            return "19000101";
        }
        String date = "";
        String separator = "-";
        if ("ko".equals(locale)) {
            date = dateString.substring(0, 4) + separator + dateString.substring(4, 6) + separator + dateString.substring(6, 8);
        } else {
            date = dateString.substring(6, 8) + ", " + dateString.substring(4, 6) + ", " + dateString.substring(0, 4);
        }
        return date;
    }

    public static String getDateStringYYYYMM(String dateString, String locale) {
        if (dateString == null || dateString.length() < 6 || locale == null) {
            return "198001";
        }
        String date = "";
        String strMonth = dateString.substring(4, 6);
        String strYear = dateString.substring(0, 4);
        int month = Integer.parseInt(strMonth);

        if ("en".equals(locale)) {
            date = getMonthString(month) + ", " + strYear;
        } else {
            date = strYear + ", " + getMonthString(month);
        }
        return date;
    }

    public static String getMonthString(int month) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, month - 1);
        return month_date.format(cal.getTime());
    }

    public static String getNotConvertedDateTime(long timeMs) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timeMs));
    }

    public static long getDateLongFromServer(String date) {
        long dateMs = 0;
        if (date.length() < 8) {
            return 0;
        }

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);

        dateMs = calendar.getTimeInMillis();

        return dateMs;
    }

    public static String convertLocalToUTCTime(String HHmm) {
        String utcTime;
        try {
            Date localTime = new Date();
            localTime.setHours(Integer.parseInt(HHmm.substring(0, 2)));
            localTime.setMinutes(Integer.parseInt(HHmm.substring(2, 4)));
            long utcTimeMs = DateTimeUtil.convertLocalToUTCTimeMs(localTime.getTime());
            localTime.setTime(utcTimeMs);
            int convertedHour = localTime.getHours();
            int convertedMinute = localTime.getMinutes();
            utcTime = String.format(Locale.getDefault(), "%02d%02d", convertedHour, convertedMinute);
        } catch (Exception e) {
            return HHmm;
        }
        return utcTime;
    }

    public static long convertLocalToUTCTimeMs(long localTimeMs) {
        TimeZone z = TimeZone.getDefault();
        int offset = z.getOffset(localTimeMs);
        return localTimeMs - offset;
    }

    public static long convertUTCToLocalTimeMs(long utcTimeMs) {
        TimeZone z = TimeZone.getDefault();
        int offset = z.getOffset(utcTimeMs);
        return utcTimeMs + offset;
    }

    public static String getUtcDateTimeStringFromUtcTimestamp(long utcTimeMs) {
        if (utcTimeMs == 0) return "180101-000000";
        return DateFormat.format("yyMMdd-HHmmss", convertLocalToUTCTimeMs(utcTimeMs)).toString();
    }

    public static String getLocalDateTimeStringFromUtcTimestamp(long utcTimeMs) {
        if (utcTimeMs == 0) return "180101-000000";
        return DateFormat.format("yyMMdd-HHmmss", utcTimeMs).toString();
    }

    public static String getLocalTimeStringFromUtcTimestamp(long utcTimeMs) {
        if (utcTimeMs == 0) return "00:00";
        return DateFormat.format("a hh:mm", utcTimeMs).toString();
    }

    public static String getLocalDateTimeStringFromLocalTimestamp(long localTimeMs) {
        if (localTimeMs == 0) return "180101-000000";
        return DateFormat.format("yyMMdd-HHmmss", convertLocalToUTCTimeMs(localTimeMs)).toString();
    }

    public static String getUtcDateTimeStringFromLocalTimestamp(long localTimeMs) {
        if (localTimeMs == 0) return "180101-000000";
        return DateFormat.format("yyMMdd-HHmmss", convertLocalToUTCTimeMs(convertLocalToUTCTimeMs(localTimeMs))).toString();
    }

    public static String getLocalDateTimeStringFromUTCDateTimeString(String utcFormat, String localFormat, String utcDateTimeString) {
        String localDateTimeString = null;
        SimpleDateFormat utcDateTimeFormat = new SimpleDateFormat(utcFormat);
        SimpleDateFormat localDateTimeFormat = new SimpleDateFormat(localFormat);
        try {
            Date dateUtcTime = utcDateTimeFormat.parse(utcDateTimeString);
            long longUtcTime = dateUtcTime.getTime();
            TimeZone zone = TimeZone.getDefault();
            int offset = zone.getOffset(longUtcTime);
            long longLocalTime = longUtcTime + offset;
            Date dateLocalTime = new Date();
            dateLocalTime.setTime(longLocalTime);
            localDateTimeString = localDateTimeFormat.format(dateLocalTime);
        } catch (ParseException e) {
            e.printStackTrace();
            localDateTimeString = null;
        }
        return localDateTimeString;
    }

    public static String getDebuggingTime(long time) {
        return DateFormat.format("HH:mm:ss", time).toString();
    }

    public static String getStringElapsedTime(int separator, long time) {
        return getStringElapsedTimeBetween(separator, time, System.currentTimeMillis(), true);
    }

    public static String getStringElapsedTime(int separator, long time, boolean showSecond) {
        return getStringElapsedTimeBetween(separator, time, System.currentTimeMillis(), showSecond);
    }

    public static String getStringElapsedTimeBetween(int separator, long startTime, long lastTime) {
        return getStringElapsedTimeBetween(separator, lastTime, startTime, true);
    }

    public static String getStringElapsedTimeBetween(int separator, long startTime, long lastTime, boolean showSecond) {
        long diffTime = lastTime - startTime;
        String str = "";

        if (mContext == null) {
            separator = TYPE_COLON;
        }

        long day = diffTime / (1000 * 60 * 60 * 24);
        long hour = diffTime / (1000 * 60 * 60);
        long minute = (diffTime / (1000 * 60)) % 60;
        long second = (diffTime / (1000)) % 60;

        if (day > 0) {
            if (mContext != null) {
                return day + mContext.getString(R.string.time_elapsed_day);
            } else {
                return day + "days";
            }
        }

        if (hour > 0) {
            str += hour + "%";
        }

        if (minute < 10) {
            str += "0";
        }
        str += minute + "^";

        if (showSecond) {
            if (second < 10) {
                str += "0";
            }
            str += second + "&";
        }

        if (separator == TYPE_COLON) {
            str = str.replace("$", ":");
            str = str.replace("%", ":");
            str = str.replace("^", ":");
            str = str.replace("&", "");
        } else {
            str = str.replace("$", mContext.getString(R.string.time_elapsed_day));
            str = str.replace("%", mContext.getString(R.string.time_elapsed_hour));
            str = str.replace("^", mContext.getString(R.string.time_elapsed_minute));
            str = str.replace("&", mContext.getString(R.string.time_elapsed_second));
        }

        return str;
    }

    public static String getStringSpecificTimeInDay(int separator, long time) {
        String str = "";

        if (mContext == null) {
            separator = TYPE_COLON;
        }

        switch (separator) {
            case TYPE_COLON:
                str = DateFormat.format("a hh:mm", time).toString();
                break;
            case TYPE_CHARACTER:
                if (mContext != null) {
                    str = DateFormat.format("a hh", time).toString() + mContext.getString(R.string.time_hour);
                    str += DateFormat.format("mm", time).toString() + mContext.getString(R.string.time_minute);
                } else {
                    str = DateFormat.format("a hh", time).toString() + "Hour";
                    str += DateFormat.format("mm", time).toString() + "Minute";
                }
            default:
                str = DateFormat.format("a hh:mm", time).toString();
                break;
        }
        return str;
    }

    public static String getStringTimeFromSeconds(int separator, long seconds) {
        String str = "";

        if (mContext == null) {
            separator = TYPE_COLON;
        }

        boolean valid = false;
        long day = seconds / (60 * 60 * 24);
        long hour = seconds / (60 * 60) % 24;
        long minute = (seconds / 60) % 60;
        long second = seconds % 60;

        if (day > 0) {
            valid = true;
            str += day + "$";
        }

        if (hour > 0 || valid) {
            valid = true;
            str += hour + "%";
        }

        if (minute > 0 || valid) {
            valid = true;
            str += minute + "^";
        }

        if (str.length() == 0) {
            str += second + "&";
        }

        if (separator == TYPE_COLON) {
            str = str.replace("$", ":");
            str = str.replace("%", ":");
            str = str.replace("^", ":");
            str = str.replace("&", "");
        } else {
            str = str.replace("$", mContext.getString(R.string.time_elapsed_day));
            str = str.replace("%", mContext.getString(R.string.time_elapsed_hour));
            str = str.replace("^", mContext.getString(R.string.time_elapsed_minute));
            str = str.replace("&", mContext.getString(R.string.time_elapsed_second));
            //str = str.replace("$", "d");
            //str = str.replace("%", "h");
            //str = str.replace("^", "m");
            //str = str.replace("&", "s");
        }

        return str;
    }

    public static String getStringElapsedDayFromNow(long lastTime) {
        return getStringElapsedDay(lastTime, System.currentTimeMillis());
    }

    public static String getStringElapsedDay(long from, long to) {
        long diffTime = to - from;
        return (diffTime / (24 * 60 * 60 * 1000) + 1) + "";
    }

    public static String getDateString(long time, String locale) {
        String format;

        switch(DateTimeUtil.getDateNotationType(locale)) {
            case DateTimeUtil.DATE_NOTATION_TYPE_YYMMDD:
                format = "yyyy-MM-dd";
                break;
            case DateTimeUtil.DATE_NOTATION_TYPE_MMDDYY:
                format = "MMM, dd, yyyy";
                break;
            case DateTimeUtil.DATE_NOTATION_TYPE_DDMMYY:
                format = "dd, MMM, yyyy";
                break;
            default:
                format = "yyyy-MM-dd";
                break;
        }

        if (time == 0) {
            return DateFormat.format(format, System.currentTimeMillis()).toString();
        } else {
            return DateFormat.format(format, time).toString();
        }
    }

    public static String getDateStringWithDay(long time, String locale) {
        String format;

        switch(DateTimeUtil.getDateNotationType(locale)) {
            case DateTimeUtil.DATE_NOTATION_TYPE_YYMMDD:
                format = "yyyy-MM-dd(E)";
                break;
            case DateTimeUtil.DATE_NOTATION_TYPE_MMDDYY:
                format = "MMM, dd(E), yyyy";
                break;
            case DateTimeUtil.DATE_NOTATION_TYPE_DDMMYY:
                format = "dd(E), MMM, yyyy";
                break;
            default:
                format = "yyyy-MM-dd(E)";
                break;
        }

        if (time == 0) {
            return DateFormat.format(format, System.currentTimeMillis()).toString();
        } else {
            return DateFormat.format(format, time).toString();
        }
    }

    public static String getDateTimeStringWithDay(long time, String locale) {
        String format;

        switch(DateTimeUtil.getDateNotationType(locale)) {
            case DateTimeUtil.DATE_NOTATION_TYPE_YYMMDD:
                format = "yyyy-MM-dd(E), a hh:mm";
                break;
            case DateTimeUtil.DATE_NOTATION_TYPE_MMDDYY:
                format = "MMM, dd(E), yyyy, hh:mm a";
                break;
            case DateTimeUtil.DATE_NOTATION_TYPE_DDMMYY:
                format = "dd(E), MMM, yyyy, hh:mm a";
                break;
            default:
                format = "yyyy-MM-dd(E), a hh:mm";
                break;
        }

        if (time == 0) {
            return DateFormat.format(format, System.currentTimeMillis()).toString();
        } else {
            return DateFormat.format(format, time).toString();
        }
    }

    public static String getDateTimeString(long utcTimeMs, String region) {
        String format;
        if ("ko".equals(region)){
            format = "M월 d일 a hh:mm:ss";
        } else {
            format = "dd, MMM, hh:mm:ss a";
        }

        if (utcTimeMs == 0) {
            return DateFormat.format(format, System.currentTimeMillis()).toString();
        } else {
            return DateFormat.format(format, utcTimeMs).toString();
        }
    }

    public static long[] getCurrentDayFromToTime() {
        return getSpecificDayFromToTime(System.currentTimeMillis());
    }

    public static long[] getSpecificDayFromToTime(long now) {
        long[] time = new long[2];
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long from = calendar.getTimeInMillis();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, day + 1);
        long to = calendar.getTimeInMillis() - 1;
        time[0] = from;
        time[1] = to;
        return time;
    }

    public static long[] getPrevDayFromToTime(long now) {
        long[] time = getSpecificDayFromToTime(now);
        time[0] = time[0] - (1000 * 60 * 60 * 24); // from
        time[1] = time[1] - (1000 * 60 * 60 * 24); // to
        return time;
    }

    public static long[] getNextDayFromToTime(long now) {
        long[] time = getSpecificDayFromToTime(now);
        time[0] = time[0] + (1000 * 60 * 60 * 24); 	// from
        time[1] = time[1] + (1000 * 60 * 60 * 24); 	// to
        return time;
    }

    public static long[] getCurrentMonthFromToTime() {
        return getSpecificMonthFromToTime(System.currentTimeMillis());
    }

    public static long[] getSpecificMonthFromToTime(long now) {
        long[] time = new long[2];
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long from = calendar.getTimeInMillis();

        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        long to = calendar.getTimeInMillis();

        time[0] = from;
        time[1] = to;
        return time;
    }

    // Local기준의 Day시작 MilliSec
    public static long getDayBeginMillis(long localTimeMs) {
        return localTimeMs - localTimeMs % ONE_DAY_MILLIS;
    }

    public static long getDayEndMillis(long localTimeMs) {
        return getDayBeginMillis(localTimeMs) + ONE_DAY_MILLIS - 1;
    }

    public static long getWeekBeginMillis(long thisWeekLastMs) {
        return thisWeekLastMs - ONE_WEEK_MILLIS + 1;
    }

    public static long getWeekLastMillis(long thisWeekBeginMs) {
        return thisWeekBeginMs + ONE_WEEK_MILLIS - 1;
    }

    public static long getMonthBeginMillis(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getMonthLastMillis(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() - 1;
    }

    public static int[] getWeekDays(long thisWeekLastTime) {
        long beginWeekMillis = getWeekBeginMillis(thisWeekLastTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(beginWeekMillis);

        int[] days = new int[7];
        for (int i = 0; i < 7; i++) {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            days[i] = day;
            calendar.set(Calendar.DAY_OF_MONTH, day + 1);
        }
        return days;
    }

    public static int[] getMonthDays(long thisMonthLastTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(thisMonthLastTime);
        int monthLastDay = calendar.get(Calendar.DAY_OF_MONTH);

        int[] days = new int[monthLastDay];
        for (int i = 0; i < monthLastDay; i++) {
            days[i] = i + 1;
        }
        return days;
    }

    public static long[] getPrevWeekBeginLastMs(long thisWeekBeginMs) {
        long[] weekMs = new long[2];
        long weekLastMs = thisWeekBeginMs - 1;
        long weekBeginMs = getWeekBeginMillis(weekLastMs);
        weekMs[0] = weekBeginMs;
        weekMs[1] = weekLastMs;
        return weekMs;
    }

    public static long[] getNextWeekBeginLastMs(long thisWeekLastMs) {
        long[] weekMs = new long[2];
        long weekBeginMs = thisWeekLastMs + 1;
        long weekLastMs = getWeekLastMillis(weekBeginMs);
        weekMs[0] = weekBeginMs;
        weekMs[1] = weekLastMs;
        return weekMs;
    }

    public static long[] getPrevMonthBeginLastMs(long time) {
        long[] monthMs = new long[2];
        long monthLastMs = getMonthBeginMillis(time) - 1;
        long monthBeginMs = getMonthBeginMillis(monthLastMs);
        monthMs[0] = monthBeginMs;
        monthMs[1] = monthLastMs;
        return monthMs;
    }

    public static long[] getNextMonthBeginLastMs(long time) {
        long[] monthMs = new long[2];
        long monthBeginMs = getMonthLastMillis(time) + 1;
        long monthLastMs = getMonthLastMillis(monthBeginMs);
        monthMs[0] = monthBeginMs;
        monthMs[1] = monthLastMs;
        return monthMs;
    }

    public static long getUtcTimeStampFromLocalString(String strDate, String strType) {

        Date selectedDate = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat(strType);
            selectedDate = format.parse(strDate);
        } catch (ParseException e) {
            selectedDate = new Date();
        }
        return selectedDate.getTime();
    }

    public static int getDateNotationType(String locale) {
        /*
        1. 연도-월-일 순으로 표기하는 국가들 (예) 2008년 6월 28일 = 2008-06-28
           한국, 중국, 대만, 마카오, 일본, 몽골, 중동, 네팔, 덴마크, 헝가리, 라트비아, 리투아니아, 스웨덴,남아프리카 공화국, 캐나다(다른 표기방식들도 사용)

        2. 월-일-연도 순으로 표기하는 국가들 (예) 2008년 6월 28일 = 06/28/08
           미국, 캐나다(단, 대부분의 공식 문서에는 연도-월-일로 표기), 미크로네시아 연방공화국, 팔라우, 필리핀

        3. 일-월-연도 순으로 표기하는 국가들 (예) 2008년 6월 28일 = 28/06/2008
           가이아나, 그레나다, 그루지야, 그리스, 네덜란드, 노르웨이, 뉴질랜드, 덴마크, 도미니카 공화국, 도미니카 연방, 독일, 라트비아, 러시아, 리비아, 마카오, 말레이시아, 멕스코, 몬테네그로, 바베이도스, 방글라데시, 벨기에, 베네주엘라, 베트남, 벨로루시,벨리즈, 볼리비아, 불가리아, 브라질, 사우디 아라비아 (주요 기업들의 경우 미국 방식으로 월/일/연도 표기를 따름), 세르비아, 세인트 빈센트 그레나딘, 세인트 루시아, 세인트 키츠 네비스, 스페인, 스위스, 스웨덴(연도-월-일 방식이 더 자주 쓰임), 슬로바키아, 슬로베니아, 싱가포르, 아르메니아, 아르헨티나, 아일랜드, 알바니아, 아이슬란드, 아제르바이잔, 영국, 에스토니아, 에콰도르, 엘 살바도르, 오스트리아, 우크라이나,우루과이, 우즈베키스탄, 요르단, 이라크, 이란, 이스라엘, 이집트, 이탈리아, 인도, 인도네시아, 자메이카, 체코, 칠레, 카자흐스탄, 캐나다(다른 표기방식들도 사용), 케냐, 콜롬비아, 크로아티아, 키르기즈스탄, 키프로스, 타지키스탄, 태국, 트리니다드 토바고 공화국, 터키, 투르키메니스탄, 파나마, 파라과이, 파키스탄, 페루, 포르투갈, 폴란드,푸에토리코, 프랑스, 핀란드, 필리핀, 호주, 홍콩
        */
        if ("ko".equals(locale)) {
            return DATE_NOTATION_TYPE_YYMMDD;
        } else if ("jp".equals(locale)) {
            return DATE_NOTATION_TYPE_YYMMDD;
        } else if ("zh".equals(locale)) {
            return DATE_NOTATION_TYPE_YYMMDD;
        } else if ("en".equals(locale)) {
            return DATE_NOTATION_TYPE_MMDDYY;
        } else {
            return DATE_NOTATION_TYPE_DDMMYY;
        }
    }

    public static String getElapsedTimeString(Context context, int minutes) {
        int hour = minutes / 60;
        minutes = minutes % 60;
        String timeString = "";

        if (hour == 0) {
            timeString = minutes + " " + context.getString(R.string.time_elapsed_minute);
        } else {
//            if ("ko".equals(Locale.getDefault().getLanguage())) {
                timeString = hour + " " + context.getString(R.string.time_elapsed_hour) + " " + minutes + " " + context.getString(R.string.time_elapsed_minute);
//            } else {
//                timeString = hour + " " + context.getString(R.string.time_hour_short) + " " + minutes + " " + context.getString(R.string.time_minute_short);
//            }
        }
        return timeString;
    }
}
