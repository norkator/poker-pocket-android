package com.nitramite.dynamic;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

// This class contains seat object
@SuppressWarnings("WeakerAccess")
public class Seat {

    // Declare seat related components
    private FrameLayout seatFrame;
    public CardView seatCard; // This means cardView for glow effect
    public ImageView seatCard0, seatCard1, seatCard2, seatCard3, seatCard4, seatCard5;
    public TextView seatNameTV, seatMoneyTV;
    public ProgressBar seatTimeBar;
    public FrameLayout seatBetFrame;
    public ImageView seatChipsIV;
    public TextView seatTotalBetTV;
    public TextView seatWinsCountTV;
    public TextView seatLastUserActionTV;
    public TextView seatCardsNumberTV;
    public ImageView seatDealerChipIV;
    public Float collectAnimToX;
    public Float collectAnimToY;

    // Constructor
    public Seat(FrameLayout seatFrame_, CardView seatCard_, ImageView seatCard0_, ImageView seatCard1_, ImageView seatCard2_, ImageView seatCard3_, ImageView seatCard4_, ImageView seatCard5_,
                TextView seatNameTV_, TextView seatMoneyTV_, ProgressBar seatTimeBar_, FrameLayout seatBetFrame_, ImageView seatChipsIV_, TextView seatTotalBetTV_, TextView seatWinsCountTV_,
                TextView seatLastUserActionTV_, TextView seatCardsNumberTV_, ImageView seatDealerChipIV_, final Float collectAnimToX_, final Float collectAnimToY_) {
        this.seatCard0 = seatCard0_;
        this.seatCard1 = seatCard1_;
        this.seatCard2 = seatCard2_;
        this.seatCard3 = seatCard3_;
        this.seatCard4 = seatCard4_;
        this.seatCard5 = seatCard5_;
        this.seatNameTV = seatNameTV_;
        this.seatMoneyTV = seatMoneyTV_;
        this.seatTimeBar = seatTimeBar_;
        this.seatCard = seatCard_;
        this.seatFrame = seatFrame_;
        this.seatBetFrame = seatBetFrame_;
        this.seatChipsIV = seatChipsIV_;
        seatTotalBetTV = seatTotalBetTV_;
        seatWinsCountTV = seatWinsCountTV_;
        this.seatLastUserActionTV = seatLastUserActionTV_;
        this.seatCardsNumberTV = seatCardsNumberTV_;
        this.seatDealerChipIV = seatDealerChipIV_;
        this.collectAnimToX = collectAnimToX_;
        this.collectAnimToY = collectAnimToY_;

        // Run function
        this.setDefaults();
    }

    // Set's default view states
    private void setDefaults() {
        if (this.seatFrame != null) {
            this.seatFrame.setVisibility(View.GONE); // Gone for default
            if (this.seatBetFrame != null) {
                this.seatBetFrame.setVisibility(View.GONE);
            }
            if (this.seatWinsCountTV != null) {
                this.seatWinsCountTV.setText("");
            }
            if (this.seatDealerChipIV != null) {
                seatDealerChipIV.setVisibility(View.GONE);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers

    // Activate's seat
    public void activateSeat() {
        this.seatFrame.setVisibility(View.VISIBLE);
    }

    // Clear's seat
    public void clearSeat() {
        this.seatFrame.setVisibility(View.INVISIBLE);
    }

    // ---------------------------------------------------------------------------------------------

} 