package com.batzeesappstudio.citytraffic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;

public class Intro extends AppIntro2 {

    public static final String CITYTRAFFICPREFERENCES = "CityTrafficPreference" ;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedpreferences = getSharedPreferences(CITYTRAFFICPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        setImmersiveMode(true);
        setGoBackLock(true);
        setColorTransitionsEnabled(true);

//        addSlide(AppIntroFragment.newInstance(getResources().getText(R.string.gecko_smart_title), getResources().getText(R.string.gecko_smart_description), R.drawable.smart_logo, getResources().getColor(R.color.intro_background_color)));
//        addSlide(AppIntroFragment.newInstance(getResources().getText(R.string.gecko_green_title), getResources().getText(R.string.gecko_green_description), R.drawable.green_logo, getResources().getColor(R.color.intro_background_color)));

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        editor.putBoolean("GECKO_INTRO",true);
        editor.commit();
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        editor.putBoolean("GECKO_INTRO",true);
        editor.commit();
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
