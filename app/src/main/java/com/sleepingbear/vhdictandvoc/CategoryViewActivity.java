package com.sleepingbear.vhdictandvoc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CategoryViewActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        dbHelper = new DbHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();

        Bundle b = this.getIntent().getExtras();

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle(b.getString("CATEGORY"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        ArrayList<CategoryViewItem> al = new ArrayList<CategoryViewItem>();
        if ( "C04".equals(b.getString("KIND")) ) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT  SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
            sql.append("FROM    NAVER_CONVERSATION" + CommConstants.sqlCR);
            sql.append("WHERE   CATEGORY = '" + b.getString("CATEGORY") +"'" + CommConstants.sqlCR);
            sql.append("ORDER   BY ORD" + CommConstants.sqlCR);
            DicUtils.dicSqlLog(sql.toString());

            Cursor cursor = db.rawQuery(sql.toString(), null);
            while ( cursor.moveToNext() ) {
                al.add(new CategoryViewItem(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")), cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2")), "", ""));
            }
        } else if ( "C05".equals(b.getString("KIND")) || "C06".equals(b.getString("KIND")) ) {
            String[] lvl = b.getString("CATEGORY").split("-");
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT  SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
            sql.append("FROM    VSL" + CommConstants.sqlCR);
            sql.append("WHERE   LVL1 = '" + lvl[0] +"'" + CommConstants.sqlCR);
            sql.append("AND     LVL2 = '" + lvl[1] +"'" + CommConstants.sqlCR);
            sql.append("AND     LVL3 = 'D'" + CommConstants.sqlCR);
            sql.append("ORDER   BY SEQ" + CommConstants.sqlCR);
            DicUtils.dicSqlLog(sql.toString());

            Cursor cursor = db.rawQuery(sql.toString(), null);
            while ( cursor.moveToNext() ) {
                if ( cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")).length() > 5 &&
                        !"FILE:".equals(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")).substring(0,5)) ) {
                    al.add(new CategoryViewItem(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")), cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2")), "", ""));
                }
            }
        } else {
            String[] samples = b.getString("SAMPLES").split("\n");
            String words = "";
            for (int i = 0; i < samples.length; i++) {
                if (!"".equals(samples[i])) {
                    String[] row = samples[i].split(":");
                    words += ("".equals(words) ? "" : ",") + row[0].trim();
                }
            }
            HashMap wordsInfo = DicDb.getWordsInfo(db, words.toLowerCase());

            for (int i = 0; i < samples.length; i++) {
                if (!"".equals(samples[i])) {
                    String[] row = samples[i].split(":");
                    if (row.length == 1) {
                        al.add(new CategoryViewItem(row[0].trim(), "", (String) wordsInfo.get(row[0].trim().toLowerCase() + "_SPELLING"), (String) wordsInfo.get(row[0].trim().toLowerCase() + "_ENTRY_ID")));
                    } else if (row.length == 2) {
                        al.add(new CategoryViewItem(row[0].trim(), row[1].trim(), (String) wordsInfo.get(row[0].trim().toLowerCase() + "_SPELLING"), (String) wordsInfo.get(row[0].trim().toLowerCase() + "_ENTRY_ID")));
                    }
                }
            }
        }

        CategoryViewAdapter m_adapter = new CategoryViewAdapter(this, R.layout.content_category_view_item, al);
        ((ListView) this.findViewById(R.id.my_lv)).setAdapter(m_adapter);

        DicUtils.setAdView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_help, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_categoryView);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}

class CategoryViewAdapter extends ArrayAdapter<CategoryViewItem> {
    int fontSize = 0;
    private ArrayList<CategoryViewItem> items;

    public CategoryViewAdapter(Context context, int textViewResourceId, ArrayList<CategoryViewItem> items) {
        super(context, textViewResourceId, items);
        this.items = items;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.content_category_view_item, null);
        }

        CategoryViewItem p = items.get(position);
        if (p != null) {
            ((TextView) v.findViewById(R.id.my_tv_line1)).setText(p.getLine1());
            ((TextView) v.findViewById(R.id.my_tv_line2)).setText(p.getLine2());

            if ( "".equals(p.getSpelling()) ) {
                ((TextView) v.findViewById(R.id.my_tv_spelling)).setVisibility(View.GONE);
            } else {
                ((TextView) v.findViewById(R.id.my_tv_spelling)).setText(p.getSpelling());
            }

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.line1 = p.getLine1();
            viewHolder.line2 = p.getLine2();
            viewHolder.spelling = p.getSpelling();
            viewHolder.entryId = p.getEntryId();
            v.setTag(viewHolder);

            //Item 선택
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder vViewHolder = (ViewHolder) v.getTag();

                    if ( vViewHolder.entryId != null && !"".equals(vViewHolder.entryId) ) {
                        Bundle bundle = new Bundle();
                        bundle.putString("entryId", vViewHolder.entryId);

                        Intent intent = new Intent(getContext(), WordViewActivity.class);
                        intent.putExtras(bundle);

                        getContext().startActivity(intent);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("foreign", vViewHolder.line1);
                        bundle.putString("han", vViewHolder.line2);

                        Intent intent = new Intent(getContext(), SentenceViewActivity.class);
                        intent.putExtras(bundle);

                        getContext().startActivity(intent);
                    }
                }
            });
        }

        //사이즈 설정
        ((TextView) v.findViewById(R.id.my_tv_line1)).setTextSize(fontSize);
        ((TextView) v.findViewById(R.id.my_tv_line2)).setTextSize(fontSize);
        ((TextView) v.findViewById(R.id.my_tv_spelling)).setTextSize(fontSize);

        return v;
    }

    static class ViewHolder {
        protected String line1;
        protected String line2;
        protected String spelling;
        protected String entryId;
    }
}

class CategoryViewItem {
    private String line1;
    private String line2;
    private String spelling;
    private String entryId;

    public CategoryViewItem(String _line1, String _line2, String _spelling, String _entryId) {
        this.line1 = _line1;
        this.line2 = _line2;
        this.spelling = _spelling;
        this.entryId = _entryId;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getSpelling() {
        return spelling;
    }

    public String getEntryId() {
        return entryId;
    }
}