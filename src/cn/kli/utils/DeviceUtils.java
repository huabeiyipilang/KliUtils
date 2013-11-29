package cn.kli.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class DeviceUtils {
	final static private String LOG_TAG = "DeviceUtils";
	
	private Context mContext;
    private WindowManager mWinManager;
	
	public DeviceUtils(){
	    mContext = KliUtils.instance().mContext;
	    init();
	}
	
	public DeviceUtils(Context context){
	    mContext = context;
	    init();
	}
	
	private void init(){
        mWinManager =  (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	}
	
	public static String getFormattedKernelVersion() {
		String procVersionStr;

		try {
			String FILENAME_PROC_VERSION = "/proc/version";
			procVersionStr = readLine(FILENAME_PROC_VERSION);

			final String PROC_VERSION_REGEX = "\\w+\\s+" + /* ignore: Linux */
			"\\w+\\s+" + /* ignore: version */
			"([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
			"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /*
														 * group 2:
														 * (xxxxxx@xxxxx
														 * .constant)
														 */
			"\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /* ignore: (gcc ..) */
			"([^\\s]+)\\s+" + /* group 3: #26 */
			"(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
			"(.+)"; /* group 4: date */

			Pattern p = Pattern.compile(PROC_VERSION_REGEX);
			Matcher m = p.matcher(procVersionStr);

			if (!m.matches()) {
				Log.e(LOG_TAG, "Regex did not match on /proc/version: "
						+ procVersionStr);
				return "Unavailable";
			} else if (m.groupCount() < 4) {
				Log.e(LOG_TAG, "Regex match on /proc/version only returned "
						+ m.groupCount() + " groups");
				return "Unavailable";
			} else {
				return (new StringBuilder(m.group(1)).append("\n")
						.append(m.group(2)).append(" ").append(m.group(3))
						.append("\n").append(m.group(4))).toString();
			}
		} catch (IOException e) {
			return "Unavailable";
		}
	}

	private static String readLine(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename),
				256);
		try {
			return reader.readLine();
		} finally {
			reader.close();
		}
	}

	public static long getmem_TOLAL() {
		long mTotal;
		// ϵͳ�ڴ�
		String path = "/proc/meminfo";
		// �洢������
		String content = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				// �ɼ��ڴ���Ϣ
				content = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// beginIndex
		int begin = content.indexOf(':');
		// endIndex
		int end = content.indexOf('k');
		// �ɼ��������ڴ�
		content = content.substring(begin + 1, end).trim();
		// ת��ΪInt��
		mTotal = Integer.parseInt(content);
		return mTotal;
	}

	public static long getmem_UNUSED(Context mContext) {
		long MEM_UNUSED;
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		MEM_UNUSED = mi.availMem / 1024;
		return MEM_UNUSED;
	}
	
	/**  
	* �ƶ�������翪��
	* @param true Ϊ���� falseΪ ������  
	* @return 0Ϊ �ɹ� -1Ϊʧ��  
	*/   
	public static int setMobileDataEnabled(Context context, boolean flag) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Method setMobileDataEnabl;
		try {
			setMobileDataEnabl = cm.getClass().getDeclaredMethod(
					"setMobileDataEnabled", boolean.class);
			setMobileDataEnabl.invoke(cm, flag);
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * �����ڴ�
	 * 
	 * @param mContext
	 * @return
	 */
	public static double getAvailMemory(Context mContext) {
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// mi.availMem; ��ǰϵͳ�Ŀ�����
//		DecimalFormat df = new DecimalFormat("0.00");// ����2λС��
		return mi.availMem / 1024;// ת��ΪKB
	}

	/**
	 * ���ڴ�
	 * 
	 * @return
	 */
	public static double getTotalMemory() {
		double mTotal;
		// ϵͳ�ڴ�
		String path = "/proc/meminfo";
		// �洢����
		String content = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				// �ɼ��ڴ���Ϣ
				content = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// beginIndex
		int begin = content.indexOf(':');
		// endIndex
		int end = content.indexOf('k');
		// �ɼ���������
		content = content.substring(begin + 1, end).trim();
		// ת��ΪInt
		mTotal = Double.parseDouble(content);// ��λΪKB
//		DecimalFormat df = new DecimalFormat("0.00");// ����2λС��
		return mTotal;
	}
	
	public DisplayMetrics getDisplayMetrics(){
	    DisplayMetrics metrics = new DisplayMetrics();
	    mWinManager.getDefaultDisplay().getMetrics(metrics);
	    return metrics;
	}
	
	public Point getRealSize(){
	    Point p = new Point();
	    mWinManager.getDefaultDisplay().getRealSize(p);
	    return p;
	}
}
