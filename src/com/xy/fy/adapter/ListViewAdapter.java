package com.xy.fy.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xy.fy.main.MainActivity;
import com.xy.fy.main.MessageDetailActivity;
import com.xy.fy.main.PictureActivity;
import com.xy.fy.main.R;
import com.xy.fy.singleton.Message;
import com.xy.fy.util.DownLoadThread;
import com.xy.fy.util.ExpressionUtil;
import com.xy.fy.util.ImageDownloader;
import com.xy.fy.util.StaticVarUtil;
import com.xy.fy.util.ViewUtil;

@SuppressLint("HandlerLeak")
public class ListViewAdapter extends BaseAdapter {

	private Context context;// ������
	private ArrayList<Message> allMessage = null;
	private LayoutInflater inflater;
	private boolean isAllowLoad = true;

	@Override
	public View getView(final int position, View view, ViewGroup parent) {

		//����������д��룬���������
		if (view == null) {
			view = inflater.inflate(R.layout.custom_list_item, null);
		}

		final ViewHolder holder = new ViewHolder();
		holder.collect = (Button) view.findViewById(R.id.collect);
		holder.comment = (Button) view.findViewById(R.id.comment);
		holder.date = (TextView) view.findViewById(R.id.date);
		holder.nickname = (TextView) view.findViewById(R.id.nickname);
		holder.praise = (Button) view.findViewById(R.id.praise);
		holder.time = (TextView) view.findViewById(R.id.time);
		holder.headPhoto = (ImageView) view.findViewById(R.id.headPhoto);
		holder.pic = (ImageView) view.findViewById(R.id.pic);
		holder.text = (TextView) view.findViewById(R.id.text);

		final Message message = allMessage.get(position);
		String headPhotoUrl = message.getHeadPhoto();
		String text = message.getText();
		String picUrl = message.getSmaPic();

		holder.collect.setText(message.getColNum() + "");
		holder.comment.setText(message.getComNum() + "");
		holder.date.setText(message.getDate());
		holder.nickname.setText(message.getNickname());
		holder.praise.setText(message.getPraNum() + "");
		holder.time.setText(message.getTime());

		/**
		 * ͷ��
		 */
		if (headPhotoUrl != null && headPhotoUrl.equals("") == false) {// �����Ϊ�գ��첽����
			if (isAllowLoad == true) {
				ImageDownloader downloader = ImageDownloader.getInstance(context);
				holder.headPhoto.setImageBitmap(downloader.getBitmap(holder.headPhoto, headPhotoUrl));
			}
		} else {
			holder.headPhoto.setImageResource(R.drawable.default_head_photo_small);
		}

		/**
		 * �������
		 */
		holder.comment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StaticVarUtil.message = message;
				Intent intent = new Intent();
				intent.setClass(context, MessageDetailActivity.class);
				context.startActivity(intent);
			}
		});

		/**
		 * ˵˵�ı�
		 */
		if (text == null || text.equals("")) {// ���Ϊ�գ���ô�ؼ�����ʾ
			holder.text.setVisibility(View.GONE);
		} else {
			holder.text.setVisibility(View.VISIBLE);
			setTextToPicture(holder.text, text);// ��˵˵������ͼƬ����ʽչ�ֳ���
		}
		holder.text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StaticVarUtil.message = message;
				Intent intent = new Intent();
				intent.setClass(context, MessageDetailActivity.class);
				context.startActivity(intent);
			}
		});
		/**
		 * ˵˵ͼƬ
		 */
		if (picUrl == null || picUrl.equals("")) {
			holder.pic.setVisibility(View.GONE);
		} else {
			holder.pic.setVisibility(View.VISIBLE);
			holder.pic.setImageResource(R.drawable.message_default);
			if (isAllowLoad == true) {// ���������صĻ�
				ImageDownloader downloader = ImageDownloader.getInstance(context);
				holder.pic.setImageBitmap(downloader.getBitmap(holder.pic, picUrl));
			}
		}
		holder.pic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StaticVarUtil.largePicPath = message.getLarPic();// �õ���ͼƬ·��
				StaticVarUtil.smallPicPath = message.getSmaPic();// СͼƬ·��
				Intent intent = new Intent();
				intent.setClass(context, PictureActivity.class);
				context.startActivity(intent);
			}
		});
		/**
		 * �ް�ť
		 */
		if (StaticVarUtil.isPraised(message.getMsgId(), context)) {
			holder.praise.setBackgroundResource(R.drawable.message_praise_yes);
		} else {
			holder.praise.setBackgroundResource(R.drawable.message_praise_no);
		}
		holder.praise.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int messagePraNum = message.getPraNum();
				if (StaticVarUtil.isPraised(message.getMsgId(), context)) {
					ViewUtil.toastShort("���Ѿ��޹���", (MainActivity) context);
					return;
				}
				message.setPraNum(messagePraNum + 1);// ������һ
				StaticVarUtil.praise(message.getMsgId(), context);
				holder.praise.setBackgroundResource(R.drawable.message_praise_yes);
				holder.praise.setText(message.getPraNum() + "");
				// �����߳̽����޲���

				StaticVarUtil.executorService.submit(new DownLoadThread(praiseHandler, message.getMsgId()));
			}
		});

		/**
		 * �ղذ�ť
		 */
		if (StaticVarUtil.isCollect(message.getMsgId(), context) == true) {
			holder.collect.setBackgroundResource(R.drawable.message_collect_yes);
		} else {
			holder.collect.setBackgroundResource(R.drawable.message_collect_no);
		}
		holder.collect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StaticVarUtil.student == null) {
					ViewUtil.toastShort("�Բ����ղ����ȵ�¼...", (MainActivity) context);
					return;
				}
				if (StaticVarUtil.isCollect(message.getMsgId(), context) == true) {
					ViewUtil.toastShort("���Ѿ��ղع���", (MainActivity) context);
					holder.collect.setBackgroundResource(R.drawable.message_collect_yes);
					return;
				}
				StaticVarUtil.collect(message.getMsgId(), context);// �ղ����˵˵
				message.setColNum(message.getColNum() + 1);// ����ѭ����ʾ������
				holder.collect.setBackgroundResource(R.drawable.message_collect_yes);
				holder.collect.setText(message.getColNum() + "");

				StaticVarUtil.executorService.submit(new DownLoadThread(collectHandler, message.getMsgId(), StaticVarUtil.student.getAccount()));
			}
		});

		return view;
	}

	/**
	 * ���ݷ���ֵ���в���,�����collect��handler
	 */
	private Handler collectHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case StaticVarUtil.START:
				break;
			case StaticVarUtil.END_SUCCESS:
				ViewUtil.toastShort("�ղسɹ�", (MainActivity) context);
				break;
			case StaticVarUtil.END_FAIL:
				ViewUtil.toastShort("���Ѿ��ղع���", (MainActivity) context);
				break;
			case StaticVarUtil.INTERNET_ERROR:
				ViewUtil.toastShort("�����쳣", (MainActivity) context);
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
			Drawable d = context.getResources().getDrawable(id);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
	};

	/**
	 * @param dataResource
	 *        ����Դ
	 * @param context
	 */
	public ListViewAdapter(String dataResource, Context context) {
		this.context = context;
		this.allMessage = StaticVarUtil.getAllMessage(dataResource);
		this.inflater = LayoutInflater.from(context);
		// ÿ�μ�¼���һ��˵˵����Ϣ
		if (allMessage.size() > 0) {
			StaticVarUtil.lastMessageTime = allMessage.get(allMessage.size() - 1).getDate() + " " + allMessage.get(allMessage.size() - 1).getTime();
		}
	}

	/**
	 * �������
	 */
	public void addData(String dataResource) {
		if (this.allMessage != null) {
			this.allMessage.addAll(StaticVarUtil.getAllMessage(dataResource));
		}
	}

	/**
	 * �������
	 */
	public void clearData() {
		if (this.allMessage != null) {
			this.allMessage.clear();
		}
	}

	@Override
	public int getCount() {
		return allMessage.size();
	}

	@Override
	public Object getItem(int position) {
		return allMessage.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * ��סʱ���������ͼƬ
	 */
	public void lock() {
		this.isAllowLoad = false;
	}

	/**
	 * ����ʱ����ͼƬ
	 */
	public void unlock() {
		this.isAllowLoad = true;
	}

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
