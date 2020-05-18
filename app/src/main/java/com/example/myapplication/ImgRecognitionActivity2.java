package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.myapplication.Adapter.PokemonAdapter;
import com.example.myapplication.model.Pokemon;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource;
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource;
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ImgRecognitionActivity2 extends AppCompatActivity {

    private static final String TAG = "ImgRecognitionActivity2";
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private ByteBuffer imgData = ByteBuffer.allocateDirect(4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
    private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    private ImageView mfinalImageView, mbtnCamera, mbtnNew, mbtnFront;
    private FrameLayout mfinalImageFrame;
    private BottomSheetBehavior sheetBehavior;
    private CameraView mcameraView;

    private static final int GALLERY_INTENT = 3;
    private static final int CAMERA_REQUEST_CODE = 4;

    private Uri mResultUri= null;
    private boolean mImgClick = false;
    private boolean frontCamera = false;


    private ArrayList<String> labellist = new ArrayList<String>();
    private ArrayList<String> probabilitylist = new ArrayList<String>();
    private String mCurrentPhotoPath;
    private Bitmap mImageBitmap;

    private SharedPreferences prefs;

    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSION_CAMERA = 2;

    private FirebaseModelInterpreter firebaseInterpreter;
    private FirebaseModelInputOutputOptions inputOutputOptions;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_recognition2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setTitle("Camera");

        RelativeLayout mbottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);
        mbtnCamera = (ImageView) mbottomLayout.findViewById(R.id.btnCamera);
        mbtnNew = (ImageView) mbottomLayout.findViewById(R.id.btnNew);
        mbtnFront = (ImageView) mbottomLayout.findViewById(R.id.btnFront);
        this.imgData.order(ByteOrder.nativeOrder());

        mfinalImageView = (ImageView) findViewById(R.id.finalImageView);
        mfinalImageFrame = (FrameLayout) findViewById(R.id.finalImageFrame);
        mcameraView = (CameraView) findViewById(R.id.cameraView);
        mcameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        mcameraView.setLifecycleOwner(this);

        mbtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mbtnCamera.setAlpha(0.5f);
                mbtnCamera.setClickable(false);
                mcameraView.capturePicture();
            }
        });

        mbtnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mbtnCamera.setAlpha(1.0f);
                mbtnCamera.setClickable(true);
                mfinalImageFrame.setVisibility(View.GONE);
            }
        });

        mbtnFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (frontCamera) {
                    frontCamera = false;
                    mcameraView.setFacing(Facing.BACK);
                }
                else {
                    frontCamera = true;
                    mcameraView.setFacing(Facing.FRONT);
                }

            }
        });

        mcameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                // Create a bitmap or a file...
                // CameraUtils will read EXIF orientation for you, in a worker thread.
                Log.e(TAG, "onPictureTaken");
                convertByteArrayToBitmap(picture);
            }
        });


        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder().requireWifi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        // Build a FirebaseCloudModelSource object by specifying the name you assigned the model
        // when you uploaded it in the Firebase console.
        FirebaseCloudModelSource cloudSource = new FirebaseCloudModelSource.Builder("optimized_graph")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource);


        FirebaseLocalModelSource localSource =
                new FirebaseLocalModelSource.Builder("optimized_graph")  // Assign a name for this model
                        .setAssetFilePath("optimized_graph.tflite")
                        .build();
        FirebaseModelManager.getInstance().registerLocalModelSource(localSource);


        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setCloudModelName("optimized_graph")
                .setLocalModelName("optimized_graph")
                .build();

        try {
            firebaseInterpreter = FirebaseModelInterpreter.getInstance(options);
            inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                            .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        mcameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mcameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mcameraView.destroy();
    }

    private void convertByteArrayToBitmap(final byte[] byteArray) {
        Log.e(TAG, "convertByteArrayToBitmap = " );

        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1000);

        mfinalImageFrame.setVisibility(View.VISIBLE);
        mfinalImageView.setImageBitmap(resizedBitmap);
        mResultUri = getImageUri(ImgRecognitionActivity2.this,resizedBitmap);
        new CameraOperation().execute(byteArray);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private class CameraOperation extends AsyncTask<byte[], Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(byte[]... params) {

            byte[] byteArray = params[0];
            ExifInterface exifInterface = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    exifInterface = new ExifInterface((InputStream)(new ByteArrayInputStream(byteArray)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            int orientation = exifInterface.getAttributeInt(exifInterface.TAG_ORIENTATION, 1);
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            //to fix images coming out to be rotated
            //https://github.com/google/cameraview/issues/22#issuecomment-269321811
            Matrix m = new Matrix();
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_180:
                    m.postRotate(180.0F);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    m.postRotate(90.0F);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    m.postRotate(270.0F);
                    break;
                default:
                    break;
            }

            int cropX = (int)((double)bitmap.getWidth() * 0.2D);
            int cropY = (int)((double)bitmap.getHeight() * 0.25D);

            Bitmap currentBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, bitmap.getWidth() - 2 * cropX, bitmap.getHeight() - 2 * cropY, m, true);
            //free up the original bitmap
            bitmap.recycle();

            //create a scaled bitmap for Tensorflow
            final Bitmap scaledBitmap = Bitmap.createScaledBitmap(currentBitmap, 224, 224, false);
            return scaledBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap scaledBitmap) {
            try {

                startProcess(scaledBitmap);
            } catch (FirebaseMLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Picture", null);
        return Uri.parse(path);
    }

    private final ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        this.imgData.rewind();
        if (bitmap != null) {
            bitmap.getPixels(this.intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        }

        int pixel = 0;
        int var3 = 0;

        for(int var4 = DIM_IMG_SIZE_X; var3 < var4; ++var3) {
            int var5 = 0;

            for(int var6 = DIM_IMG_SIZE_Y; var5 < var6; ++var5) {
                int currPixel = this.intValues[pixel++];
                this.imgData.putFloat((float)((currPixel >> 16 & 255) - IMAGE_MEAN) / IMAGE_STD);
                this.imgData.putFloat((float)((currPixel >> 8 & 255) - IMAGE_MEAN) / IMAGE_STD);
                this.imgData.putFloat((float)((currPixel & 255) - IMAGE_MEAN) / IMAGE_STD);
            }
        }

        Log.e(TAG, "this.imgData = " + this.imgData);

        imgData.order(ByteOrder.nativeOrder());

        return this.imgData;
    }


    private void startProcess(Bitmap scaledBitmap) throws FirebaseMLException {

        if (scaledBitmap != null) {
            Log.e(TAG, "startProcess = " );

            FirebaseModelInputs inputs = null;
            try {
                inputs = new FirebaseModelInputs.Builder()
                        .add(convertBitmapToByteBuffer(scaledBitmap))  // add() as many input arrays as your model requires
                        .build();
            } catch (FirebaseMLException e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                Log.e(TAG, "firebaseInterpreter = " + firebaseInterpreter);

                ProgressDialog mProgress = new ProgressDialog(ImgRecognitionActivity2.this);
                mProgress.setMessage("Capturing..");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.setCancelable(false);
                mProgress.show();

                firebaseInterpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {

                                        float[][] output = result.getOutput(0);
                                        float[] probabilities = output[0];

                                        BufferedReader reader = null;

                                        try {
                                            reader = new BufferedReader(
                                                    new InputStreamReader(getAssets().open("output_labels.txt")));
                                        } catch (IOException e) {
                                            mProgress.dismiss();
                                            e.printStackTrace();
                                        }

                                        Log.e("MLKit", "probabilities.length =" + probabilities.length);

                                        for (int i = 0; i < probabilities.length; i++) {
                                            String label = null;
                                            try {
                                                label = reader.readLine();
                                                Log.e("MLKit", "label = " + label);
                                            } catch (IOException e) {
                                                mProgress.dismiss();
                                                e.printStackTrace();
                                            }

                                            if (probabilities[i] > 0.0) {
                                                int intprobability = Math.round(probabilities[i]*100);
                                                probabilitylist.add(Integer.toString(intprobability));
                                                labellist.add(label);
                                                Log.e("MLKit", String.format("%s: %1.4f", label, probabilities[i]));
                                            }

                                            mProgress.dismiss();

                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        mProgress.dismiss();
                                    }
                                });
            }

        }

    }

    

    private void checkCameraPermission() {
        //Check USER Camera Permission
        Log.e(TAG, "checkCameraPermission");
        if(ContextCompat.checkSelfPermission(ImgRecognitionActivity2.this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            Log.e(TAG, "checkCameraPermission 1");
            if(ActivityCompat.shouldShowRequestPermissionRationale(ImgRecognitionActivity2.this,
                    android.Manifest.permission.CAMERA)){

                Log.e(TAG, "checkCameraPermission 2");

                ActivityCompat.requestPermissions(ImgRecognitionActivity2.this,
                        new String []{android.Manifest.permission.CAMERA},MY_PERMISSION_CAMERA);
            }
            else{

                Log.e(TAG, "checkCameraPermission 3");
                ActivityCompat.requestPermissions(ImgRecognitionActivity2.this,
                        new String []{android.Manifest.permission.CAMERA},MY_PERMISSION_CAMERA);
            }
        }
        else {

            Log.e(TAG, "checkCameraPermission 4");
            checkStoragePermission();
        }
    }

    private void checkStoragePermission() {
        //Check USER Read External Storage Permission
        if(ContextCompat.checkSelfPermission(ImgRecognitionActivity2.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(ImgRecognitionActivity2.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(ImgRecognitionActivity2.this,
                        new String []{android.Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_READ_EXTERNAL_STORAGE);
            }
            else{
                ActivityCompat.requestPermissions(ImgRecognitionActivity2.this,
                        new String []{android.Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        }
        else {
            openCamera();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        Log.e(TAG, "image 4" + image);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        Log.e(TAG,"requestCode = " + requestCode);
        Log.e(TAG,"MY_PERMISSION_READ_EXTERNAL_STORAGE = " + MY_PERMISSION_READ_EXTERNAL_STORAGE);
        Log.e(TAG,"MY_PERMISSION_CAMERA = " + MY_PERMISSION_CAMERA);
        switch (requestCode){

            case MY_PERMISSION_CAMERA:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(ImgRecognitionActivity2.this,
                            android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        Log.e(TAG,"openCamera");
                        checkStoragePermission();
                    }
                    else{
                        Toast.makeText(ImgRecognitionActivity2.this, "No Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case MY_PERMISSION_READ_EXTERNAL_STORAGE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(ImgRecognitionActivity2.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        openCamera();
                    }
                    else{
                        Toast.makeText(ImgRecognitionActivity2.this, "No Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void openStorage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/* ");
        startActivityForResult(galleryIntent,GALLERY_INTENT );
    }


    private void openCamera() {

        Log.e(TAG, "openCamera 2");

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            Log.e(TAG, "openCamera 3");
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "IOException" + ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.e(TAG, "openCamera 4" + photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
            else {
                Log.e(TAG, "openCamera 5");
            }
        }
        else {
            Log.e(TAG, "openCamera 6");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            Uri imageuri = data.getData();

            //path = getPathFromURI(data.getData());

            CropImage.activity(imageuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri imageuri = Uri.parse(mCurrentPhotoPath);

                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                mImageBitmap = Bitmap.createScaledBitmap(mImageBitmap, 224, 224, false);

                CropImage.activity(imageuri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setFixAspectRatio(true)
                        .setAspectRatio(1, 1)
                        .start(this);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mImgClick = true;
                mResultUri = result.getUri();
                mfinalImageView.setVisibility(View.VISIBLE);
                mfinalImageView.setImageURI(mResultUri);
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();

            }
            else if (resultCode == RESULT_CANCELED){
                mImgClick= false;
                mResultUri = null;
            }
            else{
                mImgClick= false;
                mResultUri = null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.camera_menu:
                if (probabilitylist.size() != 0 && labellist.size() != 0 && mResultUri != null) {
                    Intent intent = new Intent();
                    intent.putExtra("probabilitylist", probabilitylist);
                    intent.putExtra("labellist", labellist);
                    intent.putExtra("imageurl", mResultUri.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
