package com.yildizkabaran.bouncedemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;


public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

  private static final float MIN_TENSION = 2;
  private static final float MAX_TENSION = 40;

  private static final float MIN_DAMPING = 0.01F;
  private static final float MAX_DAMPING = 0.4F;

  private static final float MIN_SCALE = 0F;
  private static final float MAX_SCALE = 3F;

  private View bounceView;
  private SeekBar seekBarScale, seekBarTension, seekBarDamping;
  private TextView tvScale, tvTension, tvDamping;
  private Bouncer bouncer;
  private DecimalFormat decimalFormat = new DecimalFormat("#.###");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    bounceView = findViewById(R.id.bounce_view);
    seekBarScale = (SeekBar) findViewById(R.id.seekbar_target_scale);
    seekBarDamping = (SeekBar) findViewById(R.id.seekbar_damping);
    seekBarTension = (SeekBar) findViewById(R.id.seekbar_tension);
    tvScale = (TextView) findViewById(R.id.tv_target_scale);
    tvDamping = (TextView) findViewById(R.id.tv_damping);
    tvTension = (TextView) findViewById(R.id.tv_tension);

    setupDemo();
  }

  @Override
  protected void onDestroy() {
    bounceView = null;
    seekBarScale = null;
    seekBarTension = null;
    seekBarDamping = null;
    tvScale = null;
    tvTension = null;
    tvDamping = null;
    bouncer = null;
    super.onDestroy();
  }

  private void setupDemo() {
    bouncer = new Bouncer();
    int threshold = getResources().getDimensionPixelSize(R.dimen.click_threshold);
    bouncer.setBoundsThreshold(threshold);

    bounceView.setOnClickListener(this);
    bounceView.setOnTouchListener(bouncer);

    setScaleVal(Bouncer.DEF_TARGET_SCALE);
    setTensionVal(Bouncer.DEF_TENSION);
    setDampingVal(Bouncer.DEF_DAMPING);
    seekBarScale.setOnSeekBarChangeListener(this);
    seekBarTension.setOnSeekBarChangeListener(this);
    seekBarDamping.setOnSeekBarChangeListener(this);
  }

  private void setScaleVal(float val) {
    float percentage = 100F * (val - MIN_SCALE) / (MAX_SCALE - MIN_SCALE);
    seekBarScale.setProgress(Math.round(percentage));
    tvScale.setText(decimalFormat.format(val));
  }

  private void setTensionVal(float val) {
    float percentage = 100F * (val - MIN_TENSION) / (MAX_TENSION - MIN_TENSION);
    seekBarTension.setProgress(Math.round(percentage));
    tvTension.setText(decimalFormat.format(val));
  }

  private void setDampingVal(float val) {
    float percentage = 100F * (val - MIN_DAMPING) / (MAX_DAMPING - MIN_DAMPING);
    seekBarDamping.setProgress(Math.round(percentage));
    tvDamping.setText(decimalFormat.format(val));
  }

  private float getScaleVal() {
    float progressPercentage = seekBarScale.getProgress() / 100F;
    return MIN_SCALE + ((MAX_SCALE - MIN_SCALE) * progressPercentage);
  }

  private float getTensionVal() {
    float progressPercentage = seekBarTension.getProgress() / 100F;
    return MIN_TENSION + ((MAX_TENSION - MIN_TENSION) * progressPercentage);
  }

  private float getDampingVal() {
    float progressPercentage = seekBarDamping.getProgress() / 100F;
    return MIN_DAMPING + ((MAX_DAMPING - MIN_DAMPING) * progressPercentage);
  }

  private void updateScale() {
    float scaleVal = getScaleVal();
    bouncer.setTargetScale(scaleVal);
    tvScale.setText(decimalFormat.format(scaleVal));
  }

  private void updateDamping() {
    float dampingVal = getDampingVal();
    bouncer.setDamping(dampingVal);
    tvDamping.setText(decimalFormat.format(dampingVal));
  }

  private void updateTension() {
    float tensionVal = getTensionVal();
    bouncer.setTension(tensionVal);
    tvTension.setText(decimalFormat.format(tensionVal));
  }

  @Override
  public void onClick(View v) {
    Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    int id = seekBar.getId();
    switch (id) {
      case R.id.seekbar_target_scale:
        updateScale();
        break;
      case R.id.seekbar_damping:
        updateDamping();
        break;
      case R.id.seekbar_tension:
        updateTension();
        break;
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
  }
}
