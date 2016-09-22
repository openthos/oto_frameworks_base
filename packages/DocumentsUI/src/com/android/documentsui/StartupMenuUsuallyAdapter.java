package com.android.documentsui;

import java.util.List;
import com.android.documentsui.R;
import com.android.documentsui.util.AppInfo;
import com.android.documentsui.util.MySqliteOpenHelper;

import android.content.ContentValues;
import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;

public class StartupMenuUsuallyAdapter extends BaseAdapter {
    public static final int START_MENU_RIGHT_MOUSE_UI_NUMBER = 57;

    private List<AppInfo> mlistViewAppInfo = null;
    LayoutInflater mInfater = null;

    private Context mContext;
    private SQLiteDatabase mdb;
    private MySqliteOpenHelper mMsoh;
    private int mStartMenuCommonlWidth;
    private int mStartMenuCommonlHeight;

    public StartupMenuUsuallyAdapter(Context context, List<AppInfo> apps) {
        mInfater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistViewAppInfo = apps;
        mContext=context;
        mStartMenuCommonlWidth = mContext.getResources()
                                  .getDimensionPixelSize(R.dimen.start_menu_commonl_width);
        mStartMenuCommonlHeight = mContext.getResources()
                                  .getDimensionPixelSize(R.dimen.start_menu_commonl_height);
        mMsoh = new MySqliteOpenHelper(mContext, "Application_database.db", null, 1);
        mdb = mMsoh.getWritableDatabase();
    }

    @Override
    public int getCount() {
        return mlistViewAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistViewAppInfo.get(position);
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
            view = mInfater.inflate(R.layout.activity_listview_item, null);
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
                switch (what) {
                    case MotionEvent.BUTTON_PRIMARY:
                        String pkgName = StartupMenuActivity.mlistViewAppInfo
                                                            .get(position).getPkgName();
                        Intent intent = StartupMenuActivity.mlistViewAppInfo
                                                            .get(position).getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        StartupMenuAdapter.openAppBroadcast(mContext);
                        Cursor c = mdb.rawQuery("select * from perpo where pkname = ?",
                                new String[] { pkgName });
                        c.moveToNext();
                        int numbers = c.getInt(c.getColumnIndex("int"));
                        numbers++;
                        int number = c.getInt(c.getColumnIndex("int"));
                        number++;
                        ContentValues values = new ContentValues();
                        values.put("int", numbers);
                        values.put("click", number);
                        mdb.update("perpo", values, "pkname = ?", new String[] { pkgName });
                        break;
                    case MotionEvent.BUTTON_TERTIARY:
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        if (position < 0 || position >= mlistViewAppInfo.size()) {
                            return false;
                        }
                        showMenuDialog1(position,motionEvent);
                        break;
                }
                return false;
            }
        });
        view.setOnHoverListener(hoverListener);
        return view;
    }

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.drawable.power_background);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(R.color.appUsuallyBackground);
                    break;
            }
            return false;
        }
    };

    private void showMenuDialog1(int position,MotionEvent motionEvent){
        StartupMenuActivity.mStartMenuUsuallyDialog.setPosition(position);
        int[] location = new int[2];
        //((StartupMenuActivity)infater).mBackBtn.getLocationOnScreen(location);
        StartupMenuActivity.mStartMenuUsuallyDialog.showDialog(
                (int)motionEvent.getRawX() - location[0],
                (int)motionEvent.getRawY() - location[1] + START_MENU_RIGHT_MOUSE_UI_NUMBER,
                mStartMenuCommonlWidth, mStartMenuCommonlHeight, 1);
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
        //TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.list_package_image);
            this.tvAppLabel = (TextView) view.findViewById(R.id.list_package_name);
            // this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
        }
    }

}
