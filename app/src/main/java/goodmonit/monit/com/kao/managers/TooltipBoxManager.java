package goodmonit.monit.com.kao.managers;

import android.content.Context;

public class TooltipBoxManager {
    private Context mContext;

    public TooltipBoxManager(Context context) {
        mContext = context;
    }

    public void initPreferences() {
        PreferenceManager prefManager = PreferenceManager.getInstance(mContext);
        sm stringMgr = new sm();
        prefManager.setDoNotShowTooltipBox(stringMgr.getParameter(2000), false);
        prefManager.setDoNotShowTooltipBox(stringMgr.getParameter(2001), false);
        prefManager.setDoNotShowTooltipBox(stringMgr.getParameter(2002), false);
    }
}
