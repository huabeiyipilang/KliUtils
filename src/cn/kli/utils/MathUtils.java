package cn.kli.utils;

import java.util.Random;

public class MathUtils {
	public static int randomNumber(int min, int max){
		Random r = new Random();
		int num = r.nextInt(max - min);
		num += min;
		return num;
	}
}
