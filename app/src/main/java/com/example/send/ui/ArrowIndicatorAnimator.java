package com.example.send.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

public abstract class ArrowIndicatorAnimator {

    abstract void onClickArrow();
    final ObjectAnimator scaleDown;
    final ImageView arrow1;

    ArrowIndicatorAnimator(ImageView arrow){
        this.arrow1 = arrow;

        scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                arrow1,
                PropertyValuesHolder.ofFloat("translationX", 15),
                PropertyValuesHolder.ofFloat("alpha", 0.4f));

        scaleDown.setDuration(700);

        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleDown.start();

        arrow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickArrow();
                arrow1.setVisibility(View.GONE);
            }
        });


    }
    void cancel(){
        scaleDown.cancel();
        arrow1.setVisibility(View.GONE);
    }
}
