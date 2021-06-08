package com.daphnistech.dtcskinclinic.transaction;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.adapter.TransactionAdapter;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.UserInterface;
import com.daphnistech.dtcskinclinic.model.Transaction;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class TransactionsList extends Fragment {
    List<Transaction> transactionList;
    TransactionAdapter transactionAdapter;
    RecyclerView recyclerView;
    TextView noMessage;
    LottieAnimationView animationView;
    ImageView back;

    public TransactionsList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        initViews(view);
        getTransactions();
        transactionAdapter = new TransactionAdapter(getActivity(), transactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(transactionAdapter);
        back.setOnClickListener(v -> getFragmentManager().popBackStackImmediate());
    }

    private void getTransactions() {
        CustomProgressBar.showProgressBar(getActivity(), false);
        transactionList = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UserInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        UserInterface api = retrofit.create(UserInterface.class);

        Call<String> call;
        if (new PreferenceManager(getActivity(), Constant.USER_DETAILS).getLoginType().equals(Constant.PATIENT))
            call = api.getPatientTransactions(new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID());
        else
            call = api.getDoctorTransactions(new PreferenceManager(getActivity(), Constant.USER_DETAILS).getUserID());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            if (!jsonObject.getBoolean("error")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("transactions");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject transactions = new JSONObject(jsonArray.getString(i));
                                    transactionList.add(new Transaction(transactions.getInt("payment_id"), transactions.getInt("transaction_id"), transactions.getInt("appointment_id"), transactions.getInt("transaction_amount"), transactions.getString("transaction_date"), transactions.getString("transaction_time"), transactions.getString("transaction_status")));
                                }
                                transactionAdapter.notifyDataSetChanged();
                                CustomProgressBar.hideProgressBar();
                                if (transactionList.size() > 0) {
                                    animationView.setVisibility(View.GONE);
                                    noMessage.setVisibility(View.GONE);
                                }
                            } else {
                                Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                CustomProgressBar.hideProgressBar();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            CustomProgressBar.hideProgressBar();
                        }
                        //Parsing the JSON

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        Toast.makeText(getActivity(), "Returned empty response", Toast.LENGTH_SHORT).show();
                        CustomProgressBar.hideProgressBar();
                    }
                } else if (response.errorBody() != null) {
                    Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
                    CustomProgressBar.hideProgressBar();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                CustomProgressBar.hideProgressBar();
            }
        });
    }

    private void initViews(View view) {
        back = view.findViewById(R.id.back);
        recyclerView = view.findViewById(R.id.transactionRecyclerView);
        noMessage = view.findViewById(R.id.noMessage);
        animationView = view.findViewById(R.id.animationView);
    }
}