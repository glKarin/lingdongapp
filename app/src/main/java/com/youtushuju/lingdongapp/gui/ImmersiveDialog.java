package com.youtushuju.lingdongapp.gui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;

public class ImmersiveDialog extends AlertDialog
{
	public ImmersiveDialog(Context context)
	{
		super(context, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Window w = getWindow();
		w.setDimAmount(0);
		w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setCancelable(false);
	}
}
