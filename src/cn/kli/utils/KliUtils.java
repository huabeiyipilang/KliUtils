package cn.kli.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class KliUtils {
	
	private static KliUtils sInstance;
	Context mContext;
	
	private KliUtils(Context context){
		mContext = context;
		if(mContext != null){
			ApplicationInfo info = mContext.getApplicationInfo();
			String appname = (String) mContext.getPackageManager().getApplicationLabel(info);
			klilog.setTAG(appname);
		}
	}
	
	public static void init(Context context){
		if(sInstance == null){
			sInstance = new KliUtils(context);
		}
	}
	
	public static KliUtils instance(){
		return sInstance;
	}
}
