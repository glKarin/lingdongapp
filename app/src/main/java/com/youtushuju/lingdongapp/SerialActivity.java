package com.youtushuju.lingdongapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.youtushuju.lingdongapp.device.GetOpenDoorReqStruct;
import com.youtushuju.lingdongapp.device.LingDongApi;
import com.youtushuju.lingdongapp.device.PutOpenDoorReqStruct;
import com.youtushuju.lingdongapp.device.SerialDataDef;
import com.youtushuju.lingdongapp.device.SerialPortFunc;
import com.youtushuju.lingdongapp.device.SerialPortFunc_cepr;
import com.youtushuju.lingdongapp.device.SerialReqStruct;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Configs;
import com.youtushuju.lingdongapp.common.Constants;
import com.youtushuju.lingdongapp.common.Logf;
import com.youtushuju.lingdongapp.gui.App;

public class SerialActivity extends AppCompatActivity {
    private static final String ID_TAG = "SerialActivity";

    private LingDongApi m_lingdongApi;
    private Spinner m_typeSpinner;
    private EditText m_doorIdEdit;
    private EditText m_timeEdit;
    private EditText m_tokenEdit;
    private TextView m_respText;
    private TextView m_reqText;
    private SerialPortFunc m_serialPort = null;
    private StringBuffer m_stringBuffer = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_page);

        m_lingdongApi = Configs.Instance().GetLingDongApi(this);

        m_typeSpinner = (Spinner)findViewById(R.id.serial_type);
        m_doorIdEdit = (EditText)findViewById(R.id.serial_door_id);
        m_timeEdit = (EditText)findViewById(R.id.serial_time);
        m_tokenEdit = (EditText)findViewById(R.id.serial_token);
        m_respText = (TextView)findViewById(R.id.serial_resp_text);
        m_reqText = (TextView)findViewById(R.id.serial_req_text);
        SetupUI();

        App.Instance().PushActivity(this);
    }

    private void SetupUI()
    {
        m_timeEdit.setText(SerialReqStruct.CurrentTime());
        m_doorIdEdit.setText(SerialDataDef.ID_DOOR_ID_1);
        m_tokenEdit.setText(SerialDataDef.ID_SERIAL_DATA_DEFAULT_TOKEN);

        m_typeSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{
                SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR,
                SerialDataDef.ID_SERIAL_DATA_TYPE_GET_OPEN_DOOR,
        }));
        m_typeSpinner.setSelection(0);
        ((Button)findViewById(R.id.serial_send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = m_typeSpinner.getSelectedItem().toString();
                if(Common.StringIsBlank(type))
                {
                    OpenWarningDialog("请选择消息类型");
                    return;
                }
                String doorId = m_doorIdEdit.getText().toString();
                if(Common.StringIsBlank(doorId))
                {
                    OpenWarningDialog("请输入门ID");
                    return;
                }
                String time = m_timeEdit.getText().toString();
                if(Common.StringIsBlank(time))
                {
                    time = PutOpenDoorReqStruct.CurrentTime();
                    m_timeEdit.setText(time);
                }
                String token = m_tokenEdit.getText().toString();
                if(Common.StringIsBlank(token))
                {
                    token = SerialDataDef.ID_SERIAL_DATA_DEFAULT_TOKEN;
                    m_tokenEdit.setText(token);
                }
                SerialReqStruct d = null;
                switch (type)
                {
                    case SerialDataDef.ID_SERIAL_DATA_TYPE_GET_OPEN_DOOR:
                        d = new GetOpenDoorReqStruct();
                        break;
                    case SerialDataDef.ID_SERIAL_DATA_TYPE_PUT_OPEN_DOOR:
                        d = new PutOpenDoorReqStruct();
                        break;
                    default:
                        OpenWarningDialog("类型错误");
                        break;
                }
                if(d != null)
                {
                    d.type = type;
                    if(d instanceof GetOpenDoorReqStruct) ((GetOpenDoorReqStruct)d).door_id = doorId;
                    else if(d instanceof PutOpenDoorReqStruct) ((PutOpenDoorReqStruct)d).door_id = doorId;
                    d.token = token;
                    d.time = time;
                    Send(d);
                }
            }
        });
        ((Button)findViewById(R.id.serial_clear)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (m_stringBuffer) {
                    m_stringBuffer.setLength(0);
                }
                m_respText.setText("");
            }
        });
    }

    private void Send(SerialReqStruct d)
    {
        String json = d.Dump() + '\r'; // 添加回车符
        Logf.d(ID_TAG, json);
        m_reqText.setText(json);

        if(m_serialPort == null)
        {
            m_serialPort = Configs.Instance().GetLingDongSerialDriver(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String path = preferences.getString(Constants.ID_PREFERENCE_SERIAL_PATH, Configs.ID_PREFERENCE_DEFAULT_SERIAL_PATH);
            int baudrate = Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE;
            try
            {
                baudrate = Integer.parseInt(preferences.getString(Constants.ID_PREFERENCE_SERIAL_BAUDRATE, "" + Configs.ID_PREFERENCE_DEFAULT_SERIAL_BAUDRATE));
            }
            catch (Exception e)
            {
                App.HandleException(e);
            }
            if(!m_serialPort.InitSerialPort(path, baudrate))
            {
                m_respText.setText("串口初始化错误");
                OpenWarningDialog("串口初始化错误");
                return;
            }
            if(!m_serialPort.Open())
            {
                m_respText.setText("串口打开错误");
                OpenWarningDialog("串口打开错误");
                return;
            }
            m_serialPort.SetOnDataReceivedListener(m_dataReceivedListener);
        }
        int len = m_serialPort.Send(
                //json.getBytes()
                Common.String8BitsByteArray(json) // TODO: 8bits
        );
        if(len <= 0)
        {
            OpenWarningDialog("串口写入错误: " + len);
        }
        //OpenWarningDialog(json);
    }

    private SerialPortFunc_cepr.OnDataReceivedListener m_dataReceivedListener = new SerialPortFunc_cepr.OnDataReceivedListener() {
        @Override
        public void OnDataReceived(byte[] data, int length) {
            String json = new String(data, 0, length);

            synchronized (m_stringBuffer) {
                m_stringBuffer.append(json);
            }
            Logf.d(ID_TAG, "本次接收(%s), 长度(%d)", json, length);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_respText.setText(m_stringBuffer.toString());
                }
            });
        }
    };

    private void OpenWarningDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("错误");
        builder.setMessage(message);
        builder.setIcon(R.drawable.icon_error);
        builder.setPositiveButton("确定", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.Instance().PopActivity();
        if(m_serialPort != null)
        {
            m_serialPort.ShutdownSerialPort();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(m_serialPort != null)
        {
            m_serialPort.ShutdownSerialPort();
            m_serialPort = null;
        }
    }
}
