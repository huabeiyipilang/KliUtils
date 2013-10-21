/**
 *  Author :  hmg25
 *  Description :
 */
package cn.kli.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Conversion {
	
	public static Bitmap drawable2Bitmap(Drawable d){
		BitmapDrawable bd = (BitmapDrawable) d;
		return bd.getBitmap();
	}
	
	public static String convertStream2String(InputStream inputStream)
    {
        String jsonStr = "";
        // ByteArrayOutputStream�൱���ڴ������
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        // ��������ת�Ƶ��ڴ��������
        try
        {
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1)
            {
                out.write(buffer, 0, len);
            }
            // ���ڴ���ת��Ϊ�ַ���
            jsonStr = new String(out.toByteArray());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonStr;
    }
}
