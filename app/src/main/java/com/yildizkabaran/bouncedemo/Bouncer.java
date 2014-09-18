package com.yildizkabaran.bouncedemo;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yildizkabaran on 10.09.2014.
 * Implementation of View.OnTouchListener. The touched view will change its scale with a bouncing
 * animation. The animation is a fixed step physics animation implemented with an imaginary spring.
 * The target scale of the view, and the tension and damping factors of the spring are adjustable.
 *
 * Since, this class will override the OnTouchListener of the view, some features of the view will
 * stop working. Some of these include: listening to long press events, clicking the sub-views,
 * touching the sub-views. The regular OnClickListener of the view will remain functional, though
 * it will be triggered by this class and therefore might behave differently than other clickable
 * views.
 *
 * Each instance of a Bouncer can only be used with a single view. Attaching the same bouncer to
 * multiple views will cause and exception to be thrown.
 */
public class Bouncer implements View.OnTouchListener {

  /** default parameter values **/
  public static final float DEF_TARGET_SCALE = 0.7F;
  public static final float DEF_DAMPING = 0.15F;
  public static final float DEF_TENSION = 7.5F;
  public static final int DEF_BOUNDS_THRESHOLD = 10;

  /** fixed values **/
  private static final long FIXED_ANIM_DELAY = 20; // physics are updated every 20ms
  private static final float STOP_THRESHOLD = 0.01F; // threshold value used to determine end of animation
  private static final float MASS_COEFFICIENT = 100F; // a dummy value to allow tension to have more realistic values

  /** some variables for easy access **/
  private Rect mBounds; // determine the bounds the view will be active on
  private View mView = null; // hold a reference to the view

  /** adjustable variables **/
  private float targetScale = DEF_TARGET_SCALE; // scales the view up/down to this target, overshoots
  private float friction = 1 - DEF_DAMPING; // friction applied to velocity to damp the motion
  private float springConstant = DEF_TENSION / MASS_COEFFICIENT; // amount the spring force is multiplied with before being applied
  private int boundsThreshold = DEF_BOUNDS_THRESHOLD; // can move touch this much outside of view bounds and still click

  /** variables for keeping the state of the motion **/
  private boolean animateToTarget = true; // a flag for determining the direction of animation
  private float currentScale = 1F; // current scale of the view, assumed to be 1 in the beginning
  private float currentVelocity = 0F; // the current velocity of the animation in arbitrary units
  private boolean isFinished = true; // flag for determining whether or not the animation is going on
  private boolean isTouchActive = false; // flag for determining if the user is currently touching the view

  // animation updater, it is posted to the view to update on UI thread
  // the updater runs assuming that it is called at fixed time intervals
  // this ensures that the animation will look realistic not matter what
  // however the animation may slow down if the device lags
  private Runnable animUpdater = new Runnable(){
    @Override
    public void run() {
      // double check that the view is there
      if(mView == null){
        return;
      }

      // calculate the force according to the animation direction
      float force;
      if (animateToTarget) {
        // if the view is scaling to target, the force center is at the target scale point
        force = targetScale - currentScale;
      } else {
        // if the view is scaling back, the force center is at normal scale point
        force = 1 - currentScale;
      }

      // apply friction to the current velocity
      currentVelocity *= friction;
      // update the velocity with the given force
      currentVelocity += force * springConstant;
      // update the scale with current velocity
      currentScale = currentScale + currentVelocity;

      // check the velocity and current scale against the stopping threshold
      // checking the force instead of the scale works because the force is 0 when the target scale is reached
      if (Math.abs(currentVelocity) < STOP_THRESHOLD && Math.abs(force) < STOP_THRESHOLD) {
        // animation is finished
        isFinished = true;
        // make sure velocity is absolutely 0
        currentVelocity = 0F;
        // make sure scale is not off by even a couple pixels
        // so set it to target or regular scale depending on the direction of motion
        currentScale = animateToTarget ? targetScale : 1;
      }

      // update the view with the new scale values
      mView.setScaleX(currentScale);
      mView.setScaleY(currentScale);

      // if not finished, post the updater to the view with a fixed delay
      if(!isFinished){
        mView.postDelayed(this, FIXED_ANIM_DELAY);
      }
    }
  };

  /***
   * Setter for the scale for the animation to target on pressed state. The animation may overshoot
   * this target depending on the spring constant and damping parameters.
   * @param targetScale a value greater than or equal to 0, although values larger than 2 are not suggested
   */
  public void setTargetScale(float targetScale){
    if(targetScale < 0){
      throw new IllegalArgumentException("target scale must be greater than or equal to 0");
    }
    this.targetScale = targetScale;
  }

