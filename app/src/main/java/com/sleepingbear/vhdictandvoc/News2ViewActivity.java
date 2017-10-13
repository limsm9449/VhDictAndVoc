package com.sleepingbear.vhdictandvoc;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;
import java.util.Locale;

public class News2ViewActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private TextToSpeech myTTS;
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    int fontSize = 0;
    private TextView mean;
    private RelativeLayout meanRl;
    private String entryId = "";

    private String clickWord;
    public WebView webView;
    private final Handler handler = new Handler();
    private ProgressDialog mProgress;
    private int mSelect = 0;
    private int m2Select = 0;
    private ImageButton addBtn;
    private ImageButton searchBtn;
    private String newsUrl;

    private ActionMode mActionMode = null;
    private String title;
    private String url;
    private String kind;
    private String contents;
    private String htmlContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news2_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle b = this.getIntent().getExtras();
        title = b.getString("TITLE");
        url = b.getString("URL");
        kind = b.getString("KIND");
        contents = b.getString("CONTENTS");

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_webViewFont ) );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(b.getString("CATEGORY"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        myTTS = new TextToSpeech(this, this);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        //하단 뜻 영역을 숨김
        meanRl = (RelativeLayout) this.findViewById(R.id.my_c_news_rl);
        meanRl.setVisibility(View.GONE);
        meanRl.setClickable(true);  //클릭시 하단 광고가 클릭되는 문제로 rl이 클릭이 되게 해준다.

        //뜻 롱클릭시 단어 상세 보기
        mean = (TextView) this.findViewById(R.id.my_c_news_mean);
        mean.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if ( entryId != null && !"".equals(entryId) ) {
                    Intent intent = new Intent(getApplication(), WordViewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("entryId", entryId);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }

                return false;
            }
        });

        //버튼 설정
        addBtn = (ImageButton) this.findViewById(R.id.my_c_news_ib_add);
        searchBtn = (ImageButton) this.findViewById(R.id.my_c_news_ib_search);
        searchBtn.setVisibility(View.GONE);

        this.findViewById(R.id.my_c_news_ib_add).setOnClickListener(this);
        this.findViewById(R.id.my_c_news_ib_search).setOnClickListener(this);

        webView = (WebView) this.findViewById(R.id.my_c_news_wv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new News2ViewActivity.AndroidBridge(), "android");
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new News2ViewActivity.MyWebViewClient());

        htmlContents = DicUtils.getHtmlString(title, contents, fontSize);
        webView.loadDataWithBaseURL(null, htmlContents, "text/html", "UTF-8", null);

        DicUtils.setAdView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news2_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if ( id == R.id.action_web ) {
            Bundle bundle = new Bundle();
            bundle.putString("kind", kind);
            bundle.putString("url", url);

            Intent helpIntent = new Intent(getApplication(), NewsWebViewActivity.class);
            helpIntent.putExtras(bundle);
            startActivity(helpIntent);
        } else if (id == R.id.action_tts) {
            Intent ttsIntent = new Intent(getApplicationContext(), NewsTtsService.class);
            stopService(ttsIntent);

            String[] sentences  = DicUtils.getSentencesArray(contents);

            ttsIntent = new Intent(getApplicationContext(), NewsTtsService.class);
            ttsIntent.putExtra("sentences", sentences);
            startService(ttsIntent);
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_news2View);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent ttsIntent = new Intent(getApplicationContext(), NewsTtsService.class);
        stopService(ttsIntent);

        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    myTTS.shutdown();

                    if ( webView.canGoBack() ) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onInit(int status) {
        Locale loc = new Locale("en");

        if (status == TextToSpeech.SUCCESS) {
            int result = myTTS.setLanguage(new Locale("vi", "VN"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();

        Intent ttsIntent = new Intent(getApplicationContext(), NewsTtsService.class);
        stopService(ttsIntent);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        DicUtils.dicLog("onActionModeFinished");
        mActionMode = null;
        super.onActionModeFinished(mode);
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_c_news_ib_add) {
            //메뉴 선택 다이얼로그 생성
            Cursor cursor = db.rawQuery(DicQuery.getSentenceViewContextMenu(), null);
            final String[] kindCodes = new String[cursor.getCount()];
            final String[] kindCodeNames = new String[cursor.getCount()];

            int idx = 0;
            while (cursor.moveToNext()) {
                kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                idx++;
            }
            cursor.close();

            final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("메뉴 선택");
            dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    mSelect = arg1;
                }
            });
            dlg.setNegativeButton("취소", null);
            dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DicDb.insDicVoc(db, entryId, kindCodes[mSelect]);

                    DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                    Toast.makeText(getApplicationContext(), "단어장에 등록했습니다. 메인화면의 '단어장' 탭에서 내용을 확인하세요.", Toast.LENGTH_SHORT).show();
                }
            });
            dlg.show();
        } else if ( v.getId() == R.id.my_c_news_ib_search) {
            wordSearch();
        }
    }

    public void wordSearch() {
        final String[] kindCodes = new String[]{"Naver","Daum"};

        final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("검색 사이트 선택");
        dlg.setSingleChoiceItems(kindCodes, m2Select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                m2Select = arg1;
            }
        });
        dlg.setNegativeButton("취소", null);
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = new Bundle();
                bundle.putString("kind", CommConstants.dictionaryKind_f);
                bundle.putString("site", kindCodes[m2Select]);
                bundle.putString("word", clickWord);

                Intent intent = new Intent(getApplication(), WebDictionaryActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        dlg.show();
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        DicUtils.dicLog("onActionModeStarted");
        if (mActionMode == null) {
            mActionMode = mode;
            Menu menu = mode.getMenu();

            // Remove the default menu items (select all, copy, paste, search)
            menu.clear();

            // Inflate your own menu items
            mode.getMenuInflater().inflate(R.menu.menu_news2_view_cm, menu);

            //클릭시 onContextItemSelected를 호출해주도록 이벤트를 걸어준다.
            MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    onContextualMenuItemClicked(item);
                    return true;
                }
            };
            for (int i = 0, n = menu.size(); i < n; i++) {
                menu.getItem(i).setOnMenuItemClickListener(listener);
            }
        }

        super.onActionModeStarted(mode);
    }

    public void onContextualMenuItemClicked(MenuItem item) {
        DicUtils.dicLog("onContextualMenuItemClicked");
        switch (item.getItemId()) {
            case R.id.action_copy:
                webView.loadUrl("javascript:window.android.action('COPY', window.getSelection().toString())");

                break;
            case R.id.action_all_copy:
                webView.loadUrl("javascript:window.android.action('COPY', $('#news_contents').text())");

                break;
            case R.id.action_word_view:
                webView.loadUrl("javascript:window.android.action('WORD', window.getSelection().toString())");

                break;
            case R.id.action_translate:
                webView.loadUrl("javascript:window.android.action('TRANSLATE', window.getSelection().toString())");

                break;
            case R.id.action_word_search:
                webView.loadUrl("javascript:window.android.action('WORD_SEARCH', window.getSelection().toString())");

                break;
            case R.id.action_sentence_view:
                webView.loadUrl("javascript:window.android.action('SENTENCE', window.getSelection().toString())");

                break;
            case R.id.action_tts_all:
                webView.loadUrl("javascript:window.android.action('TTS', $('#news_contents').text())");

                break;
            case R.id.action_tts:
                webView.loadUrl("javascript:window.android.action('TTS', window.getSelection().toString())");

                break;
            default:
                // ...
                break;
        }

        // This will likely always be true, but check it anyway, just in case
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //return super.shouldOverrideUrlLoading(view, url);

            DicUtils.dicLog("url = " + url);
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if (mProgress == null) {
                mProgress = new ProgressDialog(News2ViewActivity.this);
                mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgress.setMessage("페이지 로딩 및 변환 중입니다.");
                mProgress.setCancelable(false);
                mProgress.setButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mProgress.dismiss();
                        mProgress = null;
                    }
                });
                mProgress.show();
            }

            DicUtils.dicLog("onPageStarted : " + url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
                mProgress = null;
            }

            DicUtils.dicLog("onReceivedError : " + error.toString());
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
                mProgress = null;
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    webView.scrollTo(0, 0);
                }
            }, 300);
        }
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setWord(final String arg) { // must be final
            handler.post(new Runnable() {
                public void run() {
                    meanRl.setVisibility(View.VISIBLE);

                    clickWord = DicUtils.getWordString(arg);

                    HashMap info = DicDb.getMean(db, clickWord);
                    mean.setText(clickWord + " " + DicUtils.getString((String)info.get("SPELLING")) + " : " + DicUtils.getString((String)info.get("MEAN")));

                    entryId = DicUtils.getString((String)info.get("ENTRY_ID"));
                    if ( !"".equals(entryId) ) {
                        DicDb.insDicClickWord(db, entryId, "");

                        DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                        addBtn.setVisibility(View.VISIBLE);
                        searchBtn.setVisibility(View.GONE);
                    } else {
                        addBtn.setVisibility(View.GONE);
                        searchBtn.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @JavascriptInterface
        public void action(final String kind, final String arg) { // must be final
            handler.post(new Runnable() {
                public void run() {
                    DicUtils.dicLog(arg);
                    if ( "COPY".equals(kind) ) {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("simple text", arg);
                        clipboard.setPrimaryClip(clip);
                    } else if ( "WORD".equals(kind) ) {
                        HashMap info = DicDb.getMean(db, DicUtils.getWordString(arg));

                        if ( info.containsKey("ENTRY_ID") ) {
                            Intent intent = new Intent(getApplication(), WordViewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("entryId", (String) info.get("ENTRY_ID"));
                            intent.putExtras(bundle);

                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "등록된 단어가 아닙니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else if ( "WORD_SEARCH".equals(kind) ) {
                        clickWord = DicUtils.getWordString(arg);
                        wordSearch();
                    } else if ( "SENTENCE".equals(kind) ) {
                        Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("foreign", arg);
                        bundle.putString("han", "");
                        intent.putExtras(bundle);

                        startActivity(intent);
                    } else if ( "TTS".equals(kind) ) {
                        if ( arg.length() > 4000 ) {
                            Toast.makeText(getApplicationContext(), "TTS는 4,000자 까지만 가능합니다.", Toast.LENGTH_SHORT).show();
                            myTTS.speak(arg.substring(0, 3900), TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            myTTS.speak(arg, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else if ( "URL".equals(kind) ) {
                        newsUrl = arg.replace("http://","").replace("https://","");
                        DicUtils.dicLog("URL : " + newsUrl);
                    } else if ( "TRANSLATE".equals(kind) ) {
                        final String[] kindCodes = new String[]{"Naver","Google"};

                        final AlertDialog.Builder dlg = new AlertDialog.Builder(News2ViewActivity.this);
                        dlg.setTitle("번역 사이트 선택");
                        dlg.setSingleChoiceItems(kindCodes, m2Select, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                m2Select = arg1;
                            }
                        });
                        dlg.setNegativeButton("취소", null);
                        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //클립보드에 복사
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("simple text", arg);
                                clipboard.setPrimaryClip(clip);

                                Bundle bundle = new Bundle();
                                bundle.putString("site", kindCodes[m2Select]);
                                bundle.putString("sentence", arg);

                                Intent intent = new Intent(getApplication(), WebTranslateActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                        dlg.show();
                    }
                }
            });
        }
    }
}
