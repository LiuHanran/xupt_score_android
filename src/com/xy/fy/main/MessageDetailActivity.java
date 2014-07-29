package com.xy.fy.main;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xy.fy.adapter.CommentAdapter;
import com.xy.fy.singleton.Comment;
import com.xy.fy.singleton.Message;
import com.xy.fy.util.DownLoadThread;
import com.xy.fy.util.ExpressionUtil;
import com.xy.fy.util.ImageDownloader;
import com.xy.fy.util.StaticVarUtil;
import com.xy.fy.util.ViewUtil;
import com.xy.fy.view.CustomListView;
import com.xy.fy.view.CustomListView.OnMoreButtonListener;

/**
 * ˵˵���飬�󲿷���ListViewAdapter�еĴ��븴�ã�����ListView�ǿ�
 * 
 * @author Administrator
 * 
 */
@SuppressLint("HandlerLeak")
public class MessageDetailActivity extends Activity {

	private ViewHolder holder = null;
	private CustomListView listView = null;
	private Message message = null;
	private CommentAdapter adapter = null;
	private int page = 0;
	private ProgressBar progress = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_message_detail);

		message = StaticVarUtil.message;

		String headPhotoUrl = message.getHeadPhoto();
		String text = message.getText();
		String picUrl = message.getSmaPic();

		holder = new ViewHolder();
		holder.collect = (Button) findViewById(R.id.collect);
		holder.comment = (Button) findViewById(R.id.comment);
		holder.date = (TextView) findViewById(R.id.date);
		holder.nickname = (TextView) findViewById(R.id.nickname);
		holder.praise = (Button) findViewById(R.id.praise);
		holder.time = (TextView) findViewById(R.id.time);
		holder.headPhoto = (ImageView) findViewById(R.id.headPhoto);
		holder.pic = (ImageView) findViewById(R.id.pic);
		holder.text = (TextView) findViewById(R.id.text);

		listView = (CustomListView) findViewById(R.id.commentListView);

		progress = (ProgressBar) findViewById(R.id.progress);

		holder.collect.setText(message.getColNum() + "");
		holder.comment.setText(message.getComNum() + "");
		holder.date.setText(message.getDate());
		holder.nickname.setText(message.getNickname());
		holder.praise.setText(message.getPraNum() + "");
		holder.time.setText(message.getTime());

		/**
		 * ���ؼ�
		 */
		Button back = (Button) findViewById(R.id.butBack);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * ͷ��
		 */
		if (headPhotoUrl != null && headPhotoUrl.equals("") == false) {// �����Ϊ�գ��첽����
			ImageDownloader downloader = ImageDownloader.getInstance(MessageDetailActivity.this);
			holder.headPhoto.setImageBitmap(downloader.getBitmap(holder.headPhoto, headPhotoUrl));
		} else {
			holder.headPhoto.setImageResource(R.drawable.default_head_photo_small);
		}

		/**
		 * ˵˵�ı�
		 */
		if (text == null || text.equals("")) {// ���Ϊ�գ���ô�ؼ�����ʾ
			holder.text.setVisibility(View.GONE);
		} else {
			holder.text.setVisibility(View.VISIBLE);
			setTextToPicture(holder.text, text);// ��˵˵������ͼƬ����ʽչ�ֳ���
		}

		/**
		 * ˵˵ͼƬ
		 */
		if (picUrl == null || picUrl.equals("")) {
			holder.pic.setVisibility(View.GONE);
		} else {
			holder.pic.setVisibility(View.VISIBLE);
			holder.pic.setImageResource(R.drawable.message_default);
			ImageDownloader downloader = ImageDownloader.getInstance(MessageDetailActivity.this);
			holder.pic.setImageBitmap(downloader.getBitmap(holder.pic, picUrl));
		}
		holder.pic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StaticVarUtil.largePicPath = message.getLarPic();// �õ���ͼƬ·��
				StaticVarUtil.smallPicPath = message.getSmaPic();// СͼƬ·��
				Intent intent = new Intent();
				intent.setClass(MessageDetailActivity.this, PictureActivity.class);
				startActivity(intent);
			}
		});

		/**
		 * �ް�ť
		 */
		if (StaticVarUtil.isPraised(message.getMsgId(), MessageDetailActivity.this)) {
			holder.praise.setBackgroundResource(R.drawable.message_praise_yes);
		} else {
			holder.praise.setBackgroundResource(R.drawable.message_praise_no);
		}
		holder.praise.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int messagePraNum = message.getPraNum();
				if (StaticVarUtil.isPraised(message.getMsgId(), MessageDetailActivity.this)) {
					ViewUtil.toastShort("���Ѿ��޹���", MessageDetailActivity.this);
					return;
				}
				message.setPraNum(messagePraNum + 1);// ������һ
				StaticVarUtil.praise(message.getMsgId(), MessageDetailActivity.this);
				holder.praise.setBackgroundResource(R.drawable.message_praise_yes);
				holder.praise.setText(message.getPraNum() + "");
				// �����߳̽����޲���

				StaticVarUtil.executorService.submit(new DownLoadThread(praiseHandler, message.getMsgId()));
			}
		});

		/**
		 * �ղذ�ť
		 */
		if (StaticVarUtil.isCollect(message.getMsgId(), MessageDetailActivity.this) == true) {
			holder.collect.setBackgroundResource(R.drawable.message_collect_yes);
		} else {
			holder.collect.setBackgroundResource(R.drawable.message_collect_no);
		}
		holder.collect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StaticVarUtil.student == null) {
					ViewUtil.toastShort("�Բ����ղ����ȵ�¼...", MessageDetailActivity.this);
					return;
				}
				if (StaticVarUtil.isCollect(message.getMsgId(), MessageDetailActivity.this) == true) {
					ViewUtil.toastShort("���Ѿ��ղع���", MessageDetailActivity.this);
					holder.collect.setBackgroundResource(R.drawable.message_collect_yes);
					return;
				}
				StaticVarUtil.collect(message.getMsgId(), MessageDetailActivity.this);// �ղ����˵˵
				message.setColNum(message.getColNum() + 1);// ����ѭ����ʾ������
				holder.collect.setBackgroundResource(R.drawable.message_collect_yes);
				holder.collect.setText(message.getColNum() + "");

				StaticVarUtil.executorService.submit(new DownLoadThread(collectHandler, message.getMsgId(), StaticVarUtil.student.getAccount()));
			}
		});

		final EditText etComment = (EditText) findViewById(R.id.etComment);

		/**
		 * ȷ��������Ϣ��ť
		 */
		Button butConfirm = (Button) findViewById(R.id.butConfirm);
		butConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StaticVarUtil.student == null) {
					ViewUtil.toastShort("�Բ����������ȵ�¼...", MessageDetailActivity.this);
					return;
				}
				if (etComment.getText().toString() == null || etComment.getText().toString().equals("")) {
					ViewUtil.toastShort("���۲���Ϊ�գ�", MessageDetailActivity.this);
					return;
				}
				// �������˵˵
				// �����̷߳�������
				String comment = etComment.getText().toString();
				int account = StaticVarUtil.student.getAccount();
				int messageId = message.getMsgId();

				etComment.setText("");

				StaticVarUtil.executorService.submit(new DownLoadThread(handler, messageId, account, comment));

			}
		});

		listView.setOnMoreListener(new OnMoreButtonListener() {
			@Override
			public void onClick(View v) {
				listView.start();
				page++;
				StaticVarUtil.executorService.submit(new DownLoadThread(handlerCommentMore, message.getMsgId() + "", page + ""));
			}
		});

		StaticVarUtil.executorService.submit(new DownLoadThread(handlerComment, message.getMsgId() + "", page + ""));
	}

	/**
	 * ���ظ������۵�handler
	 */
	private Handler handlerCommentMore = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case StaticVarUtil.START:
				listView.start();
				break;
			case StaticVarUtil.END_SUCCESS:
				listView.finish();
				adapter.addComments(StaticVarUtil.response);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				listView.setSelection(page * 20);
				break;
			case StaticVarUtil.END_FAIL:
				listView.finish();
				ViewUtil.toastShort("��������ʧ��", MessageDetailActivity.this);
				break;
			case StaticVarUtil.INTERNET_ERROR:
				listView.finish();
				ViewUtil.toastShort("�����쳣", MessageDetailActivity.this);
				break;
			default:
				break;
			}
		};
	};
	/**
	 * �������۵�handler
	 */
	private Handler handlerComment = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case StaticVarUtil.START:
				progress.setVisibility(View.VISIBLE);
				break;
			case StaticVarUtil.END_SUCCESS:
				progress.setVisibility(View.GONE);
				ArrayList<Comment> allComments = StaticVarUtil.getAllComments(StaticVarUtil.response);
				adapter = new CommentAdapter(allComments, MessageDetailActivity.this);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				break;
			case StaticVarUtil.END_FAIL:
				progress.setVisibility(View.GONE);
				ViewUtil.toastShort("��������ʧ��", MessageDetailActivity.this);
				break;
			case StaticVarUtil.INTERNET_ERROR:
				progress.setVisibility(View.GONE);
				ViewUtil.toastShort("�����쳣", MessageDetailActivity.this);
				break;
			default:
				break;
			}
		};
	};

	/**
	 * ���۵�handler
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case StaticVarUtil.START:
				break;
			case StaticVarUtil.END_SUCCESS:
				ViewUtil.toastShort("���۳ɹ�", MessageDetailActivity.this);
				page = 0;
				StaticVarUtil.executorService.submit(new DownLoadThread(handlerComment, message.getMsgId() + "", page + ""));
				break;
			case StaticVarUtil.END_FAIL:
				ViewUtil.toastShort("����ʧ��", MessageDetailActivity.this);
				break;
			case StaticVarUtil.INTERNET_ERROR:
				ViewUtil.toastShort("�����쳣", MessageDetailActivity.this);
				break;
			default:
				break;
			}
		};
	};

	/**
	 * ���ݷ���ֵ���в���,�����collect��handler
	 */
	private Handler collectHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case StaticVarUtil.START:
				break;
			case StaticVarUtil.END_SUCCESS:
				ViewUtil.toastShort("�ղسɹ�", MessageDetailActivity.this);
				break;
			case StaticVarUtil.END_FAIL:
				ViewUtil.toastShort("���Ѿ��ղع���", MessageDetailActivity.this);
				break;
			case StaticVarUtil.INTERNET_ERROR:
				ViewUtil.toastShort("�����쳣", MessageDetailActivity.this);
				break;
			default:
				break;
			}
		};
	};

	/**
	 * ���ݷ���ֵ���в���,�����praise��handler
	 */
	private Handler praiseHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
		};
	};

	/**
	 * ������ת��ΪͼƬ
	 * 
	 * @param textView
	 * @param text
	 */
	private void setTextToPicture(TextView textView, String text) {
		textView.setText(text);
		for (int i = 1; i <= 140; i++) {
			while (text.contains("[expression" + i + "]")) {
				int start = text.indexOf("[expression" + i + "]");// 13����ĸ
				int end = 0;
				if (i < 10) {
					end = start + 13;
				} else if (i >= 10 && i < 100) {
					end = start + 14;
				} else if (i >= 100) {
					end = start + 15;
				}
				text = text.substring(0, start) + "<img src=\"" + ExpressionUtil.all.get("expression" + i) + "\" />" + text.substring(end);
			}
		}
		textView.setText(Html.fromHtml(text, imageGetter, null));
	}

	/**
	 * ����sourceת��ΪͼƬ����
	 */
	private ImageGetter imageGetter = new Html.ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			int id = Integer.valueOf(source);
			Drawable d = MessageDetailActivity.this.getResources().getDrawable(id);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
	};

	/**
	 * �����࣬�������б��е�ÿһ��Item����������
	 */
	class ViewHolder {
		ImageView headPhoto;// ͷ��
		TextView nickname;// �ǳ�
		TextView date;// ����
		TextView time;// ʱ��
		TextView text;// �ı���Ϣ
		ImageView pic;// ͼƬ��Ϣ
		Button praise;// ��
		Button comment;// ����
		Button collect;// �ղ�
	}
}
