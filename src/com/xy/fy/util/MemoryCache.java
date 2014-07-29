package com.xy.fy.util;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * �ڴ滺��,�����ã����ڴ治���ʱ�������ڴ�,Ӳ���ã�һ�㲻�����׻���
 */
public class MemoryCache {

	private static final int SOFT_CACHE_SIZE = 20; // �����û�������
	private static LruCache<String, Bitmap> allLruCache;// Ӳ���û���
	private static LinkedHashMap<String, SoftReference<Bitmap>> allSoftCache;// �����û���

	public static MemoryCache instance = null;

	public static MemoryCache getInstance(Context context) {
		if (instance == null) {
			instance = new MemoryCache(context);
		}
		return instance;
	}

	/**
	 * ���췽��
	 * 
	 * @param context
	 */
	private MemoryCache(Context context) {
		// this.strUrl = convertUrlToFileName(strUrl);// ��·������
		int memoryMB = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		int cacheSize = 1024 * 1024 * memoryMB / 10;// Ӳ����������СΪ��������ʮ��֮һ
		allLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				if (value != null) {
					return value.getRowBytes() * value.getHeight();
				} else {
					return 0;
				}
			}

			@Override
			protected void entryRemoved(boolean evicted, String key,
					Bitmap oldValue, Bitmap newValue) {
				if (oldValue != null)
					// Ӳ���û�����������ʱ�򣬻����LRU�㷨�����û�б�ʹ�õ�ͼƬת��������û���,������ʱ�����ڴ�
					allSoftCache.put(key, new SoftReference<Bitmap>(oldValue));
			}
		};
		allSoftCache = new LinkedHashMap<String, SoftReference<Bitmap>>(
				SOFT_CACHE_SIZE, 0.75f, true) {
			private static final long serialVersionUID = 6040103833179403725L;

			@Override
			protected boolean removeEldestEntry(
					Entry<String, SoftReference<Bitmap>> eldest) {
				if (size() > SOFT_CACHE_SIZE) {
					return true;
				}
				return false;
			}
		};
	}

	/**
	 * ���ڴ��У�����URL�õ���Ӧ��BitmapͼƬ
	 * 
	 * @param url
	 *            ͼƬURL
	 * @return BitmapͼƬ
	 */
	public Bitmap getBitmap(String url) {
		String strUrl = convertUrlToFileName(url);
		Bitmap bitmap;

		// ��Ӳ���û����л�ȡ
		synchronized (allLruCache) {
			bitmap = allLruCache.get(strUrl);
			if (bitmap != null) {
				// ����ҵ��Ļ�����Ԫ���Ƶ�LinkedHashMap����ǰ�棬�Ӷ���֤��LRU�㷨�������ɾ��
				allLruCache.remove(strUrl);
				allLruCache.put(strUrl, bitmap);
				System.out.println("����Ӳ����");
				return bitmap;
			}
		}

		// Ӳ����û�У���ô���������û�������
		synchronized (allSoftCache) {
			SoftReference<Bitmap> bitmapReference = allSoftCache.get(strUrl);
			if (bitmapReference != null) {
				bitmap = bitmapReference.get();
				if (bitmap != null) {
					// ��ͼƬ�ƻ�Ӳ����
					allLruCache.put(strUrl, bitmap);
					allSoftCache.remove(strUrl);
					System.out.println("����������");
					return bitmap;
				} else {
					allSoftCache.remove(strUrl);
				}
			}
		}
		return null;
	}

	/**
	 * ���ͼƬ������
	 * 
	 * @param bitmap
	 *            Ҫ�����ͼƬ
	 * @param url
	 *            ����ͼƬ��URL
	 */
	public void saveCache(Bitmap bitmap, String url) {
		String strUrl = convertUrlToFileName(url);
		if (bitmap != null) {
			synchronized (allLruCache) {
				allLruCache.put(strUrl, bitmap);
			}
		}
	}

	/**
	 * ��urlת���ļ��������������ͨ�ã�ֻ���������ҵ��������������Ϊ��֪���������ĵ�ַ����
	 * 
	 * @param url
	 *            ͼƬ��ַ
	 * @return �ļ���
	 */
	private String convertUrlToFileName(String url) {
		// �����ͨ�õĻ���ֱ�Ӹĳ�url.split("/");����
		String[] strs = url.substring(36).split("/");

		StringBuffer string = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			string.append(strs[i]);
		}
		return string.toString();// �����ļ���
	}

	/**
	 * ������
	 */
	public void clearCache() {
		allSoftCache.clear();
	}
}
