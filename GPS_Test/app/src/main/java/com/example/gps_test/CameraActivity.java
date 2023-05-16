package com.example.gps_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kotlin.jvm.internal.PropertyReference0Impl;

public class CameraActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int CAMERA_NORMAL_REQUEST_CODE = 103;
    // public static final int GALLERY_REQUEST_CODE = 105;

    Button go_menu,camera,createPDF;
    ImageView selectedImage;

    String currentPhotoPath;

    String tituloText = "Titulo del documento PDF";

    Bitmap image_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // ------------------------------ Define variables -----------------------------------------

        go_menu = findViewById(R.id.goMenu);
        camera = findViewById(R.id.cameraBtn);
        createPDF = findViewById(R.id.createPDF);

        selectedImage = findViewById(R.id.imageCamera);



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            askPermission();
        } else {
            Toast.makeText(getApplicationContext(),"simona",Toast.LENGTH_SHORT).show();
        }

        // ----------------------------------- Listeners --------------------------------------------
            go_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CameraActivity.this, MenuActivity.class));
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                normalPhoto();
            }
        });

        createPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generarPDF(image_1,"test_test_test");
            }
        });

    }

    // ---------------------------------------- Methods --------------------------------------------


    private void askPermission() {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERM_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission of camera, write and read granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Camera,Read and write Permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK){
                File file = new File(currentPhotoPath);
                selectedImage.setImageURI(Uri.fromFile(file));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contetnUri = Uri.fromFile(file);
                mediaScanIntent.setData(contetnUri);
                this.sendBroadcast(mediaScanIntent);
            }
        }
        if (requestCode == CAMERA_NORMAL_REQUEST_CODE){
            assert data != null;
            image_1 = (Bitmap) data.getExtras().get("data");
            selectedImage.setImageBitmap(image_1);
        }
        /* if (requestCode == GALLERY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                assert data != null;
                Uri contentUri = data.getData();
                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                selectedImage.setImageURI(contentUri);
            }
        } */
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(c.getType(contentUri));
    }

    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);  -- not able to see in phone gallery

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName, // prefix
            ".jpg",        // suffix
            storageDir     // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return  image;
    }

    @SuppressLint("QueryPermissionsNeeded")
    private  void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // CHECK IF THERE IS CAMERA TO HANDLE INTENT
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            //File where phot should go
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex){
                // ERROR WHILE CREATING FILE
            }

            // IF FILE IS SUCCESFULLY CREATED
            if (photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.file-provider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private  void normalPhoto(){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera,CAMERA_NORMAL_REQUEST_CODE);
    }

    public  void generarPDF(Bitmap bitmap, String descripcionText){
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        TextPaint titulo = new TextPaint();
        TextPaint descripcion = new TextPaint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(816,1054,1).create();
        PdfDocument.Page pagina1 = pdfDocument.startPage(pageInfo);

        Canvas canvas = pagina1.getCanvas();

        Bitmap bitmapScale;
        bitmapScale = Bitmap.createScaledBitmap(bitmap,200,200,false);
        canvas.drawBitmap(bitmapScale,300,40,paint);

        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titulo.setTextSize(20);
        canvas.drawText(tituloText,10,150,titulo);

        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas.drawText(descripcionText,10,200,descripcion);

        pdfDocument.finishPage(pagina1);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Test.pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(getApplicationContext(),"Se creo piola",Toast.LENGTH_LONG).show();
        } catch (Exception e){
            Toast.makeText(getApplicationContext(),"mamo",Toast.LENGTH_LONG).show();
        }

        pdfDocument.close();
    }

}
