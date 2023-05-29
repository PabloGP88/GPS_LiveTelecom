package com.example.gps_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int CAMERA_RADIO_REQUEST_CODE = 103;
    public static final int CAMERA_MASTIL_REQUEST_CODE = 104;
    public static final int CAMERA_DOBLE_MASTIL_REQUEST_CODE = 105;
    public static final int CAMERA_BASE_REQUEST_CODE = 106;
    public static final int CAMERA_ROUTER_REQUEST_CODE = 107;
    // public static final int GALLERY_REQUEST_CODE = 105;

    Button cameraRadio,cameraMastil,cameraDoble_mastil,cameraBase,cameraRouter,createPDF;
    Switch switch_mastil,switch_base;
    EditText descriptionMastil,descriptionBase;
    ImageView imageRadio,imageMastil,imageDoble_mastil,imageBase,imageRouter;

    String currentPhotoPath;

    Boolean isDobleMastil;
    Boolean isBase;

    Bitmap image_1,image_2,image_3,image_4,image_5;

    // ----------------------------- FireBase --------------------------------------------------

    StorageReference storageReference;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // ------------------------------ Define variables -----------------------------------------

        cameraRadio = findViewById(R.id.cameraBtn_radio);
        cameraMastil = findViewById(R.id.cameraBtn_mastil);
        cameraDoble_mastil = findViewById(R.id.cameraBtn_doble_mastil);
        cameraBase = findViewById(R.id.cameraBtn_base);
        cameraRouter = findViewById(R.id.cameraBtn_router);

        descriptionMastil = findViewById(R.id.descriptionMastil);
        descriptionBase = findViewById(R.id.descriptionBase);

        switch_mastil = findViewById(R.id.switch_doble_mastil);
        switch_base = findViewById(R.id.switch_base);

        createPDF = findViewById(R.id.createPDF);

        imageRadio = findViewById(R.id.imageRadio);
        imageMastil = findViewById(R.id.imageMastil);
        imageDoble_mastil = findViewById(R.id.imageDobleMastil);
        imageBase = findViewById(R.id.imageBase);
        imageRouter = findViewById(R.id.imageRouter);

        isDobleMastil = false;
        isBase = false;

        // ------------------------------------------------- DataBase ------------------------------

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Uploads");

        // -------------------------------------- Permissions --------------------------------------

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            askPermission();
        } else {
            Toast.makeText(getApplicationContext(),"Permisos consedidos",Toast.LENGTH_SHORT).show();
        }

        // ----------------------------------- Listeners --------------------------------------------

        cameraRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                normalPhoto(CAMERA_RADIO_REQUEST_CODE);
            }
        });

        cameraMastil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                normalPhoto(CAMERA_MASTIL_REQUEST_CODE);
            }
        });

        cameraDoble_mastil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDobleMastil){
                    normalPhoto(CAMERA_DOBLE_MASTIL_REQUEST_CODE);
                } else {
                    Toast.makeText(getApplicationContext(),"No tiene asignado doble mastil",Toast.LENGTH_LONG).show();
                }
            }
        });

        cameraBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBase){
                    normalPhoto(CAMERA_BASE_REQUEST_CODE);
                } else {
                    Toast.makeText(getApplicationContext(),"No tiene asignado base",Toast.LENGTH_LONG).show();
                }
            }
        });

        cameraRouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                normalPhoto(CAMERA_ROUTER_REQUEST_CODE);
            }
        });

        switch_mastil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDobleMastil = switch_mastil.isChecked();
            }
        });

        switch_base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBase = switch_base.isChecked();
            }
        });

        createPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (image_1 != null && image_2 != null && image_5 != null){
                    if (isBase && !isDobleMastil){
                        if (image_4 != null && !descriptionBase.getText().toString().equals("")){
                            generarPDF();
                        } else {
                            Toast.makeText(getApplicationContext(),"Es necesario sacar las fotos de evidencia",Toast.LENGTH_LONG).show();
                        }
                    } else if (!isBase && isDobleMastil){
                        if (image_3 != null && !descriptionMastil.getText().toString().equals("")){
                            generarPDF();
                        } else {
                            Toast.makeText(getApplicationContext(),"Es necesario sacar las fotos de evidencia y añadir la descripcion del metodo de pago del doble mastil",Toast.LENGTH_LONG).show();
                        }
                    } else if (isBase && isDobleMastil){
                        if (image_3 != null && image_4 != null){
                            generarPDF();
                        } else {
                            Toast.makeText(getApplicationContext(),"Es necesario sacar las fotos de evidencia y añadir la descripcion del metodo de pago de la base",Toast.LENGTH_LONG).show();
                        }
                    } else if (!isDobleMastil && !isBase){
                        generarPDF();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Es necesario sacar las fotos de evidencia",Toast.LENGTH_LONG).show();
                }
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

        if (requestCode == CAMERA_RADIO_REQUEST_CODE){
            assert data != null;
            image_1 = (Bitmap) data.getExtras().get("data");
            imageRadio.setImageBitmap(image_1);
        }
        if (requestCode == CAMERA_MASTIL_REQUEST_CODE){
            assert data != null;
            image_2 = (Bitmap) data.getExtras().get("data");
            imageMastil.setImageBitmap(image_2);
        }
        if (requestCode == CAMERA_DOBLE_MASTIL_REQUEST_CODE){
            assert data != null;
            image_3 = (Bitmap) data.getExtras().get("data");
            imageDoble_mastil.setImageBitmap(image_3);
        }
        if (requestCode == CAMERA_BASE_REQUEST_CODE){
            assert data != null;
            image_4 = (Bitmap) data.getExtras().get("data");
            imageBase.setImageBitmap(image_4);
        }
        if (requestCode == CAMERA_ROUTER_REQUEST_CODE){
            assert data != null;
            image_5 = (Bitmap) data.getExtras().get("data");
            imageRouter.setImageBitmap(image_5);
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

    private  void normalPhoto(int CODE){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera,CODE);
    }

    public  void generarPDF(){

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        TextPaint titulo = new TextPaint();
        TextPaint descripcion = new TextPaint();
        TextPaint descripcion_dos = new TextPaint();

        PdfDocument.PageInfo pageInfo_1 = new PdfDocument.PageInfo.Builder(816,1054, 1).create();
        PdfDocument.Page pagina1 = pdfDocument.startPage(pageInfo_1);
        Canvas canvas_1 = pagina1.getCanvas();

        Bitmap bitmapScale_1,bitmapScale_2,bitmapScale_3,bitmapScale_4,bitmapScale_5;

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        String formatDate = DateFormat.getDateInstance(DateFormat.FULL).format(currentTime);
        String time = formatTime.format(currentTime);

        // ----------------------------------------------- PAGINA 1 ---------------------------------------------------

        bitmapScale_1 = Bitmap.createScaledBitmap(image_1,300,400,false);
        canvas_1.drawBitmap(bitmapScale_1,400,50,paint);

        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titulo.setTextSize(20);
        canvas_1.drawText("Foto Radio",10,150,titulo);

        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas_1.drawText("foto del radio que se instalo",10,200,descripcion);

        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas_1.drawText(formatDate,10,450,descripcion);
        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas_1.drawText(time,10,475,descripcion);

        bitmapScale_2 = Bitmap.createScaledBitmap(image_2,300,400,false);
        canvas_1.drawBitmap(bitmapScale_2,400,600,paint);

        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titulo.setTextSize(20);
        canvas_1.drawText("Foto Mastil",10,750,titulo);

        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas_1.drawText("foto del mastil que se instalo",10,800,descripcion);

        pdfDocument.finishPage(pagina1);

        // ----------------------------------------------- PAGINA 2 ---------------------------------------------------

        if (isBase || isDobleMastil) {

            PdfDocument.PageInfo pageInfo_2 = new PdfDocument.PageInfo.Builder(816, 1054, 1).create();
            PdfDocument.Page pagina2 = pdfDocument.startPage(pageInfo_2);
            Canvas canvas_2 = pagina2.getCanvas();

            if (isDobleMastil) {
                bitmapScale_3 = Bitmap.createScaledBitmap(image_3, 300, 400, false);
                canvas_2.drawBitmap(bitmapScale_3, 400, 50, paint);

                titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                titulo.setTextSize(20);
                canvas_2.drawText("Foto Doble Mastil", 10, 150, titulo);

                descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                titulo.setTextSize(14);
                canvas_2.drawText(descriptionMastil.getText().toString(), 10, 200, descripcion);

                descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                titulo.setTextSize(14);
                canvas_2.drawText(formatDate,10,450,descripcion);
                descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                titulo.setTextSize(14);
                canvas_2.drawText(time,10,475,descripcion);
            }

            if (isBase) {
                bitmapScale_4 = Bitmap.createScaledBitmap(image_4, 300, 400, false);
                canvas_2.drawBitmap(bitmapScale_4, 400, 600, paint);

                titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                titulo.setTextSize(20);
                canvas_2.drawText("Foto Base", 10, 750, titulo);

                descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                titulo.setTextSize(14);
                canvas_2.drawText(descriptionBase.getText().toString(), 10, 800, descripcion);

                descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                titulo.setTextSize(14);
                canvas_2.drawText(formatDate,10,450,descripcion);
                descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                titulo.setTextSize(14);
                canvas_2.drawText(time,10,475,descripcion);
            }

            pdfDocument.finishPage(pagina2);
        }

        // ----------------------------------------------- PAGINA 3 ---------------------------------------------------

        PdfDocument.PageInfo pageInfo_3 = new PdfDocument.PageInfo.Builder(816,600, 1).create();
        PdfDocument.Page pagina3 = pdfDocument.startPage(pageInfo_3);
        Canvas canvas_3 = pagina3.getCanvas();

        bitmapScale_5 = Bitmap.createScaledBitmap(image_5,300,400,false);
        canvas_3.drawBitmap(bitmapScale_5,400,50,paint);

        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titulo.setTextSize(20);
        canvas_3.drawText("Foto Router",10,150,titulo);

        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas_3.drawText("foto del router interno que se instalo",10,200,descripcion);

        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas_3.drawText(formatDate,10,450,descripcion);
        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        titulo.setTextSize(14);
        canvas_3.drawText(time,10,475,descripcion);

        pdfDocument.finishPage(pagina3);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Test_3.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(getApplicationContext(),"Se creo el PDF exitosamente",Toast.LENGTH_SHORT).show();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("File is loading...");
            progressDialog.show();

            StorageReference reference = storageReference.child("Uploads/" + System.currentTimeMillis() + ".pdf");
            reference.putFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(),"File uploaded",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    progressDialog.setMessage("File Uploaded..." + (int) progress + "%");
                }
            });
        } catch (Exception e){
            Toast.makeText(getApplicationContext(),"No se pudo crear el PDF",Toast.LENGTH_LONG).show();
        }

        pdfDocument.close();
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

}
