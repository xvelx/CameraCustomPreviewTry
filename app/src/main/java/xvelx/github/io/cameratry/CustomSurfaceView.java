package xvelx.github.io.cameratry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView {

    private Bitmap mCurrentImage;
    private Paint paint = new Paint();

    public CustomSurfaceView(Context context) {
        super(context);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCurrentImage != null) {
            canvas.drawBitmap(mCurrentImage, 0, 0, paint);
        }
    }

    void setCurrentImage(Bitmap currentImage) {
        mCurrentImage = currentImage;
        invalidate();
    }

    public Bitmap getmCurrentImage() {
        return mCurrentImage;
    }
}
