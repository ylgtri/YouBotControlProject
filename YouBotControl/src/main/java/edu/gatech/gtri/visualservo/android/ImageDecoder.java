package edu.gatech.gtri.visualservo.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Choreographer;

import java.util.Arrays;
import java.util.Random;

import static edu.gatech.gtri.visualservo.android.MainFragment.pool;
import static edu.gatech.gtri.visualservo.android.MainFragment.queue;

public class ImageDecoder implements Runnable {

    // this decodes the file we got

    private static int timeLength = 13; // Maybe this value shouldn't be hardcoded...
    private byte[] tmp;
    private Long time;

    public ImageDecoder(byte[] tmp, Long time) {
        this.tmp = tmp;
        this.time = time;
    }

    @Override
    public void run() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferQualityOverSpeed = false; // gotta go fast?
            options.inDither = false; // there's no point dithering if you're receiving RGBA_8888
            options.inMutable = true; // for use in inBitmap
            options.inSampleSize = 1; // sometimes android scales the image, so let's avoid this
            // really cuts down decoding time, now stuttering is purely network
            // needs to be pooled, so we're using the 1st 20 frames as a pool (don't ask me why)
            if (pool.size() >= 20) {
                Random rand = new Random(); // It's not economical to use an iterator, so why not
                int min = 1;
                int max = 19;
                int randomNum = rand.nextInt(max - min + 1) + min;
                while (randomNum == MainFragment.currentIndex) { // closest thing we have to "intelligent load distribution" (Heroku, anyone)?
                    randomNum = rand.nextInt(max - min + 1) + min;
                }
                MainFragment.currentIndex = randomNum; // lock this pool, we don't need multiple locks for speed
                Bitmap other = pool.get(randomNum);
                if (other != null) {
                    // Bitmaps shouldn't be null, but hey, it happens somehow
                    options.inBitmap = other; // sets inBitmap as the one we got from the pool
                    // Log.d("POOL", "Size of pool " + pool.size() + " got " + randomNum);
                }
            }
            tmp = Arrays.copyOfRange(tmp, timeLength, tmp.length); // removes the timestamp header
            Bitmap bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length, options);
            if (pool.size() <= 25) { // each 1280x800 image takes about 6MB in RAM, so we'd like to avoid this
                pool.add(bitmap); // hopefully this won't kill the heap
            }
            Log.d("RECEIVEDFROMSOCKET", "bitmap " + bitmap.getWidth() + " x " + bitmap.getHeight());
            queue.put(bitmap); // push the bitmap to the bottom of the rendering queue (frame skipping is handled by choreographer, phew
            MainFragment.choreographer.postFrameCallback(new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long l) {
                    MainFragment.mImageView.invalidate(); // for some reason, this makes it faster
                    MainFragment.mImageView.setImageBitmap(MainFragment.queue.poll()); // displays the image from the top of the pool
                    // according to StackOverflow, canvas.drawBitmap() isn't hardware accelerated, and timings confirm this
                    // ImageViews are hardware accelerated, and in addition, we don't need to handle the stretching (BitmapFactory)
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
