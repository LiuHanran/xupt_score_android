package com.xy.fy.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.mc.util.HttpUtilMc;
import com.xy.fy.util.ConnectionUtil;
import com.xy.fy.util.HttpUtil;
import com.xy.fy.util.StaticVarUtil;
import com.xy.fy.util.ViewUtil;
import com.xy.fy.view.ToolClass;

@SuppressLint("HandlerLeak")
public class LoginActivity extends Activity {

	private EditText account;// �˺�
	private EditText password;// ����
	private Button forgetPassWord;// ��������
	private CheckBox rememberPassword;// ��ס����
	private Button login;// ��½
	private ProgressDialog progressDialog;
	private String session;// session
    //��������herf
	private List<HashMap<String, String>> listHerf;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_login);

		Intent i = getIntent();
		session = i.getStringExtra("session");
		StaticVarUtil.session =session;
		this.findViewById();

		if (!this.initData()) {// ��ʼ��һЩ���������
			ViewUtil.toastLength("�ڴ濨������", LoginActivity.this);
		}

		this.isRemember();// �Ƿ��Ǽ�ס�����

		ToolClass.map();// ��ʼ��ӳ���ϵ����ֹ�Ժ��õ�

		// �������밴ť
		this.forgetPassWord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*
				 * Intent intent = new Intent();
				 * intent.setClass(getApplicationContext(),
				 * ForgetPasswordActivity.class); startActivity(intent);
				 */
				Toast.makeText(getApplicationContext(), "�ݲ����ã��������ע������", 1000)
						.show();
			}
		});
		// ��½��ť
		this.login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String strAccount = account.getText().toString();
				String strPassword = password.getText().toString();
				try {
					Integer.parseInt(strAccount);
				} catch (Exception e) {
					ViewUtil.toastShort("�˺ű���Ϊʮλ���ڵ����֣�", LoginActivity.this);
					return;
				}
				if (strAccount == null || strAccount.equals("")
						|| strPassword.equals("") || strPassword == null) {
					ViewUtil.toastShort("�˺����벻��Ϊ��", LoginActivity.this);
					return;
				}
				if (!ConnectionUtil.isConn(getApplicationContext())) {
					ConnectionUtil.setNetworkMethod(LoginActivity.this);
					return;
				}
				rememberPassword(strAccount, strPassword);
				login();
			}
		});
	}

	/**
	 * ��ʼ��һЩ���������
	 */
	private boolean initData() {
		boolean isSDcardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
		if (isSDcardExist) {
			File sdDir = Environment.getExternalStorageDirectory();// ��ȡ��·��
			StaticVarUtil.PATH = sdDir.toString() + HttpUtil.FENGYUN;
			// ����Ӧ��ר��·��
			File file = new File(StaticVarUtil.PATH);
			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// ����fileCache�ļ���
			file = new File(StaticVarUtil.PATH + "/fileCache");
			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// ����save�ļ���
			file = new File(StaticVarUtil.PATH + "/save");
			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// ����jsonCache�ļ���
			file = new File(StaticVarUtil.PATH + "/jsonCache");
			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// ����temp.png�ļ�
			file = new File(StaticVarUtil.PATH + "/temp.JPEG");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// ����ϴ�ͼƬ
			file = new File(StaticVarUtil.PATH + "/upload.JPEG");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * �Ƿ��Ǽ�ס����
	 */
	private void isRemember() {
		SharedPreferences preferences = getSharedPreferences(
				StaticVarUtil.USER_INFO, MODE_PRIVATE);
		String account = preferences.getString(StaticVarUtil.ACCOUNT, "");
		String password = preferences.getString(StaticVarUtil.PASSWORD, "");
		boolean isRemember = preferences.getBoolean(StaticVarUtil.IS_REMEMBER,
				false);
		if (isRemember == true) {
			this.rememberPassword.setChecked(true);
			this.account.setText(account);
			this.password.setText(password);
		} else {
			this.rememberPassword.setChecked(false);
			this.account.setText(account);
			this.password.setText("");
		}

	}

	/**
	 * ��ס����
	 */
	private void rememberPassword(String account, String password) {
		SharedPreferences preferences = getSharedPreferences(
				StaticVarUtil.USER_INFO, MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(StaticVarUtil.ACCOUNT, account);
		if (rememberPassword.isChecked() == true) {
			editor.putString(StaticVarUtil.PASSWORD, password);
			editor.putBoolean(StaticVarUtil.IS_REMEMBER, true);// ��ס����
		} else {
			editor.putString(StaticVarUtil.PASSWORD, "");
			editor.putBoolean(StaticVarUtil.IS_REMEMBER, false);// ����ס����
		}
		editor.commit();
	}

	/**
	 * �������̵߳�½
	 */
	private void login() {

		LoginAsyntask loginAsyntask = new LoginAsyntask();
		loginAsyntask.execute();
	}

	/**
	 * �ҵ��ؼ�ID
	 */
	private void findViewById() {
		this.account = (EditText) findViewById(R.id.etAccount);
		this.password = (EditText) findViewById(R.id.etPassword);
		this.forgetPassWord = (Button) findViewById(R.id.butForgetPassword);
		this.rememberPassword = (CheckBox) findViewById(R.id.butRememberPassword);
		this.login = (Button) findViewById(R.id.butLogin);
		this.progressDialog = ViewUtil.getProgressDialog(LoginActivity.this,
				"���ڵ�¼");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!ConnectionUtil.isConn(getApplicationContext())) {
			ConnectionUtil.setNetworkMethod(LoginActivity.this);
			return;
		}
	}

	// �첽���ص�¼
	class LoginAsyntask extends AsyncTask<Object, String, String> {

		@Override
		protected String doInBackground(Object... params) {
			// TODO Auto-generated method stub
			String url;
			url = HttpUtilMc.BASE_URL + "login.jsp?username="
					+ account.getText().toString().trim() + "&password="
					+ password.getText().toString().trim() + "&session=" + session;
			System.out.println("url" + url);
			// ��ѯ���ؽ��
			String result = HttpUtilMc.queryStringForPost(url);
			System.out.println("=========================  " + result);
			return result;

		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// progress.cancel();
			try {
				if (!HttpUtilMc.CONNECT_EXCEPTION.equals(result)) {

					if (!result.equals("error")) {

						listHerf = new ArrayList<HashMap<String,String>>();
						JSONObject json = new JSONObject(result);
						JSONArray jsonArray = (JSONArray) json.get("listHerf");
                        for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject o = (JSONObject) jsonArray.get(i);
							HashMap<String,String> map = new HashMap<String, String>();
							map.put("herf", o.getString("herf"));
							map.put("tittle", o.getString("tittle"));
							listHerf.add(map);
						}
                        StaticVarUtil.listHerf  = listHerf;//����Ϊ��̬
                        Intent intent = new Intent();
    					intent.setClass(LoginActivity.this, MainActivity.class);
    					startActivity(intent);
    					StaticVarUtil.student.setAccount(Integer.valueOf(account.getText().toString().trim()));
    					StaticVarUtil.student.setPassword( password.getText().toString().trim());
    					finish();
					} else {
						Toast.makeText(getApplicationContext(), "��¼ʧ��", 1)
								.show();
					}

				} else {
					Toast.makeText(getApplicationContext(),
							HttpUtilMc.CONNECT_EXCEPTION, 1).show();
					// progress.cancel();
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.i("LoginActivity", e.toString());
			}

		}

	}
}
