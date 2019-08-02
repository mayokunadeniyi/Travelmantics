package com.example.travelmantics.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.travelmantics.Model.TravelDeal;
import com.example.travelmantics.R;
import com.example.travelmantics.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private EditText titleTxt;
    private EditText descriptionTxt;
    private EditText priceTxt;
    private ImageButton dealImage;
    private ProgressDialog progressDialog;
    private Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUtil.openFbReference("traveldeals");

        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        storageReference = FirebaseUtil.storageReference;

        progressDialog = new ProgressDialog(this);
        titleTxt = (EditText) findViewById(R.id.txtTitle);
        descriptionTxt = (EditText) findViewById(R.id.txtDescription);
        priceTxt = (EditText) findViewById(R.id.txtPrice);
        dealImage = (ImageButton) findViewById(R.id.new_deal_image);

        dealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MainActivity.this);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageURI = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
            dealImage.setImageURI(imageURI);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_action:
                saveDeal();
                clean();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clean() {
        titleTxt.setText("");
        descriptionTxt.setText("");
        priceTxt.setText("");
        dealImage.setImageDrawable(getResources().getDrawable(R.drawable.add_deal_image_btn));
        titleTxt.requestFocus();
    }

    private void saveDeal() {
        progressDialog.setMessage("Saving deal...");
        progressDialog.show();

        final String title = titleTxt.getText().toString().trim();
        final String description = descriptionTxt.getText().toString().trim();
        final String price = priceTxt.getText().toString().trim();

        if (!title.isEmpty() && !description.isEmpty() && !price.isEmpty() && imageURI != null){

            final StorageReference filePath = storageReference.child("TravelDeal_Images")
                    .child((imageURI.getLastPathSegment()));
            filePath.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                               String downloadUrl = uri.toString();

                                TravelDeal travelDeal = new TravelDeal(title, description, price, downloadUrl);
                                databaseReference.push().setValue(travelDeal);
                                progressDialog.dismiss();
                                Snackbar.make(getCurrentFocus(), getString(R.string.snackbar_text_save_deal), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

    }
}
