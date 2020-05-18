package com.example.myapplication;

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
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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


public class ImgRecognitionActivity extends AppCompatActivity {

    private static final String TAG = "ImgRecognitionActivity";
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private ByteBuffer imgData = ByteBuffer.allocateDirect(4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
    private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    private EditText mNameField, mEmailField, mPasswordField;
    private Button mRegisterBtn;
    private ImageButton maboutaddBtn;
    private ImageView mprofileBtn;
    private ImageButton mbackBtn;
    private TextView mtextView4;

    private BottomSheetBehavior sheetBehavior;
    private CameraView mcameraView;
    private RecyclerView mrvLabel;
    private Toolbar mToolbar;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private PokemonAdapter recyclerAdapter;

    private static final int GALLERY_INTENT = 3;
    private static final int CAMERA_REQUEST_CODE = 4;

    private Uri mResultUri= null;

    private String mCurrentPhotoPath;
    private Bitmap mImageBitmap;

    private boolean mImgClick = false;

    private SharedPreferences prefs;
    private List<Pokemon> pokemonlist;

    // variable to track event time
    private long mLastClickTime = 0;

    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSION_CAMERA = 2;

    private FirebaseModelInterpreter firebaseInterpreter;
    private FirebaseModelInputOutputOptions inputOutputOptions;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_recognition);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setTitle("Camera");

        LinearLayout mbottomLayout = (LinearLayout)findViewById(R.id.bottomLayout);
        mProgressBar = (ProgressBar) mbottomLayout.findViewById(R.id.progressBar);
        mrvLabel = (RecyclerView) mbottomLayout.findViewById(R.id.rvLabel);
        mrvLabel.setHasFixedSize(false);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(false);

        pokemonlist = new ArrayList<Pokemon>();
        recyclerAdapter = new PokemonAdapter(pokemonlist,ImgRecognitionActivity.this);

        mrvLabel.setLayoutManager(mLayoutManager);
        mrvLabel.setAdapter(recyclerAdapter);

        // init the bottom sheet behavior
        sheetBehavior = BottomSheetBehavior.from(mbottomLayout);
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        /*mToolbar = (Toolbar)findViewById(R.id.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(" ");
        mToolbar.setSubtitle(" ");*/

        this.imgData.order(ByteOrder.nativeOrder());


        prefs = getSharedPreferences("saved", Context.MODE_PRIVATE);
        if(prefs.contains("TARGET_INTRO")){
            showTarget();
        }

        mcameraView = (CameraView) findViewById(R.id.cameraView);
        ImageView mcameraImg = (ImageView) findViewById(R.id.cameraFrame);
        mcameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        mcameraView.setLifecycleOwner(this);
        mcameraView.setFacing(Facing.BACK);
        mcameraImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e(TAG, "mcameraView Pressed");

                mProgressBar.setVisibility(View.VISIBLE);
                pokemonlist.clear();
                mcameraView.capturePicture();
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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

        /*maboutaddBtn = (ImageButton) findViewById(R.id.aboutaddBtn);
        mtextView4 = (TextView) findViewById(R.id.textView4);
        mprofileBtn =(ImageView) findViewById(R.id.profile_image);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                try {
                    startProcess();
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }
            }
        });

        maboutaddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });*/

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

