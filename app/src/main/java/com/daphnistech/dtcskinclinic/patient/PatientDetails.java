package com.daphnistech.dtcskinclinic.patient;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.github.dhaval2404.imagepicker.ImagePicker;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientDetails extends Fragment {
    public CircleImageView profilePic;
    EditText name, age, address, pin;
    TextView next;
    RadioButton male, female, others;
    RadioGroup genderGroup;
    ImageView back;
    PreferenceManager preferenceManager;
    ArrayAdapter<String> accountAdapter;
    private String[] stateArray;
    private Spinner spinner;

    public PatientDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_details, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        accountAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, stateArray);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(accountAdapter);

        settingValues();

        address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (address.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                return false;
            }
        });

        back.setOnClickListener(v -> getActivity().finish());

        next.setOnClickListener(v -> {
            if (validateFields()) {
                setPreferences();
                assert getActivity().getSupportFragmentManager() != null;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CurrentDiseases()).addToBackStack("user").commit();
            } else {
                Toast.makeText(getActivity(), "Fill all required fields", Toast.LENGTH_SHORT).show();
            }
        });

        profilePic.setOnClickListener(v -> ImagePicker.Companion.with(getActivity())
                .crop()
                .galleryOnly()
                .compress(256)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start());


    }

    private boolean validateFields() {
        if (name.getText().toString().isEmpty())
            return false;

        if (age.getText().toString().isEmpty())
            return false;

        if (genderGroup.getCheckedRadioButtonId() == -1)
            return false;

        return true;
    }

    private void setPreferences() {
        preferenceManager.setName(name.getText().toString());
        preferenceManager.setAge(age.getText().toString());
        preferenceManager.setGender(male.isChecked() ? Constant.MALE : female.isChecked() ? Constant.FEMALE : Constant.OTHERS);
        preferenceManager.setAddress(address.getText().toString());
        preferenceManager.setState(stateArray[spinner.getSelectedItemPosition()]);
        preferenceManager.setPIN(pin.getText().toString());
    }

    private void initViews(View view) {
        profilePic = view.findViewById(R.id.editProfilePic);
        name = view.findViewById(R.id.name);
        age = view.findViewById(R.id.age);
        address = view.findViewById(R.id.address);
        pin = view.findViewById(R.id.pin);
        next = view.findViewById(R.id.submit);
        back = view.findViewById(R.id.back);
        genderGroup = view.findViewById(R.id.genderLayout);
        male = view.findViewById(R.id.male);
        female = view.findViewById(R.id.female);
        others = view.findViewById(R.id.others);
        spinner = view.findViewById(R.id.spinner);

        stateArray = getResources().getStringArray(R.array.india_states);
    }

    private void settingValues() {
        preferenceManager = new PreferenceManager(getActivity(), Constant.USER_DETAILS);
        if (!preferenceManager.getProfileImage().equals(""))
            profilePic.setImageURI(Uri.parse(preferenceManager.getProfileImage()));
        name.setText(preferenceManager.getName());
        age.setText(preferenceManager.getAge());

        switch (preferenceManager.getGender()) {
            case Constant.MALE:
                male.setChecked(true);
                break;
            case Constant.FEMALE:
                female.setChecked(true);
                break;
            case Constant.OTHERS:
                others.setChecked(true);
                break;
        }

        address.setText(preferenceManager.getAddress());
        spinner.setSelection(accountAdapter.getPosition(preferenceManager.getState()));
        pin.setText(preferenceManager.getPIN());
    }
}