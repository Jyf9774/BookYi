package com.jyf9774.bookyi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<com.jyf9774.bookyi.OrderAdapter.MyViewHolder> {

    ArrayList<Order> mArray;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private com.jyf9774.bookyi.OrderAdapter.OnItemClickListener mOnItemClickListener;


    public OrderAdapter(final Context context, ArrayList<Order> array) {
        mArray = array;
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView txv_orderBookId, txv_orderBookName,txv_orderBuyerName, txv_orderOwnerName, txv_orderState, txv_orderTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.orderCard);
            txv_orderBookId = itemView.findViewById(R.id.order_book_id);
            txv_orderBookName = itemView.findViewById(R.id.order_book_name);
            txv_orderBuyerName = itemView.findViewById(R.id.order_buyer_name);
            txv_orderOwnerName = itemView.findViewById(R.id.order_owner_name);
            txv_orderState = itemView.findViewById(R.id.order_State);
            txv_orderTime = itemView.findViewById(R.id.order_create_time);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }


    public void setOnItemClickListener(com.jyf9774.bookyi.OrderAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public com.jyf9774.bookyi.OrderAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new com.jyf9774.bookyi.OrderAdapter.MyViewHolder(mLayoutInflater.inflate(R.layout.order_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final com.jyf9774.bookyi.OrderAdapter.MyViewHolder holder, int position) {
        holder.txv_orderState.setText(mArray.get(position).state);
        holder.txv_orderBuyerName.setText("买家:" + mArray.get(position).buyerName);
        holder.txv_orderOwnerName.setText("书主:" + mArray.get(position).ownerName);
        holder.txv_orderBookId.setText("书籍ID:" + mArray.get(position).bookId);
        if(mArray.get(position).bookName.charAt(0) == '《'){
            holder.txv_orderBookName.setText(mArray.get(position).bookName);
        }
        else{
            holder.txv_orderBookName.setText("《"+mArray.get(position).bookName+"》");
        }
        if (mArray.get(position).state.equals("待确认")) {
            holder.cardView.setCardBackgroundColor(Color.argb(75,255,255,125));
            holder.txv_orderTime.setText(TimeUtility.getTime(mArray.get(position).createTime.substring(0,19)));
        } else if(mArray.get(position).state.equals("已完成")){
            holder.cardView.setCardBackgroundColor(Color.argb(75,150,255,150));
            holder.txv_orderTime.setText(TimeUtility.getTime(mArray.get(position).confirmTime.substring(0,19)));
        }else{
            holder.cardView.setCardBackgroundColor(Color.argb(75,255,50,0));
            holder.txv_orderTime.setText(TimeUtility.getTime(mArray.get(position).confirmTime.substring(0,19)));
        }


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
