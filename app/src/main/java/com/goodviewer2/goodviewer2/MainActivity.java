package com.goodviewer2.goodviewer2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static boolean enabling = false;

    private TimerTask mTask;
    private Timer mTimer;
    private ProgressBar m_progress;
    private EditText txtAddr0;
    private EditText txtAddr1;
    private EditText txtAddr2;
    private EditText txtIP;
    private WebView webView;
    private boolean webLoadEnd = false;
    Worker worker;
    private int cnt = 0;
    String addr;

    private String gStrIP = "000.000.000.000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        findViewById(R.id.btnGo).setOnClickListener(mClickListener);

        txtAddr0 = (EditText) findViewById(R.id.txtAddr0);
        txtAddr1 = (EditText) findViewById(R.id.txtAddr1);
        txtAddr2 = (EditText) findViewById(R.id.txtAddr2);
        txtIP = (EditText) findViewById(R.id.txtIP);
        webView = (WebView) findViewById(R.id.webView);
        m_progress = (ProgressBar) findViewById(R.id.webview_progress);

        txtAddr0.setText("http://m.blog.naver.com/hell8032");
        //txtAddr0.setText("http://blog.naver.com/hell8032/220379508465");
        //txtAddr.setText("http://m.cafe.daum.net/jykim31337/_rec");
        //txtAddr.setText("http://m.blog.naver.com/jykim31337");
        //txtAddr.setText("http://m.blog.naver.com/jykim31337/220629910116");
        //txtAddr0.setText("http://ag1408.blogspot.kr/?m=1");

        String strIP = getLocalIpAddress(INET4ADDRESS);

        txtIP.setText(strIP);

        webView.getSettings().setJavaScriptEnabled(true);
    }

    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Button btnGo = (Button) findViewById(R.id.btnGo);
            String txt = btnGo.getText().toString();

            if (txt.equals("START")) {
                btnGo.setText("STOP");

                addr = txtAddr0.getText().toString();

                worker = new Worker(true);
                worker.start();
            } else if (txt.equals("STOP")) {
                btnGo.setText("START");
                worker.stopThread();
            }
        }
    };

    //지원되는 버전인지 체크하고, 비행모드 On/OFF에 따라서 설정 변경
    private void setAirplaneMode(int mode) {
        Settings.System.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, mode);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", mode);
        sendBroadcast(intent);
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public final static int INET4ADDRESS = 1;
    public final static int INET6ADDRESS = 2;

    public static String getLocalIpAddress(int type) {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        switch (type) {
                            case INET6ADDRESS:
                                if (inetAddress instanceof Inet6Address) {
                                    return inetAddress.getHostAddress().toString();
                                }
                                break;

                            case INET4ADDRESS:
                                if (inetAddress instanceof Inet4Address) {
                                    return inetAddress.getHostAddress().toString();
                                }
                                break;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.e("StackTrace", exceptionAsString);
        }
        return null;
    }

    class Worker extends Thread {
        private boolean isPlay = false;

        public Worker(boolean isPlay) {
            this.isPlay = isPlay;
        }

        public void stopThread() {
            this.isPlay = false;
        }

        private void sleep(int tick) {
            try {
                Thread.sleep(tick);
            } catch (Exception ex) {
                //ex.printStackTrace();
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                Log.e("StackTrace", exceptionAsString);
            }
        }

        /*
        public void ScrollEnd() {
            try {
                sleep(3000);

                final int contentHeight = webView.getContentHeight() + webView.getHeight();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.scrollTo(0, contentHeight / 2);
                    }
                });

                sleep(3000);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.scrollTo(0, contentHeight);
                    }
                });

                sleep(3000);
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                Log.e("StackTrace", exceptionAsString);
            }
        }
        */

        //메인 쓰레드
        @Override
        public void run() {
            try {
                while (isPlay)
                {
                    enabling = isAirplaneModeOn(MainActivity.this);

                    if (!enabling)
                    {
                        //에어플레인 모드가 아님
                        while (true)
                        {
                            //에어플레인 모드 켬
                            setAirplaneMode(1);

                            enabling = isAirplaneModeOn(MainActivity.this);

                            //에어플레인 모드가 켜졌다면
                            if (enabling == true)
                            {
                                //초기화
                                webView.loadUrl("about:blank");

                                CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(webView.getContext());
                                CookieManager cookieManager = CookieManager.getInstance();
                                cookieManager.setAcceptCookie(true);
                                cookieManager.removeSessionCookie();
                                cookieManager.removeAllCookie();
                                cookieSyncManager.sync();
                                break;
                            }

                            //슬립, 에어플레인 모드가 완전히 켜질 때 까지
                            sleep(100);
                        }
                    } else {
                        //에어플레인 모드
                        while (true) {
                            //에어플레인 모드 끔
                            setAirplaneMode(0);

                            enabling = isAirplaneModeOn(MainActivity.this);

                            //에어플레인 모드가 꺼졌다면
                            if (enabling == false)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtIP.setText("IP를 가져오고 있습니다.");
                                    }
                                });

                                sleep(100 * 1);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while(true) {
                                            String strIP = getLocalIpAddress(INET4ADDRESS);

                                            if(strIP != null)
                                            {
                                                txtIP.setText(strIP);
                                                break;
                                            }

                                            sleep(100);
                                        }
                                    }
                                });

                                addr = txtAddr0.getText().toString();

                                try
                                {
                                    //슬립, IP 주소 가져온, 후 실제로 통신이 되기 전 까지
                                    sleep(1000 * 3);
                                    webView.loadUrl(addr);
                                    //슬립, 사이트 내용을 모두 가져올 때 까지
                                    sleep(1000);
                                } catch (Exception ex_) {
                                    StringWriter sw = new StringWriter();
                                    ex_.printStackTrace(new PrintWriter(sw));
                                    String exceptionAsString = sw.toString();
                                    Log.e("StackTrace", exceptionAsString);
                                }
                                break;
                            }

                            //슬립, 에어플레인 모드가 완전히 꺼질 때 까지
                            sleep(100);
                        }
                    }
                }
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                Log.e("StackTrace", exceptionAsString);
            }
        }

        /*
        @Override
        public void run() {
            try {
                while (isPlay) {
                    webView.loadUrl("about:blank");

                    CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(webView.getContext());
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    cookieManager.removeSessionCookie();
                    cookieManager.removeAllCookie();
                    cookieSyncManager.sync();

                    webView.loadUrl(addr);
                    sleep(1000);
                }
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                Log.e("StackTrace", exceptionAsString);
            }
        }
        */
    }
}
