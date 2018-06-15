package com.lee.edu.mydemo;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.Manifest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

public class MainActivity extends AppCompatActivity {
    private double[] Freqarrary = {17500, 17850, 18200, 18550, 18900, 19250, 19600, 19950, 20300, 20650};        //设置播放频率
    private int numfre = 8;
    private Button btnPlayRecord;        //开始按钮
    private Button btnStopRecord;        //结束按钮
    private Spinner userSpinner;
    private Spinner gestureSpinner;
    private TextView tvDist;            //显示距离
    private TextView tvDist2;            //显示距离
    private boolean flag = true;        //播放标志
    private AudioRecord audioRecord;    //录音对象
    private int recBufSize = 4400 * 2;            //定义录音片长度
    private int count = 0;
    /**
     * 采样率（默认44100，每秒44100个点）
     */
    private int sampleRateInHz = 44100;
    /**
     * 编码率（默认ENCODING_PCM_16BIT）
     */
    private int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 声道（默认单声道）
     */
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;        //立体道
    /**
     * 1s内17500hz的波值
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        //设置圆环角度
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    tvDist.setText(msg.obj.toString());
                    break;
                case 2:
                    tvDist2.setText(msg.obj.toString());
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestPermission();

        InitView();

        InitListener();

    }

    private void InitListener() {
        //17500Hz的波形
        /**
         * 这部分取最大波形恐怕有问题，录取声音不一定为小于1的正弦波
         *
         */
        //播放按钮
        btnPlayRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlayRecord.setEnabled(false);
                btnStopRecord.setEnabled(true);
                count++;
                flag = true;
                new ThreadInstantPlay().start();        //播放(发射超声波)


