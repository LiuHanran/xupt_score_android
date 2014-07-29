package com.xy.fy.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * ͼƬ�����࣬���첽����ͼƬ�������ͼƬ��ʲô������Բ�ǣ���С֮��ģ�����Ҫ��ImageDownloader�в�������Ҫ�������һ��
 * 
 * @author Administrator
 * 
 */
@SuppressLint("HandlerLeak")
public class ImageThread implements Runnable {

	private ImageView image;
	private Context context;
	private String strUrl;
	public static final int END = 1;
	private Bitmap bitmap = null;

	public ImageThread(ImageView image, Context context, String strUrl) {
		this.image = image;
		this.context = context;
		this.strUrl = strUrl;
	}

	@Override
	public void run() {
		HttpUtil http = new HttpUtil();
		bitmap = http.getBitmapFromUrl(strUrl);

		// ��֪ͨ������
		Message msg = new Message();
		msg = new Message();
		msg.what = END;
		handler.sendMessage(msg);

		// �����û���һ�ݣ�Ӳ���û���һ�ݣ��ļ�����һ��
		saveCache();

	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case END:
				image.setImageBitmap(bitmap);// ����ͼƬ
				break;
			default:
				break;
			}
		};
	};

	/**
	 * �����û���һ�ݣ�Ӳ���û���һ�ݣ��ļ�����һ��
	 */
	private void saveCache() {
		// �ڴ滺�棬Ӳ���û��棬�����û���
		MemoryCache memory = MemoryCache.getInstance(context);
		memory.saveCache(bitmap, strUrl);
		// �ļ�����
		FileCache file = new FileCache();
		file.saveCache(bitmap, strUrl);
	}
}
