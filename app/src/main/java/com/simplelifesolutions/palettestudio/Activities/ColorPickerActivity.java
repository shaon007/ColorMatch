package com.simplelifesolutions.palettestudio.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.simplelifesolutions.palettestudio.Beans.BeanColor;
import com.simplelifesolutions.palettestudio.Beans.BeanMain;
import com.simplelifesolutions.palettestudio.Helpers.DatabaseHelper;
import com.simplelifesolutions.palettestudio.Helpers.MyGradientView;
import com.simplelifesolutions.palettestudio.Helpers.MySpinAdapter_PaletteNames;
import com.simplelifesolutions.palettestudio.R;

import java.util.ArrayList;

public class ColorPickerActivity extends AppCompatActivity
{
//region...... variables declaration
    private Context   mContext = ColorPickerActivity.this;
    private MyGradientView mTop;
    private MyGradientView mBottom;
    private TextView mTextView;
    private Drawable mIcon;
    private Button mBtnColorPicker;
    int mColor = 0;

    String colorPickerResult_hexColor ="";

    private DatabaseHelper myDbHelper;
    MySpinAdapter_PaletteNames mSpinnerAdapter;
    private Spinner mSpinner;
    ArrayList<BeanMain> listPaletteDB ;
    public String mpalettetNameFromSpinner ="";
    public String mpalettetIDFromSpinner ="";

    //-----for dialog view
    EditText mEdtVwPltName_new;
    Spinner mSpinnerPaletteName_exist;
    MySpinAdapter_PaletteNames adapter_Spinner;

    Long paletteID_pkDB = -1L;
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(this, R.layout.activity_color_picker, null);
        setContentView(view);



        myDbHelper = new DatabaseHelper(this);
        listPaletteDB = new ArrayList<BeanMain>();

        mIcon = getResources().getDrawable(R.mipmap.colorchk);
        mTextView = (TextView)findViewById(R.id.txtVwColorCode);
        mBtnColorPicker = (Button)findViewById(R.id.btnColorPicker);


        mTextView.setCompoundDrawablesWithIntrinsicBounds(mIcon, null, null, null);
        mTop = (MyGradientView)findViewById(R.id.pickerTop);
        mBottom = (MyGradientView)findViewById(R.id.pickerBottom);
        mTop.setBrightnessGradientView(mBottom);
        mBottom.setOnColorChangedListener(new MyGradientView.OnColorChangedListener() {
            @Override
            public void onColorChanged(MyGradientView view, int color) {
                mTextView.setTextColor(color);
                mColor = color;
                mTextView.setText("#" + Integer.toHexString(color));
                 colorPickerResult_hexColor = String.format("#%06X", (0xFFFFFF & color));
                Log.d("colroCHk"," Color:: " +color +"\n rgbconverted:: " +colorPickerResult_hexColor );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mIcon.setTint(color);
                }
            }
        });

        int color = 0xFF394572;
        mTop.setColor(color);
    }


    public void btnClkPickerExisting(View v)
        {  addColorToPlt(mColor);   }



