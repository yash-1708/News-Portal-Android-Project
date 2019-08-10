package te_compa.mcoe_news_portal;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

public class NewsDetails extends AppCompatActivity {
    static newsData news;
    TextView headline;
    EditText articleBody;
    ImageView newsImage;
    ProgressBar progressBar;
    ImageButton approvedbtn;
    ImageButton emailbtn;
    ImageButton deletebtn;
    ImageButton editbtn;
    FirebaseDatabase database1;
    DatabaseReference myRef1;
    FirebaseStorage mFirebaseStorage;
    StorageReference photoRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_details);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        news = (newsData) getIntent().getSerializableExtra("news");
        headline = findViewById(R.id.newsDtitle);
        articleBody = findViewById(R.id.newsDarticle);
        newsImage = findViewById(R.id.newsDimage);
        headline.setText(news.getNewsTitle());
        articleBody.setText(news.getArticle());
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        approvedbtn=findViewById(R.id.approvedButton);
        deletebtn= findViewById(R.id.deleteButton);
        emailbtn= findViewById(R.id.emailButton);
        editbtn= findViewById(R.id.editButton);
        database1 = FirebaseDatabase.getInstance();
        articleBody.setEnabled(false);
        if(news.getApproved())
            approvedbtn.setEnabled(false);
        if(news.getImgurl() != null)
        {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this).load(news.getImgurl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(NewsDetails.this,"Could not Load Image. Please check connection !",Toast.LENGTH_LONG).show();
                    return false;
                }
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(newsImage);
            newsImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(news.getImgurl()),"image/*");
                    startActivity(intent);
                }
            });
        }
        else{
            newsImage.setImageResource(R.drawable.noimage);
        }

        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewsDetails.this);
                builder.setTitle("Caution!");
                builder.setIcon(R.mipmap.delete_news);
                builder.setMessage("   Are you sure you want to delete this news ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                myRef1 = database1.getReference();
                                myRef1.child(MainActivity.type).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot newsDataSnapshot : dataSnapshot.getChildren()) {
                                            newsData news1 = newsDataSnapshot.getValue(newsData.class);
                                            if(news1.getDate().getTime() == news.getDate().getTime()){
                                                if(news.getImgurl()!= null){
                                                    mFirebaseStorage = FirebaseStorage.getInstance();
                                                    photoRef = mFirebaseStorage.getReferenceFromUrl(news.getImgurl());
                                                    photoRef.delete();
                                                }
                                                newsDataSnapshot.getRef().removeValue();
                                                finish();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(NewsDetails.this,"Database Error!",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        emailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                //emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                //emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                //emailIntent.putExtra(Intent.EXTRA_CC, CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, news.getNewsTitle());
                emailIntent.putExtra(Intent.EXTRA_TEXT, news.getArticle()+"\n\n"+news.getImgurl());

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(NewsDetails.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        approvedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef1 = database1.getReference();
                myRef1.child(MainActivity.type).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot newsDataSnapshot : dataSnapshot.getChildren()) {
                            newsData news1 = newsDataSnapshot.getValue(newsData.class);
                            if(news1.getDate().getTime() == news.getDate().getTime()){
                                newsDataSnapshot.child("approved").getRef().setValue(true);
                                finish();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(NewsDetails.this,"Database Error!",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!articleBody.isEnabled())
                    articleBody.setEnabled(true);
                else{
                    myRef1 = database1.getReference();
                    myRef1.child(MainActivity.type).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot newsDataSnapshot : dataSnapshot.getChildren()) {
                                newsData news1 = newsDataSnapshot.getValue(newsData.class);
                                if(news1.getDate().getTime() == news.getDate().getTime()){
                                    newsDataSnapshot.child("article").getRef().setValue(articleBody.getText().toString());
                                    Toast.makeText(NewsDetails.this,"Edit Saved to Database", Toast.LENGTH_SHORT).show();
                                    articleBody.setEnabled(false);

                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(NewsDetails.this,"Database Error!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}