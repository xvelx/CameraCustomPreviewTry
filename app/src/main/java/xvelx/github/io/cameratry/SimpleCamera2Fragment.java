package xvelx.github.io.cameratry;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SimpleCamera2Fragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private CameraManager mCameraManager;
    private CameraDevice mCamera;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private ImageView mImageView;
    private CustomSurfaceView mRendererView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_camera2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestCameraPermissionIfRequired();

        mImageView = view.findViewById(R.id.imageView);
        mRendererView = view.findViewById(R.id.rendererView);
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        openCamera();
    }

    @SuppressLint("MissingPermission")
    void openCamera() {
        // 0 Rear-- Camera
        try {
            mCameraManager.openCamera("0", new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCamera = camera;
                    openCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void openCaptureSession() {
        try {
            mCamera.createCaptureSession(Arrays.asList(createImageReader().getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCameraCaptureSession = session;
                            openPreviewSession();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openPreviewSession() {
        CaptureRequest.Builder requestBuilder;
        try {
            requestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(mImageReader.getSurface());
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }

        try {
            mCameraCaptureSession.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
            }, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    ImageReader createImageReader() {
        if (mImageReader == null) {
            // size hardcoded
            mImageReader = ImageReader.newInstance(480, 320, ImageFormat.JPEG, 2);
            setImageProcessor();
        }

        return mImageReader;
    }

    private void setImageProcessor() {
        setImageInSurfaceView();
//        setImageInImageView();
    }

//    private void setImageInImageView() {
//        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//            @Override
//            public void onImageAvailable(ImageReader reader) {
//                Image latestImage = reader.acquireLatestImage();
//                System.out.println("New Image available to edit and send it to preview -- ");
//                ByteBuffer buffer = latestImage.getPlanes()[0].getBuffer();
//                byte[] bytes = new byte[buffer.remaining()];
//                buffer.get(bytes);
//                mImageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
//                latestImage.close();
//            }
//        }, new Handler());
//    }

    private void setImageInSurfaceView() {
        HandlerThread updaterThread = new HandlerThread("updaterThread");
        updaterThread.start();
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image latestImage = reader.acquireLatestImage();
                System.out.println("New Image available to edit and send it to preview -- ");
                ByteBuffer buffer = latestImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                latestImage.close();
                mRendererView.setCurrentImage(bitmap);
            }
            // Need to provide a secondary thread handler for heavy process.
        }, new Handler(updaterThread.getLooper()));
    }

    private void requestCameraPermissionIfRequired() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionIfRequired();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
//            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mRendererView.stop();
    }
}
