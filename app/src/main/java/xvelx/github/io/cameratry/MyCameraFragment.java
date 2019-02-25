package xvelx.github.io.cameratry;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MyCameraFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private AutoFitTextureView mTextureView;

    TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private ImageReader mImageReader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView = view.findViewById(R.id.texture);
        view.findViewById(R.id.picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
    }

    private void captureImage() {
        try {
            CaptureRequest.Builder captureRequest =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequest.addTarget(imageCapturer().getSurface());
            mCameraCaptureSession.capture(captureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    System.out.println("Capture completed");
                }
            }, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

//        findCamera();


        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera("0", new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    System.out.println("Camera Opened");
                    mCameraDevice = camera;

                    createCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    System.out.println("Camera Disconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    System.out.println("On Error " + error + " -- ");
                }
            }, new Handler());
        } catch (CameraAccessException e) {
            System.out.println("Exception while open camera");
            e.printStackTrace();
        }
    }

    private void createCaptureSession() {
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(mTextureView.getSurface(),
                    imageCapturer().getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            System.out.println("Capture session configured");
                            mCameraCaptureSession = session;
                            openPreviewMode(session);
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            System.out.println("Capture session failed");
                        }
                    }, new Handler());
        } catch (CameraAccessException e) {
            System.out.println("Exception while capture session");
            e.printStackTrace();
        }
    }

    private ImageReader imageCapturer() {
        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(480, 320, ImageFormat.YUV_420_888, 2);
            HandlerThread imageCapturerHandler = new HandlerThread("boom");
            imageCapturerHandler.start();
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    saveImage();
                    System.out.println("On Image Available -- ");
                }
            }, new Handler(imageCapturerHandler.getLooper()));
        }
        return mImageReader;
    }

    private void saveImage() {
        Image latestImage = mImageReader.acquireLatestImage();
        ByteBuffer buffer = latestImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        try {
            FileOutputStream outputStream = new FileOutputStream(new File(
                    "storage/self/primary/Pictures/my.jpg"));
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        latestImage.close();
    }

    private void openPreviewMode(@NonNull CameraCaptureSession session) {
        try {
            HandlerThread surfaceHandlerThread = new HandlerThread("surfaceHandler");
            surfaceHandlerThread.start();

            CaptureRequest.Builder previewRequestBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            previewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE);
//            previewRequestBuilder.addTarget(mTextureView.getSurface());
            previewRequestBuilder.addTarget(imageCapturer().getSurface());

            session.setRepeatingRequest(previewRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    System.out.println("On caputre Completed -- preview " + result.getFrameNumber() + " -- " + result.getSequenceId());
//                    for (CaptureResult.Key<?> key : result.getKeys()) {
//                        System.out.println("capture key " + key);
//                    }
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                    System.out.println("on capture progressed -- preview");
                }

                @Override
                public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                                       int sequenceId, long frameNumber) {
                    super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                    System.out.println("on capture sequence completed -- preview");
                }
            }, new Handler(surfaceHandlerThread.getLooper()));


//            HandlerThread imageHandlerThread = new HandlerThread("image-handler");
//            imageHandlerThread.start();
//            CaptureRequest.Builder previewRequestBuilder2 = mCameraDevice
//                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            previewRequestBuilder2.addTarget(imageCapturer().getSurface());
//            session.setRepeatingRequest(previewRequestBuilder2.build(), new CameraCaptureSession.CaptureCallback() {
//            }, new Handler(imageHandlerThread.getLooper()));
        } catch (CameraAccessException e) {
            System.out.println("Failed to open preview mode " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int findCamera() {
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager != null) {
                for (String cameraId : cameraManager.getCameraIdList()) {
//                    CameraCharacteristics characteristics = cameraManager
//                            .getCameraCharacteristics(cameraId);
//                    for (String physicalCameraId : characteristics.getPhysicalCameraIds()) {
//                        System.out.println("Physical Camera Ids " + physicalCameraId);
//                    }
                }
            }
        } catch (CameraAccessException ex) {
            System.out.println("Exception while accessing camera " + ex.getMessage());
        }
        return -1;
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
//            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }
    }
}
