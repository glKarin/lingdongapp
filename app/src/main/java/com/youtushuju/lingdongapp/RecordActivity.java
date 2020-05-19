package com.youtushuju.lingdongapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.database.DB;
import com.youtushuju.lingdongapp.database.PagedArrayAdapter_base;
import com.youtushuju.lingdongapp.database.RecordModel;
import com.youtushuju.lingdongapp.database.RecordServices;
import com.youtushuju.lingdongapp.gui.App;
import com.youtushuju.lingdongapp.gui.IndicatorDialog;
import com.youtushuju.lingdongapp.gui.PagedListView;

public class RecordActivity extends AppCompatActivity {
    private static final String ID_TAG = "RecordActivity";
    private PagedListView m_listView;
    private RecordListAdapter m_adapter;
    private RecordServices m_recordDB = null;
    private RecordServices.RecordCond m_recordCond = null;
    private RecordServices.RecordOrder m_recordOrder = null;
    private AlertDialog m_progressDialog = null;
    private Handler m_handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record_page);

        m_recordDB = new RecordServices(this);
        m_recordOrder = new RecordServices.RecordOrder();
        m_listView = (PagedListView)findViewById(R.id.record_content);

        m_adapter = new RecordListAdapter(this);
        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        m_listView.setAdapter(m_adapter);
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OpenRecordDetail(m_adapter.getItem((int)id /*position - 1*/));
            }
        });

        PagedListView.DataProvider provider = new PagedListView.DataProvider() {
            public void RefreshData()
            {
                LoadRecordList(0);
            }

            public void MoreData()
            {
                if(!m_adapter.GetPaged().HasNext())
                {
                    Toast.makeText(RecordActivity.this, "没有更多...", Toast.LENGTH_LONG).show();
                    return;
                }
                LoadRecordList(1);
            }
        };
        m_listView.SetDataProvider(provider);

    }

    private void BeginLoading()
    {
        if(m_progressDialog != null)
        {
            m_progressDialog.dismiss();
            m_progressDialog = null;
        }
        m_listView.EndMoreLoading(true);

        if(m_listView.LoadMode() == PagedListView.PAGED_LIST_VIEW_MODE_MORE)
            m_listView.BeginMoreLoading();
        else
        {
            m_progressDialog = new IndicatorDialog(this);
            m_progressDialog.show();
        }
    }

    private void EndLoading(boolean success)
    {
        if(m_progressDialog != null)
        {
            m_progressDialog.dismiss();
            m_progressDialog = null;
        }
        if(m_listView.LoadMode() == PagedListView.PAGED_LIST_VIEW_MODE_MORE && !success)
            m_listView.EndMoreLoading(false);
        else
            m_listView.EndMoreLoading(true);
    }

    private void LoadRecordList(int o)
    {
        BeginLoading();
        if(o == 0)
            m_adapter.clear();
        else if(o > 0)
            m_adapter.GetPaged().NextPage();
        m_adapter.SetPaged(m_recordDB.List(m_recordCond, m_recordOrder, m_adapter.GetPaged()));
        m_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EndLoading(true);
            }
        }, 500);
    }

    private void CleanRecord()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        builder.setMessage("确定要清空所有记录?");
        builder.setIcon(R.drawable.icon_warning);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_recordDB.DeleteAll();
                LoadRecordList(0);
                Toast.makeText(RecordActivity.this, "记录已清空", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        
        dialog.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.record_page_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.record_menu_clean_record:
                CleanRecord();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
    ;
    private void OpenRecordDetail(final RecordModel record)
    {
        final String title = "记录详情";
        final String message = record.toString();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(title, message);
                        cm.setPrimaryClip(clipData);
                        Toast.makeText(RecordActivity.this, "记录详情已复制到粘贴板", Toast.LENGTH_LONG).show();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        if(m_recordDB.DeleteById(record.Id()))
                        {
                            LoadRecordList(0);
                            Toast.makeText(RecordActivity.this, "删除该条记录成功", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(RecordActivity.this, "删除该条记录失败", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(R.drawable.icon_info);
        builder.setPositiveButton("复制", listener);
        builder.setNegativeButton("关闭", null);
        builder.setNeutralButton("删除", listener);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.Instance().PopActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadRecordList(0);
    }

    // internal
    private class RecordListAdapter extends PagedArrayAdapter_base<RecordModel>
    {
        public RecordListAdapter(Context context)
        {
            super(context, R.layout.record_deledate);
        }

        public View GenerateView(int position, View view, ViewGroup parent, RecordModel data)
        {
            TextView textView;
            int result = data.Result();

            textView = (TextView)view.findViewById(R.id.record_delegate_name);
            textView.setText(data.Name());
            textView = (TextView)view.findViewById(R.id.record_delegate_device);
            textView.setText(data.Device());
            textView = (TextView)view.findViewById(R.id.record_delegate_weight);
            textView.setText(data.Weight());
            textView = (TextView)view.findViewById(R.id.record_delegate_operation);
            textView.setText(data.Operation());
            textView = (TextView)view.findViewById(R.id.record_delegate_time);
            textView.setText(Common.TimestampToStr(data.Time()));

            //int color = result == RecordModel.ID_RESULT_SUCCESS ? Color.GREEN : (result == RecordModel.ID_RESULT_ERROR ? Color.RED : Color.YELLOW);
            int resource = result == RecordModel.ID_RESULT_SUCCESS ? R.drawable.icon_success : (result == RecordModel.ID_RESULT_ERROR ? R.drawable.icon_error : R.drawable.icon_warning);

            ((ImageView)view.findViewById(R.id.record_delegate_icon)).setImageResource(resource);

            return view;
        }
    }
}