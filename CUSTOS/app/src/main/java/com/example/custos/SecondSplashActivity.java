package com.example.custos;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.example.custos.utils.Common;
import com.example.custos.utils.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecondSplashActivity extends AppCompatActivity {
    ImageView imageView;
    TextView name, email, id, homeLocation;
    Button signOut,setHomeButton,backButton;
    GoogleSignInClient googleSignInClient;
    List<Address> addresses=new ArrayList<>();
    Geocoder geocoder;
    SetHomeLocation setHomeLocation = new SetHomeLocation();
    final Handler handler = new Handler();
    //DBHandler db = new DBHandler();
    private DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_splash_activity);
        databaseReference = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);
        progressBar = findViewById(R.id.progress_circular2);
        progressBar.setVisibility(View.INVISIBLE);
        imageView =     findViewById(R.id.imageView);
        name =          findViewById(R.id.textName);
        email =         findViewById(R.id.textEmail);
        id =            findViewById(R.id.textId);
        signOut =       findViewById(R.id.signout_button);
        homeLocation =  findViewById(R.id.homeLocation);
        setHomeButton = findViewById(R.id.setHomeLocation);
        backButton =    findViewById(R.id.back_button2);
        stringAddress(setHomeLocation.getLatitude(),setHomeLocation.getLongtitude());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondSplashActivity.this,MapsActivity.class);
                startActivity(intent);

            }
        });
        signOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                signOut();
            }
        });

        setHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondSplashActivity.this,SetHomeLocationActivity.class);
                startActivity(intent);
            }
        });

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            String personName = account.getDisplayName();
            String personEmail = account.getEmail();
            String personID = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    User user = new User();
                    user.setUID(firebaseUser.getUid());
                    if(dataSnapshot.child(firebaseUser.getUid()).child(Common.USER_ADDRESS)
                            .child(Common.HOME_LOC)
                            .exists()){
                        String fullAddress = dataSnapshot.child(firebaseUser.getUid())
                                .child(Common.USER_ADDRESS)
                                .child(Common.HOME_LOC)
                                .getValue().toString();
                        homeLocation.setText(fullAddress);
                    }else {
                        homeLocation.setText("Home address is not set");
                    }

                 }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            name.setText(personName);
            email.setText(personEmail);
            id.setText(personID);
            Glide.with(this).load(String.valueOf(personPhoto)).into(imageView);
        }
    }
    private void stringAddress(double latitude, double longitude){
        if(!addresses.isEmpty()){
            try {
                addresses = geocoder.getFromLocation(latitude,longitude,1);
                if(addresses.size()>0){
                    String myAddress = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String fullAddress = myAddress + " " + city + " " + state;
                    homeLocation.setText(fullAddress);
                    System.out.println("---------------------------------"+myAddress + city);
                    Log.i("test","address: "+ myAddress + city + state);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private void updateUI(FirebaseUser firebaseUser){
        //signOutButton.setVisibility(View.VISIBLE);
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(googleSignInAccount != null){
            String personName = googleSignInAccount.getDisplayName();
            String personGivenName = googleSignInAccount.getGivenName();
            String personFamilyName = googleSignInAccount.getFamilyName();
            String personEmail = googleSignInAccount.getEmail();
            String personId = googleSignInAccount.getId();
            Uri personPhoto = googleSignInAccount.getPhotoUrl();
            Toast.makeText(SecondSplashActivity.this, "\t"+personName + "\n" + personEmail,Toast.LENGTH_SHORT).show();
        }
        User user = new User();
        if(firebaseUser != null){

            user.setUserEmail(firebaseUser.getEmail());
            user.setUID(firebaseUser.getUid());
        }else{
            user.setUserEmail(null);
            user.setUID(null);
        }
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        googleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.VISIBLE);
                        signOut.setVisibility(View.INVISIBLE);
                        Toast.makeText(SecondSplashActivity.this,"Logging out...", Toast.LENGTH_LONG).show();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateUI(null);
                                Intent intent = new Intent(SecondSplashActivity.this,SplashActivity.class);
                                startActivity(intent);
                            }
                        },2500);

                    }
                });

    }
}
