package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import goodmonit.monit.com.kao.constants.Configuration;

/**
 * Created by Jake on 2017-05-08.
 */

public class AnimationManager {
    private static final String TAG = Configuration.BASE_TAG + "AnimationManager";
    private static final boolean DBG = Configuration.DBG;

    private class AnimationFrame{
        private int mResourceId;
        private int mDuration;
        AnimationFrame(int resourceId, int duration){
            mResourceId = resourceId;
            mDuration = duration;
        }
        public int getResourceId() {
            return mResourceId;
        }
        public int getDuration() {
            return mDuration;
        }
    }
    private ArrayList<AnimationFrame> mAnimationFrames; // list for all frames of animation
    private int mIndex; // index of current frame

    private boolean mIsRunning; // true if the animation prevents starting the animation twice
    private SoftReference<ImageView> mSoftReferenceImageView; // Used to prevent holding ImageView when it should be dead.
    private boolean mOneShot;

    private Bitmap mRecycleBitmap;  //Bitmap can recycle by inBitmap is SDK Version >=11

    // Listeners
    private OnAnimationStoppedListener mOnAnimationStoppedListener;
    private OnAnimationFrameChangedListener mOnAnimationFrameChangedListener;

    public AnimationManager(ImageView imageView) {
        init(imageView);
        mRecycleBitmap = null;
    }

    /**
     * initialize imageview and frames
     * @param imageView
     */
    public void init(ImageView imageView){
        if(mIsRunning){
            stop();
        }

        mAnimationFrames = new ArrayList<AnimationFrame>();
        mSoftReferenceImageView = new SoftReference<ImageView>(imageView);
        mOnAnimationFrameChangedListener = null;
        mOnAnimationStoppedListener = null;
        mOneShot = true;
        mIndex = -1;
    }

    private static final int MSG_SHOW_ANIMATION	= 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_ANIMATION:
                    ImageView imageView = mSoftReferenceImageView.get();
                    if (imageView == null) {
                        stop();
                        return;
                    }

                    if (imageView.isShown()) {
                        AnimationFrame frame = getNext();

                        if (frame == null) {
                            if (mOnAnimationStoppedListener != null) {
                                mOnAnimationStoppedListener.onAnimationStopped();
                            }
                            stop();
                            return;
                        }
                        GetImageDrawableTask task = new GetImageDrawableTask(imageView);
                        task.execute(frame.getResourceId());
                        // TODO postDelayed after onPostExecute
                        this.sendEmptyMessageDelayed(MSG_SHOW_ANIMATION, frame.getDuration());
                    }
                    break;
            }
        }

    };

    /**
     * add a frame of animation
     * @param index index of animation
     * @param resId resource id of drawable
     * @param interval milliseconds
     */
    public void addFrame(int index, int resId, int interval){
        mAnimationFrames.add(index, new AnimationFrame(resId, interval));
    }

    /**
     * add a frame of animation
     * @param resId resource id of drawable
     * @param interval milliseconds
     */
    public void addFrame(int resId, int interval){
        mAnimationFrames.add(new AnimationFrame(resId, interval));
    }

    /**
     * add all frames of animation
     * @param resIds resource id of drawable
     * @param interval milliseconds
     */
    public void addAllFrames(int[] resIds, int interval){
        for(int resId : resIds){
            mAnimationFrames.add(new AnimationFrame(resId, interval));
        }
    }

    /**
     * remove a frame with index
     * @param index index of animation
     */
    public void removeFrame(int index){
        mAnimationFrames.remove(index);
    }

    /**
     * clear all frames
     */
    public void removeAllFrames(){
        mAnimationFrames.clear();
    }

    /**
     * change a frame of animation
     * @param index index of animation
     * @param resId resource id of drawable
     * @param interval milliseconds
     */
    public void replaceFrame(int index, int resId, int interval){
        mAnimationFrames.set(index, new AnimationFrame(resId, interval));
    }

    private AnimationFrame getNext() {
        mIndex++;
        if (mIndex >= mAnimationFrames.size()) {
            if (mOneShot) {
                return null;
            } else {
                mIndex = 0;
            }
        }
        return mAnimationFrames.get(mIndex);
    }

    /**
     * Listener of animation to detect stopped
     *
     */
    public interface OnAnimationStoppedListener{
        void onAnimationStopped();
    }

    /**
     * Listener of animation to get index
     *
     */
    public interface OnAnimationFrameChangedListener{
        void onAnimationFrameChanged(int index);
    }


    /**
     * set a listener for OnAnimationStoppedListener
     * @param listener OnAnimationStoppedListener
     */
    public void setOnAnimationStoppedListener(OnAnimationStoppedListener listener){
        mOnAnimationStoppedListener = listener;
    }

    /**
     * set a listener for OnAnimationFrameChangedListener
     * @param listener OnAnimationFrameChangedListener
     */
    public void setOnAnimationFrameChangedListener(OnAnimationFrameChangedListener listener){
        mOnAnimationFrameChangedListener = listener;
    }

    /**
     * Starts the animation
     */
    public synchronized void start() {
        if (DBG) Log.i(TAG, "start");
        mIsRunning = true;
        mHandler.sendEmptyMessage(MSG_SHOW_ANIMATION);
    }

    /**
     * Stops the animation
     */
    public synchronized void stop() {
        if (DBG) Log.i(TAG, "stop");
        mIsRunning = false;
        mIndex = -1;
        mOnAnimationFrameChangedListener = null;
        mOnAnimationStoppedListener = null;
        if (mHandler.hasMessages(MSG_SHOW_ANIMATION)) {
            mHandler.removeMessages(MSG_SHOW_ANIMATION);
        }
    }

    public synchronized void setOneShot(boolean oneShot) {
        mOneShot = oneShot;
    }

    private class GetImageDrawableTask extends AsyncTask<Integer, Void, Drawable> {

        private ImageView mImageView;
        private Context mContext;
        public GetImageDrawableTask(ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mContext = mImageView.getContext();
        }

        @Override
        protected Drawable doInBackground(Integer... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            if (mRecycleBitmap != null) {
                options.inBitmap = mRecycleBitmap;
            }
            mRecycleBitmap = BitmapFactory.decodeResource(mContext.getResources(), params[0], options);
            BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), mRecycleBitmap);
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            if(result!=null) mImageView.setImageDrawable(result);
            if (mOnAnimationFrameChangedListener != null)
                mOnAnimationFrameChangedListener.onAnimationFrameChanged(mIndex);
        }

    }
}