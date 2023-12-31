package com.tranxit.stripepayment.activity.add_card;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.tranxit.stripepayment.R;

public class StripeAddCardActivity extends AppCompatActivity {

    private static final String TAG = "StripePaymentActivity";
    private CardForm cardForm;
    private Button submitButton;

    private String stripe_token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        cardForm = findViewById(R.id.card_form);
        stripe_token = getIntent().getStringExtra("stripe_token");

        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(false)
                .mobileNumberRequired(false)
                .actionLabel(getString(R.string.add_card_details))
                .setup(this);
        submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(v -> onSubmit());
    }

    public void onSubmit() {
        setSubmitEnabled(false);
        if (cardForm.getCardNumber().isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_card_number),
                    Toast.LENGTH_SHORT).show();
            setSubmitEnabled(true);
            return;
        }
        if (cardForm.getExpirationMonth().isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_card_expiration_details),
                    Toast.LENGTH_SHORT).show();
            setSubmitEnabled(true);
            return;
        }
        if (cardForm.getCvv().isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_card_cvv), Toast.LENGTH_SHORT).show();
            setSubmitEnabled(true);
            return;
        }
        if (!TextUtils.isDigitsOnly(cardForm.getExpirationMonth()) || !TextUtils.isDigitsOnly(cardForm.getExpirationYear())) {
            Toast.makeText(this, getString(R.string.please_enter_card_expiration_details),
                    Toast.LENGTH_SHORT).show();
            setSubmitEnabled(true);
            return;
        }

        String cardNumber = cardForm.getCardNumber();
        int cardMonth = Integer.parseInt(cardForm.getExpirationMonth());
        int cardYear = Integer.parseInt(cardForm.getExpirationYear());
        String cardCvv = cardForm.getCvv();
        Log.d("CARD",
                "CardDetails Number: " + cardNumber + "Month: " + cardMonth + " Year: " + cardYear + " Cvv " + cardCvv);
        Card card = new Card(cardNumber, cardMonth, cardYear, cardCvv);
        addCard(card);

    }

    private void addCard(Card card) {
        Stripe stripe = new Stripe(this, stripe_token);
        stripe.createToken(card, new TokenCallback() {
                    public void onSuccess(Token token) {
                        setSubmitEnabled(false);
                        Log.d("CARD:", " " + token.getId());
                        Log.d("CARD:", " " + token.getCard().getLast4());
                        String stripeToken = token.getId();

                        Intent intent = new Intent();
                        intent.putExtra("stripetoken", stripeToken);
                        setResult(100, intent);
                        finish();
                    }

                    public void onError(Exception error) {
                        setSubmitEnabled(true);
                        Toast.makeText(getApplicationContext(), error.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Set the enabled state of this button.
     *
     * @param enabled True if this view is enabled, false otherwise.
     */
    private void setSubmitEnabled(boolean enabled) {
        submitButton.setEnabled(enabled);
    }
}
