package com.fireflyglobe.kmamo.click;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.common.internal.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class purchases extends AppCompatActivity implements PurchasesUpdatedListener {
    BillingClient billingClient;
    List<String> skuList = new ArrayList<>();
    private String TAG = "purchases";

    Button loadProduct;
    RecyclerView recyclerProduct;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchases);
        skuList.add("100m");
        skuList.add("50m");

        setupBillingClient();
        Toolbar toolbar = findViewById(R.id.toolbarPurchases);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Upgrade");

        //view
        recyclerProduct = (RecyclerView) findViewById(R.id.purchasesReView);

        recyclerProduct.setHasFixedSize(true);
        recyclerProduct.setLayoutManager(new LinearLayoutManager(this));

    }

    private void loadProductToRecyclerView(List<SkuDetails> skuDetailsList) {
        purchaseAdapter adapter = new purchaseAdapter(this,skuDetailsList,billingClient);
        recyclerProduct.setAdapter(adapter);
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    if(billingClient.isReady()){
                        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                                .setSkusList(skuList)
                                .setType(BillingClient.SkuType.SUBS) // if we add from manage product,
                                .build();

                        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                    loadProductToRecyclerView(skuDetailsList);
                                }else{
                                    Toast.makeText(purchases.this, "cannot query product", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(purchases.this, "Billing client is not ready", Toast.LENGTH_SHORT).show();

                    }
                    Toast.makeText(purchases.this, "Success", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(purchases.this, ""+billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(purchases.this, "You were disconnected from billing",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

        //Here, if user click to buy, we will retrieve data here
        //Toast.makeText(this, "purchases item: " + purchases.size(), Toast.LENGTH_SHORT).show();

        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases!= null){

                for(Purchase purchase : purchases){
                    handlePurchase(purchase);
                }
        }else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.d(TAG, "onPurchasesUpdated: " + billingResult.getResponseCode());
        }

    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            Log.d(TAG, "onAcknowledgePurchaseResponse: works");
        }

    };

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            SharedPreferences pref =
                    getSharedPreferences("shared preferences", MODE_PRIVATE);         //copy the groups from shared pref into arraylist

            SharedPreferences.Editor mEditor = pref.edit();
            if (purchase.getSku().equals("100m")) {
                mEditor.putInt("radius", 100).apply();
                Toast.makeText(this, "radius extended to 100m", Toast.LENGTH_LONG).show();
            }
            if (purchase.getSku().equals("50m")) {
                mEditor.putInt("radius", 50).apply();
                Toast.makeText(this, "radius extended to 50m", Toast.LENGTH_LONG).show();
            }
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }


        }else if((purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) ){
            Toast.makeText(purchases.this, "Please follow the instructions that are given.", Toast.LENGTH_LONG).show();


        }
    }
}
