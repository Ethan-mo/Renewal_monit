package goodmonit.monit.com.kao.provider;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.managers.WidgetManager;

public class WidgetProvider2x1 extends WidgetProvider {
    private static final String TAG = Configuration.BASE_TAG + "Widget2x1";
    private static final boolean DBG = Configuration.DBG;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int cntWidgetId = appWidgetIds.length;
        WidgetManager widgetMgr = new WidgetManager(context);
        for (int i = 0; i < cntWidgetId; i++) {
            int appWidgetId = appWidgetIds[i];
            if (DBG) Log.d(TAG, "onUpdate: " + appWidgetId);
            RemoteViews views = widgetMgr.buildViews(appWidgetId, 1, false);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
