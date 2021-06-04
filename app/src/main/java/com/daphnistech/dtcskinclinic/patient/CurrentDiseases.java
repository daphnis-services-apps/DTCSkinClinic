package com.daphnistech.dtcskinclinic.patient;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
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

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class CurrentDiseases extends Fragment {
    ImageView back;
    TextView next;
    TextView skinDisease, cosmetology, hairProblem, sexDisease;
    boolean checkSkinDisease = false, checkCosmetology = false, checkHairProblem = false, checkSexDisease = false;
    PreferenceManager preferenceManager;
    private List<String> currentHistory;


    public CurrentDiseases() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_diseases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
        }
        back = view.findViewById(R.id.back);
        next = view.findViewById(R.id.submit);
        skinDisease = view.findViewById(R.id.skinDisease);
        cosmetology = view.findViewById(R.id.cosmetology);
        hairProblem = view.findViewById(R.id.hairProblem);
        sexDisease = view.findViewById(R.id.sexDisease);

        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);

        settingValues();

        skinDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSkinDisease = checkText(skinDisease, checkSkinDisease);
            }
        });

        cosmetology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCosmetology = checkText(cosmetology, checkCosmetology);
            }
        });

        hairProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkHairProblem = checkText(hairProblem, checkHairProblem);
            }
        });

        sexDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSexDisease = checkText(sexDisease, checkSexDisease);
            }
        });


        back.setOnClickListener(v -> {
            assert getActivity().getSupportFragmentManager() != null;
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        });

        next.setOnClickListener(v -> {
            getSelectedValues();
            if (currentHistory.size() != 0) {
                preferenceManager.setSkinDisease(checkSkinDisease);
                preferenceManager.setCosmetology(checkCosmetology);
                preferenceManager.setHairProblem(checkHairProblem);
                preferenceManager.setSexDisease(checkSexDisease);
                assert getActivity().getSupportFragmentManager() != null;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CurrentDiseasesDetails(currentHistory, 0)).addToBackStack("current").commit();
            } else {
                Toast.makeText(getActivity(), "Nothing Selected", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    private void settingValues() {
        if (preferenceManager.isSkinDisease()) {
            skinDisease.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkSkinDisease = true;
        }

        if (preferenceManager.isCosmetology()) {
            cosmetology.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkCosmetology = true;
        }

        if (preferenceManager.isHairProblem()) {
            hairProblem.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkHairProblem = true;
        }

        if (preferenceManager.isSexDisease()) {
            sexDisease.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkSexDisease = true;
        }
    }

    private void getSelectedValues() {
        currentHistory = new ArrayList<>();
        if (checkSkinDisease) {
            currentHistory.add(Constant.SKIN_DISEASE);
        }

        if (checkCosmetology) {
            currentHistory.add(Constant.COSMETOLOGY);
        }

        if (checkHairProblem) {
            currentHistory.add(Constant.HAIR_PROBLEM);
        }

        if (checkSexDisease) {
            currentHistory.add(Constant.SEX_DISEASE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    private boolean checkText(TextView textView, boolean check) {
        if (check) {
            textView.setBackground(getResources().getDrawable(R.drawable.textview_round));
        } else {
            if (textView == skinDisease)
                textView.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            if (textView == cosmetology)
                textView.setBackground(getResources().getDrawable(R.drawable.pink_round_selected));
            if (textView == hairProblem)
                textView.setBackground(getResources().getDrawable(R.drawable.oranage_round_selected));
            if (textView == sexDisease)
                textView.setBackground(getResources().getDrawable(R.drawable.yellow_round_selected));
        }
        return !check;
    }
}