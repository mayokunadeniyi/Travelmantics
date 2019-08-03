package com.example.travelmantics.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.travelmantics.Model.TravelDeal;
import com.example.travelmantics.R;
import com.example.travelmantics.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

public class NewTravelDealActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private EditText titleTxt;
    private EditText descriptionTxt;
    private EditText priceTxt;
    private ImageButton dealImage;
    private ProgressDialog progressDialog;
    private Uri imageURI;
    private TravelDeal travelDeal;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_travel_deal);

        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        storageReference = FirebaseUtil.storageReference;

        progressDialog = new ProgressDialog(this);
        titleTxt = (EditText) findViewById(R.id.txtTitle);
        descriptionTxt = (EditText) findViewById(R.id.txtDescription);
        priceTxt = (EditText) findViewById(R.id.txtPrice);
        dealImage = (ImageButton) findViewById(R.id.new_deal_image);

        Intent intent = getIntent();
        TravelDeal travelDeal = (TravelDeal) intent.getSerializableExtra("TravelDeal");
        if (travelDeal == null) {
            travelDeal = new TravelDeal();
        }
        this.travelDeal = travelDeal;
        titleTxt.setText(travelDeal.getTitle());
        descriptionTxt.setText(travelDeal.getDescription());
        priceTxt.setText(travelDeal.getPrice());

        dealImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(NewTravelDealActivity.this);
            }
        });

        Picasso.get()
                .load(travelDeal.getImageUrl())
                .into(dealImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if (resultCode == RESULT_OK) {
            imageURI = result.getUri();
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Exception error = result.getError();
            Log.d("ImageError", error.getMessage());
        }
        dealImage.setImageURI(imageURI);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (FirebaseUtil.isAdmin == true) {
            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.save_action).setVisible(true);
            enableEditTexts(true);
        } else {
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.save_action).setVisible(false);
            enableEditTexts(false);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_action:
                saveDeal();
                return true;

            case R.id.action_delete:
                deleteDeal();
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

    private void backToTravelListActivity() {
        startActivity(new Intent(getApplicationContext(), TravelDealListActivity.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveDeal() {
        progressDialog.setMessage("Saving deal...");
        progressDialog.show();

        if (travelDeal.getId() != null) {

            travelDeal.setImageUrl(travelDeal.getImageUrl());
            travelDeal.setTitle(titleTxt.getText().toString());
            travelDeal.setDescription(descriptionTxt.getText().toString());
            travelDeal.setPrice(priceTxt.getText().toString());

            databaseReference.child(travelDeal.getId()).setValue(travelDeal).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(NewTravelDealActivity.this, "Deal Saved", Toast.LENGTH_LONG).show();
                    clean();
                    backToTravelListActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("PushError", e.getMessage());
                }
            });
        } else {
            final StorageReference filePath = storageReference.child("TravelDeal_Images")
                    .child((Objects.requireNonNull(imageURI.getLastPathSegment())));
            filePath.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                    String imageName = task.getResult().getStorage().getPath();
                    Log.d("imageName", imageName);
                    travelDeal.setImageName(imageName);
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                travelDeal.setImageUrl(downloadUrl);
                                travelDeal.setTitle(titleTxt.getText().toString());
                                travelDeal.setDescription(descriptionTxt.getText().toString());
                                travelDeal.setPrice(priceTxt.getText().toString());

                                if (travelDeal.getId() == null) {
                                    Log.d("checkID", travelDeal.getImageUrl() + "\n\n" + travelDeal.getTitle() + "\n\n" + travelDeal.getDescription());
                                    databaseReference.push().setValue(travelDeal).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(NewTravelDealActivity.this, "Deal Saved", Toast.LENGTH_LONG).show();
                                            clean();
                                            backToTravelListActivity();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("PushError", e.getMessage());
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });

        }
    }

    private void deleteDeal() {
        if (travelDeal.getId() == null) {
            Toast.makeText(getApplicationContext(), "Save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        } else {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Are you sure you want to delete this deal?");
            alertDialog.setCancelable(true);
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    databaseReference.child(travelDeal.getId()).removeValue();
                    StorageReference picRef = FirebaseUtil.firebaseStorage.getReference().child(travelDeal.getImageName());
                    picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Delete Image", "Successful deletion");
                            Toast.makeText(NewTravelDealActivity.this, "Deal Deleted!", Toast.LENGTH_SHORT).show();
                            backToTravelListActivity();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Delete Image", e.getMessage());
                        }
                    });
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();

        }

    }

    private void enableEditTexts(boolean state) {
        titleTxt.setEnabled(state);
        descriptionTxt.setEnabled(state);
        priceTxt.setEnabled(state);
        dealImage.setEnabled(state);
    }


}
