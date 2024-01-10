package com.nitramite.popups;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.PopupWindowCompat;

import com.nitramite.pokerpocket.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RelativePopup extends PopupWindow {

    @IntDef({
            VerticalPosition.CENTER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface VerticalPosition {
        int CENTER = 0;
    }

    @IntDef({
            HorizontalPosition.CENTER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface HorizontalPosition {
        int CENTER = 0;
        int LEFT = 1;
        int RIGHT = 2;
        int ALIGN_LEFT = 3;
        int ALIGN_RIGHT = 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public RelativePopup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RelativePopup() {
        super();
    }


    public void showOnAnchor(@NonNull final View anchor, @VerticalPosition int vertPos, @HorizontalPosition int horizPos, int x, int y, final int number) {
        final View contentView = getContentView();
        CardView cardView = (CardView) contentView.findViewById(R.id.cardView);
        cardView.setOnClickListener(view -> RelativePopup.this.dismiss());
        TextView numberTV = (TextView) contentView.findViewById(R.id.numberTV);
        if (number > 0) {
            numberTV.setText("+" + String.valueOf(number));
        } else {
            numberTV.setText(String.valueOf(number));
        }
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int measuredW = contentView.getMeasuredWidth();
        final int measuredH = contentView.getMeasuredHeight();
        switch (vertPos) {
            case VerticalPosition.CENTER:
                y -= anchor.getHeight() / 2 + measuredH / 6;
                break;
        }
        switch (horizPos) {
            case HorizontalPosition.CENTER:
                x += anchor.getWidth() / 2 - measuredW;
                break;
        }
        PopupWindowCompat.showAsDropDown(this, anchor, x, y, Gravity.NO_GRAVITY);
    }

}
