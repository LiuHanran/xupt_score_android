package com.mc.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.xy.fy.util.StaticVarUtil;

public class Util {
	/**
	 * 将图片保存到本地
	 * @param bmp
	 * @param username
	 * @return
	 */
	public static boolean saveBitmap2file(Bitmap bmp, String username) {
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 100;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(StaticVarUtil.PATH + "/" + username
					+ ".JPEG");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}

	// 从网络获取图片,并保存找到本地
	public static Bitmap getBitmap(String pictureUrl) {
		URL url = null;
		Bitmap bitmap = null;

		InputStream in = null;
		try {
			if (pictureUrl != null) {
				url = new URL(pictureUrl);
				HttpURLConnection httpURLConnection = (HttpURLConnection) url
						.openConnection();
				httpURLConnection.setConnectTimeout(6000);// 最大延迟6000毫秒
				httpURLConnection.setDoInput(true);// 连接获取数据流
				httpURLConnection.setUseCaches(true);// 用缓存
				in = httpURLConnection.getInputStream();
				bitmap = BitmapFactory.decodeStream(in);
			} else {
				return null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return bitmap;

	}

	// 将文件转化为图片
	public static Bitmap convertToBitmap(String path, int w, int h) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 设置为ture只获取图片大小
		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// 返回为空
		BitmapFactory.decodeFile(path, opts);
		int width = opts.outWidth;
		int height = opts.outHeight;
		float scaleWidth = 0.f, scaleHeight = 0.f;
		if (width > w || height > h) {
			// 缩放
			scaleWidth = ((float) width) / w;
			scaleHeight = ((float) height) / h;
		}
		opts.inJustDecodeBounds = false;
		float scale = Math.max(scaleWidth, scaleHeight);
		opts.inSampleSize = (int) scale;
		WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(path, opts));
		return Bitmap.createScaledBitmap(weak.get(), w, h, true);
	}

	private static boolean haveChar(String str) {
		try {
			int num = Integer.valueOf(str);// 把字符串强制转换为数字
			return false;// 如果是数字，返回True
		} catch (Exception e) {
			return true;// 如果抛出异常，返回False
		}

	}

	private static boolean hasDigit(String content) {

		boolean flag = false;

		Pattern p = Pattern.compile(".*\\d+.*");

		Matcher m = p.matcher(content);

		if (m.matches())

			flag = true;

		return flag;

	}

	public static boolean hasDigitAndNum(String str) {
		if (haveChar(str) & hasDigit(str)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 从listHerf中获取具体 tittle中的herf
	 * 
	 * @param tittle
	 * @return
	 */
	public static String getURL(String tittle) {
		String result = "";
		for (int i = 0; i < StaticVarUtil.listHerf.size(); i++) {
			HashMap<String, String> map = StaticVarUtil.listHerf.get(i);
			if (map.get("tittle").equals(tittle)) {
				result = map.get("herf");
			}
		}
		result = result.replace("%3D", "=");
		result = result.replace("%26", "&");
		result = result.replace("%3f", "?");
		return result;
	}

	/**
	 * 获取软件版本号
	 */
	public static String getVersion(Context context) {
		String version = "";
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pt = pm.getPackageInfo(context.getPackageName(), 0);
			version = pt.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
	}
}
