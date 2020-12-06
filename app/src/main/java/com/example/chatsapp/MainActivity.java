package com.example.chatsapp;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.HashBiMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DatabaseReference reference;
    private FirebaseUser user;
    public static boolean onSearchIcon = false;
    private AppBarLayout appBarLayout;
    private  AppBarLayout.LayoutParams paramsTool,paramsTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout2);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Chatsapp");

        //setFlagonTab
        paramsTab = (AppBarLayout.LayoutParams) tabLayout.getLayoutParams();
        paramsTool = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        user = FirebaseAuth.getInstance().getCurrentUser();


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatFragment(), "Chats");
        viewPagerAdapter.addFragment(new UsersFragment(), "Users");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        final MenuItem search = menu.findItem(R.id.ic_search);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    search.setVisible(true);
                    UsersFragment.searchUsers.setVisibility(View.GONE);
                } else {
                    search.setVisible(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.ic_profile:
                Intent intent1 = new Intent(MainActivity.this, UserInformation.class);
                intent1.putExtra("FROM_MAIN", 0);
                startActivity(intent1);
                return true;
            case R.id.ic_search:
                boolean enabled = false;
                Toast.makeText(MainActivity.this, "Search", Toast.LENGTH_SHORT).show();
                paramsTab.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);
                appBarLayout.setExpanded(false,true);
                UsersFragment.searchUsers.setVisibility(View.VISIBLE);
                onSearchIcon = true;
                return true;
            default:
                return false;

        }
    }

    @Override
    public void onBackPressed() {

        if (onSearchIcon) {
           appBarLayout.setExpanded(true,true);
           paramsTab.setScrollFlags(0);
            UsersFragment.searchUsers.setVisibility(View.GONE);
            onSearchIcon = false;
        } else {
            super.onBackPressed();
        }
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        reference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
