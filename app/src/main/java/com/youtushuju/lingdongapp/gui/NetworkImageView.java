package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.network.NetworkAccessManager;
import com.youtushuju.lingdongapp.network.NetworkReply;
import com.youtushuju.lingdongapp.network.NetworkRequest;

import java.io.ByteArrayInputStream;

public class NetworkImageView extends AppCompatImageView
{
	private static final String ID_TAG = "NetworkImageView";
	private String m_url = null;
	private NetworkAccessManager m_manager = null;
	
	public NetworkImageView(Context context)
	{
		super(context);
	}

	public NetworkImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public NetworkImageView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	private NetworkAccessManager InstanceNetworkAccessManager()
	{
		if(m_manager != null)
		{
			m_manager = new NetworkAccessManager();
			m_manager.SetTimeout(30000);
		}
		return m_manager;
	}
	
	public void SetUrl(String url)
	{
		m_url = url;
		if(Common.StringIsEmpty(m_url))
			return;
		NetworkAccessManager manager = InstanceNetworkAccessManager();
		NetworkRequest req = new NetworkRequest(url);
		manager.Get(req, new NetworkAccessManager.NetworkReplyHandler(){
			@Override
			public void Handle(NetworkReply reply)
			{
				Message msg = new Message();
				if(!reply.GetResponseResult())
				{
					Logf.e(ID_TAG, "加载网络图片失败");
				}
				else
				{
					byte data[] = reply.GetReplyData();
					final Drawable image = Drawable.createFromStream(new ByteArrayInputStream(data), m_url);
					CacheImage(m_url, data);
					NetworkImageView.this.post(new Runnable() {
						@Override
						public void run() {
							setImageDrawable(image);
						}
					});
				}
			}
		});
	}

	public String Url()
	{
		return m_url;
	}
	
	private synchronized String CacheImage(String url, byte data[])
	{
		// TODO
		return null;
	}
}
