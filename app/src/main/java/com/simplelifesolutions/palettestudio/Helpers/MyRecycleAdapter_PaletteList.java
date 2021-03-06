package com.simplelifesolutions.palettestudio.Helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.simplelifesolutions.palettestudio.Beans.BeanColor;
import com.simplelifesolutions.palettestudio.Beans.BeanImage;
import com.simplelifesolutions.palettestudio.Beans.BeanObject;
import com.simplelifesolutions.palettestudio.Beans.BeanObjectList;
import com.simplelifesolutions.palettestudio.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MyRecycleAdapter_PaletteList extends RecyclerView.Adapter<MyRecycleAdapter_PaletteList.MyViewHolder>
{
    private Context mContext;
    private List<BeanObjectList> beanClassList_s;

    private onRecyclerViewItemClickListener mItemClickListener;

    private DatabaseHelper myDbHelper;

   public MyRecycleAdapter_PaletteList(Context mContext, List<BeanObjectList> beanClassList) {
        this.mContext = mContext;
        this.beanClassList_s = beanClassList;

          myDbHelper = new DatabaseHelper(mContext);
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClickListener(View view, int position, String pltID, String pltName, String cvr_flag, String cvr_ID);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        Button mDeleteButton , mShareButton;
         ImageView mCoverImgVw;
         LinearLayout lnrLayout;
         TextView mTxtVw;


        public MyViewHolder(final View view)
        {
            super(view);

            mDeleteButton = (Button) view.findViewById(R.id.btnDeletePalette);
            mShareButton = (Button) view.findViewById(R.id.btnSharePalette);

            mCoverImgVw = (ImageView) view.findViewById(R.id.coverImageView);
            mTxtVw = (TextView) view.findViewById(R.id.title);

            lnrLayout = (LinearLayout)view.findViewById(R.id.lnrColorholder);

            mCoverImgVw.setOnClickListener(this);
            lnrLayout.setOnClickListener(this);
            mShareButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null)
            {
                mItemClickListener.onItemClickListener(v, getAdapterPosition(), beanClassList_s.get(getAdapterPosition()).get_paletteObj().getPaletteID(), beanClassList_s.get(getAdapterPosition()).get_paletteObj().getPaletteName(),
                        beanClassList_s.get(getAdapterPosition()).get_paletteObj().getCoverID_flag(),beanClassList_s.get(getAdapterPosition()).get_paletteObj().getCoverID());
            }
        }
    }


    @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_card_palettelist, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position)
    {
      final   BeanObjectList  beanClass = beanClassList_s.get(position);

        holder.mTxtVw.setText(beanClass.get_paletteObj().getPaletteName() );

        holder.mDeleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//region...............Delete button ................
             final  String plt_id = beanClass.get_paletteObj().getPaletteID();
             final String coverIdFlag_inPalleteObj = beanClass.get_paletteObj().getCoverID_flag();

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                   myDbHelper.deleteSinglePalette(plt_id);

                                   if(coverIdFlag_inPalleteObj.equals("image"))
                                   {
                                       if(!beanClass.get_paletteObj().getCoverID().equals("0")) {
                                           for (BeanObject mObj : beanClass.get_imgOrClrObjLst()) {
                                               if (mObj.getFlag_imgOrClr().equals("image"))
                                               {
                                                   BeanImage imgObj = (BeanImage) mObj.getAnyObjLst();
                                                   String mainImgPath = imgObj.getimagePath();
                                                   String thumbPath = imgObj.getThumbPath();

                                                   if (chkImageExist(mainImgPath))
                                                       MyImageHelper.deleteRecursive(new File(mainImgPath));

                                                   if (chkImageExist(thumbPath))
                                                       MyImageHelper.deleteRecursive(new File(thumbPath));
                                                }
                                           }
                                       }
                                   }

                                   beanClassList_s.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, beanClassList_s.size());
                                    notifyDataSetChanged();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                    //
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyDialogTheme);
                builder.setMessage("Are you sure you want to delete the palette?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
//endregion
            }
        });


        holder.lnrLayout.removeAllViewsInLayout();

     /*   if (beanClass.get_paletteObj().getCoverID_flag().equals("image") && beanClass.get_paletteObj().getCoverID().toString().equals("0"))
            holder.mCoverImgVw.setBackgroundResource(R.mipmap.icon_no_image);
        else{*/
            ArrayList<String> cover_arr = myDbHelper.getCoverFromID(beanClass.get_paletteObj().getPaletteID().toString());

            if(cover_arr.get(1).equals("image"))
            {
                if(cover_arr.get(0).equals("0"))
                    holder.mCoverImgVw.setBackgroundResource(R.mipmap.icon_no_image);
                else {
                    try { holder.mCoverImgVw.setBackground(Drawable.createFromPath(cover_arr.get(4)));
                        /*Bitmap cameraBitmap = MyImageHelper.rotateImageFromURI(mContext, Uri.fromFile(new File(cover_arr.get(4))));
                        holder.mCoverImgVw.setImageBitmap(cameraBitmap);*/
                    }
                    catch(Exception exp)
                        {   Log.d("error_colorApp", exp.toString()); }


                }
            }
            else if(cover_arr.get(1).equals("color"))
            {
                holder.mCoverImgVw.setBackgroundColor(Color.parseColor(cover_arr.get(3)));
            }
       // }