                try {
                    Thread.sleep(10);    //等待开始播放再录音
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                new ThreadInstantRecord().start();        //录音
                //录音播放线程
            }
        });

        //停止按钮
        btnStopRecord.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 自动生成的方法存根
                btnPlayRecord.setEnabled(true);
                btnStopRecord.setEnabled(false);
                flag = false;
            }
        });
    }

    private void InitView() {
        btnPlayRecord = (Button) findViewById(R.id.btnplayrecord);
        btnStopRecord = (Button) findViewById(R.id.btnstoprecord);
        userSpinner = (Spinner) findViewById(R.id.spinner_user);
        gestureSpinner = (Spinner) findViewById(R.id.spinner_gesture);

        ArrayList<String> user_list = new ArrayList<String>();
        user_list.add("XUE");
        user_list.add("WANG");
        user_list.add("MLI");
        user_list.add("CAI");
        user_list.add("WLI");
        user_list.add("ZHANG");

        ArrayAdapter userAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, user_list);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(userAdapter);
        ArrayList<String> gesture_list = new ArrayList<String>();
        for (int i = 1; i <= 15; i++) {
            gesture_list.add(String.valueOf((char) (i + 64)));
        }
        ArrayAdapter gestureAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gesture_list);
        gestureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gestureSpinner.setAdapter(gestureAdapter);

        tvDist = (TextView) findViewById(R.id.textView1);
        tvDist.setText(String.valueOf(0));
        tvDist2 = (TextView) findViewById(R.id.textView2);
        tvDist2.setText(String.valueOf(0));
        btnStopRecord.setEnabled(false);    //

        int minBufSize = recBufSize;      //0.1s
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, sampleRateInHz,
                channelConfig,
                encodingBitrate, minBufSize);
    }


    /**
     * 即时播放线程
     *
     * @author lisi
     */
    class ThreadInstantPlay extends Thread {
        @Override
        public void run() {
            FrequencyPlayer FPlay = new FrequencyPlayer(numfre, Freqarrary);
            FPlay.palyWaveZ();
            while (flag) {
            }
            FPlay.colseWaveZ();
        }
    }

    /**
     * 即时录音线程
     */
    class ThreadInstantRecord extends Thread {
        @Override
        public void run() {
            short[] bsRecord = new short[recBufSize];
            short[] bsRecordL = new short[recBufSize / 2];
            short[] bsRecordR = new short[recBufSize / 2];
            short[] BIGDATA = new short[44100 * 30];
            short[] BIGDATA2 = new short[44100 * 30];
            double[] BIGDATAIIL = new double[44100 * 30];//这边4个从1开始取 注意+1
            double[] BIGDATAQQL = new double[44100 * 30];
            double[] BIGDATAIIR = new double[44100 * 30];
            double[] BIGDATAQQR = new double[44100 * 30];
            //八个频率调整好的数据
            double[] needIL = new double[880 + 2];
            double[] needQL = new double[880 + 2];
            double[] needIR = new double[880 + 2];
            double[] needQR = new double[880 + 2];
            int n = 0;
            double totPhase = 0;
            double lastDist = 0;
            double lastDistR = 0;
            double NowPhase = 0;
            //--------------jni------------------------
            DemoNew();

            while (flag == false) {
            }
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                // 录音开始失败
                e.printStackTrace();
                return;
            }
            //Log.w("tip","start");
            int Len;
            while (flag)//大循环
            {
                Len = audioRecord.read(bsRecord, 0, recBufSize);//读取录音
                for (int i = 0; i < Len; i++) {
                    BIGDATA[n] = bsRecord[i];
                    bsRecordL[i / 2] = bsRecord[i++];
                    BIGDATA2[n++] = bsRecord[i];
                    bsRecordR[i / 2] = bsRecord[i];
                }
                double[] di = new double[110];
                //-----------------------你们需要的数据就是这个tempII 和tempQQ------------------------------------
                //-------------------------下面有保存方法saveToSDCard ，你们可以自己试着按照你们的需要保存----------------------------------
                double[] tempIIL = new double[880];
                double[] tempQQL = new double[880];
                DemoL(bsRecordL, di, tempIIL, tempQQL);
                lastDist = di[110 - 1];

                double[] tempIIR = new double[880];
                double[] tempQQR = new double[880];
                DemoR(bsRecordR, di, tempIIR, tempQQR);
                int nn = 1;
                for (int i = 0; i < 880; i++, nn++) {   //为了让数据从1开始
                    BIGDATAIIL[nn] = tempIIL[i];
                    BIGDATAQQL[nn] = tempQQL[i];
                    BIGDATAIIR[nn] = tempIIR[i];
                    BIGDATAQQR[nn] = tempQQR[i];
                }
                //这边得用BIGDATAIIL的数据
                for (int i = 1; i <= 880; i++) {
                    int j = i / 8;//第几个8
                    int k = i % 8;   //除8的余数
                    if (k == 0) k = 8;//
                    needIL[(k - 1) * 110 + j + 1] = BIGDATAIIL[i];
                    needQL[(k - 1) * 110 + j + 1] = BIGDATAQQL[i];
                    needIR[(k - 1) * 110 + j + 1] = BIGDATAIIR[i];
                    needQR[(k - 1) * 110 + j + 1] = BIGDATAQQR[i];
                }
                for (int i = 880 - 110 + 1; i < 880; i++) {
                    needIL[i] = needIL[i + 1];
                    needQL[i] = needIL[i + 1];
                    needIR[i] = needIL[i + 1];
                    needQR[i] = needIL[i + 1];
                }
                needIL[880] = BIGDATAIIL[880];
                needQL[880] = BIGDATAQQL[880];
                needIR[880] = BIGDATAIIR[880];
                needQR[880] = BIGDATAQQR[880];

                lastDistR = di[110 - 1];
                NowPhase += totPhase / 2;
                while (NowPhase < 0) NowPhase += Math.PI * 2;
                while (NowPhase > Math.PI * 2) NowPhase -= Math.PI * 2;

                Message msg1 = new Message();
                msg1.what = 1;
                DecimalFormat df = new DecimalFormat("#.00");

                msg1.obj = (df.format(lastDist));
                mHandler.sendMessage(msg1);

                Message msg2 = new Message();
                msg2.what = 2;

                msg2.obj = (df.format(lastDistR));
                mHandler.sendMessage(msg2);
            }//while end
            audioRecord.stop();
            // Log.w("tip","stop");
            try {
                //long a =System.currentTimeMillis();

                //这边还没调试，出错的话可能在这
                Calendar c = Calendar.getInstance();
                int mDay = c.get(Calendar.DAY_OF_MONTH);//日期
                int mHour = c.get(Calendar.HOUR_OF_DAY);//时
                int mMinute = c.get(Calendar.MINUTE);//分
                int mSecond = c.get(Calendar.SECOND);//秒

                // saveToSDCard("001"+a+".txt",BIGDATA,n);
                //saveToSDCard("002"+a+".txt",BIGDATA2,n);
                String user = (String) userSpinner.getSelectedItem();
                String gesture = (String) gestureSpinner.getSelectedItem();
                String channel = "Left";
                String cnt = String.valueOf(count);
                String filePath = Environment.getExternalStorageDirectory() + "/ASSET" + "/" + user + "/" + gesture + "/";
                String temp = user + "_" + gesture + "_";
                //还没与学长原本的波形比较
                SaveFile saveFile1=new SaveFile(temp + "I_" + mDay + mHour + mMinute + mSecond + ".txt", filePath + channel + "/", needIL, 880);
                SaveFile saveFile2=new SaveFile(temp + "Q_" + mDay + mHour + mMinute + mSecond + ".txt", filePath + channel + "/", needQL, 880);
                channel = "Right";
                SaveFile saveFile3=new SaveFile(temp + "I_" + mDay + mHour + mMinute + mSecond + ".txt", filePath + channel + "/", needIR, 880);
                SaveFile saveFile4=new SaveFile(temp + "Q_" + mDay + mHour + mMinute + mSecond + ".txt", filePath + channel + "/", needQR, 880);

                saveFile1.execute("");
                saveFile2.execute("");
                saveFile3.execute("");
                saveFile4.execute("");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    private void RequestPermission() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (PermissionsUtil.hasPermission(MainActivity.this, permissions)) {
            //已经获取相关权限
        } else {
            PermissionsUtil.requestPermission(MainActivity.this, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permission) {
                //用户授予了权限
                }

                @Override
                public void permissionDenied(@NonNull String[] permission) {
                //用户拒绝了权限
                    Toast.makeText(MainActivity.this, "相关权限被拒绝，本应用将无法正常运行", Toast.LENGTH_SHORT).show();
                }
            }, permissions);
        }
    }

    //本地方法，由java调用
    public native String stringFromJNI(int[] I);

    public native void mycicFromJNI(int[] I, double[] II);

    public native void myADistFromJNI(double[] inC, double[] inS, double[] RE);

    public native void DemoNew();

    public native int DemoL(short[] Record, double[] DIST, double[] tempII, double[] tempQQ);

    public native int DemoR(short[] Record, double[] DIST, double[] tempII, double[] tempQQ);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
}