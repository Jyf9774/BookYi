package com.jyf9774.bookyi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.MyViewHolder> {

    ArrayList<Book> mArray;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;


    public BookAdapter(final Context context, ArrayList<Book> array) {
        mArray = array;
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txv_bookId, txv_bookName, txv_bookPriceOrDate,txv_userName;
        ImageView bookImg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txv_bookId = itemView.findViewById(R.id.card_book_id);
            txv_bookName = itemView.findViewById(R.id.card_book_name);
            txv_bookPriceOrDate = itemView.findViewById(R.id.card_book_price_or_date);
            txv_userName = itemView.findViewById(R.id.card_user_name);
            bookImg = itemView.findViewById(R.id.card_book_image);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }


    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public BookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.book_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final BookAdapter.MyViewHolder holder, int position) {
        holder.txv_bookId.setText("图书ID："+mArray.get(position).bookId);
        if(mArray.get(position).bookName.charAt(0) == '《'){
            holder.txv_bookName.setText(mArray.get(position).bookName);
        }
        else{
            holder.txv_bookName.setText("《"+mArray.get(position).bookName+"》");
        }
        holder.txv_userName.setText("发布者："+mArray.get(position).username);
        if(mArray.get(position).bookSaled){
            holder.txv_bookPriceOrDate.setText("交易已完成");
        }else{
            if(!mArray.get(position).bookSaleOrBorrow){
                holder.txv_bookPriceOrDate.setText("¥"+mArray.get(position).bookPrice+"元");
            }else{
                holder.txv_bookPriceOrDate.setText("出借"+mArray.get(position).bookBorrowDate+"天");
            }
        }
        Glide.with(mContext).load(mArray.get(position).bookPicture).into(holder.bookImg);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return mArray == null ? 0 : mArray.size();
    }
}
