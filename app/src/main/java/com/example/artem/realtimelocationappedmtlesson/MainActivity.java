package com.example.artem.realtimelocationappedmtlesson;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.artem.realtimelocationappedmtlesson.Models.User;
import com.example.artem.realtimelocationappedmtlesson.Utils.Common;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 7117;

    DatabaseReference userInfoRef;
    List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Paper.init(this);

//        Init firebase
        userInfoRef = FirebaseDatabase.getInstance().getReference(Common.USER_INFO);

//        Init providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

//        Request permission location
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        showSignInOptions();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You must accept permission to use app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){
                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//              Check if user existson Database
                userInfoRef.orderByKey()
                        .equalTo(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null){ //If user is exists
                                    if (!dataSnapshot.child(firebaseUser.getUid()).exists()){ //If key uid is not exists
                                        Common.loggedUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
//                                      Add to database
                                        userInfoRef.child(Common.loggedUser.getUid()).setValue(Common.loggedUser);

                                    }

                                }else { //If user is available
                                    Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                                }

//                              Save UID to storage to update location from background
                                Paper.book().write(Common.USER_UID_SAVE_KEY, Common.loggedUser.getUid());
                                updateToken(firebaseUser);
                                setupUI();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }
    }



    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(), REQUEST_CODE
        );
    }

    private void updateToken(final FirebaseUser firebaseUser) {
        final DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS);

//      Get Token
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                tokens.child(firebaseUser.getUid()).setValue(instanceIdResult.getToken());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUI() {
        //Navigate home
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }
}
