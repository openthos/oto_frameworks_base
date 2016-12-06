package com.android.startupmenu.adapter;

import java.util.List;
import java.util.Map;

import com.android.startupmenu.R;
import android.content.ContentValues;
import com.android.startupmenu.util.AppInfo;
import android.database.Cursor;
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
import com.android.startupmenu.util.MySqliteOpenHelper;

import android.os.UserHandle;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.util.Log;

public class StartupMenuAdapter extends BaseAdapter {
    public static final int START_MENU_RIGHT_MOUSE_UI_NUMBER = 57;
    public static String strPkgName;

    private List<AppInfo> mlistAppInfo = null;
    private Map<Integer,Boolean> isCheckedMap;
    LayoutInflater infater = null;
    private Context mContext;
    private StartupMenuActivity mStartupMenuActivity;
    private SQLiteDatabase mdb;
    private MySqliteOpenHelper mMsoh;
    private int mStartMenuAppWidth;
    private int mStartMenuAppHeight;

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
        mMsoh = new MySqliteOpenHelper(mContext, "Application_database.db", null, 1);
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
                StartupMenuActivity.setFocus(true);
                switch (what) {
                case MotionEvent.BUTTON_PRIMARY:
                    String pkgName = StartupMenuActivity.mlistAppInfo.get(position).getPkgName();
                    Intent intent = StartupMenuActivity.mlistAppInfo.get(position).getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    openAppBroadcast(mContext);
                    Cursor c = mdb.rawQuery("select * from perpo where pkname = ?",
                                            new String[] { pkgName });
                    c.moveToNext();
                    if (c.moveToFirst()) {
                        int numbers = c.getInt(c.getColumnIndex("int"));
                        numbers++;
                        int number = c.getInt(c.getColumnIndex("click"));
                        number++;
                        ContentValues values = new ContentValues();
                        values.put("int", numbers);
                        values.put("click", number);
                        mdb.update("perpo", values, "pkname = ?", new String[] { pkgName });
                        SharedPreferences sharedPreference = mContext.getSharedPreferences("click",
                                                                             Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreference.edit();
                        String type = sharedPreference.getString("type", "sortName");
                        int order = sharedPreference.getInt("order", 0);
                        editor.clear();
                        editor.putInt("isClick", 1);
                        editor.putString("type", type);
                        editor.putInt("order", order);
                        editor.putInt("isSql", 1);
                        editor.commit();
                    }
                    mStartupMenuActivity.killStartupMenu();
                    break;
                case MotionEvent.BUTTON_TERTIARY:
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    if (position < 0 || position >= mlistAppInfo.size())
                        return false;
                    strPkgName = StartupMenuActivity.mlistAppInfo.get(position).getPkgName();
                    showMenuDialog1(position,motionEvent);
                    break;
                default :
                    StartupMenuActivity.setFocus(false);
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

    private void showMenuDialog1(int position,MotionEvent motionEvent){
        StartupMenuActivity.mStartMenuDialog.setPosition(position);
        int[] location = new int[2];
        //((StartupMenuActivity)infater).mBackBtn.getLocationOnScreen(location);
        StartupMenuActivity.mStartMenuDialog.showDialog((int)motionEvent.getRawX() - location[0]
                    ,(int)motionEvent.getRawY() - location[1] + START_MENU_RIGHT_MOUSE_UI_NUMBER
                    , mStartMenuAppWidth, mStartMenuAppHeight, 0);
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
