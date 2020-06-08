package com.youtushuju.lingdongapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.MovableView;

public class LayoutActivity extends AppCompatActivity {
    private static final String ID_TAG = "LayoutActivity";
    private MovableView m_menuButton;
    private View m_contentView;
    private SharedPreferences m_sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_page);
        m_menuButton = (MovableView)findViewById(R.id.layout_menu_button);
        m_contentView = findViewById(android.R.id.content);
        m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI() {
        m_menuButton.SetPositionListener(new MovableView.PositionListener() {
            @Override
            public void OnPositionChanged(int x, int y, int relativeX, int relativeY, int lastDeltaX, int lastDeltaY, int startDeltaX, int startDeltaY) {
                m_menuButton.setX(x - relativeX);
                m_menuButton.setY(y - relativeY);
                int marginRight = m_contentView.getWidth() - (x - relativeX) - m_menuButton.getWidth();
                int marginTop = y - relativeY;
                String value = String.format("%d,%d,%d,%d", -marginRight, marginTop, m_menuButton.getWidth(), m_menuButton.getHeight());
                SharedPreferences.Editor editor = m_sharedPreferences.edit();
                {
                    editor.putString(Constants.ID_PREFERENCE_MAIN_MENU_GEOMETRY, value);
                }
                editor.commit();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus)
            return;

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String menuGeometry = preferences.getString(Constants.ID_PREFERENCE_MAIN_MENU_GEOMETRY, Configs.ID_PREFERENCE_DEFAULT_MAIN_MENU_GEOMETRY);
        if(!Common.StringIsEmpty(menuGeometry))
        {
            String parts[] = menuGeometry.split(",");
            if(parts.length >= 2)
            {
                try {
                    int horizontalMargin = Integer.parseInt(parts[0]);
                    int verticalMargin = Integer.parseInt(parts[1]);
                    m_menuButton.setX(horizontalMargin >= 0 ? horizontalMargin : (m_contentView.getWidth() - m_menuButton.getWidth() - -horizontalMargin));
                    m_menuButton.setY(verticalMargin >= 0 ? verticalMargin : (m_contentView.getHeight() - m_menuButton.getHeight() - -verticalMargin));
                    //Logf.e(ID_TAG, "%d %d", horizontalMargin >= 0 ? horizontalMargin : (m_contentView.getWidth() - m_menuButton.getWidth() - horizontalMargin), verticalMargin >= 0 ? verticalMargin : (m_contentView.getHeight() - m_menuButton.getHeight() - verticalMargin));
                }
                catch (Exception e)
                {
                    App.HandleException(e);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.Instance().PopActivity();
    }
}