    private void showTarget() {
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.cameraFrame), getString(R.string.capture), getString(R.string.capture_message))
                        // All options below are optional
                        .outerCircleColor(R.color.colorPrimary)
                        .outerCircleAlpha(0.95f)
                        .targetCircleColor(R.color.colorAccent)
                        .titleTextSize(24)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(16)
                        .descriptionTextColor(R.color.white)
                        .textTypeface(Typeface.DEFAULT)
                        .drawShadow(true)
                        .cancelable(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .targetRadius(180),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        prefs.edit().putBoolean("TARGET_INTRO", true).apply();
                    }

                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        super.onTargetCancel(view);      // This call is optional
                        prefs.edit().putBoolean("TARGET_INTRO", true).apply();
                    }
                });
    }

    private void convertByteArrayToBitmap(final byte[] byteArray) {
        Log.e(TAG, "convertByteArrayToBitmap = " );
        new CameraOperation().execute(byteArray);
    }

    private class CameraOperation extends AsyncTask<byte[], Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(byte[]... params) {

            byte[] byteArray = params[0];
            ExifInterface exifInterface = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
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
            mProgressBar.setVisibility(View.GONE);
            /*int batchNum = 0;
            float[][][][] input = new float[1][224][224][3];
            for (int x = 0; x < 224; x++) {
                for (int y = 0; y < 224; y++) {
                    int pixel = mImageBitmap.getPixel(x, y);
                    // Normalize channel values to [0.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [-1.0, 1.0] instead.
                    input[batchNum][x][y][0] = Color.red(pixel) / 255.0f;
                    input[batchNum][x][y][1] = Color.green(pixel) / 255.0f;
                    input[batchNum][x][y][2] = Color.blue(pixel) / 255.0f;
                }
            }*/

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

                firebaseInterpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {

                                        float[][] output = result.getOutput(0);
                                        float[] probabilities = output[0];

                                        BufferedReader reader = null;
                                        ArrayList<String> labellist = new ArrayList<String>();
                                        ArrayList<Float> mylist = new ArrayList<Float>();

                                        try {
                                            reader = new BufferedReader(
                                                    new InputStreamReader(getAssets().open("output_labels.txt")));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        Log.e("MLKit", "probabilities.length =" + probabilities.length);

                                        for (int i = 0; i < probabilities.length; i++) {
                                            String label = null;
                                            try {
                                                label = reader.readLine();
                                                Log.e("MLKit", "label = " + label);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            if (probabilities[i] > 0.2) {
                                               // mylist.add(probabilities[i]);
                                               // labellist.add(label);
                                                Log.e("MLKit", String.format("%s: %1.4f", label, probabilities[i]));
                                                Pokemon pokemons = new Pokemon();
                                                pokemons.setName(label);
                                                pokemons.setAccuracy(probabilities[i]);
                                                pokemonlist.add(pokemons);
                                            }
                                        }

                                        recyclerAdapter.notifyDataSetChanged();

                                        /*Float maxVal = Collections.max(mylist);
                                        int index = mylist.indexOf(maxVal);
                                        String outputval = labellist.get(index);

                                        mtextView4.setText("IT IS " + outputval);
                                        mtextView4.setVisibility(View.VISIBLE);*/
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
            }

        }

    }

    

    private void checkCameraPermission() {
        //Check USER Camera Permission
        Log.e(TAG, "checkCameraPermission");
        if(ContextCompat.checkSelfPermission(ImgRecognitionActivity.this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            Log.e(TAG, "checkCameraPermission 1");
            if(ActivityCompat.shouldShowRequestPermissionRationale(ImgRecognitionActivity.this,
                    android.Manifest.permission.CAMERA)){

                Log.e(TAG, "checkCameraPermission 2");

                ActivityCompat.requestPermissions(ImgRecognitionActivity.this,
                        new String []{android.Manifest.permission.CAMERA},MY_PERMISSION_CAMERA);
            }
            else{

                Log.e(TAG, "checkCameraPermission 3");
                ActivityCompat.requestPermissions(ImgRecognitionActivity.this,
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
        if(ContextCompat.checkSelfPermission(ImgRecognitionActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(ImgRecognitionActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(ImgRecognitionActivity.this,
                        new String []{android.Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_READ_EXTERNAL_STORAGE);
            }
            else{
                ActivityCompat.requestPermissions(ImgRecognitionActivity.this,
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
                    if(ContextCompat.checkSelfPermission(ImgRecognitionActivity.this,
                            android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        Log.e(TAG,"openCamera");
                        checkStoragePermission();
                    }
                    else{
                        Toast.makeText(ImgRecognitionActivity.this, "No Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case MY_PERMISSION_READ_EXTERNAL_STORAGE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(ImgRecognitionActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        openCamera();
                    }
                    else{
                        Toast.makeText(ImgRecognitionActivity.this, "No Permission Granted", Toast.LENGTH_SHORT).show();
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
                mprofileBtn.setImageURI(mResultUri);
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

}