  /***
   * Setter for the touch bounds of the view. The down touch must occur inside the view no matter what.
   * The user is allowed to move his finger around after the down touch. The user is also allowed to go
   * outside of the bounds of the original view by this threshold amount. If the user moves outside even
   * more, then the click is canceled and the move is processed as an up event.
   * It is suggested that the threshold pixel amount is calculated from a fixed dip value not set as
   * a fixed number of pixels.
   * @param boundsThreshold a value greater than or equal to 0
   */
  public void setBoundsThreshold(int boundsThreshold){
    if(boundsThreshold < 0){
      throw new IllegalArgumentException("threshold must be greater than or equal to 0");
    }
    this.boundsThreshold = boundsThreshold;
  }

  /***
   * Setter for the animation tension. The view is scaled down and up assuming a system with a spring
   * that is connected on one end to scale of 1, and another to target scale; depending on the
   * direction of motion. The force created by this spring is calculated as F = k * d where F is force,
   * d is the amount of displacement to the target scale, and k is the spring constant determined by
   * the tension here. The units of the spring constant are arbitrary, though higher spring constants
   * will speed up the animation.
   * @param tension any value greater than 0, although values outside of 2 to 40 are not expected to behave well
   */
  public void setTension(float tension){
    if(tension <= 0){
      throw new IllegalArgumentException("tension must be greater than 0");
    }
    // scale the input by MASS_COEFFICIENT to allow for a nicer range for the allowed values
    this.springConstant = tension / MASS_COEFFICIENT;
  }

  /***
   * Setter for the amount of damping to be applied to the animation. The velocity of the animation is
   * updated on every frame as vf = vi * (1 - d) where vf is final velocity, vi is initial velocity,
   * and d is the damping coefficient. The damping coefficient has no units, and increasing the damping
   * will slow down the animation. Using too much damping may cause the animation to never reach completion,
   * and using too little damping will cause the animation to continue bouncing back and forth forever,
   * in some cases causing the animation to keep gaining speed and reaching very large scales.
   * @param damping a value between 0 and 1 inclusive, although suggested values are between 0.01F and 0.4F
   */
  public void setDamping(float damping){
    if(damping < 0 || damping > 1){
      throw new IllegalArgumentException("damping must be between 0 and 1 inclusive");
    }
    // friction is more useful in calculations so convert damping to friction
    friction = 1 - damping;
  }

  /***
   * Implementation of the View.OnTouchListener interface so that the Bouncer can be attached as
   * a touch listener. Because the OnTouchListener of the view is overridden, previous touch, click,
   * and long press handlers will be replaced by Bouncer. The Bouncer will trigger the click handler
   * of the given view, but any touch, or click handlers assigned to the sub views will stop working,
   * and the long press listener of the view will also not work.
   * @param v
   * @param event
   * @return
   */
  @Override
  public boolean onTouch(View v, MotionEvent event) {
    // check the arguments before processing anything else
    if(v == null || event == null){
      throw new IllegalArgumentException("View and MotionEvent parameters cannot be null");
    }

    if(mView == null){
      // this is the first time the view is touched, obtain a reference to the view
      mView = v;
      // get the view bounds, it is assumed that the view is already measured since a down touch event has occurred
      mBounds = new Rect(v.getLeft() - boundsThreshold, v.getTop() - boundsThreshold, v.getRight() + boundsThreshold, v.getBottom() + boundsThreshold);
    }

    // if the bouncer is attached to a second view, then throw an exception, because the Bouncer
    // is not capable of keeping track of the states of two different views at the same time
    if(mView != v){
      throw new IllegalStateException(Bouncer.class.getSimpleName() + " can only be used with one view at a time");
    }

    // get the masked action
    int action = event.getActionMasked();

    if(action == MotionEvent.ACTION_DOWN){
      // flag that touch has started
      isTouchActive = true;
      // notify touch down
      onTouchDown();
      // return true because the event is handled
      return true;
    } else if(isTouchActive && action == MotionEvent.ACTION_MOVE){
      // check bounds
      if(!mBounds.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
        // moved outside the bounds so cancel the touch and notify touch up
        isTouchActive = false;
        onTouchUp();
      }
      // return true because the event is handled
      return true;
    } else if(isTouchActive && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)){
      // the user removed his finger or the touch got interrupted so cancel the touch and notify touch up
      isTouchActive = false;
      onTouchUp();

      // if this was the user lifting his finger up on purpose then click the button
      if(action == MotionEvent.ACTION_UP) {
        v.performClick();
      }
      // return true because the event is handled
      return true;
    }
    // return false because the event is not handled
    return false;
  }


  /***
   * Notify the state that the user started touching the view
   */
  private void onTouchDown(){
    // set animation direction to target
    animateToTarget = true;
    // start the animation if necessary
    startForce();
  }

  /***
   * Notify the state that the user stopped touching the view
   */
  private void onTouchUp(){
    // set animation direction back to normal
    animateToTarget = false;
    // start the animation if necessary
    startForce();
  }

  /***
   * Check if the view is currently animating and start the animation if necessary
   */
  private void startForce(){
    // start the animation if and only if the current animation has ended
    if(isFinished){
      // flag that the animation has not yet ended
      isFinished = false;
      // post to updater to the view
      mView.post(animUpdater);
    }
  }

}
