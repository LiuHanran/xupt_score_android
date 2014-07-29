package com.xy.fy.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * ͼƬ�����࣬����bitmap����ͼƬ����DrawableͼƬ���õ��첽����ͼƬ����
 * 
 * @author Administrator 1.���ȴ��ڴ��ж�ȡ���� 2.����ڴ���û�л��棬��ô��Ӳ�̻����ȡ���� 3.���Ӳ�̻���Ҳû�����ݣ���ô�Ӹ�����URL�п����߳���������
 */
public class ImageDownloader {

	private Context context;

	/**
	 * ʹ�õ���ģʽ��ʹ�ó���ֻ����һ���̳߳غ�һ���ڴ滺����ļ�����
	 */
	public static ImageDownloader instance;

	public static ImageDownloader getInstance(Context context) {
		if (instance == null) {
			instance = new ImageDownloader(context);
		}
		return instance;
	}

	private ImageDownloader(Context context) {
		this.context = context;
	}

	
	/**
	 * 1.���ȴ��ڴ��ж�ȡ���� 2.����ڴ���û�л��棬��ô��Ӳ�̻����ȡ���� 3.���Ӳ�̻���Ҳû�����ݣ���ô�Ӹ�����URL�п����߳���������
	 */
	public Bitmap getBitmap(ImageView image, String strUrl) {
		MemoryCache memory = MemoryCache.getInstance(context);
		Bitmap bitmap = memory.getBitmap(strUrl);
		if (bitmap == null) {
			FileCache file = new FileCache();
			bitmap = file.getBitmap(strUrl);
			if (bitmap == null) {
				StaticVarUtil.executorService.submit(new ImageThread(image, context, strUrl));
			}
		} else {
			System.out.println("�ڴ�����ѽ��������������������");
		}
		return bitmap;
	}

}
