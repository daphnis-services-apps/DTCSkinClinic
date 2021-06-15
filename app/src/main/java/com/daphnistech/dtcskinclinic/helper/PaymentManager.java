package com.daphnistech.dtcskinclinic.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaymentManager {
    private static Context context;

    public static synchronized PaymentManager getInstance(Context ctx) {
        context = ctx;
        return new PaymentManager();
    }

    public Map<String, String> upiPaymentDataOperation(ArrayList<String> data) {
        if (MyApplication.isConnectionAvailable(context)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: " + str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String txnId = "";
            String[] response = str.split("&");
            for (String s : response) {
                String[] equalStr = s.split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    } else if (equalStr[0].toLowerCase().equals("txnId".toLowerCase())) {
                        txnId = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(context, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: " + approvalRefNo);
                Map<String, String> map = new HashMap<>();
                map.put("status", "Success");
                map.put("transaction_id", txnId);
                return map;
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(context, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            } else if (status.equals("pending")) {
                Toast.makeText(context, "Transaction Pending. Please wait 48 hours for Payment update. If payment not updated kindly contact us.", Toast.LENGTH_LONG).show();
                Map<String, String> map = new HashMap<>();
                map.put("status", "Pending from Bank End");
                map.put("transaction_id", txnId);
                return map;
            } else {
                Toast.makeText(context, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
