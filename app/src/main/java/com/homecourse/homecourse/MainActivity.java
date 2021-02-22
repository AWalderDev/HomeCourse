package com.homecourse.homecourse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 1337;
    List<AuthUI.IdpConfig> providers;
    Button btn_sign_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        btn_sign_out = findViewById(R.id.btn_sign_out);
        if (auth.getCurrentUser() == null) {
            //NEED TO WORK ON THIS TO HAVE PERSISTENT LOGIN/NO LOGIN SCREEN AFTER INITIAL LOGIN
            // User is not logged in
            //init provider
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(false).build()
            );
            showSignInOptions();

        }else{
            btn_sign_out.setEnabled(true);
        }

        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOGOUT
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                btn_sign_out.setEnabled(false);
                                showSignInOptions();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "OH NO" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




    }
    //make so not crash on orientation change
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.test_layout);
    }

    /** Called when the user taps the Bag Management button */
    public void openBM(View view) {
        Intent intent = new Intent(this, BagManagement.class);
        startActivity(intent);
    }

    public void openMM(View view) {
        Intent intent = new Intent(this, MemberActivity.class);
        startActivity(intent);
    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.GreenTheme)
                .build(),MY_REQUEST_CODE
        );
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
                //GET USER
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //SHOW EMAIL ON TOAST
                Toast.makeText(this,""+user.getEmail(), Toast.LENGTH_SHORT).show();
                //SET BUTTON SIGN OUT
                btn_sign_out.setEnabled(true);
            }
            else{
                Toast.makeText(this,""+response.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

}
