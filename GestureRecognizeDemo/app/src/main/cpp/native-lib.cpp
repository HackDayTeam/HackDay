#include <jni.h>
#include <string>
#include <fcntl.h>
#include <android/log.h>

#include "mycic/MyCic.h"
#include "sup/support.cpp"

#define TAG    "myjni-test" // 这个是自定义的LOG的标识
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型

extern "C"
jstring
Java_com_lee_edu_mydemo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */,
        jintArray a) {
    jint *b =env->GetIntArrayElements(a,0);
    b[0]=1;
    b[1]=1;
    b[2]=1;
    env->ReleaseIntArrayElements(a,b,0);
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}




extern "C"
JNIEXPORT void
Java_com_lee_edu_mydemo_MainActivity_mycicFromJNI(
        JNIEnv *env,
        jobject /* this */,
        jintArray I,
        jdoubleArray II) {
    jint  * I1 = (env)->GetIntArrayElements(I,0);
    jdouble *II1 = (env)->GetDoubleArrayElements(II,0);
    long long II2[165];

    MyCic(I1,II2);
    for(int i =0;i<165;i++)
        II1[i]=(double)II2[i];

    (env)->ReleaseIntArrayElements(I,I1,0);
    (env)->ReleaseDoubleArrayElements(II,II1,0);
}

extern "C"
JNIEXPORT void
Java_com_lee_edu_mydemo_MainActivity_myADistFromJNI(
                JNIEnv *env,
                jobject /* this */,
                jdoubleArray inC,
                jdoubleArray inS,
                jdoubleArray RE) {
    jdouble *Ori_0 = (env)->GetDoubleArrayElements(inC,0);
    jdouble *Ori_1 = (env)->GetDoubleArrayElements(inS,0);
    jdouble *Ori_2 = (env)->GetDoubleArrayElements(RE,0);
    ADist(Ori_0,Ori_1,Ori_2);
    (env)->ReleaseDoubleArrayElements(inC ,Ori_0,0);
    (env)->ReleaseDoubleArrayElements(inS,Ori_1,0);
    (env)->ReleaseDoubleArrayElements(RE,Ori_2,0);
}

newdemo *demo_data = NULL;

extern "C"
JNIEXPORT void
Java_com_lee_edu_mydemo_MainActivity_DemoNew(
        JNIEnv *env,
        jobject /* this */
) {
    if(demo_data != NULL)
        delete demo_data;
    demo_data = new newdemo();
}

extern "C"
JNIEXPORT jdouble
Java_com_lee_edu_mydemo_MainActivity_DemoL(
        JNIEnv *env,
        jobject /* this */,
        jshortArray BUFF,
        jdoubleArray REDist ,
        jdoubleArray tII,
        jdoubleArray tQQ
) {
    jshort* Buff = (env)->GetShortArrayElements(BUFF,0);
    jdouble *O_dist = (env)->GetDoubleArrayElements(REDist,0);
    jdouble *tempII = (env)->GetDoubleArrayElements(tII,0);
    jdouble *tempQQ = (env)->GetDoubleArrayElements(tQQ,0);
    double RE = -1;


    short *coL = new short[6600];
    if(demo_data->now>3) {//过滤每次录音前0.3
        memcpy(coL,demo_data->lastRecordL,2200*sizeof(short));        //上一切片
        memcpy(coL+2200,Buff,4400*sizeof(short));        //当前切片
    }
    memcpy(demo_data->lastRecordL,Buff+2200,2200*sizeof(short));   //为下一窗口保留  recBufSize/2 - LastLength = 2200

    if(demo_data->now>4) {
        demo(coL, demo_data->now, demo_data->lastL, demo_data->levdIL, demo_data->levdQL,O_dist,tempII,tempQQ);

        RE = 1;
    }
//    demo_data->now++;         //后调用的负责加一

    (env)->ReleaseDoubleArrayElements(tQQ ,tempQQ,0);
    (env)->ReleaseDoubleArrayElements(tII ,tempII,0);
    (env)->ReleaseShortArrayElements(BUFF,Buff,0);
    (env)->ReleaseDoubleArrayElements(REDist,O_dist,0);

    return RE;

}

extern "C"
JNIEXPORT jdouble
Java_com_lee_edu_mydemo_MainActivity_DemoR(
        JNIEnv *env,
        jobject /* this */,
        jshortArray BUFF,
        jdoubleArray REDist ,
        jdoubleArray tII,
        jdoubleArray tQQ
) {
    jshort* Buff = (env)->GetShortArrayElements(BUFF,0);
    jdouble *O_dist = (env)->GetDoubleArrayElements(REDist,0);
    jdouble *tempII = (env)->GetDoubleArrayElements(tII,0);
    jdouble *tempQQ = (env)->GetDoubleArrayElements(tQQ,0);

    double RE = -1;

    short *coR = new short[6600];
    if(demo_data->now>3) {//过滤每次录音前0.3
        memcpy(coR,demo_data->lastRecordR,2200*sizeof(short));        //上一切片
        memcpy(coR+2200,Buff,4400*sizeof(short));        //当前切片
    }
    memcpy(demo_data->lastRecordR,Buff+2200,2200*sizeof(short));   //为下一窗口保留  recBufSize/2 - LastLength = 2200

    if(demo_data->now>4) {

        demo(coR, demo_data->now, demo_data->lastR, demo_data->levdIR, demo_data->levdQR,O_dist,tempII,tempQQ);

//        LOGW("temp%lf,%lf",tempII[0],tempII[11]);
//        LOGW("temp%lf,%lf",tempQQ[0],tempQQ[11]);
        RE = 1;
    }
    demo_data->now++;         //后调用的负责加一

    (env)->ReleaseDoubleArrayElements(tQQ ,tempQQ,0);
    (env)->ReleaseDoubleArrayElements(tII ,tempII,0);
    (env)->ReleaseShortArrayElements(BUFF,Buff,0);
    (env)->ReleaseDoubleArrayElements(REDist,O_dist,0);

    return RE;
}