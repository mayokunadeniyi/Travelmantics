package com.example.travelmantics.Utils;

import com.example.travelmantics.Model.TravelDeal;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FirebaseUtil {

    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    public static StorageReference storageReference;
    private static FirebaseUtil firebaseUtil;
    public static ArrayList<TravelDeal> travelDealArrayList;

    private FirebaseUtil(){}

    public static void openFbReference(String reference){
        if (firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            firebaseDatabase = FirebaseDatabase.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
            travelDealArrayList = new ArrayList<>();
        }
        databaseReference = firebaseDatabase.getReference().child(reference);
    }

}
