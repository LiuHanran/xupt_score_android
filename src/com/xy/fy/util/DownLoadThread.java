package com.xy.fy.util;

import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.os.Message;

/**
 * ������ ˵�� urlKind ����url���ࣨ1-16���ã����಻���� account �˺� password ���� email ���� nickname �ǳ� collegeId ��ѧID������3728��0�������д�ѧ�� messageId ��ϢID messageKind ��Ϣ���ࣨ0����������Ϣ��1-9����9�಻ͬ����Ϣ�� sortKind �������ࣨ1����7�չ�ע��2���������У� page ��ҳ��������0��ʼ�� lastMessageTime ����ʱ���������һ��˵˵��ʱ��(��ʽΪ"2013-04-02 13:36:29"[yyyy-MM-dd HH:mm:SS]) commentContentText �������� messageContentText ˵˵���� messageContentPic ˵˵ͼƬ
 * 
 * @author Administrator
 * 
 */
public class DownLoadThread implements Runnable {

	private Handler handler;

	private HashMap<String, String> allParams = null;

	/**
	 * �����޵Ľӿ�
	 * 
	 * @param handler
	 */
	public DownLoadThread(Handler handler, int messageId) {
		this.handler = handler;
		allParams = new HashMap<String, String>();
		allParams.put(HttpUtil.URL_KIND, HttpUtil.PRAISE + "");
		allParams.put(HttpUtil.MESSAGE_ID, messageId + "");
	}

	/**
	 * �ҷ����˵˵�������۵�˵˵�����ղص�˵˵
	 * 
	 * @param handler
	 */
	public DownLoadThread(int account, int urlKind) {
		allParams = new HashMap<String, String>();
		allParams.put(HttpUtil.URL_KIND, urlKind + "");
		allParams.put(HttpUtil.ACCOUNT, account + "");
	}

	/**
	 * ����page,ר�����������ҵķ����ҵ����ۣ��ҵ��ղص�
	 * 
	 * @param page
	 */
	public void setPageAndHanlder(int page, Handler handler) {
		this.handler = handler;
		allParams.put(HttpUtil.PAGE, page + "");
	}

	/**
	 * �鿴�������۵Ľӿ�
	 * 
	 * @param handler
	 */
	public DownLoadThread(Handler handler, String messageId, String page) {
		this.handler = handler;
		allParams = new HashMap<String, String>();
		allParams.put(HttpUtil.URL_KIND, HttpUtil.MESSAGE_COMMENTS + "");
		allParams.put(HttpUtil.MESSAGE_ID, messageId);
		allParams.put(HttpUtil.PAGE, page);
	}

	/**
	 * �����ղصĽӿ�
	 * 
	 * @param handler
	 */
	public DownLoadThread(Handler handler, int messageId, int account) {
		this.handler = handler;
		allParams = new HashMap<String, String>();
		allParams.put(HttpUtil.URL_KIND, HttpUtil.COLLECT + "");
		allParams.put(HttpUtil.ACCOUNT, account + "");
		allParams.put(HttpUtil.MESSAGE_ID, messageId + "");
	}

	/**
	 * �������۵Ľӿ�
	 * 
	 * @param handler
	 */
	public DownLoadThread(Handler handler, int messageId, int account, String comment) {
		this.handler = handler;
		allParams = new HashMap<String, String>();
		allParams.put(HttpUtil.URL_KIND, HttpUtil.PUBLISH_COMMENT + "");
		allParams.put(HttpUtil.ACCOUNT, account + "");
		allParams.put(HttpUtil.MESSAGE_ID, messageId + "");
		allParams.put(HttpUtil.COMMENT_CONTENT_TEXT, comment);
	}

	/**
	 * 
	 * @param handler
	 *        һ���Ƿ��ư�һ���ǽ���
	 * @param collegeId
	 *        ��9��10�ӿڶ��õ�
	 * @param messageKind
	 *        ��9��10�ӿڶ��õ�
	 * @param messageSort
	 *        ��9�ӿ��õ�
	 * @param page
	 *        ��9�ӿ��õ�
	 * @param date
	 *        ��ʮ�ӿ��õ�
	 */
	public DownLoadThread(Handler handler, int collegeId, int messageKind, int messageSort, int page, String lastMessageTime) {
		this.handler = handler;
		allParams = new HashMap<String, String>();// ���еĲ���
		if (messageSort == 0) {// ���õ�ʮ�ӿ�,��Զ����20�����ݣ�����
			allParams.put(HttpUtil.URL_KIND, HttpUtil.MESSAGE_TIME + "");
			allParams.put(HttpUtil.COLLEGE_ID, collegeId + "");
			allParams.put(HttpUtil.MESSAGE_KIND, messageKind + "");
			allParams.put(HttpUtil.LAST_MESSAGE_TIME, lastMessageTime);
		} else if (messageSort == 1 || messageSort == 2) {// ���õھŽӿڣ�����page�������ݣ�������
			allParams.put(HttpUtil.URL_KIND, HttpUtil.MESSAGE + "");
			allParams.put(HttpUtil.COLLEGE_ID, collegeId + "");
			allParams.put(HttpUtil.MESSAGE_KIND, messageKind + "");
			allParams.put(HttpUtil.PAGE, page + "");
			allParams.put(HttpUtil.SORT_KIND, messageSort + "");
		}
	}

	@Override
	public void run() {
		Message msg = new Message();
		msg.what = StaticVarUtil.START;
		this.handler.sendMessage(msg);

		System.out.println("���еĲ�������");
		for (Map.Entry<String, String> param : allParams.entrySet()) {
			System.out.println(param.getKey() + "-->" + param.getValue());
		}

		HttpUtil http = new HttpUtil();
		try {
			StaticVarUtil.response = http.downLoad(HttpUtil.URL1, allParams);
		} catch (Exception e) {
			System.out.println("DownLoadThread.run()������");
			msg = new Message();
			msg.what = StaticVarUtil.INTERNET_ERROR;
			this.handler.sendMessage(msg);
			e.printStackTrace();
			return;// ���س���ı����ô�
		}

		if (StaticVarUtil.response.equals(HttpUtil.FAIL)) {
			msg = new Message();
			msg.what = StaticVarUtil.END_FAIL;
			this.handler.sendMessage(msg);
		} else {
			msg = new Message();
			msg.what = StaticVarUtil.END_SUCCESS;
			this.handler.sendMessage(msg);
		}

	}
}
