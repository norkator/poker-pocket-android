package com.nitramite.pokerpocket;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainMenu extends AppCompatActivity {

    // Logging
    private final static String TAG = MainMenu.class.getSimpleName();

    // Components
    private TabLayout tabLayout;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private SharedPreferences sharedPreferences;
    private Fragment holdemFragment = null;

    // View pager icons
    private int[] tabIcons = {
            R.mipmap.diamonds_icon,     // Texas Holdem
            R.mipmap.spades_icon,       // Blackjack
            R.mipmap.hearts_icon,       // 5-Card Draw
            R.mipmap.clubs_icon,        // Seven of clubs
            R.mipmap.diamonds_icon,     // Square solitaire
            R.mipmap.spades_icon,       // Other game modes
            R.mipmap.hearts_icon,       // Slot Machine
            R.mipmap.clubs_icon,        // About
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        // Find components
        ImageView nitramiteLinkBtn = (ImageView) findViewById(R.id.nitramiteLinkBtn);
        Button settingsBtn = (Button) findViewById(R.id.settingsBtn);
        Button feedbackBtn = (Button) findViewById(R.id.feedbackBtn);


        nitramiteLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainMenu.this, CardTest.class));
            }
        });

        settingsBtn.setOnClickListener(view -> {
            audioPlayer.playCardSlideOne(MainMenu.this);
            startActivity(new Intent(MainMenu.this, Preferences.class));
        });

        feedbackBtn.setOnClickListener(view -> {
            String url = "https://play.google.com/store/apps/details?id=com.nitramite.pokerpocket";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        // Shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (!sharedPreferences.getBoolean(Constants.SP_README_COMPLETED, false)) {
            readMeDialog();
        }
        if (sharedPreferences.getBoolean(Constants.SP_SHOW_DEVELOPMENT_FEATURES, false)) {
        }

    } // End of onCreate();


    // Tab menu icons
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
        tabLayout.getTabAt(4).setIcon(tabIcons[4]);
        tabLayout.getTabAt(5).setIcon(tabIcons[5]);
        tabLayout.getTabAt(6).setIcon(tabIcons[6]);
        // tabLayout.getTabAt(7).setIcon(tabIcons[7]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        holdemFragment = new FragmentHoldem();
        adapter.addFragment(holdemFragment, "Hold'em");
        adapter.addFragment(new FragmentBlackJack(), "Blackjack");
        adapter.addFragment(new FragmentFiveCardDraw(), "5-Card Draw");
        adapter.addFragment(new FragmentSlotMachine(), "Slot's");
        adapter.addFragment(new FragmentSquareSolitaire(), "Solitaire");
        adapter.addFragment(new FragmentOthers(), "Others");
        // adapter.addFragment(new FragmentSevens(), "Seven's");
        adapter.addFragment(new FragmentAbout(), "About");
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                }
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (holdemFragment != null) {
            FragmentHoldem fragment = (FragmentHoldem) getSupportFragmentManager().findFragmentByTag(holdemFragment.getTag());
            try {
                fragment.onlineButtonChecks();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    public void openNitramiteLink() {
        String url = "http://www.nitramite.com/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void readMeDialog() {
        new AlertDialog.Builder(MainMenu.this)
                .setTitle("Readme")
                .setMessage("Poker Pocket is old project which is currently no longer developed further. The project's goal was to become a super simple and \"just play without messing around\" poker game. \n\n" +
                        "Poker Pocket will never include content like half naked women or other content similar to that. It also wont pressure you to spent real money on coins whatsoever.\n\n" +
                        "There was option to get more funds by watching ads but that is replaced by just press of a button. Ads are currently removed.\n\n" +
                        "Links to external resources that I used (for the table and cards) can be found in the \"about\" section.\n\n" +
                        "Poker Pocket also has a web app for online Hold'em. You are able to log in there with the same account and play. The web app is ad free.\n\n")
                .setNegativeButton("Understand", (dialog, which) -> {
                    SharedPreferences setSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainMenu.this.getBaseContext());
                    SharedPreferences.Editor editor = setSharedPreferences.edit();
                    editor.putBoolean(Constants.SP_README_COMPLETED, true);
                    editor.apply();
                    // Return
                })
                .setIcon(R.mipmap.logo)
                .show();
    }

} 