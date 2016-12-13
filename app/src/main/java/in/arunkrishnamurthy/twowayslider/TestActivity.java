package in.arunkrishnamurthy.twowayslider;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity implements TwowaySliderView.OnTwowaySliderListener{

    private Toast toast;
    private TwowaySliderView sliderView, sliderView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        sliderView = (TwowaySliderView) findViewById(R.id.sliderControl);
        sliderView2 = (TwowaySliderView) findViewById(R.id.sliderControlCostomized);
        sliderView.setListener(this);
        sliderView2.setListener(this);
    }

    private void showToast(String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onSliderLongPress() {
        showToast("The slider facing a long press.");
    }

    @Override
    public void onSliderMoveLeft() {
        showToast("The slider is moved left.");
    }

    @Override
    public void onSliderMoveRight() {
        showToast("The slider is moved right.");
    }
}
