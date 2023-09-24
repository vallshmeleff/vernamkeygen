package com.example.czechentropy;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//--------------------------------------------------
//
// Czech Entropy Synchronous random number generator
// For the same given text, generates the same set of "random" numbers. Does not use synchronization channels (hidden).
// The application creates identical encryption pads for the sender and recipient of messages.
//
// Email:
// 3f9d45e41863fa14dac44196ff1401a4c6aa230951f0924db95925409c2ac23b30bd22bcb39be1adca6074f3b315939075272c6093d5f70fb8
// Key:
// 6fef2a836a029779bfb66db69a7a66cda8cf467b7dd0fb23cf3c4b34f358e20610cb43d0dfee8cdea20d119fd673f5d0124a4d09fffb9460d5
//
// --------------------------------------------------
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int BUFFER_SIZE = 1024; 
    private TextToSpeech repeatTTS;
    Button btnspeakout;
    EditText edtTexttoSpeak;
    // public static Context context;
    public File file = null;
    public static String sfile[] = new String[3]; 
    ;
    public static String zipfile = new String(); 

    public File path = null; 
    public String destFileName = "vernam.dat";
    public String copyright = "Czech Entropy - (Android Java application) - a random number generator in synchronous mode for the sender and recipient of encrypted information. Does not use sync channels or conversion formulas. (c) by OFLAMERON";
    public int[] vernamkey = new int[60]; 
    public int[] vernamkey1 = new int[60]; 
    public int[] vernamkey2 = new int[60]; 
    public int content; // FFile content
    public int x = 0; 
    public int n = 0; 
    public int m = 0; 
    public int g = 0; 


    public static String ie = ""; // Vernam Key in INT
    public static String hexie = ""; // Vernam Key in HEX
    public static String sysmsg = "";

    public static final String KEY_PARAM_UTTERANCE_ID = "utteranceId";
    public static long length = 0; 
    public static int cnter = 0; 
    public static int nonzerocounter = 0; 
    public static int gd = 0; 

    HashMap<String, String> myHashRender = new HashMap();
    public static String wakeUpText = "Czech Entropy - Synchronous random number generator for the Vernam cipher without a synchronization channel.";
    public TextToSpeech textToSpeechSystem;
    public static String gh = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText cxtv = findViewById(R.id.NumInStream); 
        EditText sxtv = findViewById(R.id.SysMessage); 
        Button bttv = findViewById(R.id.btnGenerate); 

        CountDownTimer yourCountDownTimer = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
                // cxtv.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                // cxtv.setText("Enter a text longer than 100 characters \n");
                cxtv.setHint("Enter a text longer than 100 characters \n");
            }
        }.start();


        CountDownTimer yourCountDownTimer2 = new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
                // cxtv.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                cxtv.setHint("Enter a text longer than 100 characters and click the GENERATE button \n");
            }
        }.start();

        CountDownTimer yourCountDownTimer3 = new CountDownTimer(6000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                cxtv.setHint("Enter a text longer than 100 characters and click the GENERATE button \n [Application Ready] \n");
                bttv.setVisibility(View.VISIBLE);
            }
        }.start();



        cxtv.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.length() > 0)
                { 
                    gd=cxtv.length();
                    sxtv.setText("Entered text lenght " + String.valueOf(gd) + "\n");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //-----------------------------------------------------------------


        TextView Guide = (TextView) findViewById(R.id.Guide);
        Guide.setText(Html.fromHtml("<a href='https://rescuewebcam.blogspot.com'>Czech Entropy</a> - Synchronous random number generator for the Vernam cipher ver 0.0 at the sender and receiver of messages without a synchronization channel. \n", Html.FROM_HTML_MODE_COMPACT));
        Guide.setMovementMethod(LinkMovementMethod.getInstance()); 

        Context context = getApplicationContext();
        path = context.getApplicationContext().getFilesDir();
        file = new File(path, "/" + "vernam.dat"); 
        sfile[0] = file.toString();
        zipfile = new File(path, "/" + "vernam.zip").toString(); 
        file.setWritable(true);

        destFileName = file.toString();
        Log.e("FILES Path", "== DAT Files path == " + destFileName);


    } // OnCreate


    // @Override necessary if you use onStart instead of ontheStart
    // @SuppressWarnings("deprecation")
    protected void ontheStart() {
        TextView stxt = (TextView) findViewById(R.id.NumInStream); 

        final String utteranceId = "myTestingId"; 
        Bundle bundleTts = new Bundle();
        super.onStart();
        textToSpeechSystem = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeechSystem.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                        sysmsg = "An intermediate file created in progress\n";
                        handler.sendEmptyMessage(0); 
                    }

                    @Override
                    public void onDone(String utteranceId) { 
                        Log.i("DAT onInit", "==== DAT File Done ====");
                        ReadFile(); 
                        // Some additional changes. Not public
                        sysmsg = "Vernam Key generated in INT and HEX (may be copied to clipboard)\n";
                        handler.sendEmptyMessage(0); 
                        reStart(); 
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.i("DAT onInit", "==== DAT Error ====");
                    }
                });
                wakeUpText = stxt.getText().toString(); 
                myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "12345"); 
                // ................. :-))
                sysmsg = "Text conversion started";
                handler.sendEmptyMessage(0); 

            } // onInit
        });
    }


    public void ReadFile() { 
        NoneZeroByteCount(); 
        FileInputStream fis = null;
        length = file.length(); 
        try {
            fis = new FileInputStream(path + "/" + "vernam.dat");
            Log.i("== ReadFile =", " == == Total file size to read (in bytes) == == " + fis.available());

            g = 0; 
            n = 0; 
            m = 0; 

            while ((content = fis.read()) != -1) {

                if (content > 0) { 
                    KeyBuild3(); 
                    cnter++; 
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //// Log.i("== ReadFile =", " == == Show Key 60 bytes Vernamkey (in INT) == == "+ ie);


    } // ReadFile


    public void NoneZeroByteCount() { 
        EditText sysmessage = findViewById(R.id.SysMessage); 

        FileInputStream fis = null;
        length = file.length(); 
        try {
            fis = new FileInputStream(path + "/" + "vernam.dat");
            while ((content = fis.read()) != -1) {

                if (content > 0) {
                    nonzerocounter++;
                }

            }

        } catch (IOException e) {
            sysmessage.setText("Err. Error creating an intermediate file for choosing random numbers");
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


    } 


    public void KeyBuild3() { // Key Build ver 3
                // ................. :-))
    } // KeyBuild


    public void entropyControl1() { 
        EditText sxtv = findViewById(R.id.SysMessage); 
        if (n > 0) {
            if (vernamkey2[n] == vernamkey2[n-1]) { 
                if (vernamkey2[n] > 250) { 
                    vernamkey2[n] = vernamkey2[n] - 47;
                } else { 
                    vernamkey2[n] = vernamkey2[n] + 33;
                }


                n++; 
            }

        }



    }



    public void copyToClipboardHEX(View v) { 
        EditText cxtv = findViewById(R.id.VernamKeyHex); 

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", cxtv.getText());
        clipboard.setPrimaryClip(clip);
    }

    public void copyToClipboardINT(View v) { 
        EditText cxtv = findViewById(R.id.VernamKeyInt); 

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", cxtv.getText());
        clipboard.setPrimaryClip(clip);
    }


    @Override
    public void onInit(int status) {

    }

    @Override
    public void onPause() {
        if (textToSpeechSystem != null) {
            textToSpeechSystem.stop();
            textToSpeechSystem.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (textToSpeechSystem != null) {
            textToSpeechSystem.stop();
            textToSpeechSystem.shutdown();
        }
        super.onDestroy();
    }

    public void reStart() {
        x = 0; 
        n = 0; 
        m = 0; 
        g = 0; 
        length = 0; 
        cnter = 0; 
        nonzerocounter = 0; 
        gd = 0; 
        gh = "";
        fileDelete(); 


    }


    Handler handler = new Handler(Looper.getMainLooper()) { 
        @Override
        public void handleMessage(Message msg) {

            EditText smsg = findViewById(R.id.SysMessage); 
            smsg.setText(MainActivity.sysmsg);
            EditText sxtv = findViewById(R.id.VernamKeyInt); 
            sxtv.setText(MainActivity.ie);
            EditText cxtv = findViewById(R.id.VernamKeyHex); 
            cxtv.setText(MainActivity.hexie);

        }
    };


    public void onButtonExit(View arg0) {
        fileDelete(); 
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);

    }

    public void onButtonGenerate(View arg0) {
        EditText sxtv = findViewById(R.id.VernamKeyInt); 
        EditText cxtv = findViewById(R.id.VernamKeyHex); 
        ie = " ";
        hexie = " ";
        handler.sendEmptyMessage(0); 


        EditText keytext = findViewById(R.id.NumInStream); 
        EditText sysmessage = findViewById(R.id.SysMessage); 
        String sourcetext = keytext.getText().toString(); 
        if (keytext.getText().toString().equals("Czech Entropy - Synchronous random number generator for the Vernam cipher without a synchronization channel. ")) {
            sysmessage.setText("Err. Enter original text to generate a block of numbers. \n");

        } else {
            if (keytext.getText().length() > 100) { 
                ontheStart(); 
            } else {
                sysmessage.setText("Err. Enter text longer than 100 characters. \n");
            }

        }
    }

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }
    }

    public void fileDelete() { 

        if (file.exists()) { 
            if (file.delete()) {
                Log.i("== Delete file =", " == == File vernam.dat deleted == == ");
            } else {
                Log.i("== Delete file =", " == == File vernam.dat not deleted == == ");
            }
        }


    }


} // MainActivity
