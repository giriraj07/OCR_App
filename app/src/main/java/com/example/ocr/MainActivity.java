package com.example.ocr;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static StringBuilder stringBuilder;
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView textValue;
    private Button Select;
    private ImageView imgView;
    Bitmap bitmap;
    private static final int CAMERA_REQUEST = 1888;
    private static final int RC_OCR_CAPTURE = 9003;
    private  static final int PICK_IMAGE=1000;
    private static final String TAG = "MainActivity";
    final int KERNAL_WIDTH = 3;
    final int KERNAL_HEIGHT = 3;
    int[][] kernalBlur ={
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage =findViewById(R.id.status_message);
        textValue =findViewById(R.id.text_value);

        autoFocus =findViewById(R.id.auto_focus);
        useFlash = findViewById(R.id.use_flash);
        imgView=findViewById(R.id.imgView);
        Select=findViewById(R.id.select);
        Select.setOnClickListener(this);
        findViewById(R.id.read_text).setOnClickListener(this);
        findViewById(R.id.search).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_text) {  //  For displaying textblock on live screen of mobile
               OcrGraphic.Fetched=new StringBuilder();
               textValue.setText("");
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            intent.putExtra(OcrCaptureActivity.AutoFocus
                    , autoFocus.isChecked());
            intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());
            startActivityForResult(intent, RC_OCR_CAPTURE);
        }
        else if(v.getId()==R.id.gallery)   // Select image from gallery
        {
            openGallery();
        }
          else if(v.getId()==R.id.select)  // capture directly by opening camera
        {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
          else if(v.getId()==R.id.search)  // search the captured text
          {
              Intent intent=new Intent(this,Activity2.class);
              startActivity(intent);
          }
    }

    private Bitmap processingBitmap(Bitmap src, int[][] knl){
        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), src.getConfig());

        int bmWidth = src.getWidth();
        int bmHeight = src.getHeight();
        int bmWidth_MINUS_2 = bmWidth - 2;
        int bmHeight_MINUS_2 = bmHeight - 2;

        for(int i = 1; i <= bmWidth_MINUS_2; i++){
            for(int j = 1; j <= bmHeight_MINUS_2; j++){

                //get the surround 3*3 pixel of current src[i][j] into a matrix subSrc[][]
                int[][] subSrc = new int[KERNAL_WIDTH][KERNAL_HEIGHT];
                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSrc[k][l] = src.getPixel(i-1+k, j-1+l);
                    }
                }
                //subSum = subSrc[][] * knl[][]
                int subSumA = 0;
                int subSumR = 0;
                int subSumG = 0;
                int subSumB = 0;

                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSumA += Color.alpha(subSrc[k][l]) * knl[k][l];
                        subSumR += Color.red(subSrc[k][l]) * knl[k][l];
                        subSumG += Color.green(subSrc[k][l]) * knl[k][l];
                        subSumB += Color.blue(subSrc[k][l]) * knl[k][l];
                    }
                }

                if(subSumA<0){
                    subSumA = 0;
                }else if(subSumA>255){
                    subSumA = 255;
                }

                if(subSumR<0){
                    subSumR = 0;
                }else if(subSumR>255){
                    subSumR = 255;
                }

                if(subSumG<0){
                    subSumG = 0;
                }else if(subSumG>255){
                    subSumG = 255;
                }

                if(subSumB<0){
                    subSumB = 0;
                }else if(subSumB>255){
                    subSumB = 255;
                }

                dest.setPixel(i, j, Color.argb(
                        subSumA,
                        subSumR,
                        subSumG,
                        subSumB));
            }
        }

        return dest;
    }
    private void openGallery(){
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.putExtra("uri",MediaStore.Images.Media.INTERNAL_CONTENT_URI);
         i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrGraphic.Fetched.toString());
                    statusMessage.setText(R.string.ocr_success);
                    textValue.setText(OcrGraphic.Fetched);
                    Log.d(TAG, "Text read: " + text);
                } else {
                    String text =OcrGraphic.Fetched.toString();
                    statusMessage.setText("Text Captured");
                    textValue.setText(text);
                   Button search=findViewById(R.id.search);

                   search.setOnClickListener(new View.OnClickListener() {

                       @Override
                       public void onClick(View v) {
                           Intent intent=new Intent(Intent.ACTION_WEB_SEARCH);
                           intent.putExtra(SearchManager.QUERY, textValue.getText());
                           startActivity(intent);
                       }
                   });
                    Log.d(TAG, "No Text captured, intent data is null"+"hhhh"+text+"hhhh");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.ocr_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else if (requestCode == CAMERA_REQUEST)
        {
            if (resultCode == RESULT_OK)
            {
                 Bundle extras = data.getExtras();
                 bitmap = (Bitmap) extras.get("data");
                 imgView.setImageBitmap(bitmap);
                Bitmap afterSharpen = processingBitmap(bitmap, kernalBlur);  // optional function to sharpen
                                                                            // the image being captured,might not be called
                imgView.setImageBitmap(afterSharpen);
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if (!textRecognizer.isOperational()) {
                    Toast.makeText(getApplicationContext(), "Detector is not available yet", Toast.LENGTH_LONG);
                }
                Frame frame = new Frame.Builder().setBitmap(afterSharpen).build();
                final SparseArray<TextBlock> items = textRecognizer.detect(frame);
                Log.d("TAG", String.valueOf(items.size()));
                if (items.size() != 0) {
                    textValue.post(new Runnable() {
                        @Override
                        public void run() {
                             stringBuilder = new StringBuilder();
                            for (int i = 0; i < items.size(); i++) {
                                TextBlock item = items.valueAt(i);
                                stringBuilder.append(item.getValue());
                            }
                            textValue.setText(stringBuilder.toString());
                        }
                    });
                }
            }
            else
            {
                Log.d("Status:", "Photopicker cancelled");
            }
        }
        else if(requestCode==PICK_IMAGE && resultCode==RESULT_OK)
        {
            Uri image_URI = data.getData();
            imgView.setImageURI(image_URI);
            try {
                InputStream pic = getContentResolver().openInputStream(image_URI);
                bitmap = BitmapFactory.decodeStream(pic);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();// Building up the text
            if (!textRecognizer.isOperational()) {
                Toast.makeText(getApplicationContext(), "Detector is not available yet", Toast.LENGTH_LONG);
            }
            Frame frame = new Frame.Builder().setBitmap(bitmap).build(); // find all the image Frames

            final SparseArray<TextBlock> items = textRecognizer.detect(frame);
            Log.d("TAG", String.valueOf(items.size()));
            if (items.size() != 0) {

                textValue.post(new Runnable() {
                    @Override
                    public void run() {
                        stringBuilder=new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue());
                        }
                        textValue.setText(stringBuilder.toString());
                    }
                });
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
