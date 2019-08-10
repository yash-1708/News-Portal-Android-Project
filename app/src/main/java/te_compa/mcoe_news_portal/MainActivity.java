package te_compa.mcoe_news_portal;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    ListView mNewsListView;
    TextView noNews;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private NewsAdapter newsAdapter;
     ArrayList<newsData> newslist;
    private ProgressBar mProgressBar;
    public static String type = "General Notice";

    public MainActivity() {
        newslist = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        FirebaseApp.initializeApp(this);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOnline()) {
                    Toast.makeText(MainActivity.this,"You Are Offine!",Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(MainActivity.this,Submissions.class);
                    startActivity(i);
                }
            }
        });

        mNewsListView = findViewById(R.id.newslistview);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mProgressBar = findViewById(R.id.progressBar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mProgressBar.setVisibility(ProgressBar.GONE);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.notices);
        navigationView.getMenu().performIdentifierAction(R.id.notices, 0);
        noNews=findViewById(R.id.nonewstextview);
        noNews.setVisibility(TextView.GONE);
        mNewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mProgressBar.getVisibility()== ProgressBar.GONE){
                    newsData news=newslist.get(position);
                    Intent i=new Intent(MainActivity.this,NewsDetails.class);
                    i.putExtra("news", news);
                    startActivity(i);
                }
            }
        });
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.addAdmin) {
            Intent i = new Intent(MainActivity.this,AdminLogin.class);
            i.putExtra("newuser",true);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i = new Intent(this,DevInfo.class);
        newslist = new ArrayList<>();
        if (id == R.id.notices) {
            newslist.clear();
            type = getString(R.string.General);
            mNewsListView.setAdapter(newsAdapter);
            toolbar.setTitle(R.string.General);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            myRef.child("General Notice").addValueEventListener(readFromDatabase());
        } else if (id == R.id.sports) {
            toolbar.setTitle(R.string.Sports);
            newslist.clear();
            type = getString(R.string.Sports);
            mNewsListView.setAdapter(newsAdapter);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            myRef.child("Sports News").addValueEventListener(readFromDatabase());
        } else if (id == R.id.cultural) {
            toolbar.setTitle(R.string.Cultural);
            newslist.clear();
            type = getString(R.string.Cultural);
            mNewsListView.setAdapter(newsAdapter);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            myRef.child("Cultural Programs").addValueEventListener(readFromDatabase());
        }
        else if (id == R.id.technical) {
            toolbar.setTitle(R.string.Technical);
            newslist.clear();
            type = getString(R.string.Technical);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            myRef.child("Technical Programs").addValueEventListener(readFromDatabase());
        } else if (id == R.id.achievements) {
            toolbar.setTitle(R.string.Academic);
            newslist.clear();
            type = getString(R.string.Academic);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            myRef.child("Academic Achievements").addValueEventListener(readFromDatabase());
        } else if (id == R.id.AboutUs) {
            startActivity(i);
        } else if (id == R.id.nav_share) {
            Intent shareintent = new Intent(android.content.Intent.ACTION_SEND);
            shareintent.setType("text/plain");
            String shareBodyText = "https://drive.google.com/open?id=1-v_jIXs-Zq2rEektOejakcpijoQ7IoSd";
            shareintent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Mcoe News Portal Admin App");
            shareintent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
            startActivity(Intent.createChooser(shareintent, "Choose sharing method"));
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private ValueEventListener readFromDatabase() {
        return new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newslist.clear();
                for (DataSnapshot newsDataSnapshot : dataSnapshot.getChildren()) {
                    newsData news = newsDataSnapshot.getValue(newsData.class);
                    newslist.add(news);
                }
                if(newslist.isEmpty())
                    noNews.setVisibility(TextView.VISIBLE);
                else
                    noNews.setVisibility(TextView.GONE);
                Collections.reverse(newslist);
                mProgressBar.setVisibility(ProgressBar.GONE);
                newsAdapter = new NewsAdapter(MainActivity.this, R.layout.item_news, newslist);
                mNewsListView.setAdapter(newsAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,"Database Error!",Toast.LENGTH_SHORT).show();
            }
        };

    }
}
