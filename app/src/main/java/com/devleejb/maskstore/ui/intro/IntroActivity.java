package com.devleejb.maskstore.ui.intro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.devleejb.maskstore.R;
import com.devleejb.maskstore.ui.main.MainActivity;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSeparatorColor(Color.BLACK);
        setColorDoneText(Color.BLACK);
        setNextArrowColor(Color.BLACK);
        setIndicatorColor(Color.GRAY, Color.BLACK);

        SliderPage sliderPage1 = new SliderPage();
        SliderPage sliderPage2 = new SliderPage();
        SliderPage sliderPage3 = new SliderPage();

        sliderPage1.setTitle("우리동네 마스크");
        sliderPage1.setDescription("내 위치 주변의 마스크 판매처가 지도 위에 표시됩니다." +
                "\n핀은 색깔별로 재고 상태가 구분됩니다." +
                "\n핀은 지도를 일정 수준 이상 확대하였을 때 표시됩니다.");
        sliderPage1.setImageDrawable(R.drawable.intro_1);
        sliderPage1.setTitleColor(Color.BLACK);
        sliderPage1.setDescColor(Color.BLACK);
        sliderPage1.setBgColor(Color.WHITE);

        sliderPage2.setTitle("우리동네 마스크");
        sliderPage2.setDescription("지도 위의 핀을 클릭하면 판매처 상호명을 볼 수 있습니다.");
        sliderPage2.setImageDrawable(R.drawable.intro_2);
        sliderPage2.setTitleColor(Color.BLACK);
        sliderPage2.setDescColor(Color.BLACK);
        sliderPage2.setBgColor(Color.WHITE);

        sliderPage3.setTitle("우리동네 마스크");
        sliderPage3.setDescription("상호명을 클릭하면 상세한 정보를 볼 수 있습니다.");
        sliderPage3.setImageDrawable(R.drawable.intro_3);
        sliderPage3.setTitleColor(Color.BLACK);
        sliderPage3.setDescColor(Color.BLACK);
        sliderPage3.setBgColor(Color.WHITE);

        addSlide(AppIntroFragment.newInstance(sliderPage1));
        addSlide(AppIntroFragment.newInstance(sliderPage2));
        addSlide(AppIntroFragment.newInstance(sliderPage3));

        showSkipButton(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        finish();
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
    }
}
