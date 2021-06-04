package com.daphnistech.dtcskinclinic.patient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daphnistech.dtcskinclinic.R;
import com.daphnistech.dtcskinclinic.helper.Constant;
import com.daphnistech.dtcskinclinic.helper.CustomProgressBar;
import com.daphnistech.dtcskinclinic.helper.PreferenceManager;
import com.daphnistech.dtcskinclinic.helper.Signature;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.florent37.inlineactivityresult.InlineActivityResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class CurrentDiseasesDetails extends Fragment implements View.OnClickListener {
    public final int diseaseCount;
    private final List<String> currentHistory;
    public RelativeLayout pic1Layout, pic2Layout, pic3Layout;
    public ImageView uploadCardView;
    public ImageView pic1, pic2, pic3, image;
    public ImageView pic1Cancel, pic2Cancel, pic3Cancel;
    public String diseaseName;
    public TextView pdfName;
    public boolean isPdfChoose;
    public LinearLayout imageLayout;
    ArrayAdapter<String> accountAdapter, numberAdapter, weekAdapter, problemAdapter;
    private TextView heading, heading1;
    private EditText oldAge, comments;
    private TextView next;
    private TextView affectedArea;
    private TextView viewArea;
    private String[] spinnerArray, numberArray, weekArray, problemArray;
    private ImageView pdfUpload;
    private Spinner spinner, numberSpinner, weekSpinner, problemSpinner;
    private PreferenceManager preferenceManager;
    private View headerView;
    private RelativeLayout problemsLayout, imageView;

    private LinearLayout mContent;
    private Signature mSignature;
    private Bitmap bitmap;
    private TextView mClear, mGetSign, mCancel;
    private Dialog dialog;
    private View view;

    public CurrentDiseasesDetails(List<String> currentHistory, int diseaseCount) {
        this.currentHistory = currentHistory;
        this.diseaseCount = diseaseCount;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_diseases_details, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        accountAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, spinnerArray);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(accountAdapter);

        numberAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, numberArray);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberSpinner.setAdapter(numberAdapter);

        weekAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, weekArray);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinner.setAdapter(weekAdapter);

        if (!diseaseName.equals(Constant.SEX_DISEASE)) {
            problemAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, problemArray);
            problemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            problemSpinner.setAdapter(problemAdapter);
        }

        settingValues();

        comments.setOnTouchListener((v, event) -> {
            if (comments.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }
            return false;
        });

        uploadCardView.setOnClickListener(v -> ImagePicker.Companion.with(getActivity())
                .crop()
                .galleryOnly()
                .compress(256)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start());

        next.setOnClickListener(v -> {
            CustomProgressBar.showProgressBar(getActivity(), false);
            new Handler().postDelayed(() -> {
                savePath();
                preferenceManager.setOldAge(numberArray[numberSpinner.getSelectedItemPosition()] + " " + weekArray[weekSpinner.getSelectedItemPosition()]);
                preferenceManager.setDiseaseType(spinnerArray[spinner.getSelectedItemPosition()]);
                preferenceManager.setComments(comments.getText().toString());
                if (!diseaseName.equals(Constant.SEX_DISEASE))
                    preferenceManager.setSubProblem(problemArray[problemSpinner.getSelectedItemPosition()]);
                assert getActivity().getSupportFragmentManager() != null;
                if (diseaseCount < currentHistory.size() - 1) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new CurrentDiseasesDetails(currentHistory, diseaseCount + 1)).addToBackStack("disease" + diseaseCount).commit();
                } else {
                    //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ConfirmDetails()).addToBackStack("disease" + diseaseCount).commit();
                    new ConfirmDetails().loginPatient(getActivity());
                }
                CustomProgressBar.hideProgressBar();
            }, 500);
        });

        pdfUpload.setOnClickListener(v -> {
            isPdfChoose = true;
            new InlineActivityResult(getActivity())
                    .startForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT).setType("application/pdf"))
                    .onSuccess(result -> pdfName.setText(result.getData().getData().getPath()))
                    .onFail(result -> Toast.makeText(CurrentDiseasesDetails.this.getActivity(), "Failed", Toast.LENGTH_SHORT).show());
        });

        affectedArea.setOnClickListener(v -> show());

        viewArea.setOnClickListener(v -> dialog.show());

        pic1Cancel.setOnClickListener(this);
        pic2Cancel.setOnClickListener(this);
        pic3Cancel.setOnClickListener(this);
    }

    private void savePath() {
        String path = getActivity().getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath();
        if (pic1.getDrawable() != null) {
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic1" + ".PNG");
                ((BitmapDrawable) (pic1.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                preferenceManager.setPic1Path(path + "/pic1" + ".PNG");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            new File(path + "/pic1" + ".PNG").delete();
            preferenceManager.setPic1("");
            preferenceManager.setPic1Path("");
        }
        if (pic2.getDrawable() != null) {
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic2" + ".PNG");
                ((BitmapDrawable) (pic2.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                preferenceManager.setPic2Path(path + "/pic2" + ".PNG");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            new File(path + "/pic2" + ".PNG").delete();
            preferenceManager.setPic2("");
            preferenceManager.setPic2Path("");
        }
        if (pic3.getDrawable() != null) {
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(path + "/pic3" + ".PNG");
                ((BitmapDrawable) (pic3.getDrawable())).getBitmap().compress(Bitmap.CompressFormat.PNG, 50, mFileOutStream);
                preferenceManager.setPic3Path(path + "/pic3" + ".PNG");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            new File(path + "/pic3" + ".PNG").delete();
            preferenceManager.setPic3("");
            preferenceManager.setPic3Path("");
        }
        if (viewArea.getVisibility() == View.VISIBLE && new File(path + "/body" + ".PNG").exists())
            preferenceManager.setAffectedArea(path + "/body" + ".PNG");
        else {
            new File(path + "/body" + ".PNG").delete();
            preferenceManager.setAffectedArea("");
        }
    }

    private void initViews(View view) {
        pic1Layout = view.findViewById(R.id.pic1Layout);
        pic2Layout = view.findViewById(R.id.pic2Layout);
        pic3Layout = view.findViewById(R.id.pic3Layout);
        uploadCardView = view.findViewById(R.id.imageUpload);
        pic1 = view.findViewById(R.id.pic1);
        pic1Cancel = view.findViewById(R.id.pic1Cancel);
        pic2 = view.findViewById(R.id.pic2);
        pic2Cancel = view.findViewById(R.id.pic2Cancel);
        pic3 = view.findViewById(R.id.pic3);
        pic3Cancel = view.findViewById(R.id.pic3Cancel);
        image = view.findViewById(R.id.image);
        next = view.findViewById(R.id.submit);
        imageView = view.findViewById(R.id.imageView);
        pdfUpload = view.findViewById(R.id.pdfUpload);
        pdfName = view.findViewById(R.id.pdfName);
        spinner = view.findViewById(R.id.spinner);
        numberSpinner = view.findViewById(R.id.numberSpinner);
        weekSpinner = view.findViewById(R.id.weekSpinner);
        problemSpinner = view.findViewById(R.id.problemSpinner);
        heading = view.findViewById(R.id.text);
        heading1 = view.findViewById(R.id.text1);
        oldAge = view.findViewById(R.id.oldAge);
        comments = view.findViewById(R.id.diseaseProblem);
        headerView = view.findViewById(R.id.headerView);
        problemsLayout = view.findViewById(R.id.problemLayout);
        imageLayout = view.findViewById(R.id.imageLayout);
        affectedArea = view.findViewById(R.id.affectedArea);
        viewArea = view.findViewById(R.id.viewArea);
        setSpinnerArray();
    }

    private void settingValues() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (diseaseName.equals(Constant.SKIN_DISEASE)) {
                heading1.setTextColor(getResources().getColor(R.color.loginChooser));
                window.setStatusBarColor(getResources().getColor(R.color.loginChooser));
                headerView.setBackground(getActivity().getDrawable(R.drawable.dashboard_header));
                next.setBackground(getActivity().getDrawable(R.drawable.button_custom));
                imageView.setBackground(getActivity().getDrawable(R.drawable.button_custom));
                image.setImageDrawable(getActivity().getDrawable(R.drawable.skin_disease));
            } else if (diseaseName.equals(Constant.SEX_DISEASE)) {
                heading1.setTextColor(getResources().getColor(R.color.pink));
                window.setStatusBarColor(getResources().getColor(R.color.pink));
                headerView.setBackground(getActivity().getDrawable(R.drawable.pink_header));
                next.setBackground(getActivity().getDrawable(R.drawable.pink_button_custom));
                imageView.setBackground(getActivity().getDrawable(R.drawable.pink_button_custom));
                image.setImageDrawable(getActivity().getDrawable(R.drawable.sex_disease));
            } else if (diseaseName.equals(Constant.HAIR_PROBLEM)) {
                heading1.setTextColor(getResources().getColor(R.color.light_black));
                window.setStatusBarColor(getResources().getColor(R.color.light_black));
                headerView.setBackground(getActivity().getDrawable(R.drawable.light_black_header));
                next.setBackground(getActivity().getDrawable(R.drawable.light_black_button_custom));
                imageView.setBackground(getActivity().getDrawable(R.drawable.light_black_button_custom));
                image.setImageDrawable(getActivity().getDrawable(R.drawable.hair_problem));
            } else if (diseaseName.equals(Constant.COSMETOLOGY)) {
                heading1.setTextColor(getResources().getColor(R.color.palm));
                window.setStatusBarColor(getResources().getColor(R.color.palm));
                headerView.setBackground(getActivity().getDrawable(R.drawable.palm_header));
                next.setBackground(getActivity().getDrawable(R.drawable.palm_button_custom));
                imageView.setBackground(getActivity().getDrawable(R.drawable.palm_button_custom));
                image.setImageDrawable(getActivity().getDrawable(R.drawable.cosmetology));
            }
        }
        preferenceManager = new PreferenceManager(getActivity(), diseaseName);
        heading.setText(String.format("Add %s Info", diseaseName));
        heading1.setText(String.format("Add %s Info", diseaseName));
        oldAge.setText(preferenceManager.getOldAge());
        comments.setText(preferenceManager.getComments());

        spinner.setSelection(accountAdapter.getPosition(preferenceManager.getDiseaseType()));
        numberSpinner.setSelection(numberAdapter.getPosition(preferenceManager.getOldAge().split(" ")[0]));
        try {
            weekSpinner.setSelection(weekAdapter.getPosition(preferenceManager.getOldAge().split(" ")[1]));
        } catch (Exception e) {

        }
        if (!diseaseName.equals(Constant.SEX_DISEASE))
            problemSpinner.setSelection(problemAdapter.getPosition(preferenceManager.getSubProblem()));


        if (preferenceManager.getSteps() == 1) {
            pic1.setImageURI(Uri.parse(preferenceManager.getPic1Path()));
            pic1Layout.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
        } else if (preferenceManager.getSteps() == 2) {
            pic1.setImageURI(Uri.parse(preferenceManager.getPic1Path()));
            imageLayout.setVisibility(View.VISIBLE);
            pic1Layout.setVisibility(View.VISIBLE);
            pic2.setImageURI(Uri.parse(preferenceManager.getPic2Path()));
            pic2Layout.setVisibility(View.VISIBLE);
        } else if (preferenceManager.getSteps() == 3) {
            pic1.setImageURI(Uri.parse(preferenceManager.getPic1Path()));
            imageLayout.setVisibility(View.VISIBLE);
            pic1Layout.setVisibility(View.VISIBLE);
            pic2.setImageURI(Uri.parse(preferenceManager.getPic2Path()));
            pic2Layout.setVisibility(View.VISIBLE);
            pic3.setImageURI(Uri.parse(preferenceManager.getPic3Path()));
            pic3Layout.setVisibility(View.VISIBLE);
            uploadCardView.setVisibility(View.GONE);
        }
        if (!preferenceManager.getAffectedArea().equals("")) {
            affectedArea.setVisibility(View.GONE);
            viewArea.setVisibility(View.VISIBLE);
        }
        if (!preferenceManager.getPDF().equals("")) {
            pdfName.setText(preferenceManager.getPDF());
        }
    }

    private void setSpinnerArray() {
        diseaseName = currentHistory.get(diseaseCount);
        switch (currentHistory.get(diseaseCount)) {
            case Constant.SKIN_DISEASE:
                spinnerArray = getResources().getStringArray(R.array.skinDiseasesArray);
                problemArray = getResources().getStringArray(R.array.skinProblemsArray);
                break;
            case Constant.COSMETOLOGY:
                spinnerArray = getResources().getStringArray(R.array.cosmeticsArray);
                problemArray = getResources().getStringArray(R.array.cosmeticProblemsArray);
                break;
            case Constant.HAIR_PROBLEM:
                spinnerArray = getResources().getStringArray(R.array.hairProblemArray);
                problemArray = getResources().getStringArray(R.array.skinProblemsArray);
                break;
            case Constant.SEX_DISEASE:
                spinnerArray = getResources().getStringArray(R.array.sexProblemArray);
                problemsLayout.setVisibility(View.GONE);
                break;
        }
        numberArray = new String[31];
        for (int i = 0; i < 31; i++) {
            numberArray[i] = i + 1 + "";
        }
        weekArray = getResources().getStringArray(R.array.weekArray);
    }

    @Override
    public void onClick(View v) {
        //String path = getActivity().getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath();
        if (v.getId() == R.id.pic1Cancel) {
            if (pic2Layout.getVisibility() == View.VISIBLE && pic3Layout.getVisibility() == View.VISIBLE) {
                pic1.setImageDrawable(pic2.getDrawable());
                pic2.setImageDrawable(pic3.getDrawable());
                pic3.setImageURI(Uri.EMPTY);
                pic3Layout.setVisibility(View.GONE);
            } else if (pic2Layout.getVisibility() == View.VISIBLE) {
                pic1.setImageDrawable(pic2.getDrawable());
                pic2.setImageURI(Uri.EMPTY);
                pic2Layout.setVisibility(View.GONE);
            } else if (pic3Layout.getVisibility() == View.VISIBLE) {
                pic1.setImageDrawable(pic3.getDrawable());
                pic3.setImageURI(Uri.EMPTY);
                pic3Layout.setVisibility(View.GONE);
            } else {
                pic1.setImageURI(Uri.EMPTY);
                pic1Layout.setVisibility(View.GONE);
                imageLayout.setVisibility(View.GONE);
            }
        } else if (v.getId() == R.id.pic2Cancel) {
            if (pic3Layout.getVisibility() == View.VISIBLE) {
                pic2.setImageDrawable(pic3.getDrawable());
                pic3.setImageURI(Uri.EMPTY);
                pic3Layout.setVisibility(View.GONE);
            } else {
                pic2.setImageURI(Uri.EMPTY);
                pic2Layout.setVisibility(View.GONE);
            }

        } else if (v.getId() == R.id.pic3Cancel) {
            pic3.setImageURI(Uri.EMPTY);
            pic3Layout.setVisibility(View.GONE);
        }
        preferenceManager.setSteps(preferenceManager.getSteps() - 1);
        uploadCardView.setVisibility(View.VISIBLE);
    }

    private void show() {
        // Dialog Function
        dialog = new Dialog(getActivity());
        // Removing the features of Normal Dialogs
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);

        dialog_action();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void dialog_action() {
        mContent = dialog.findViewById(R.id.linearLayout);
        mSignature = new Signature(getActivity());
        mSignature.setBackground(getResources().getDrawable(R.drawable.body));
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mClear = dialog.findViewById(R.id.clear);
        mGetSign = dialog.findViewById(R.id.getsign);
        mGetSign.setEnabled(true);
        mCancel = dialog.findViewById(R.id.cancel);
        view = mContent;

        mClear.setOnClickListener(v -> {
            Log.v("log_tag", "Panel Cleared");
            mSignature.clear();
            viewArea.setVisibility(View.GONE);
            affectedArea.setVisibility(View.VISIBLE);
        });

        mGetSign.setOnClickListener(v -> {

            Log.v("log_tag", "Panel Saved");
            view.setDrawingCacheEnabled(true);
            save(view);
            dialog.dismiss();
            viewArea.setVisibility(View.VISIBLE);
            affectedArea.setVisibility(View.GONE);

        });
        mCancel.setOnClickListener(v -> {
            Log.v("log_tag", "Panel Canceled");
            mSignature.undo();
        });
        dialog.show();
    }

    public void save(View v) {
        String path = getActivity().getApplicationContext().getExternalFilesDir("Me/" + diseaseName).getAbsolutePath();
        bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        try {
            // Output the file
            OutputStream mFileOutStream = new FileOutputStream(path + "/body" + ".PNG");
            Log.v("log_tag", "path" + mFileOutStream);
            // Convert the output file to Image such as .png
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();

        } catch (Exception e) {
            Log.v("log_tag", e.toString());
        }
    }
}