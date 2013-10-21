package cn.kli.utils;

import android.content.Context;

public class KliUtils {
	
	private static KliUtils sInstance;
	Context mContext;
	
	private KliUtils(Context context){
		mContext = context;
		if(mContext != null){
			String appname = mContext.getApplicationInfo().name;
			klilog.info(appname);
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
