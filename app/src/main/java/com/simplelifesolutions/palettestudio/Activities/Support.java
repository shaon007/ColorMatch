package com.simplelifesolutions.palettestudio.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.simplelifesolutions.palettestudio.R;

public class Support extends AppCompatActivity
{
    EditText edtTvSubj, edtTvDetail;
    Button btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        edtTvSubj = (EditText)findViewById(R.id.edt_subject);
        edtTvDetail = (EditText)findViewById(R.id.edt_detail);
        btn_submit = (Button)findViewById(R.id.btn_support);



    }

    public void supportClk(View view) {

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","rashedul.hasan.shaon@gmail.com", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, edtTvSubj.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, edtTvDetail.getText().toString());
        startActivity(Intent.createChooser(intent, "Choose an Email client :"));

        finish();
    }
}