//region.... For Loop through  each obj & dynamically make views horizontally -for images & colors
        for(BeanObject mObj: beanClass.get_imgOrClrObjLst())
        {
            String mFlag = mObj.getFlag_imgOrClr();

//region... if its a image then make imageview & set image in background
            if(mFlag.equals("image") )
            {
                BeanImage imgObj = (BeanImage) mObj.getAnyObjLst();
                String thumbPath = imgObj.getThumbPath();

                if(chkImageExist(thumbPath))
                {
                   Drawable d = Drawable.createFromPath(thumbPath);

                   ImageView _imgVw = new ImageView(mContext);
                   _imgVw.setBackground(d);

                   LinearLayout.LayoutParams lnr = new LinearLayout.LayoutParams(60,80);
                   lnr.setMargins(5,5,5,5);

                   _imgVw.setLayoutParams(lnr);

                   holder.lnrLayout.addView(_imgVw);


                     /*if (beanClass.get_paletteObj().getCoverID().toString().equals("0"))
                          holder.mCoverImgVw.setBackgroundResource(R.mipmap.icon_no_image);
                   else if( (beanClass.get_paletteObj().getCoverID().toString()).equals(imgObj.getimageId().toString()) )
                    {
                        Drawable cvrImg = Drawable.createFromPath(imgObj.getimagePath());
                        holder.mCoverImgVw.setBackground(cvrImg);
                    }*/


                }
            }
//endregion

//region... if its a color then make view & set color in background
            else if(mFlag.equals("color"))
            {
                BeanColor clrObj = (BeanColor) mObj.getAnyObjLst();
              //  String mCOlorCode = "#" + clrObj.getColorCode().trim();
                String mCOlorCode = clrObj.getColorCode().trim();

                View _Vw = new View(mContext);
               _Vw.setBackgroundColor(Color.parseColor(mCOlorCode));

                LinearLayout.LayoutParams lnr = new LinearLayout.LayoutParams(60,80);
                lnr.setMargins(5,5,5,5);

                _Vw.setLayoutParams(lnr);

                holder.lnrLayout.addView(_Vw);

                /*if( (beanClass.get_paletteObj().getCoverID().toString()).equals(clrObj.getColorId().toString()) )
                {
                    holder.mCoverImgVw.setBackgroundColor(Color.parseColor(mCOlorCode));
                }*/

                Log.d("LogRecycler","BeanID:: "+beanClass.get_paletteObj().getPaletteID() + "BeanName:: "+beanClass.get_paletteObj().getPaletteName() +
                        "ColorID:: "+ clrObj.getColorId()  +"colorCode:: "+ mCOlorCode + "\n" );
            }
//endregion
            }
//endregion
    }

    private Boolean chkImageExist(String pth)
    {
       File f = new File(pth);
        if(f.exists())
            return true;
        else
            return false;

    }


    @Override
    public int getItemCount() {
        return beanClassList_s.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}
