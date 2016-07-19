package sun2people.electria.pixam;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.hardware.Camera.PictureCallback;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends Activity {

    private static String TAG = "CameraActivity";
    public static final int SCAN_IMAGE_REQUEST = 1;
    public static final String IMAGE_PARAMETERS = "IMAGE_PARAMETERS";
        public static final String EXTRA_USER_ANSWERS = "EXTRA_USER_ANSWERS";

    private Camera mCamera;
    private CameraPreview mPreview;
    private byte[] mImgBytes;
    private Button cancelButton, okButton, captureButton;
    private ImageView imgView;
    private FrameLayout previewLayout;
    private Bitmap mImgBitmap;
    private Bitmap mCroppedImg;
    private ArrayList<Integer> mUserAnswers;
    private ArrayList<Bitmap> mSplitImgs;
    private Handler mHandler;

    private int mCols;
    private int mRows;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mImgBytes = null;
        mHandler = new Handler();
        Intent intent = getIntent();
        String str = intent.getStringExtra(IMAGE_PARAMETERS);
        String[] strArray = str.split("-");
        mCols = Integer.parseInt(strArray[0]);
        mRows = Integer.parseInt(strArray[1]);

        mCamera = getCameraInstance();
        setCameraDisplayOrientation(CameraActivity.this, 1, mCamera);

        imgView = (ImageView) findViewById(R.id.image_preview);
        imgView.setVisibility(View.GONE);

        mPreview = new CameraPreview(this, mCamera);
        previewLayout = (FrameLayout) findViewById(R.id.preview_layout);
        previewLayout.addView(mPreview);

        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setVisibility(View.GONE);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgView.setVisibility(View.GONE);
                previewLayout.setVisibility(View.VISIBLE);
                captureButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                mCamera.startPreview();
            }
        });

        okButton = (Button) findViewById(R.id.button_ok);
        okButton.setVisibility(View.GONE);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                releaseCamera();
                mSplitImgs = splitImage(mCroppedImg, mCols, mRows);
                mUserAnswers = processImages(mSplitImgs);
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra(EXTRA_USER_ANSWERS, mUserAnswers);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mImgBytes = data;
            cropImage();
            cancelButton.setVisibility(View.VISIBLE);
            okButton.setVisibility(View.VISIBLE);
            captureButton.setVisibility(View.GONE);
        }
    };

    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
            showMessage("Problem opening device camera");
            finish();
        }
        return c;
    }

    public static int getRoatationAngle(Activity mContext, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        int result;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private void cropImage(){
        if(mImgBytes != null){

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            Bitmap bm = BitmapFactory.decodeByteArray(mImgBytes, 0, mImgBytes.length);
            Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);

            mImgBitmap = rotate(scaled, getRoatationAngle(CameraActivity.this, 1));

            imgView.setImageBitmap(mImgBitmap);

            Mat src, src_gray;
            Mat detected_edges;
            Mat mHierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();

            int lowThreshold = 20;
            int ratio = 3;
            int idx = 0;
            double maxArea = 0;

            src = new Mat();
            src_gray = src.clone();
            detected_edges = src_gray.clone();
            Utils.bitmapToMat(mImgBitmap, src);
            Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY );
            Imgproc.blur( src_gray, detected_edges, new Size(3,3) );
            Imgproc.Canny( detected_edges, detected_edges, lowThreshold, lowThreshold*ratio);
            Imgproc.findContours(detected_edges, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            src_gray.release();
            detected_edges.release();

            for(MatOfPoint c : contours){
                double area = Imgproc.contourArea(c);
                if(area > maxArea){
                    maxArea = area;
                    idx = contours.indexOf(c);
                }
            }

            if(contours.size() > 0) {
                Rect rect = Imgproc.boundingRect(contours.get(idx));
                Mat ROI = src.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
                mCroppedImg = Bitmap.createBitmap(ROI.cols(), ROI.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(ROI, mCroppedImg);

                ROI.release();

                imgView.setImageBitmap(mCroppedImg);
            }

            imgView.setVisibility(View.VISIBLE);
            previewLayout.setVisibility(View.GONE);
        }
    }

    private ArrayList<Bitmap> splitImage(Bitmap source, int cols, int rows){
        ArrayList<Bitmap> images = new ArrayList<>();

        if(source != null){
            int uWidth = source.getWidth()/cols,
                    uHeight = source.getHeight()/rows;
            for(int i = 0; i < rows; i++){
                for(int j = 0, x = 0, y = (i*uHeight); j < cols; j++, x += uWidth){
                    images.add(Bitmap.createBitmap(source, x, y, uWidth, uHeight));
                }
            }
        }
        else{
            images = null;
        }

        return images;
    }

    private ArrayList<Integer> processImages(ArrayList<Bitmap> images){
        if(images != null){
            long max = 0;
            int maxIndex = -1;
            ArrayList<Integer> result = new ArrayList<>();
            for(int i = 0; i < images.size(); i++){
                Bitmap image = images.get(i);
                int[] pixels = new int[image.getHeight() * image.getWidth()];
                image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
                long value = calcSum(pixels);
                if(value > max){
                    max = value;
                    maxIndex = i;
                }
                if(((i + 1) % mCols) == 0){
                    max = 0;
                    int indx = maxIndex % mCols;
                    Log.w("Scanner", "result: "+ indx);
                    result.add(indx);
                }
            }
            return result;
        }
        return null;
    }

    private long calcSum(int[] source){
        long sum = 0;
        for(int i = 0; i < source.length; i++)
            sum += source[i];

        return Math.abs(sum);
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        camera.setDisplayOrientation(getRoatationAngle(activity, cameraId));
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    private void showMessage(final String msg){
        Runnable showMessage = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        };
        mHandler.post(showMessage);
    }
}

