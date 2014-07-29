package com.xy.fy.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

import com.xy.fy.util.FileCache;
import com.xy.fy.util.StaticVarUtil;
import com.xy.fy.util.ViewUtil;

@SuppressLint("FloatMath")
public class PictureActivity extends Activity implements OnTouchListener {
	// �Ŵ���С
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// �ؼ�
	private Button download = null;// ���ذ�ť
	private ProgressBar progress = null;// ������
	private ImageView image = null;// ����ͼƬ

	private Bitmap bitmap = null;// Ҫ��ʾ��ͼƬ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_picture);
		download = (Button) findViewById(R.id.butDownLoad);
		download.setEnabled(false);// ��ʼʱ������
		progress = (ProgressBar) findViewById(R.id.progress);
		progress.setMax(100);
		image = (ImageView) findViewById(R.id.imageLargePic);

		FileCache fileCache = new FileCache();
		bitmap = fileCache.getBitmap(StaticVarUtil.largePicPath);
		if (bitmap != null) {
			image.setImageBitmap(bitmap);
			download.setEnabled(true);
			progress.setVisibility(View.GONE);
			image.setOnTouchListener(this);// ���ü���
		} else {
			bitmap = fileCache.getBitmap(StaticVarUtil.smallPicPath);
			progress.setVisibility(View.VISIBLE);
			image.setImageBitmap(bitmap);
			// ��̨�Զ�����
			try {
				new ImageDownLoadAsyncTask().execute(new URL(StaticVarUtil.largePicPath));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		/**
		 * ���浽���� ���ذ�ť
		 */
		download.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FileCache fileCache = new FileCache();
				fileCache.savePictureToLocal(bitmap, StaticVarUtil.largePicPath);
				ViewUtil.toastShort("�ļ�������sdcard/FengYun/save/Ŀ¼��", PictureActivity.this);
			}
		});
	}

	/**
	 * ���������ֱ���Params����������ִ�е��������
	 * 
	 * ����2. Progress����̨����ִ�еİٷֱ�
	 * 
	 * ����3. Result����̨����Ľ������
	 * 
	 * @author Administrator
	 * 
	 */
	private class ImageDownLoadAsyncTask extends AsyncTask<URL, Integer, Bitmap> {

		Bitmap bitmap = null;

		/**
		 * ����progressBar������ȡ�ô������ĵ�һ��ֵ
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			progress.setProgress(values[0]);// ȡ�õ�һ��ֵ
		}

		/**
		 * ��̨����
		 */
		@Override
		protected Bitmap doInBackground(URL... params) {
			// ��ʼ����
			InputStream inputStream = null;
			try {
				URLConnection conn = params[0].openConnection();
				conn.connect();
				// ���ͼ����ַ���
				inputStream = conn.getInputStream();
				int length = conn.getContentLength();// �õ����ȣ�����һ����Ϣ
				if (length != -1) {
					byte[] imgData = new byte[length];
					byte[] temp = new byte[1024 * 10];// ÿ������10k������һ����Ϣ
					int readLen = 0;
					int destPos = 0;
					while ((readLen = inputStream.read(temp)) > 0) {
						System.arraycopy(temp, 0, imgData, destPos, readLen);
						/****************** ����������ص� *****************/
						int pro = (int) ((float) destPos * 100 / length);// �ٷֱ�ֵ
						publishProgress(pro);// �ٷֱ�
						destPos += readLen;
					}
					bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			FileCache fileCache = new FileCache();
			fileCache.saveCache(bitmap, StaticVarUtil.largePicPath);
			return bitmap;
		}

		/**
		 * �����Ժ�
		 */
		@Override
		protected void onPostExecute(Bitmap result) {
			PictureActivity.this.bitmap = result;
			image.setImageBitmap(PictureActivity.this.bitmap);
			download.setEnabled(true);// ��ť����
			progress.setVisibility(View.GONE);
			image.setScaleType(ScaleType.MATRIX);
			image.setOnTouchListener(PictureActivity.this);// ���ü���
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		image.setScaleType(ScaleType.MATRIX);// ���������������ģʽ

		ImageView myImageView = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// ��������ģʽ
		case MotionEvent.ACTION_DOWN:
			matrix.set(myImageView.getImageMatrix());
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;

		// ���ö�㴥��ģʽ
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		// ��ΪDRAGģʽ�������ƶ�ͼƬ
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			}
			// ��ΪZOOMģʽ��������������
			else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					// ����˶�ű�����ͼƬ���е�λ��
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		myImageView.setImageMatrix(matrix);
		return true;
	}

	/**
	 * �����ƶ�����
	 * 
	 * @param event
	 * @return
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * �����е�λ��
	 * 
	 * @param point
	 * @param event
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}
