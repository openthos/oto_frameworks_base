package com.android.startupmenu.adapter;

import java.util.List;
import java.util.Map;

import com.android.startupmenu.R;
import com.android.startupmenu.util.AppInfo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;

import com.android.startupmenu.dialog.StartMenuDialog;
import com.android.startupmenu.StartupMenuActivity;
import com.android.startupmenu.util.StartupMenuSqliteOpenHelper;
import com.android.startupmenu.util.StartupMenuUtil;

import android.content.pm.ApplicationInfo;

public class StartupMenuAdapter extends BaseAdapter {
    public static final int START_MENU_RIGHT_MOUSE_UI_NUMBER = 57;
    public static String strPkgName;

    private List<AppInfo> mlistAppInfo = null;
    private Map<Integer, Boolean> isCheckedMap;
    LayoutInflater infater = null;
    private Context mContext;
    private StartupMenuActivity mStartupMenuActivity;
    private SQLiteDatabase mdb;
    private StartupMenuSqliteOpenHelper mMsoh;
    private int mStartMenuAppWidth;
    private int mStartMenuAppHeight;
    public static boolean mIsFullScreen;
    public static int mPositionItem;

    public StartupMenuAdapter(Context context, List<AppInfo> apps,
                              Map<Integer,Boolean> isCheckedMap) {
        mContext = context;
        mStartMenuAppWidth = mContext.getResources()
                                     .getDimensionPixelSize(R.dimen.start_menu_app_width);
        mStartMenuAppHeight = mContext.getResources()
                                      .getDimensionPixelSize(R.dimen.start_menu_app_height);
        infater = (LayoutInflater) context
                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistAppInfo = apps;
        this.isCheckedMap = isCheckedMap;
        mMsoh = new StartupMenuSqliteOpenHelper(mContext, "StartupMenu_database.db", null, 1);
        mdb = mMsoh.getWritableDatabase();
        mStartupMenuActivity = getStartupMenuActivity();
    }

    @Override
    public int getCount() {
        return mlistAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertview, ViewGroup arg2) {
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = infater.inflate(R.layout.activity_gridview, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (ViewHolder) convertview.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        String appName = appInfo.getAppLabel();
        holder.tvAppLabel.setText(appInfo.limitNameLength(appName, mContext, appInfo));
        view.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                int what = motionEvent.getButtonState();
                mStartupMenuActivity.setFocus(true);
                switch (what) {
                    case MotionEvent.BUTTON_PRIMARY:
                        String pkgName = mStartupMenuActivity.mListAppInfo
                                                                  .get(position).getPkgName();
                        Intent intent = mStartupMenuActivity.mListAppInfo.get(position).getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        openAppBroadcast(mContext);
                    /*
                    Cursor c = mdb.rawQuery("select * from " + TableIndexDefine.TABLE_APP_PERPO +
                                            " where " + TableIndexDefine.COLUMN_PERPO_PKGNAME +
                                            " = ? ", new String[] { pkgName });
                    c.moveToNext();
                    if (c.moveToFirst()) {
                        int numbers = c.getInt(c.getColumnIndex(
                                                 TableIndexDefine.COLUMN_PERPO_CLICK_NUM));
                        numbers++;
                        //int number = c.getInt(c.getColumnIndex("click"));
                        //number++;
                        ContentValues values = new ContentValues();
                        values.put(TableIndexDefine.COLUMN_PERPO_CLICK_NUM, numbers);
                        //values.put("click", number);
                        mdb.update(TableIndexDefine.TABLE_APP_PERPO, values, TableIndexDefine.
                                   COLUMN_PERPO_PKGNAME + " = ?", new String[] { pkgName });
                        SharedPreferences sharedPreference = mContext.getSharedPreferences("click",
                                                                             Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreference.edit();
                        String type = sharedPreference.getString("type", "sortName");
                        int order = sharedPreference.getInt("order", 0);
                        editor.clear();
                        editor.putBoolean("isClick", true);
                        editor.putString("type", type);
                        editor.putInt("order", order);
                        //editor.putInt("isSql", 1);
                        editor.commit();
                    }*/
                        StartupMenuUtil.updateDataStorage(mContext, pkgName);
                        mStartupMenuActivity.killStartupMenu();
                        break;
                    case MotionEvent.BUTTON_TERTIARY:
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mPositionItem = position;
                        strPkgName = mStartupMenuActivity.mListAppInfo.get(position).getPkgName();
                        mIsFullScreen = ApplicationInfo.isMaximizedStyleWindow(strPkgName) ||
                                ApplicationInfo.isRealFullScreenStyleWindow(strPkgName);
                        if (position < 0 || position >= mlistAppInfo.size())
                            return false;
                        showMenuDialog1(position, motionEvent);
                        break;
                    default:
                        mStartupMenuActivity.setFocus(false);
                        break;
                }
                return false;
            }
        });
        view.setOnHoverListener(hoverListener);
        return view;
    }

    public static void openAppBroadcast(Context context) {
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Intent.ACTION_OPEN_APPLICATION);
        context.sendBroadcast(openAppIntent);
    }

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.color.app_background);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(android.R.color.transparent);
                    break;
            }
            return false;
        }
    };

    private void showMenuDialog1(int position, MotionEvent motionEvent) {
        mStartupMenuActivity.mStartMenuDialog.setPosition(position);
        int[] location = new int[2];
        //((StartupMenuActivity)infater).mBackBtn.getLocationOnScreen(location);
        StartMenuDialog startMenuDialog = new StartMenuDialog(mContext, R.style.dialog);
        startMenuDialog.showDialog((int) motionEvent.getRawX() - location[0]
                , (int) motionEvent.getRawY() - location[1] + START_MENU_RIGHT_MOUSE_UI_NUMBER
                , mStartMenuAppWidth, mStartMenuAppHeight);
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
        //TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.package_image);
            this.tvAppLabel = (TextView) view.findViewById(R.id.package_name);
            //  this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
        }
    }

    public StartupMenuActivity getStartupMenuActivity() {
        return (StartupMenuActivity) mContext;
    }
}
