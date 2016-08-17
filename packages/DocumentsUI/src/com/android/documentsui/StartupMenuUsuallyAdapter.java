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

public class StartupMenuUsuallyAdapter extends BaseAdapter {
    public static final int START_MENU_RIGHT_MOUSE_UI_X = 260;
    public static final int START_MENU_RIGHT_MOUSE_UI_Y = 200;
    public static final int START_MENU_RIGHT_MOUSE_UI_NUMBER = 57;

    private List<AppInfo> mlistViewAppInfo = null;
    LayoutInflater mInfater = null;

    private Context mContext;
    private SQLiteDatabase mdb;
    private MySqliteOpenHelper mMsoh;

    public StartupMenuUsuallyAdapter(Context context, List<AppInfo> apps) {
        mInfater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistViewAppInfo = apps;
        mContext=context;
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
        holder.tvAppLabel.setText(appInfo.getAppLabel());
        // holder.tvPkgName.setText(appInfo.getPkgName());
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
                        Cursor c = mdb.rawQuery("select * from perpo where pkname = ?",
                                new String[] { pkgName });
                        c.moveToNext();
                        int numbers = c.getInt(c.getColumnIndex("int"));
                        numbers++;
                        ContentValues values = new ContentValues();
                        values.put("int", numbers);
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
        return view;
    }

    private void showMenuDialog1(int position,MotionEvent motionEvent){
        StartupMenuActivity.mStartMenuDialog.setPosition(position);
        int[] location = new int[2];
        //((StartupMenuActivity)infater).mBackBtn.getLocationOnScreen(location);
        StartupMenuActivity.mStartMenuDialog.showDialog((int)motionEvent.getRawX() - location[0],
                (int)motionEvent.getRawY() - location[1] + START_MENU_RIGHT_MOUSE_UI_NUMBER,
                START_MENU_RIGHT_MOUSE_UI_X, START_MENU_RIGHT_MOUSE_UI_Y, 1);
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
