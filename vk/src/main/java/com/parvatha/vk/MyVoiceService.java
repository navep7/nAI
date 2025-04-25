package com.parvatha.vk;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyVoiceService extends Service {
    protected static SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    private String TAG = "MyVoiceService";
    private Intent intent;
    private int mStreamVolume = 0;
    private AudioManager mAudioManager;
    private String[] setOfCmnds = {
            "program", "programme", "volume", "youtube", "valium", "valume", "video", "live", "channels"
    };
    private String videoIdAris = "ra6yEmAPwM4",
            videoIdIronman = "cZ1tVODlxD8",
            videoIdAvatar = "ZUgjaUZBP7o",
            videoIdBahuballi = "do7x_izM0Uc",
            videoIdKabali = "9mdJV5-eias",
            videoIdAvengers = "ddSvc8FS-JQ",
            videoIdDespacito = "kJQP7kiw5Fk",
            videoIdGstyle = "CH1XGdu-hzQ";
    private Intent HomeIntent, YoutubeIntent, LiveChannelsIntent;
    private SpeechRecognitionListener speechContext;
    private Intent appIntent;
    private static int commandContext = 1;  // 1 - Home; 2 - Youtube; 3 - LIVE channels
    private Toast mToastToShow;
    private Handler mToastHandler = null;
    private Toast toast;
    private Intent programintent;
    private String[] moviesSupported = new String[]{
            "avengers",
            "iron",
            "despacito",
            "style",
            "avatar"
    };

    private String[] programsSupported = new String[]{
            "bbc",
            "i tv",
            "bbc news",
    };
    private boolean Gsuccess = false;
    //private UIUpdateTask uiTask;
    Thread thread;
    private Toast gToast;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MainActivity.makeToast("onStartCommand");
        appIntent = intent;
        listenService();

        mToastHandler = new Handler();


        if (!thread.isAlive())
            thread.start();

        return START_STICKY;
    }

    public void sToast(final String s) {

        MainActivity.makeToast(s);
    }

    private void listenService() {


        if (mSpeechRecognizer != null)
            mSpeechRecognizer.destroy();


        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechContext = new SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(speechContext);
        //    }
        if (mSpeechRecognizerIntent == null)
            mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, false);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        //2dayf
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent); // this method make that annoying sound

    }

    private String toastSring;

    @SuppressLint("ResourceType")
    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("MyVoiceService", "************************" + "Oncreate");



        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        HomeIntent = getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        //  https://www.youtube.com/watch?v=u0IU8uQniX8&t=2m35s


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        programintent = new Intent();
        programintent.setAction("com.example.now");
        programintent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        if (Gsuccess) {
                            sToast("Waiting for command");
                        } else {
                            sToast("Waiting for OK Google");
                        }
                        Thread.sleep(2650);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }


        };


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class SpeechRecognitionListener implements RecognitionListener {

        private String WakeUpWord;
        private int index;

        private boolean Cmnd = false;
        private int presentVolume;
        private final Handler handler = new Handler();
        private MainActivity mainActivity = new MainActivity();

        @Override
        public void onReadyForSpeech(Bundle bundle) {

            showToast("listening initiated, start speaking... ");
            Log.d(TAG, "service");
            updateUI("onReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            showToast("onEndOfSpeech process, wait .. ");
            updateUI("onEndOfSpeech");
            //mainActivity.GIFlistening.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onError(int i) {
            Log.d(TAG, "ERROR - " + i);


            switch (i) {
                case 1 : showToast("1. ERROR_NETWORK_TIMEOUT");
                    break;
                case 2 : showToast("2. ERROR_NETWORK");
                    break;
                case 3 : showToast("3. ERROR_AUDIO");
                    break;
                case 4 : showToast("4. ERROR_SERVER");
                    break;
                case 5 : showToast("5. ERROR_CLIENT");
                    break;
                case 6 : showToast("6. ERROR_SPEECH_TIMEOUT");
                    break;
                case 7 : showToast("7. ERROR_NO_MATCH");
                    break;
                case 8 : showToast("8. ERROR_RECOGNIZER_BUSY");
                    break;
                case 9 : showToast("9. ERROR_INSUFFICIENT_PERMISSIONS");
                    break;
                default : showToast(" > 10 i dont know");
                    break;
            }
            if (i == 7) {
                if (WakeUpWord != null)
                    showToast("NO MATCH for " + WakeUpWord);
            }
            listenService();
        }

        @Override
        public void onResults(Bundle resultsBundle) {
            // TODO Auto-generated method stub

            ArrayList<String> matches = resultsBundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String spokenCommand = "";


            for (String res : matches) {
                WakeUpWord = res;

                if (!Gsuccess) {
                    if (res.toLowerCase().startsWith("ok")) {

                        if (res.toLowerCase().contains("google")) {
                            index = res.indexOf(' ');

                            Gsuccess = true;



                            WakeUpWord = "Ok, Google !";
                            if (index != -1)
                                spokenCommand = res.substring(index);

                            break;
                        }
                    }
                } else {
                    spokenCommand = res;
                    if (spokenCommand.toLowerCase().contains("stop")) {
                        if (commandContext == 2) {
                            playYoutubeVideo("");
                        }
                    } else if (spokenCommand.toLowerCase().contains("play")) {
                        //2Day          if (commandContext == 2) {

                        programintent = new Intent();
                        programintent.setAction("com.example.now");
                        programintent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);


                        if (spokenCommand.toLowerCase().contains("arris") || spokenCommand.toLowerCase().contains("channel")) {
                            startActivity(HomeIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    playYoutubeVideo(videoIdAris);
                                }
                            }, 1000);

                        } else if (spokenCommand.toLowerCase().contains("avengers")) {
                            Gsuccess = false;
                            startActivity(HomeIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    playYoutubeVideo(videoIdAvengers);
                                }
                            }, 1000);
                        } else if (spokenCommand.toLowerCase().contains("iron")) {
                            Gsuccess = false;
                            startActivity(HomeIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    playYoutubeVideo(videoIdIronman);
                                }
                            }, 1000);

                        } else if (spokenCommand.toLowerCase().contains("despacito")) {
                            Gsuccess = false;
                            startActivity(HomeIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    playYoutubeVideo(videoIdDespacito);
                                }
                            }, 1000);

                        } else if (spokenCommand.toLowerCase().contains("avatar")) {
                            Gsuccess = false;
                            startActivity(HomeIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    playYoutubeVideo(videoIdAvatar);
                                }
                            }, 1000);

                        } else if (spokenCommand.toLowerCase().contains("style")) {
                            Gsuccess = false;
                            startActivity(HomeIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    playYoutubeVideo(videoIdGstyle);
                                }
                            }, 1000);

                        } else if (spokenCommand.toLowerCase().contains("bbc news")) {
                            Gsuccess = false;
                            showToast("Yet2Impl");
                            getApplicationContext().startActivity(LiveChannelsIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    programintent.putExtra("key", "CH7");
                                    sendBroadcast(programintent);
                                }
                            }, 1000);
                        } else if (spokenCommand.toLowerCase().contains("bbc")) {
                            Gsuccess = false;
                            getApplicationContext().startActivity(LiveChannelsIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    programintent.putExtra("key", "CH1");
                                    sendBroadcast(programintent);
                                }
                            }, 1000);

                        } else if (spokenCommand.toLowerCase().contains("i tv")) {
                            Gsuccess = false;
                            getApplicationContext().startActivity(LiveChannelsIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    programintent.putExtra("key", "CH3");
                                    sendBroadcast(programintent);
                                }
                            }, 1000);
                        } else if (spokenCommand.toLowerCase().contains("sat")) {
                            Gsuccess = false;
                            getApplicationContext().startActivity(LiveChannelsIntent);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    programintent.putExtra("key", "CH8");
                                    sendBroadcast(programintent);
                                }
                            }, 1000);
                        }
                    } else if (spokenCommand.toLowerCase().contains("close")) {
                        Gsuccess = false;

                        startActivity(HomeIntent);

                    } else if ((spokenCommand.toLowerCase().contains("program") || spokenCommand.toLowerCase().contains("programme")) && (commandContext == 3)) {

                        showToast(spokenCommand);
                        Log.d("ProNo", spokenCommand);
                        if (spokenCommand.toLowerCase().contains("next")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "NXT");
                            showToast("P next from G");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("previous")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "PRV");
                            showToast("P prev from G");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("seven") || spokenCommand.toLowerCase().contains("7") || spokenCommand.toLowerCase().contains("bbc news")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH7");
                            showToast("P 7");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("one") || spokenCommand.toLowerCase().contains("1") || spokenCommand.toLowerCase().contains("bbc")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH1");
                            showToast("P 1");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("two") || spokenCommand.toLowerCase().contains("2")
                                || spokenCommand.toLowerCase().contains("to") || spokenCommand.toLowerCase().contains("too")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH2");
                            showToast("P 2");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("three") || spokenCommand.toLowerCase().contains("3") || spokenCommand.toLowerCase().contains("i tv")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH3");
                            showToast("P 3");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("four") || spokenCommand.toLowerCase().contains("4") || spokenCommand.toLowerCase().contains("for")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH4");
                            showToast("P 4");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("five") || spokenCommand.toLowerCase().contains("5")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH5");
                            showToast("P 5");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("six") || spokenCommand.toLowerCase().contains("6")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH6");
                            showToast("P 6");
                            sendBroadcast(programintent);
                        } else if (spokenCommand.toLowerCase().contains("eight") || spokenCommand.toLowerCase().contains("8") || spokenCommand.toLowerCase().contains("satellite")) {
                            Gsuccess = false;
                            programintent.putExtra("key", "CH8");
                            showToast("P 8");
                            sendBroadcast(programintent);
                        } else
                            Toast.makeText(getBaseContext(), "program identified as - " + spokenCommand, Toast.LENGTH_LONG).show();
                    } else showToast("commandContext - " + String.valueOf(commandContext));

                    for (int i = 0; i < setOfCmnds.length; i++) {

                        if (spokenCommand.toLowerCase().contains(setOfCmnds[i].toString())) {


                            Cmnd = true;

                            if (spokenCommand.toLowerCase().contains("youtube")) {
                                commandContext = 2;

                                initialize();


                                if (spokenCommand.toLowerCase().contains("channel")) {
                                    Gsuccess = false;
                                    playYoutubeVideo(videoIdAris);
                                } else if (spokenCommand.toLowerCase().contains("iron")) {
                                    Gsuccess = false;
                                    playYoutubeVideo(videoIdIronman);
                                } else if (spokenCommand.toLowerCase().contains("bahu")) {
                                    Gsuccess = false;
                                    playYoutubeVideo(videoIdBahuballi);
                                } else if (spokenCommand.toLowerCase().contains("kabal")) {
                                    Gsuccess = false;
                                    playYoutubeVideo(videoIdKabali);
                                } else if (spokenCommand.toLowerCase().contains("close")) {

                                    commandContext = 1;
                                    showToast("Closing Youtube");

                                    Log.d(TAG, "Closing Youtube");

                                    mSpeechRecognizer.stopListening();
                                    mSpeechRecognizer.destroy();
                                    startActivity(HomeIntent);

                                } else {
                                    playYoutubeVideo("");

                                }

                            } else if (spokenCommand.toLowerCase().contains("live") || spokenCommand.toLowerCase().contains("channels")) {

                                if (spokenCommand.toLowerCase().contains("open") || spokenCommand.toLowerCase().contains("launch")) {
                                    Gsuccess = false;
                                    commandContext = 3;
                                    initialize();

                                    try {
                                        getApplicationContext().startActivity(LiveChannelsIntent);
                                    } catch (ActivityNotFoundException ex) {
                                        Toast.makeText(getApplicationContext(), "App not installed", Toast.LENGTH_SHORT).show();

                                    }
                                } else if (spokenCommand.toLowerCase().contains("close") || spokenCommand.toLowerCase().contains("exit")) {
                                    Gsuccess = false;
                                    commandContext = 1;
                                    showToast("Closing LIVE channels");

                                    Log.d(TAG, "Closing LIVE channels");

                                    mSpeechRecognizer.stopListening();
                                    mSpeechRecognizer.destroy();
                                    startActivity(HomeIntent);
                                } else {
                                    showToast(spokenCommand + " ?");
                                }
                            } else if ((spokenCommand.toLowerCase().contains("volume")) || (spokenCommand.toLowerCase().contains("sound"))) {

                                Log.d(TAG, spokenCommand + " - working");

                                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);


                                if (spokenCommand.toLowerCase().contains("up")) {
                                    Gsuccess = false;
                                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);

                                    final Intent intent = new Intent();
                                    intent.setAction("com.example.now");
                                    intent.putExtra("key", "VOLUMEUP");
                                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                    sendBroadcast(intent);

                                    showToast("V  UP");
                                } else if (spokenCommand.toLowerCase().contains("down")) {
                                    Gsuccess = false;
                                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);

                                    final Intent intent = new Intent();
                                    intent.setAction("com.example.now");
                                    intent.putExtra("key", "VOLUMEDOWN");
                                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                    sendBroadcast(intent);

                                    showToast("V  DOWN");
                                } else if (spokenCommand.toLowerCase().contains("unmute") || spokenCommand.toLowerCase().contains(" and mute")) {
                                    Gsuccess = false;
                                    audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);

                                    final Intent intent = new Intent();
                                    intent.setAction("com.example.now");
                                    intent.putExtra("key", "VOLUMEUNMUTE");
                                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                    sendBroadcast(intent);

                                    showToast("V UNMUTE");
                                    //    HalJni.SetAudioVolume(10);
                                } else if (spokenCommand.toLowerCase().contains(" mute") || spokenCommand.toLowerCase().contains(" nude") || spokenCommand.toLowerCase().contains(" news")) {
                                    Gsuccess = false;
                                    audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);
                                    //     HalJni.SetAudioVolume(0);

                                    final Intent intent = new Intent();
                                    intent.setAction("com.example.now");
                                    intent.putExtra("key", "VOLUMEMUTE");
                                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                    sendBroadcast(intent);

                                    showToast("V MUTE");
                                } else if (spokenCommand.toLowerCase().contains("next") || spokenCommand.toLowerCase().contains("prev")) {
                                    Gsuccess = false;
                                    showToast("try UP or DOWN");
                                } else if (spokenCommand.matches(".*\\d+.*")) {
                                    showToast("V - " + extractInt(spokenCommand));
                                } else {
                                    showToast(spokenCommand + " ?");
                                }

                            } else if (spokenCommand.toLowerCase().contains("video")) {

                                if (spokenCommand.toLowerCase().contains("play")) {
                                    Gsuccess = false;
                                    showToast("VID PLAY");
                                } else if (spokenCommand.toLowerCase().contains("pause")) {
                                    Gsuccess = false;
                                    showToast("VID PAUSE");
                                } else {
                                    showToast(spokenCommand + "?");
                                }
                            } else {
                                showToast(spokenCommand + "?");
                            }


                        }
                    }
                }


            }
            listenService();
        }


        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }

    }

    public int extractInt(String str) {
        Matcher matcher = Pattern.compile("\\d+").matcher(str);


        if (!matcher.find())
            throw new NumberFormatException("For input string [" + str + "]");

        return Integer.parseInt(matcher.group());

    }


    private void playYoutubeVideo(String videoID) {
        Gsuccess = false;
        if (!videoID.equals(""))
            YoutubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/" + "watch?v=" + videoID));
        else {
            YoutubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/"));
        }


        YoutubeIntent.addFlags(YoutubeIntent.FLAG_ACTIVITY_CLEAR_TASK | YoutubeIntent.FLAG_ACTIVITY_NEW_TASK);


        try {
            getApplicationContext().startActivity(YoutubeIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Youtube app not installed", Toast.LENGTH_SHORT).show();
        }
    }


    private void initialize() {

        new CountDownTimer(7000, 1000) {


            public void onTick(long millisUntilFinished) {


                if ((millisUntilFinished / 1000) == 6) {
                    mSpeechRecognizer.cancel();
                    mSpeechRecognizer.destroy();
                    showToast("VOICE - OFF");
                } else showToast("initializing.. " + String.valueOf(millisUntilFinished / 1000));

            }

            public void onFinish() {
                showToast("VOICE - ON");
                mSpeechRecognizer.setRecognitionListener(speechContext);
                if (mSpeechRecognizerIntent == null)
                    mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, false);

                //2day
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent); // this method make that annoying sound
            }
        }.start();
    }

    private void showToast(final String s) {

        mToastHandler.post(new Runnable() {
            @Override
            public void run() {

                /*if (Gsuccess) {
                    toast.setText("Waiting for Command");
                } else {
                    toast.setText("Waiting for OK GOOGLE");
                }*/
                //    toast.show();
            }
        });


    }


    private void updateUI(String s) {
        Intent local = new Intent();

        local.setAction("com.example.now");

        local.putExtra("ui", "listen  - " + s);

        this.sendBroadcast(local);
    }
}
