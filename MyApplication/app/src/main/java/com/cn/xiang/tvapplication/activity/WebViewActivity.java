package com.cn.xiang.tvapplication.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cn.xiang.tvapplication.R;


public class WebViewActivity extends Activity {
    private View mCustomView;
    private FrameLayout mLayout;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private WebView webView;
    private RelativeLayout webviewRelativeLayout;
    private RelativeLayout backBtnContainer;
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        String url = getIntent().getStringExtra("url");


        mLayout = findViewById(R.id.fl_video);
        backBtnContainer = findViewById(R.id.back_btn_container);
        ImageButton backBtn = findViewById(R.id.btn_back);
        Button fullScreenBtn = findViewById(R.id.btn_fullscreen);
        webviewRelativeLayout = findViewById(R.id.webviewRelativeLayout);

        if (webView == null) {
            webView = new WebView(this);
        }

        webviewRelativeLayout.addView(webView);
        webView.setFocusable(true);
        webView.requestFocus();

        WebSettings webSettings = webView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        //视频自动播放
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webSettings.setAllowFileAccess(true);
        webSettings.setSupportZoom(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);

        //设置不息屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initWebChromeClient(webView);
        initWebViewClient(webView);

        initBackBtn(backBtn);
        initFullScreenBtn(fullScreenBtn);

        webView.loadUrl(url);
    }

    private void initWebChromeClient(WebView webView) {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                if (mCustomView == null) {
                    return;
                }
                mCustomView.setVisibility(View.GONE);
                mLayout.removeView(mCustomView);
                mCustomView = null;
                mLayout.setVisibility(View.GONE);
                try {
                    mCustomViewCallback.onCustomViewHidden();
                } catch (Exception e) {
                }
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏

                backBtnContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);

                //如果view 已经存在，则隐藏
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                mCustomView = view;
                mCustomView.setVisibility(View.VISIBLE);
                mCustomViewCallback = callback;
                mLayout.addView(mCustomView);
                mLayout.setVisibility(View.VISIBLE);
                mLayout.bringToFront();

                //设置横屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                //隐藏导航条
                backBtnContainer.setVisibility(View.GONE);
                //隐藏状态栏
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });
    }

    private void initWebViewClient(WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                WebViewActivity.this.url = url;

                //通过操作js实现自动全屏
                if (url.startsWith("https://www.gdtv.cn")) {
                    webView.loadUrl("javascript:(function(){setTimeout(function(){document.querySelector('.vjs-fullscreen-control').click()},3000)})()");
                }

                if (url.startsWith("https://www.yangshipin.cn")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                webView.scrollBy(0, 60);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();
                    //全屏按钮
                    webView.loadUrl("javascript:(function(){setTimeout(function(){document.querySelector('.full.full2').click()},3000)})()");

                    if (backBtnContainer.getVisibility() == View.VISIBLE) {
                        backBtnContainer.setVisibility(View.GONE);
                    }
                }

                if (url.startsWith("https://tv.cctv.com")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                webView.scrollBy(0, 200);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();
                    //全屏按钮
//                    webView.loadUrl("javascript:(function(){setTimeout(function(){document.querySelector('#player_fullscreen_no_mouseover_player').click()},3000)})()");

                    if (backBtnContainer.getVisibility() == View.VISIBLE) {
                        backBtnContainer.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void initBackBtn(ImageButton backBtn) {
        backBtn.setFocusedByDefault(false);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    private void initFullScreenBtn(Button fullScreenBtn) {
        fullScreenBtn.setFocusedByDefault(true);
        fullScreenBtn.requestFocus();
        fullScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url.startsWith("https://www.gdtv.cn")) {
                    webView.loadUrl("javascript:(function(){document.querySelector('.vjs-fullscreen-control').click();})()");
                }
                if (url.startsWith("https://tv.cctv.com")) {
                    webView.loadUrl("javascript:(function(){document.querySelector('#player_fullscreen_player').click()})()");
                }
                if (url.startsWith("https://www.yangshipin.cn")) {
                    webView.loadUrl("javascript:(function(){document.querySelector('.full.full2').click()})()");
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.resumeTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.loadUrl("about:blank");
        //清空所有cookie
        CookieSyncManager.createInstance(WebViewActivity.this);
        CookieManager.getInstance().removeAllCookies(null);
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.clearCache(true);
        webviewRelativeLayout.removeView(webView);
        webView.destroy();
        webView = null;
    }
}
