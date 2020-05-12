package com.youtushuju.lingdongapp.network;

import android.webkit.MimeTypeMap;

import java.io.*;
import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Crypto;
import com.youtushuju.lingdongapp.common.Logf;

public final class HttpUtility
{
    private HttpUtility(){}

    public static byte[] UploadFile(File file, String url, String name)
    {
        if(!file.isFile())
            return null;

        ByteArrayOutputStream os = null;
        String randStr = android.util.Base64.encodeToString(("" + System.currentTimeMillis()).getBytes(), android.util.Base64.NO_WRAP);
        String boundaryValue = Crypto.MD5(randStr).substring(8, 8 + 16);
        String boundary = "----WebKitFormBoundary" + boundaryValue;
        String filename = file.getName();
        String filetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl("file://" + file.getAbsolutePath()));
        FileInputStream fis = null;
        String nameParam = Common.StringIsBlank(name) ? "file" : name;
        byte ret[] = null;

        try
        {
            fis = new FileInputStream(file);
            int length = fis.available();
            byte filedata[] = new byte[length];
            int len = fis.read(filedata);
            if(len == length)
            {
                os = new ByteArrayOutputStream();
                // 额外参数
                /*os.write(Common.String8BitsByteArray("--"));
                os.write(Common.String8BitsByteArray(boundary));
                os.write(Common.String8BitsByteArray("\r\n"));
                os.write(Common.String8BitsByteArray("Content-Disposition: form-data; name=\"type\""));
                os.write(Common.String8BitsByteArray("\r\n"));
                os.write(Common.String8BitsByteArray("\r\n"));
                os.write(Common.String8BitsByteArray(type));
                os.write(Common.String8BitsByteArray("\r\n"));*/

                os.write(Common.String8BitsByteArray("--"));
                os.write(Common.String8BitsByteArray(boundary));
                os.write(Common.String8BitsByteArray("\r\n"));
                os.write(("Content-Disposition: form-data; name=\"" + nameParam + "\"; filename=\"" + filename + "\"").getBytes());
                os.write(Common.String8BitsByteArray("\r\n"));
                os.write(("Content-Type: " + filetype).getBytes());
                os.write(Common.String8BitsByteArray("\r\n"));
                os.write(Common.String8BitsByteArray("\r\n"));
                os.write(filedata);
                os.write(Common.String8BitsByteArray("\r\n"));

                os.write(Common.String8BitsByteArray("--"));
                os.write(Common.String8BitsByteArray(boundary));
                os.write(Common.String8BitsByteArray("--"));

                os.flush();

                NetworkAccessManager manager = new NetworkAccessManager();
                NetworkRequest req = new NetworkRequest(url);
                req.AddHeader("Content-Type", "multipart/form-data;boundary=" + boundary);
                NetworkReply reply = manager.SyncPost(req, os.toByteArray());
                if(reply != null)
                {
                    ret = reply.GetReplyData();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            try
            {
                if(fis != null)
                    fis.close();
                if(os != null)
                    os.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    ;
        return ret;
    }
}

