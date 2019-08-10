package te_compa.mcoe_news_portal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Submissions extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    ImageButton imgbutton;
    private Uri filePath;
    private Toolbar toolbar;
    ImageView imageView;
    FirebaseStorage storage;
    StorageReference storageReference;
    private final int PICK_IMAGE_REQUEST = 50;
    static String imageUrl=null;
    static EditText name;
    static TextInputEditText article;
    static Spinner types;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_submissions);
        toolbar = (Toolbar) findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        //toolbar.setTitle("News");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
                finish();
            }
        });
        database = FirebaseDatabase.getInstance();
        //database.setPersistenceEnabled(false);
        types = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(Submissions.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.newsTypes));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        types.setAdapter(myAdapter);
        name = findViewById(R.id.name);
        article = findViewById(R.id.article);
        final Button subButton = findViewById(R.id.submitButton);
        imgbutton=findViewById(R.id.imageButton);
        imageView=findViewById(R.id.newsimage);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        imgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOnline()){
                    Toast.makeText(Submissions.this,"You are offline",Toast.LENGTH_SHORT).show();
                    return;
                }
                if( name.getText().toString().length() == 0 ){
                    name.setError( "News Title is required!" );
                    return;
                }

                if( article.getText().toString().length() == 0 ){
                    article.setError( "News Article is required!" );
                    return;
                }
                subButton.setEnabled(false);
                if(filePath!=null)
                    uploadImage();
                else
                    writeToDb(name.getText().toString(), article.getText().toString(),types.getSelectedItem().toString() ,false);


            }
        });


    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
        public void writeToDb(String name, String article,String type, boolean approved){
            Date d = Calendar.getInstance().getTime();
            myRef = database.getReference().child(type);
            newsData newnews=new newsData(name,article,approved,imageUrl);
                    myRef.push().setValue(newnews).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            imageUrl = null;
                            finish();
                        }
                    });
        }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            imageUrl = taskSnapshot.getDownloadUrl().toString();
                            Toast.makeText(Submissions.this, "Uploaded Sucessfully", Toast.LENGTH_SHORT).show();
                            writeToDb(name.getText().toString(), article.getText().toString(),types.getSelectedItem().toString() ,true);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Submissions.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}