//============================ add color to palette

    private void addColorToPlt(int clr)
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_savecolor_topalette, null);

        mAlertBuilder.setPositiveButton("ok", null);
        mAlertBuilder.setNegativeButton("cancel", null);
        mAlertBuilder.setView(promptsView);

        mEdtVwPltName_new = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        final View mVwColorBg = (View) promptsView.findViewById(R.id.vwColorBackGround);
        final TextView mTxtVwColorCode = (TextView) promptsView.findViewById(R.id.txtVwColorCode);
        final EditText mEdtVwColorName = (EditText) promptsView.findViewById(R.id.etDialogImgName);
        final CheckBox mChkBx = (CheckBox) promptsView.findViewById(R.id.chkBoxCover);
        final RadioButton mRadioExist = (RadioButton) promptsView.findViewById(R.id.rdBtn_Existing);
        final RadioButton mRadioNew = (RadioButton) promptsView.findViewById(R.id.rdBtn_New);

        mSpinnerPaletteName_exist = (Spinner) promptsView.findViewById(R.id.spinner_existingPalette);

        setup_spinnerItems(mSpinnerPaletteName_exist); //------------------ setUp the spinner for existing paletteList

        mVwColorBg.setBackgroundColor(clr);
        mTxtVwColorCode.setText(colorPickerResult_hexColor);

        final AlertDialog mAlertDialog = mAlertBuilder.create();

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button btnDialog_positive = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnDialog_positive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int returnResult = 0;
                        if (mRadioExist.isChecked())
                            returnResult = func_addImageToExistingPlt(mEdtVwColorName,  mChkBx );

                        else if (mRadioNew.isChecked())
                            returnResult = func_addImageToNewPlt(mEdtVwColorName,  mChkBx );

                        if(returnResult == 1)
                        {  mAlertDialog.dismiss();

                            Intent intent_DetailsAct = new Intent(mContext, PaletteDetailsActivity.class);
                            intent_DetailsAct.putExtra("xtra_pltID_fromListClk", mpalettetIDFromSpinner);
                            intent_DetailsAct.putExtra("xtra_pltName_fromListClk", mpalettetNameFromSpinner);

                            intent_DetailsAct.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent_DetailsAct);

                            finish();}
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private int func_addImageToNewPlt(EditText mEdtVwColorName,  CheckBox mChkBx )
    {
        if ((mEdtVwPltName_new.getText().toString().trim().length() > 0) && (mEdtVwColorName.getText().toString().trim().length() > 0)) {
            String _pltName = mEdtVwPltName_new.getText().toString();
            String _clrName = mEdtVwColorName.getText().toString();

            if (myDbHelper.checkDuplicatePltName(_pltName)) {
                Toast.makeText(mContext, "Palette Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
            }
            else {
                BeanMain _PaletteObj = new BeanMain("NULL", _pltName, "image", "0", "");
                paletteID_pkDB = myDbHelper.createNewPalette(_PaletteObj);


                if (paletteID_pkDB != -1)   //new palette created successfully
                {
                    // Toast.makeText(mContext, "New Palette created succssfuly!\n Please wait for storing the image. ", Toast.LENGTH_SHORT).show();

                    mpalettetIDFromSpinner = String.valueOf(paletteID_pkDB);

                    BeanMain _mainObj = myDbHelper.getPaletteObjFromID(paletteID_pkDB.toString());
                    mpalettetNameFromSpinner = _mainObj.getPaletteName();


                    BeanColor _ColorObj = new BeanColor("NULL", mpalettetIDFromSpinner, colorPickerResult_hexColor, _clrName, "");
                    Long dbColorInsert = myDbHelper.insert_newColor(_ColorObj);


                    Log.d("dbResult_explt", "Color Created DBresult:::" + dbColorInsert.toString() + " pltID_from_Spinner: " + _ColorObj.getPaletteID().toString());

                    if (dbColorInsert == -1)
                        Toast.makeText(mContext, "Something went wrong when saving the color in the palette! \n Please try giving another name.", Toast.LENGTH_SHORT).show();
                    else {
                        if (mChkBx.isChecked()) {
                            Toast.makeText(mContext, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();
                            Long dbUpdateCover = myDbHelper.updateCoverInPalette(paletteID_pkDB.toString(), "color", dbColorInsert.toString());
                        }
                        return 1;
                    }
                } else
                    Toast.makeText(mContext, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();
            }
        } else    // either PaletteName or ImageName was not given
            Toast.makeText(mContext, "Please check the Palette & Color Name!", Toast.LENGTH_SHORT).show();

        return 0;
    }


    private int func_addImageToExistingPlt(EditText mEdtVwColorName,  CheckBox mChkBx_addAsCover )
    {
        if ((mEdtVwColorName.getText().toString().trim().length() > 0)  && !(mpalettetNameFromSpinner.equals("")))
        {
            String _clrName = mEdtVwColorName.getText().toString();

            BeanColor _ColorObj = new BeanColor("NULL", mpalettetIDFromSpinner, colorPickerResult_hexColor, _clrName, "");
            Long dbColorInsert = myDbHelper.insert_newColor(_ColorObj);

            Log.d("dbResult_explt", "Image Created DBresult:::" + dbColorInsert.toString() + " pltID_from_Spinner: " + _ColorObj.getPaletteID().toString());

            if (dbColorInsert == -1)
                Toast.makeText(mContext, "Something went wrong when saving the color in existing palette!\n Please try giving another name.", Toast.LENGTH_SHORT).show();
            else
            {
                if(mChkBx_addAsCover.isChecked()) {
                    Toast.makeText(mContext, "Color saved succssfuly!", Toast.LENGTH_SHORT).show();
                    Long dbUpdateCover = myDbHelper.updateCoverInPalette(mpalettetIDFromSpinner.toString(), "color", dbColorInsert.toString());
                }
                return 1;
            }

        }
        else
            Toast.makeText(mContext, "Please insert color name!", Toast.LENGTH_SHORT).show();

        return 0;
    }


    private void setup_spinnerItems(Spinner mSpn)
    {
        ArrayList<BeanMain> listPaletteDB = myDbHelper.getPaletteList();

        adapter_Spinner = new MySpinAdapter_PaletteNames(mContext, android.R.layout.simple_spinner_item, listPaletteDB);
        mSpn.setAdapter(adapter_Spinner);
        mSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                BeanMain _paletteObj = (BeanMain) adapter_Spinner.getItem(position);

                mpalettetNameFromSpinner = _paletteObj.getPaletteName().toString();
                mpalettetIDFromSpinner = _paletteObj.getPaletteID();

                // Toast.makeText(mContext, ""+ mpalettetIDFromSpinner + "\t" + mpalettetNameFromSpinner, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    public String onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.rdBtn_Existing:
                if (checked)
                    mEdtVwPltName_new.setVisibility(View.GONE);
                mEdtVwPltName_new.setText(null);
                mSpinnerPaletteName_exist.setVisibility(View.VISIBLE);
                break;
            case R.id.rdBtn_New:
                if (checked)
                    mEdtVwPltName_new.setVisibility(View.VISIBLE);
                mSpinnerPaletteName_exist.setVisibility(View.GONE);
                break;

        }return null;
    }




}
