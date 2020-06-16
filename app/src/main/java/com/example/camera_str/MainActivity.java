package com.example.camera_str;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private ImageView img;

    private StorageReference stref;
    private ProgressDialog prog;



    private static final int CAMERA_REQUEST_CODE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stref= FirebaseStorage.getInstance().getReference();
        btn=(Button)findViewById(R.id.upload);
        img=(ImageView)findViewById(R.id.image);
        prog=new ProgressDialog(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
                cameraIntent.putExtra( android.provider.MediaStore.EXTRA_SIZE_LIMIT, "720000");
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == CAMERA_REQUEST_CODE && resultCode ==RESULT_OK)
        {
            prog.setMessage("Upolading..image");
            prog.show();

            //get the camera image
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataBAOS = baos.toByteArray();
            //set the image into imageview
            img.setImageBitmap(bitmap);

            /*************** UPLOADS THE PIC TO FIREBASE***************/
            //Firebase storage folder where you want to put the images
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://...");

//name of the image file (add time to have different files to avoid rewrite on the same file)

            StorageReference imagesRef = storageRef.child("filename" + new Date().getTime());

//upload image

            UploadTask uploadTask = imagesRef.putBytes(dataBAOS);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(),"Sending failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    //handle success
                    prog.dismiss();
                }
            });
        }
            }

        }

