package com.xy.fy.main;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xy.fy.util.ConnectionUtil;
import com.xy.fy.util.HttpUtil;
import com.xy.fy.util.StaticVarUtil;
import com.xy.fy.util.ViewUtil;

@SuppressLint("HandlerLeak")
public class ForgetPasswordActivity extends Activity {

	private Button back;// ���ؼ�
	private Button getPassword1;// �һ����뷽ʽһ
	private Button getPassword2;// �һ����뷽ʽ��
	private EditText account;// �˺�
	private EditText email;// ����
	private String strReturn1 = null;// ��ʽһ��������
	private String strReturn2 = null;// ��ʽ����������
	private ProgressDialog progressDialog;// ���ȿ�

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_forget_password);
		this.findViewById();
		// ���ذ�ť
		this.back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// ��һ���һ����뷽ʽ
		this.getPassword1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String strAccount = account.getText().toString();
				final String strEmail = email.getText().toString();
				if (strAccount.equals("") || strAccount == null
						|| strEmail.equals("") || strEmail == null) {
					Toast.makeText(getApplicationContext(), "������ȫ",
							Toast.LENGTH_LONG).show();
					return;
				}
				if (!ConnectionUtil.isConn(ForgetPasswordActivity.this)) {
					ConnectionUtil
							.setNetworkMethod(ForgetPasswordActivity.this);
					return;
				}
				// �һ�����
				new Thread() {
					public void run() {
						getPassword1(strAccount, strEmail);
					};
				}.start();
			}
		});
		// �ڶ����һ����뷽ʽ
		this.getPassword2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String strAccount = account.getText().toString();
				if (strAccount.equals("") || strAccount == null) {
					Toast.makeText(getApplicationContext(), "�˺Ų���Ϊ�գ�",
							Toast.LENGTH_LONG).show();
					return;
				}
				new Thread() {
					public void run() {
						getPassword2(strAccount);
					};
				}.start();
			}
		});
	}

	/**
	 * ����ǰ̨�ؼ�
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case StaticVarUtil.GET_PASSWORD_BACK1_START:
				progressDialog.show();
				break;
			case StaticVarUtil.GET_PASSWORD_BACK1_END:
				progressDialog.cancel();
				if (strReturn1 == null || strReturn1.equals("")) {
					ViewUtil.toastShort("�����쳣,���Ժ�����",
							ForgetPasswordActivity.this);
				} else if (strReturn1.equals(HttpUtil.FAIL)) {
					ViewUtil.showDialog("�˺����벻ƥ��", "�һ�����",
							ForgetPasswordActivity.this);
				} else {
					ViewUtil.showDialog("������:" + strReturn1, "�һ�����",
							ForgetPasswordActivity.this);
				}
				break;
			case StaticVarUtil.GET_PASSWORD_BACK2_START:
				progressDialog.show();
				break;
			case StaticVarUtil.GET_PASSWORD_BACK2_END:
				progressDialog.cancel();
				if (strReturn2 == null || strReturn2.equals("")) {
					ViewUtil.toastShort("�����쳣,���Ժ�����",
							ForgetPasswordActivity.this);
				} else if (strReturn2.equals(HttpUtil.SUCCESS)) {
					ViewUtil.showDialog("�Ѿ������뷢��������䣬���¼��������", "�һ�����",
							ForgetPasswordActivity.this);
				} else if (strReturn2.equals(HttpUtil.FAIL)) {
					ViewUtil.showDialog("ϵͳ��û������˺�,�����¼���˺��Ƿ��������", "�һ�����",
							ForgetPasswordActivity.this);
				} else {
					ViewUtil.showDialog("����������,���Ժ�����", "�һ�����",
							ForgetPasswordActivity.this);
				}
				break;
			default:
				break;
			}
		};
	};

	/**
	 * ��ʽһ�һ�����
	 */
	private void getPassword1(String account, String email) {
		Message msg = new Message();
		msg.what = StaticVarUtil.GET_PASSWORD_BACK1_START;
		handler.sendMessage(msg);

		HttpUtil http = new HttpUtil();
		HashMap<String, String> allParams = new HashMap<String, String>();
		allParams.put(HttpUtil.URL_KIND, HttpUtil.FIND_PASSWORD_1 + "");
		allParams.put(HttpUtil.ACCOUNT, account);
		allParams.put(HttpUtil.EMAIL, email);
		try {
			strReturn1 = http.downLoad(HttpUtil.URL1, allParams);
		} catch (Exception e) {
			e.printStackTrace();
			strReturn1 = null;
		}

		msg = new Message();
		msg.what = StaticVarUtil.GET_PASSWORD_BACK1_END;
		handler.sendMessage(msg);
	}

	/**
	 * ��ʽ���һ�����
	 */
	private void getPassword2(String account) {
		Message msg = new Message();
		msg.what = StaticVarUtil.GET_PASSWORD_BACK2_START;
		handler.sendMessage(msg);

		HttpUtil http = new HttpUtil();
		HashMap<String, String> allParams = new HashMap<String, String>();
		allParams.put(HttpUtil.URL_KIND, HttpUtil.FIND_PASSWORD_2 + "");
		allParams.put(HttpUtil.ACCOUNT, account);
		try {
			strReturn2 = http.downLoad(HttpUtil.URL1, allParams);
		} catch (Exception e) {
			e.printStackTrace();
			strReturn2 = null;
		}

		msg = new Message();
		msg.what = StaticVarUtil.GET_PASSWORD_BACK2_END;
		handler.sendMessage(msg);
	}

	/**
	 * �ҵ��ؼ�
	 */
	private void findViewById() {
		this.account = (EditText) findViewById(R.id.etAccount);
		this.email = (EditText) findViewById(R.id.etEmail);
		this.getPassword1 = (Button) findViewById(R.id.butGetPassword1);
		this.getPassword2 = (Button) findViewById(R.id.butGetPassword2);
		this.back = (Button) findViewById(R.id.butBack);
		progressDialog = ViewUtil.getProgressDialog(
				ForgetPasswordActivity.this, "�����һ�����");
	}

}
