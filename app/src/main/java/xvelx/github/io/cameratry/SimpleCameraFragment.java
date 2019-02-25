package xvelx.github.io.cameratry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SimpleCameraFragment extends Fragment {
    //
    private SurfaceView mTexture;
    private Camera camera;
    private CustomSurfaceView mSampleImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTexture = view.findViewById(R.id.texture);
        mSampleImageView = view.findViewById(R.id.sampleImageView);

        camera = Camera.open();
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                System.out.println("Capturing .. ");

                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                YuvImage yuvImage = new YuvImage(data, camera.getParameters().getPreviewFormat()
                        , previewSize.width, previewSize.height, null);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height),
                        100, byteArrayOutputStream);

                byte[] imageData = byteArrayOutputStream.toByteArray();
                Bitmap imageToDraw = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                mSampleImageView.setCurrentImage(imageToDraw);
            }
        });

        mTexture.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Camera.Parameters parameters = camera.getParameters();
                for (Camera.Size supportedPreviewSize : parameters.getSupportedPreviewSizes()) {
                    if (supportedPreviewSize.width < width && supportedPreviewSize.height < height) {
                        parameters.setPreviewSize(supportedPreviewSize.width, supportedPreviewSize.height);
                        break;
                    }
                }
//                camera.setParameters(parameters);
//                parameters.setPreviewFormat(ImageFormat.JPEG);
                camera.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

//    private SurfaceView preview = null;
//    private SurfaceHolder previewHolder = null;
//    private Camera camera = null;
//    private boolean inPreview = false;
//    private boolean cameraConfigured = false;
//
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_simple_camera, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        preview = view.findViewById(R.id.texture);
//        previewHolder = preview.getHolder();
//        previewHolder.addCallback(surfaceCallback);
////        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        camera = Camera.open();
//        startPreview();
//    }
//
//    @Override
//    public void onPause() {
//        if (inPreview) {
//            camera.stopPreview();
//        }
//
//        camera.release();
//        camera = null;
//        inPreview = false;
//
//        super.onPause();
//    }
//
//    private Camera.Size getBestPreviewSize(int width, int height,
//                                           Camera.Parameters parameters) {
//        Camera.Size result = null;
//
//        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
//            if (size.width <= width && size.height <= height) {
//                if (result == null) {
//                    result = size;
//                } else {
//                    int resultArea = result.width * result.height;
//                    int newArea = size.width * size.height;
//
//                    if (newArea > resultArea) {
//                        result = size;
//                    }
//                }
//            }
//        }
//
//        return (result);
//    }
//
//    private void initPreview(int width, int height) {
//        if (camera != null && previewHolder.getSurface() != null) {
//            try {
//                camera.setPreviewDisplay(previewHolder);
//            } catch (Throwable t) {
//                Toast
//                        .makeText(SimpleCameraFragment.this.getContext(), t.getMessage(), Toast.LENGTH_LONG)
//                        .show();
//            }
//
//            if (!cameraConfigured) {
//                Camera.Parameters parameters = camera.getParameters();
//                Camera.Size size = getBestPreviewSize(width, height,
//                        parameters);
//
//                if (size != null) {
//                    parameters.setPreviewSize(size.width, size.height);
//                    camera.setParameters(parameters);
//                    cameraConfigured = true;
//                }
//            }
//        }
//    }
//
//    private void startPreview() {
//        if (cameraConfigured && camera != null) {
//            camera.startPreview();
//            inPreview = true;
//        }
//    }
//
//    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
//        public void surfaceCreated(SurfaceHolder holder) {
//            // no-op -- wait until surfaceChanged()
//        }
//
//        public void surfaceChanged(SurfaceHolder holder,
//                                   int format, int width,
//                                   int height) {
//            initPreview(width, height);
//            startPreview();
//        }
//
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            // no-op
//        }
//    };
}
