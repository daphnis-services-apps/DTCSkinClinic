package com.daphnistech.dtcskinclinic.patient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import ss.anoop.awesometextinputlayout.AwesomeTextInputLayout;

public class PreviousDiseases extends Fragment {
    ImageView back;
    TextView next;
    TextView diabetes, hyperTension, thyroidProblem, drugAllergy;
    boolean checkDiabetes = false, checkHyperTension = false, checkThyroidProblem = false, checkDrugAllergy = false;
    List<String> previousHistory;
    PreferenceManager preferenceManager;
    private EditText remarks;
    ViewGroup parent;
    AwesomeTextInputLayout remarksLayout;

    public PreviousDiseases() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_previous_diseases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        back = view.findViewById(R.id.back);
        next = view.findViewById(R.id.submit);
        diabetes = view.findViewById(R.id.diabetes);
        hyperTension = view.findViewById(R.id.hyperTension);
        thyroidProblem = view.findViewById(R.id.thyroidProblem);
        drugAllergy = view.findViewById(R.id.drugAllergy);
        remarks = view.findViewById(R.id.remarks);
        parent = view.findViewById(R.id.diseaseLayout2);
        remarksLayout = view.findViewById(R.id.remarksLayout);

        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);

        settingValues();

        diabetes.setOnClickListener(v -> checkDiabetes = checkText(diabetes, checkDiabetes));

        hyperTension.setOnClickListener(v -> checkHyperTension = checkText(hyperTension, checkHyperTension));

        thyroidProblem.setOnClickListener(v -> checkThyroidProblem = checkText(thyroidProblem, checkThyroidProblem));

        drugAllergy.setOnClickListener(v -> {
            toggle(!checkDrugAllergy);
            checkDrugAllergy = checkText(drugAllergy, checkDrugAllergy);
        });


        back.setOnClickListener(v -> {
            assert getActivity().getSupportFragmentManager() != null;
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        });

        next.setOnClickListener(v -> {
            getSelectedValues();
            preferenceManager.setDiabetes(checkDiabetes);
            preferenceManager.setHyperTension(checkHyperTension);
            preferenceManager.setThyroidProblem(checkThyroidProblem);
            preferenceManager.setDrugAllergy(checkDrugAllergy);
            preferenceManager.setDrugRemarks(remarks.getText().toString());
            assert getActivity().getSupportFragmentManager() != null;
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CurrentDiseases()).addToBackStack("history").commit();
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    private void settingValues() {
        if (preferenceManager.isDiabetes()) {
            diabetes.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkDiabetes = true;
        }

        if (preferenceManager.isHyperTension()) {
            hyperTension.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkHyperTension = true;
        }

        if (preferenceManager.isThyroidProblem()) {
            thyroidProblem.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkThyroidProblem = true;
        }

        if (preferenceManager.isDrugAllergy()) {
            drugAllergy.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
            checkDrugAllergy = true;
            toggle(true);
        }
    }

    private void getSelectedValues() {
        previousHistory = new ArrayList<>();

        if (checkDiabetes) {
            previousHistory.add("Diabetes");
        }

        if (checkHyperTension) {
            previousHistory.add("Hyper Tension");
        }

        if (checkThyroidProblem) {
            previousHistory.add("Thyroid Problem");
        }

        if (checkDrugAllergy) {
            previousHistory.add("Drug Allergy");
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressWarnings("deprecation")
    private boolean checkText(TextView textView, boolean check) {
        if (check) {
            textView.setBackground(getResources().getDrawable(R.drawable.textview_round));
        } else {
            textView.setBackground(getResources().getDrawable(R.drawable.text_view_round_selected));
        }
        return !check;
    }

    private void toggle(boolean show) {

        Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(1500);
        transition.addTarget(R.id.remarksLayout);

        TransitionManager.beginDelayedTransition(parent, transition);
        remarksLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}