package tiaohaoquan.com.uploadimage;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wuchangyu222 on 2015/12/22.
 */
public class HttpPostUtil {
    URL url;
    HttpURLConnection conn;
    String boundary = UUID.randomUUID().toString();
    Map<String, String> textParams = new HashMap<String, String>();
    Map<String, File> fileparams = new HashMap<String, File>();
    DataOutputStream ds = null;
    String contentType;

    public HttpPostUtil(String url) throws Exception {
        this.url = new URL(url);
    }

    //重新设置要请求的服务器地址，即上传文件的地址。
    public void setUrl(String url) throws Exception {
        this.url = new URL(url);
    }

    //设置文件的上传类型，图片格式为image/png,image/jpg等。非图片为application/octet-stream
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    //增加一个普通字符串数据到form表单数据中
    public void addTextParameter(String name, String value) {
        textParams.put(name, value);
    }

    //增加一个文件到form表单数据中
    public void addFileParameter(String name, File value) {
        fileparams.put(name, value);
    }

    // 清空所有已添加的form表单数据
    public void clearAllParameters() {
        textParams.clear();
        fileparams.clear();
    }

    //     发送数据到服务器
    public void send() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                InputStream in = null;
                ByteArrayOutputStream baos = null;
                try {
                    initConnection();
                    conn.connect();
                    ds = new DataOutputStream(conn.getOutputStream());
                    writeFileParams();
                    writeStringParams();
                    paramsEnd();
                    Log.e("HttpRequest", "网络返回码:" + conn.getResponseCode());
                    if (conn.getResponseCode() == 200) {
                        in = conn.getInputStream();
                        // 创建字节输出流对象
                        baos = new ByteArrayOutputStream();
                        // 定义读取的长度
                        int len = 0;
                        // 定义缓冲区
                        int bufferLen = in.available();
                        Log.e("HttpRequest", "缓冲区大小:" + bufferLen);
                        if (bufferLen == 0) {
                            return null;
                        }
                        byte buffer[] = new byte[bufferLen];
                        // 按照缓冲区的大小，循环读取
                        while ((len = in.read(buffer)) != -1) {
                            // 根据读取的长度写入到os对象中
                            baos.write(buffer, 0, len);

                            Log.e("HttpRequest", "正在访问数据");
                        }
                        String result = new String(baos.toByteArray());
                        Log.e("HttpRequest", "访问数据成功");
                        return result;

                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    try {
                        if (ds != null) {
                            ds.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                        if (baos != null) {
                            baos.close();
                        }
                        if (conn != null) {
                            conn.disconnect();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

            }
        }.execute();
    }

    //文件上传的connection的一些必须设置
    private void initConnection() throws Exception {
        conn = (HttpURLConnection) this.url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(10000); //连接超时为10秒
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
    }

    //普通字符串数据
    private void writeStringParams() throws Exception {
        Set<String> keySet = textParams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
            String name = it.next();
            String value = textParams.get(name);
            ds.writeBytes("--" + boundary + "\r\n");
            ds.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"\r\n");
            ds.writeBytes("\r\n");
            ds.writeBytes(encode(value) + "\r\n");
        }
    }

    //文件数据
    private void writeFileParams() throws Exception {
        Set<String> keySet = fileparams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
            String name = it.next();
            File value = fileparams.get(name);
            ds.writeBytes("--" + boundary + "\r\n");
            ds.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"; filename=\"" + encode(value.getName()) + "\"\r\n");
            ds.writeBytes("Content-Type: " + contentType + "\r\n");
            ds.writeBytes("\r\n");
            ds.write(getBytes(value));
            ds.writeBytes("\r\n");
        }
    }

    //把文件转换成字节数组
    private byte[] getBytes(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[in.available()];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        in.close();
        return out.toByteArray();
    }

    //添加结尾数据
    private void paramsEnd() throws Exception {
        ds.writeBytes("--" + boundary + "--" + "\r\n");
        ds.writeBytes("\r\n");
    }

    // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
    private String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

}
