package com.youtushuju.lingdongapp.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;

public class IndicatorDialog extends AlertDialog
{
	private ProgressBar m_progressBar = null;
	
	public IndicatorDialog(Context context)
	{
		super(context, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		m_progressBar = new ProgressBar(getContext());
		m_progressBar.setIndeterminate(true);
		
		setContentView(m_progressBar);
		
		Window w = getWindow();
		w.setDimAmount(0);
		w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setCancelable(false);
	}
}
