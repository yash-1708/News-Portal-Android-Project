package te_compa.mcoe_news_portal;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.FirebaseMessaging;

public class AdminLogin extends AppCompatActivity {
Button signIn;
Button signOut;
EditText usernameText;
EditText passwordText;
FirebaseDatabase loginDatabase;
DatabaseReference loginRef;
private SignInButton signinbtn;
GoogleSignInClient mGoogleSignInClient;
boolean addingAdmin;
private int RC_SIGN_IN = 1;
private ProgressBar signinProgressBar;
private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        FirebaseApp.initializeApp(this);
        if (!isOnline()){
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminLogin.this);
            builder.setTitle("You Are Offline");
            builder.setIcon(R.mipmap.wifioff);
            builder.setMessage("Do you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("Open Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        signinProgressBar = findViewById(R.id.progressBar3);
        signinProgressBar.setVisibility(View.GONE);
        signOut= findViewById(R.id.signOut);
        signinbtn = findViewById(R.id.google_button);
        addingAdmin=getIntent().getBooleanExtra("newuser",false);
        signOut = findViewById(R.id.signOut);
        signIn = findViewById(R.id.signinBtn);
        mAuth = FirebaseAuth.getInstance();
        usernameText = findViewById(R.id.username);
        passwordText = findViewById(R.id.password);
        loginDatabase = FirebaseDatabase.getInstance();
        loginRef = loginDatabase.getReference().child("Users");
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(acct == null) {
            signOut.setVisibility(View.GONE);
            signIn.setVisibility(View.GONE);
        }
        else{
            signinbtn.setVisibility(View.GONE);
        }
        if(addingAdmin){
            signIn.setText("Add Admin");
        }
        /////gmail auth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signinbtn.setVisibility(View.GONE);
                signIn();
                signinProgressBar.setVisibility(View.VISIBLE);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                signOut.setVisibility(View.GONE);
                signIn.setVisibility((View.GONE));
                signinbtn.setVisibility(View.VISIBLE);
                mGoogleSignInClient.revokeAccess();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signinProgressBar.setVisibility(View.VISIBLE);
                if( usernameText.getText().toString().length() == 0 ){
                    usernameText.setError( "Username is required!" );
                    signinProgressBar.setVisibility(View.GONE);
                    return;
                }
                if( passwordText.getText().toString().length() == 0 ){
                    passwordText.setError( "Password is required!" );
                    signinProgressBar.setVisibility(View.GONE);
                    return;
                }
                if(addingAdmin)
                {
                    writeToDb();
                }
                loginRef.addListenerForSingleValueEvent(readFromDatabase());
                readFromDatabase();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("gmailFail", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("firebasewithgoogle", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(AdminLogin.this,"Authentication failed",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        signinProgressBar.setVisibility(View.GONE);
        signOut.setVisibility(View.VISIBLE);
        signIn.setVisibility(View.VISIBLE);

        FirebaseMessaging.getInstance().subscribeToTopic("admin");
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();

            Toast.makeText(AdminLogin.this,"User : "+personName,Toast.LENGTH_SHORT).show();
            if(!personEmail.endsWith("gmail.com")){
                signOut.setVisibility(View.GONE);
                signIn.setVisibility(View.GONE);
                signinbtn.setVisibility(View.VISIBLE);
                Toast.makeText(AdminLogin.this,"Sign in with gmail email address...",Toast.LENGTH_SHORT).show();
                mGoogleSignInClient.revokeAccess();
            }
        }
    }

    protected boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private ValueEventListener readFromDatabase() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isAdmin = false;
                for (DataSnapshot newsDataSnapshot : dataSnapshot.getChildren()) {
                    UserToken user = newsDataSnapshot.getValue(UserToken.class);
                    if(user.getUsername().equals(usernameText.getText().toString().trim())&&user.getPassword().equals(passwordText.getText().toString().trim())) {
                        isAdmin = true;
                        Intent i = new Intent(AdminLogin.this,MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
                if(!isAdmin){
                    Toast.makeText(AdminLogin.this,"Unauthorised User, Please Contact Admin...",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminLogin.this,"Please Sign in with your College Email First...",Toast.LENGTH_LONG).show();
            }
        };

    }
    public void writeToDb(){
        UserToken newUser =new UserToken(usernameText.getText().toString().trim(),passwordText.getText().toString().trim());
        loginRef.push().setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(AdminLogin.this,"User created...",Toast.LENGTH_SHORT).show();
                finish();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdminLogin.this,"User not created...",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
