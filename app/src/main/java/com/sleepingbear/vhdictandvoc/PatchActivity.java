package com.sleepingbear.vhdictandvoc;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("패치 내용");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        StringBuffer patch = new StringBuffer();

        patch.append("* 신규 패치" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("- 검색된 사전에서 롱클릭을 하면 바로 단어장으로 등록을 할 수 있습니다." + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);

        patch.append("- 검색단어 입력후 X 버튼 클릭시 재조회로 불편함이 있어 입력을 받도록 수정" + CommConstants.sqlCR);
        patch.append("- 카테고리 > 단어 선택시 발생하는 오류 수정" + CommConstants.sqlCR);
        patch.append("- 처음 메모리 접근 권한을 취소한 경우 권한이 다시는 안보이는 문제점 수정" + CommConstants.sqlCR);
        patch.append("- 사전 검색시 진행바를 보여주도록 수정" + CommConstants.sqlCR);
        patch.append("- 상단에 그림자 나오는 문제점 수정" + CommConstants.sqlCR);
        patch.append("- 파일 선택시 상위로 계속 올라갈 경우 어플이 죽는 오류 수정(내부 메모리 영역에서만 파일 탐색이 되도록 수정)" + CommConstants.sqlCR);
        patch.append("- 없는 단어 및 예문을 검색시 오류로 어플이 죽는 오류 수정하였습니다." + CommConstants.sqlCR);
        patch.append("- 단어장 UI를 변경하였습니다." + CommConstants.sqlCR);
        patch.append("- 단어장에서 클릭시 바로 단어보기로 이동하고, 길게 클릭시 단어 삭제 확인을 물어보도록 수정하였습니다." + CommConstants.sqlCR);
        patch.append("- 문장예문에서 단어를 클릭시 바로 단어보기 화면으로 이동하고, 길게 클릭시 단어장을 선택하도록 수정했습니다." + CommConstants.sqlCR);
        patch.append("- 안드로이드에서 권한 문제로 폴더를 찾지 못하는 문제점을 수정하였습니다." + CommConstants.sqlCR);
        patch.append("   .등록할 단어를 텍스트 파일로 만들어서 핸드폰 vhdictandvoc 폴더에 넣고, 단어장에서 가져오기 하시면 단어장에 등록을 하실 수 있습니다." + CommConstants.sqlCR);
        patch.append("   .단어장을 내보내기 하셔서 텍스트 파일로 단어 암기를 하실 수 있습니다." + CommConstants.sqlCR);
        patch.append("- 예문을 저장 및 삭제하는 기능을 추가 하였습니다." + CommConstants.sqlCR);
        patch.append("   .예문상세 화면에서 예문에 있는 별표를 클릭하여 저장을 하실 수 있습니다. " + CommConstants.sqlCR);
        patch.append("   .나의 예문 메뉴를 클릭해서 예문을 조회 하실 수 있습니다." + CommConstants.sqlCR);
        patch.append("- 카테고리에 Naver 회화를 추가했습니다." + CommConstants.sqlCR);
        patch.append("- 안드로이드 6 에서 폴더를 찾지 못하는 문제점을 수정하였습니다." + CommConstants.sqlCR);
        patch.append("- Tab을 오늘의 단어, 사전, 단어장으로 줄이고 학습, 기타를 메뉴로 옮겼습니다." + CommConstants.sqlCR);
        patch.append("- 속도 문제로 사전 검색시 검색할 단어를 다 입력한 후에 검색이 되도록 수정 했습니다." + CommConstants.sqlCR);
        patch.append("- 사전 검색에 문장을 넣을 경우 관련 단어들을 검색하도록 수정 했습니다." + CommConstants.sqlCR);

        ((TextView) this.findViewById(R.id.my_c_patch_tv1)).setText(patch.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
