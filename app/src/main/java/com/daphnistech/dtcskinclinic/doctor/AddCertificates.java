package com.daphnistech.dtcskinclinic.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;

import org.jetbrains.annotations.NotNull;

public class AddCertificates extends Fragment implements View.OnClickListener {
    ConstraintLayout firstLayout, secondLayout, thirdLayout, fourthLayout, fifthLayout;
    ImageView achievementPic1, achievementPic2, achievementPic3, achievementPic4, achievementPic5;
    TextView addMore;
    ImageView addMoreImage;
    ImageView cancelFirstImage;
    TextView cancelFirst, cancelSecond, cancelThird, cancelFourth, cancelFifth;
    EditText description1, description2, description3, description4, description5;
    int currentVisible = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_certificates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        addMore.setOnClickListener(v -> {
            if (currentVisible == 1) {
                cancelFirst.setVisibility(View.VISIBLE);
                secondLayout.setVisibility(View.VISIBLE);
            } else if (currentVisible == 2) {
                thirdLayout.setVisibility(View.VISIBLE);
            } else if (currentVisible == 3) {
                fourthLayout.setVisibility(View.VISIBLE);
            } else if (currentVisible == 4) {
                fifthLayout.setVisibility(View.VISIBLE);
            }
            currentVisible++;
        });

        cancelFirst.setOnClickListener(this);
        cancelSecond.setOnClickListener(this);
        cancelThird.setOnClickListener(this);
        cancelFourth.setOnClickListener(this);
        cancelFifth.setOnClickListener(this);
    }

    private void initViews(View view) {
        firstLayout = view.findViewById(R.id.firstLayout);
        secondLayout = view.findViewById(R.id.secondLayout);
        thirdLayout = view.findViewById(R.id.thirdLayout);
        fourthLayout = view.findViewById(R.id.fourthLayout);
        fifthLayout = view.findViewById(R.id.fifthLayout);
        achievementPic1 = view.findViewById(R.id.achievementPic1);
        achievementPic2 = view.findViewById(R.id.achievementPic2);
        achievementPic3 = view.findViewById(R.id.achievementPic3);
        achievementPic4 = view.findViewById(R.id.achievementPic4);
        achievementPic5 = view.findViewById(R.id.achievementPic5);
        addMore = view.findViewById(R.id.addMore);
        addMoreImage = view.findViewById(R.id.addMoreImage);
        cancelFirst = view.findViewById(R.id.cancelFirst);
        cancelSecond = view.findViewById(R.id.cancelSecond);
        cancelThird = view.findViewById(R.id.cancelThird);
        cancelFourth = view.findViewById(R.id.cancelFourth);
        cancelFifth = view.findViewById(R.id.cancelFifth);
        description1 = view.findViewById(R.id.description1);
        description2 = view.findViewById(R.id.description2);
        description3 = view.findViewById(R.id.description3);
        description4 = view.findViewById(R.id.description4);
        description5 = view.findViewById(R.id.description5);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancelFirst) {
            firstLayout.setVisibility(View.GONE);
        } else if (id == R.id.cancelSecond) {
            secondLayout.setVisibility(View.GONE);
        } else if (id == R.id.cancelThird) {
            thirdLayout.setVisibility(View.GONE);
        } else if (id == R.id.cancelFourth) {
            fourthLayout.setVisibility(View.GONE);
        } else if (id == R.id.cancelFifth) {
            fifthLayout.setVisibility(View.GONE);
        }
        currentVisible--;
    }
}