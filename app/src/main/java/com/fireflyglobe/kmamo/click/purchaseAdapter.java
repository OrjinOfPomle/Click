package com.fireflyglobe.kmamo.click;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class purchaseAdapter extends RecyclerView.Adapter<purchaseAdapter.ViewHolder>{
    purchases purchases1;
    List<SkuDetails> skuDetailsList;
    BillingClient billingClint;

    public purchaseAdapter(purchases purchases1, List<SkuDetails> skuDetailsList, BillingClient billingClint) {
        this.purchases1 = purchases1;
        this.skuDetailsList = skuDetailsList;
        this.billingClint = billingClint;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(purchases1.getBaseContext()).inflate(R.layout.single_purchase,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_product.setText(skuDetailsList.get(position).getTitle());

        String priceString = skuDetailsList.get(position).getPrice() +" " + getStringVersion(skuDetailsList.get(position).getSubscriptionPeriod());
        Log.d("purchaseAdapter", "onBindViewHolder: priceString = " + priceString);
        holder.price.setText(priceString);
        final int i = position;
        //Product click
        holder.setiProductClickListener(new IProductClickListener() {
            @Override
            public void onProductClickListener(View view, int position) {
                //Launch Billing
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails((skuDetailsList.get(i)))
                        .build();
                billingClint.launchBillingFlow(purchases1, billingFlowParams);

            }
        });
    }

    @Override
    public int getItemCount() {
        return skuDetailsList.size();
    }

    private String getStringVersion(String period){
        if(period.equals("P1W")){
            return "/week";
        }
        if(period.equals("P1M")){
            return "/month";
        }
        if(period.equals("P3M")){
            return "/3 months";
        }
        if(period.equals("P6M")){
            return "/6 months";
        }
        if(period.equals("P1Y")){
            return "/year";
        }
        return "";
    }

    public class ViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_product, price;
        IProductClickListener iProductClickListener;


        public void setiProductClickListener(IProductClickListener iProductClickListener) {
            this.iProductClickListener = iProductClickListener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_product = itemView.findViewById(R.id.purchaseName);
            price = itemView.findViewById(R.id.price);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iProductClickListener.onProductClickListener(v, getAdapterPosition());
        }
    }
}
