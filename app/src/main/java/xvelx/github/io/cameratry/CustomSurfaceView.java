package xvelx.github.io.cameratry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private Bitmap mCurrentImage;
    private Paint paint = new Paint();
    private boolean isHolderAvailable;
    private boolean running;

    public CustomSurfaceView(Context context) {
        super(context);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        running = true;
        new Thread(this).start();
    }

    void setCurrentImage(Bitmap currentImage) {
        mCurrentImage = currentImage;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isHolderAvailable = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHolderAvailable = false;
    }

    @Override
    public void run() {
        while (running) {
            synchronized (this) {
                if (mCurrentImage != null && isHolderAvailable) {
                    SurfaceHolder surfaceHolder = getHolder();
                    Canvas canvas = surfaceHolder.lockCanvas();
                    canvas.drawBitmap(mCurrentImage, 0, 0, paint);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void stop() {
        running = false;
    }
}
