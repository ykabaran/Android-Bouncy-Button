# Bouncy Button for Android
by Yildiz Kabaran

Implementation of View.OnTouchListener. The touched view will change its scale with a bouncing animation. The animation is a fixed step physics animation implemented with an imaginary spring. The target scale of the view, and the tension and damping factors of the spring are adjustable.
Since, this class will override the OnTouchListener of the view, some features of the view will stop working. Some of these include: listening to long press events, clicking the sub-views, touching the sub-views. The regular OnClickListener of the view will remain functional, though it will be triggered by this class and therefore might behave differently than other clickable views.
Each instance of a Bouncer can only be used with a single view. Attaching the same bouncer to multiple views will cause and exception to be thrown.

## Installation

Simply copy the Bounce.java file into your project.

## Usage

You can create in code:
```
View myView = findViewById(R.id.my_view); // or basically any view

Bouncer bouncer = new Bouncer();
bouncer.setTargetScale(0.7F); // this is the default target scale
bouncer.setTension(7.5F); // this is the default tension
bouncer.setDamping(0.15F); // this is the default damping
// it's best to get a touch threshold from a dimen value
// or just leave it at the default of 10px
int threshold = getResources().getDimensionPixelSize(R.dimen.click_threshold);
bouncer.setBoundsThreshold(threshold); // set the threshold

myView.setOnClickListener(...); // you can use a click listener with this
```

Please feel free to ask for any fixes/customizations/additions to this project.

## Notes

- The view has only been tested on HTC One running Android 4.4.3, and therefore needs to be tested on devices with different versions and screen resolutions.

## Copyright and License

Feel free to use the code in any way you wish.
