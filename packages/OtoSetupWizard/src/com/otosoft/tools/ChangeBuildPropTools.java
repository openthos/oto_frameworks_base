package com.otosoft.tools;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zhu on 2016/7/1.
 */
public class ChangeBuildPropTools {

    public static String TAG = "############# exrc linux function£º";
    public static String exec(String cmd) {
        try {
            if (cmd != null) {
                Runtime rt = Runtime.getRuntime();
                Process process = rt.exec("su");//Root   //Process process = rt.exec("sh");//
                DataOutputStream dos = new DataOutputStream(process.getOutputStream());
                dos.writeBytes(cmd + "\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                InputStream myin = process.getInputStream();
                InputStreamReader is = new InputStreamReader(myin);
                char[] buffer = new char[1024];
                int bytes_read = is.read(buffer);
                StringBuffer aOutputBuffer = new StringBuffer();
                while (bytes_read > 0) {
                    aOutputBuffer.append(buffer, 0, bytes_read);
                    bytes_read = is.read(buffer);
                }
                Log.e(TAG, aOutputBuffer.toString());
                return aOutputBuffer.toString();
            } else {
                return "please input true cmd";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "operater err";
        }
    }


    public static String getPropertyName(String key,String name){
        try {
            File file=new File("/system/build.prop");
            InputStream in=new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader buff= new BufferedReader(inputStreamReader);
            String result="";
            String line=null;
            while ((line=buff.readLine())!=null){
                Log.e(TAG,"line:"+line);
                if (line.startsWith(key)) {
                    result = result + key +"=" + name;
                }  else {
                    result += line;
                }
                result+="\r\n";
            }
            return result;
        }catch (IOException e){
            Log.e(TAG,e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }
    }
    public static void setPropertyName(String result) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("/system/build.prop"));
            out.write(result);
            out.close();

        } catch (IOException e) {
            //Log.e("SetHostName:",e.toString());
            //Toast.makeText(getActivity(),e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
