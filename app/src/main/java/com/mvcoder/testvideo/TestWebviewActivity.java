package com.mvcoder.testvideo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.AppUtils;
import com.mingle.widget.LoadingView;
import com.mvcoder.videosegment.js.VideoJsProtoObj;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestWebviewActivity extends AppCompatActivity {

    @BindView(R.id.fl_container_webview)
    FrameLayout webViewContainer;
    @BindView(R.id.progressBar)
    LoadingView progressBar;
    @BindView(R.id.load_layout)
    FrameLayout loadLayout;
    @BindView(R.id.errorPage_text)
    ImageView errorPageText;
    @BindView(R.id.reloadBt)
    Button reloadBt;
    @BindView(R.id.errorPage)
    RelativeLayout errorPageLayout;
    @BindView(R.id.fl_container)
    FrameLayout flContainer;
    @BindView(R.id.fl_video_container)
    FrameLayout videoContainer;

    private final static String TAG = TestWebviewActivity.class.getSimpleName();

    private boolean needClearHistroy = false;

    private String IP = "192.168.3.243";
    private int port = 8050;
    private String path = "/remote_proj/test/test.html";
    private String URL = "http://" + IP + ":" + port + path;

    private WebView mWebView;
    private VideoJsProtoObj jsInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_webview);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        if (mWebView != null) {
            mWebView.loadUrl(URL);
        }
    }

    @OnClick(R.id.reloadBt)
    public void onViewClicked() {
        needClearHistroy = true;
        loadLayout.setVisibility(View.VISIBLE);
        mWebView.loadUrl(URL);
        errorPageLayout.setVisibility(View.GONE);
    }

    private void initView() {
        mWebView = new WebView(this);
        setWebViewSetting(mWebView);
        jsInterface = VideoJsProtoObj.getInstance(mWebView, videoContainer);
        mWebView.addJavascriptInterface(jsInterface, "segmentJs");
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webViewContainer.addView(mWebView, params);
        /**
         * 音视频上层容器View，当该层滑动时，会把滑动事件委托给webview
         */
        //scrollView.setDelegate(mWebView);
        //videoContainer.initVideoContainer(this);
        //webview将滑动事件通知上层音视频容器类，让容器类跟着滑动，确保音视频控件滑动时也能准确覆盖h5的控件
        /*mWebView.setOnScrollChangedCallback(new ScroolWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int dx, int dy) {
                //videoContainer.scrollBy(0,dy);
                scrollView.scrollBy(0, dy);
            }
        });*/
    }

    private void setWebViewSetting(final WebView webview) {
        if (webview != null) {
            WebSettings settings = webview.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setUseWideViewPort(true);//关键点
            //settings.setSupportMultipleWindows(true);
            settings.setAllowFileAccess(true);
            settings.setLoadsImagesAutomatically(true);
            settings.setAllowContentAccess(true);
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            settings.setSupportZoom(true); // 支持缩放
            settings.setDisplayZoomControls(false);
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH);//提高渲染优先级
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            //settings.setUseWideViewPort(true);//显示正常的视图
            settings.setAppCacheEnabled(true);
            settings.setDatabaseEnabled(true);

            /*DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int mDensity = metrics.densityDpi;
            Log.d("maomao", "densityDpi = " + mDensity);
            if (mDensity == 240) {
                settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            } else if (mDensity == 160) {
                settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
            } else if(mDensity == 120) {
                settings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
            }else if(mDensity == DisplayMetrics.DENSITY_XHIGH){
                settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            }else if (mDensity == DisplayMetrics.DENSITY_TV){
                settings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            }else{
                settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
            }*/

            webview.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    //用当前webview打开网页
                    //view.loadUrl(URL);
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    //隐藏 ProgressBar,开放网络图片加载
                    view.getSettings().setBlockNetworkImage(false);
                    super.onPageFinished(view, url);
                    //RxBus.getDefault().post(Constants.Rxcode.ONPAGEFINISH, url);
                }

                //清除历史记录
                @Override
                public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                    super.doUpdateVisitedHistory(view, url, isReload);
                    if (needClearHistroy) {
                        needClearHistroy = false;
                        view.clearHistory();
                    }
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    //显示progressBar,堵塞网络图片加载，加快页面打开速度。
                    loadLayout.setVisibility(View.VISIBLE);
                    view.getSettings().setBlockNetworkImage(true);
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    mWebView.stopLoading();
                    loadLayout.setVisibility(View.GONE);
                    errorPageLayout.setVisibility(View.VISIBLE);
                    //ToastUtils.showShort("加载URL失败："+failingUrl);
                    //webView.loadUrl(ERROR_PAGE);
                }
            });

            mWebView.setWebChromeClient(new WebChromeClient() {

                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(view.getContext());
                    builder.content(message)
                            .positiveText("确定")
                            .canceledOnTouchOutside(true)
                            .show();
                    result.confirm();
                    return/* super.onJsAlert(view, url, message, result);*/ true;
                }

                @Override
                public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(view.getContext());
                    builder.content(message)
                            .positiveText("确定")
                            .negativeText("取消")
                            .canceledOnTouchOutside(false)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (which == DialogAction.POSITIVE) {
                                        result.confirm();
                                    } else if (which == DialogAction.NEGATIVE) {
                                        result.cancel();
                                    }
                                }
                            }).show();
                    return true;
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    if (!AppUtils.isAppDebug()) return true;
                    if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.DEBUG) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("console:\nfrom: ");
                        builder.append(consoleMessage.sourceId());
                        builder.append("\n");
                        builder.append("message: ");
                        builder.append(consoleMessage.message());
                        Log.d(TAG,builder.toString());
                    }
                    return super.onConsoleMessage(consoleMessage);
                }

                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 2);
                    return true;
                }

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress == 100) {
                        loadLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    if (title.contains("404") || title.contains("not available") || title.contains("not found")) {
                        mWebView.stopLoading();
                        errorPageLayout.setVisibility(View.VISIBLE);
                        //mWebView.loadUrl(ERROR_PAGE);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        }
        webViewContainer.removeAllViews();
    }

}
