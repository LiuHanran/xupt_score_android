package com.xy.fy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

/**
 * �ļ����棬strUrl��ȡ���һ���ַ����������ļ����� �õ���֪ʶ����LRU�㷨
 * 
 * @author ����
 */
public class FileCache {

	private static final int FILE_CACHE = 50 * 1024 * 1024;// �ļ�����50MB
	private static final String FILE_CACHE_EXT = ".cach";// �ļ���׺��(��չ��)
	private static final String dir = getRealPath() + HttpUtil.FENGYUN + "/fileCache";// �õ���ʵ·��

	/**
	 * @param strUrl
	 *        ͼƬ·��
	 * @return BitmapͼƬ
	 */
	public Bitmap getBitmap(String strUrl) {
		String fileName = convertUrlToFileName(strUrl);
		String path = dir + "/" + fileName;
		File file = new File(path);
		if (file.exists()) {
			System.out.println("�ļ����������ݣ�");
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			if (bitmap == null) {
				file.delete();
			} else {
				updateFileTime(path);// ��������޸�ʱ��
			}
			return bitmap;
		} else {
			System.out.println("�ļ�������û��");
			return null;
		}
	}

	/**
	 * ���滺�棬�����������Ժ���ô������
	 * 
	 * @param bitmap
	 *        Ҫ�����Bitmap���͵�ͼƬ
	 * @param strUrl
	 *        Ҫ�����ͼƬURL·��
	 */
	public void saveCache(Bitmap bitmap, String strUrl) {
		String fileName = convertUrlToFileName(strUrl);
		// �ļ�����Ŀ¼

		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

		// ���ж��Ƿ��������һ�����ļ�������
		isTimeToClear(dir);

		File file = new File(dir + "/" + fileName);
		OutputStream outputStream = null;
		try {
			file.createNewFile();
			outputStream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * ϵͳ�Զ��ж��ļ������ļ����Ƿ񳬹�50MB
	 * 
	 * @param dir
	 *        ����·��
	 */
	private void isTimeToClear(String dir) {
		File file = new File(dir);
		File[] allFile = file.listFiles();
		int allSize = 0;
		for (int i = 0; i < allFile.length; i++) {
			allSize += allFile[i].length();
		}
		if (allSize >= FILE_CACHE) {
			Arrays.sort(allFile, new FileLastModifSort());
			// �����ǰ��һ��Ļ���
			int clearFlag = allFile.length / 2;
			for (int i = 0; i < clearFlag; i++) {
				if (allFile[i].getName().contains(FILE_CACHE_EXT)) {
					allFile[i].delete();
				}
			}
		}
	}

	/**
	 * �����ļ�������޸�ʱ�����ǰ������
	 */
	private class FileLastModifSort implements Comparator<File> {
		public int compare(File arg0, File arg1) {
			if (arg0.lastModified() > arg1.lastModified()) {
				return 1;
			} else if (arg0.lastModified() == arg1.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	/**
	 * ���ȫ������
	 */
	public void clearCache() {
		File file = new File(dir);
		if (file.exists()) {
			File[] allFile = file.listFiles();
			for (int i = 0; i < allFile.length; i++) {
				allFile[i].delete();
			}
		}

	}

	/**
	 * ��urlת���ļ��������������ͨ�ã�ֻ���������ҵ��������������Ϊ��֪���������ĵ�ַ����
	 * 
	 * @param url
	 *        ͼƬ��ַ
	 * @return �ļ���
	 */
	private String convertUrlToFileName(String url) {
		// �����ͨ�õĻ���ֱ�Ӹĳ�url.split("/");����
		String[] strs = url.substring(36).split("/");

		StringBuffer string = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			string.append(strs[i]);
		}
		return string.toString() + FILE_CACHE_EXT;// �����ļ�������չ��Ϊ.cache��
	}

	/**
	 * @return ��ʵ·��
	 */
	private static String getRealPath() {
		File sdDir = null;
		boolean isSDcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
		if (isSDcardExist) {
			sdDir = Environment.getExternalStorageDirectory();// ��ȡ��·��
			return sdDir.toString();
		} else {
			System.out.println("û��SDcard");
			return null;
		}
	}

	/**
	 * ���ظ��ļ������Ѿ����ڵ�ʱ���޸��ļ������ʱ��
	 * 
	 * @param path
	 * 
	 */
	public void updateFileTime(String path) {
		File file = new File(path);
		long newModifiedTime = System.currentTimeMillis();
		file.setLastModified(newModifiedTime);
	}

	/**
	 * ����json����Դ
	 * 
	 * @param jsonData
	 *        json����
	 * @param isAppend
	 *        �Ƿ�׷��
	 */
	public void updateJsonCache(String jsonData, Boolean isAppend, String fileName) {
		String path = getRealPath() + HttpUtil.FENGYUN + "/jsonCache/" + fileName;
		File file = new File(path);
		if (file.exists() == false) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file, isAppend);
			fileWriter.write(jsonData);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * ��ȡ��ʷjson���� jsonCache.txt,jsonCacheMyPublish.txt,jsonCacheMyCollect.txt,jsonCacheMyComment.txt
	 * 
	 * @return
	 */
	public String readHistoryJsonData(String fileName) {
		String path = getRealPath() + HttpUtil.FENGYUN + "/jsonCache/" + fileName;
		StringBuffer stringBuffer = new StringBuffer();
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * �����ڱ���
	 * 
	 * @param fileUrlPath
	 *        ͼƬ��URL·��
	 * @param bitmap
	 *        Ҫ�����BitmapͼƬ
	 */
	public void savePictureToLocal(Bitmap bitmap, String fileUrlPath) {
		// �����ͨ�õĻ���ֱ�Ӹĳ�url.split("/");����
		String[] strs = fileUrlPath.substring(36).split("/");
		StringBuffer string = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			string.append(strs[i]);
		}
		String fileName = string.toString();// �õ��ļ���
		String path = getRealPath() + HttpUtil.FENGYUN + "/save/" + fileName;// �õ�����·��
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			if (bitmap != null) {
				// Ҫ�������ʽ
				if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
					fileOutputStream.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
