package com.braincraftapps.videotimeline;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Util {
    public final static long ONE_HOUR_IN_US = 3600000000L;

    public static String convertTimeFromUsToHhMmSs(long timeInUs) {
        if (timeInUs < ONE_HOUR_IN_US) {
            return String.format(Locale.getDefault(),"%02d:%02d",
                    TimeUnit.MICROSECONDS.toMinutes(timeInUs) - TimeUnit.HOURS.toMinutes(TimeUnit.MICROSECONDS.toHours(timeInUs)),
                    TimeUnit.MICROSECONDS.toSeconds(timeInUs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(timeInUs)));
        } else {
            return String.format(Locale.getDefault(),"%02d:%02d:%02d", TimeUnit.MICROSECONDS.toHours(timeInUs),
                    TimeUnit.MICROSECONDS.toMinutes(timeInUs) - TimeUnit.HOURS.toMinutes(TimeUnit.MICROSECONDS.toHours(timeInUs)),
                    TimeUnit.MICROSECONDS.toSeconds(timeInUs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(timeInUs)));
        }
    }
}
