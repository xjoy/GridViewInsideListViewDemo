package com.src.zhang.demo;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by levin on 6/13/14.
 */
public class ApiClient {

    public static final String UTF_8 = "UTF-8";
    public static final String DESC = "descend";
    public static final String ASC = "ascend";

    private final static int TIMEOUT_CONNECTION = 20000;
    private final static int TIMEOUT_SOCKET = 20000;
    private final static int RETRY_TIME = 3;

    private static String appCookie;
    private static String appUserAgent;

    private final static String URL_HOST = "qq.com";

    public static void cleanCookie() {
        appCookie = "";
    }

    private static String getCookie(AppContext appContext) {
        if(appCookie == null || appCookie == "") {
            appCookie = appContext.getProperty("cookie");
        }
        return appCookie;
    }

    private static String getUserAgent(AppContext appContext) {
        if(appUserAgent == null || appUserAgent == "") {
            StringBuilder ua = new StringBuilder("3gz.qq.com");
            ua.append('/'+appContext.getPackageInfo().versionName+'_'+appContext.getPackageInfo().versionCode);//App版本
            ua.append("/Android");//手机系统平台
            ua.append("/"+android.os.Build.VERSION.RELEASE);//手机系统版本
            ua.append("/"+android.os.Build.MODEL); //手机型号
            ua.append("/"+appContext.getAppId());//客户端唯一标识
            appUserAgent = ua.toString();
        }
        return appUserAgent;
    }

    private static HttpClient getHttpClient() {
        /*
        HttpClient httpClient = new HttpClient();
        // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        // 设置 默认的超时重试处理策略
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        // 设置 连接超时时间
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT_CONNECTION);
        // 设置 读数据超时时间
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(TIMEOUT_SOCKET);
        // 设置 字符集
        httpClient.getParams().setContentCharset(UTF_8);
        return httpClient;
        */

        HttpClient httpClient = HttpUtil.getNewHttpClient();
        HttpParams params = httpClient.getParams();
        // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
        HttpClientParams.setCookiePolicy(params,CookiePolicy.BROWSER_COMPATIBILITY);
        // 设置 默认的超时重试处理策略
        //httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        // 设置 连接超时时间
        HttpConnectionParams.setConnectionTimeout(params,TIMEOUT_CONNECTION);
        // 设置 读数据超时时间
        HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);

        return httpClient;

    }

    private static HttpGet getHttpGet(String url, String cookie, String userAgent) {
        HttpGet httpGet = new HttpGet(url);
        HttpParams params = httpGet.getParams();
        // 设置 请求超时时间
        HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);

        //httpGet.setHeader("Host", URL_HOST);
        httpGet.setHeader("Connection", "Keep-Alive");
        httpGet.setHeader("Cookie", cookie);
        httpGet.setHeader("User-Agent", userAgent);

        // 设置 默认的超时重试处理策略


        return httpGet;
    }

    private static String _MakeURL(String p_url, Map<String, Object> params) {
        StringBuilder url = new StringBuilder(p_url);
        if(url.indexOf("?")<0)
            url.append('?');

        for(String name : params.keySet()){
            url.append('&');
            url.append(name);
            url.append('=');
            url.append(String.valueOf(params.get(name)));
            //不做URLEncoder处理
            //url.append(URLEncoder.encode(String.valueOf(params.get(name)), UTF_8));
        }

        return url.toString().replace("?&", "?");
    }

    /**
     * get请求URL
     * @param url
     * @throws AppException
     */
    private static InputStream http_get(AppContext appContext, String url) throws AppException {
        String cookie = getCookie(appContext);
        String userAgent = getUserAgent(appContext);

        HttpClient httpClient = null;
        HttpGet httpGet = null;

        String responseBody = "";
        int time = 0;
        do{
            try
            {
                httpClient = getHttpClient();
                httpGet = getHttpGet(url, cookie, userAgent);
                HttpResponse res =httpClient.execute(httpGet);
                int statusCode = res.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw AppException.http(statusCode);
                }
                responseBody = EntityUtils.toString(res.getEntity());
                break;
            } catch (Exception e) {
                time++;
                if(time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {}
                    continue;
                }
                // 发生网络异常
                e.printStackTrace();
                throw AppException.network(e);
            } finally {
                // 释放连接
                httpGet.abort();
                httpClient = null;
            }
        }while(time < RETRY_TIME);

        //responseBody = responseBody.replaceAll("\\p{Cntrl}", "\r\n");
        return new ByteArrayInputStream(responseBody.getBytes());
    }

    /**
     * 获取网络图片
     * @param url
     * @return
     */
    public static Bitmap getNetBitmap(String url) throws AppException {
        HttpClient httpClient = null;
        HttpGet httpGet = null;
        Bitmap bitmap = null;
        int time = 0;
        do{
            try
            {
                httpClient = getHttpClient();
                httpGet = getHttpGet(url, null, null);
                HttpResponse res = httpClient.execute(httpGet);
                int statusCode = res.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw AppException.http(statusCode);
                }
                InputStream inStream = res.getEntity().getContent();
                bitmap = BitmapFactory.decodeStream(inStream);
                inStream.close();
                break;
            }catch (IOException e) {
                time++;
                if(time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {}
                    continue;
                }
                // 发生网络异常
                e.printStackTrace();
                throw AppException.network(e);
            } catch (Exception e) {
                time++;
                if(time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {}
                    continue;
                }
                // 发生致命的异常，可能是协议不对或者返回的内容有问题
                e.printStackTrace();
                throw AppException.http(e);
            }  finally {
                // 释放连接
                httpGet.abort();
                httpClient = null;
            }
        }while(time < RETRY_TIME);
        return bitmap;
    }

    /**
     * 获取用户通知信息
     * @return
     * @throws AppException
     */
    public static String getChannelListData(AppContext appContext) throws AppException {

        try{
            String url = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8280/m6749/list_1.shtml";
            String retVal = "";
            retVal = HttpUtil.get(url);
            Log.e("lv------>",retVal);
            return retVal;

        }catch(Exception e){
            if(e instanceof AppException)
                throw (AppException)e;
            throw AppException.network(e);
        }
    }

}
