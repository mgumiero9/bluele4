package com.example.driver;

import android.util.Log;
import android.widget.TextView;

/**
 * Created by Gumiero on 29/09/2014.
 */
public class GenerateDeviceOnScreen {

    public boolean CSC_DEVICE_STATUS;
    public boolean RSC_DEVICE_STATUS;
    public boolean HEART_DEVICE_STATUS;
    public boolean TEMPERATURE;            /*(maybe not used)*/
    public boolean SCALE_DEVICE_STATUS;
    public boolean KOREX_DEVICE_STATUS;

    //  CSC - Bike
    public static String mCSC_CRANK_REV /*Giros Pedal*/;
    public static String mCSC_CRANK_SPPED /*Velocidade Pedal (rps)*/;
    public static String mCSC_WHEEL_REV /*Giros Roda*/;
    public static String mCSC_WHEEL_SPEED /*Velocidade Roda (rps)*/;

    //  RSC - QBand
    public static String mRSC_SPEED /*Velocidade Instântanea (m/s)*/;
    public static String mRSC_CADENCE /*Cadência (ciclos/min)*/;
    public static String mRSC_STRIDE_LEN /*Largura Passo (m)*/;
    public static String mRSC_TOTAL_LEN /*Distância Percorrida (m)*/;

    //  Heart - Heart Monitoring
    public static String mHEART_CONTACT /*Contato do Sensor*/;
    public static String mHEART_BEATS /*Batimento (bpm)*/;
    public static String mHEART_ENERGY /*Energia Gasta (J)*/;
    public static String mHEART_RR /*Intervalo RR (s)*/;

    //  Temperature - Health Temperature
    public static String mTEMP_CELSIUS /*Temperatura(oC)*/;
    public static String mTEMP_FAHREN /*Temperatura(F)*/;
    public static String mTEMP_TIME /*Temperatura Timestamp*/;
    public static String mTEMP_TYPE /*Temperatura Tipo*/;

    //  Scale - Weight Scale with Biometrics
    public static String mSCALE_GROUP /*Grupo*/;
    public static String mSCALE_CAT /*Categoria*/;
    public static String mSCALE_GEN /*Sexo*/;
    public static String mSCALE_AGE /*Idade*/;
    public static String mSCALE_HEIGHT /*Altura (cm)*/;
    public static String mSCALE_WEIGHT /*Peso (Kg)*/;
    public static String mSCALE_FAT /*Gordura (%)*/;
    public static String mSCALE_BONE /*Massa Óssea*/;
    public static String mSCALE_MUSCLE /*Massa Muscular*/;
    public static String mSCALE_VISC /*Nivel de Gordura Visceral*/;
    public static String mSCALE_WATER /*Indice de Água no Corpo*/;
    public static String mSCALE_CAL /*Calorias*/;

    //  Korex - Korex Band
    public static String mKOREX_DATE /*Relógio*/;
    public static String mKOREX_EXDATE /*Exercício Registrado*/;
    public static String mKOREX_STEP /*Passos*/;
    public static String mKOREX_STATE /*Estado*/;
    public static String mKOREX_CAL /*Calorias Gastas*/;

    public static String mBARO_PRESSURE /*Pressão Atmosférica (mbar)*/;
    public static String mBARO_ALTITUDE /*Altitude (m)*/;
    public static String mBARO_REFERENCE /*Pressão de Referência (mbar)*/;

    //  Other - Other related parameters
    public static String mBATTERY_STATE  /*Status da Bateria*/;

    public static TextView mBATTERY_STATE2;

    public static void populateDevParameters(String mKey, String mValue, String mAddress) {



        if (mKey.equals("Bateria")) {
            mBATTERY_STATE = mValue;
        }
        else if (mKey.equals("Giros Pedal")) {
            mCSC_CRANK_REV = mValue;
        }
        else if (mKey.equals("Velocidade Pedal (rps)")) {
            mCSC_CRANK_SPPED = mValue;
        }
        else if (mKey.equals("Giros Roda")) {
            mCSC_WHEEL_REV = mValue;
        }
        else if (mKey.equals("Velocidade Roda (rps)")) {
            mCSC_WHEEL_SPEED = mValue;
        }
        else if (mKey.equals(R.string.RSC_SPEED)) {
            mRSC_SPEED = mValue;
        }
        else if (mKey.equals(R.string.RSC_CADENCE)) {
            mRSC_CADENCE = mValue;
        }
        else if (mKey.equals(R.string.RSC_STRIDE_LEN)) {
            mRSC_STRIDE_LEN = mValue;
        }
        else if (mKey.equals(R.string.RSC_TOTAL_LEN)) {
            mRSC_TOTAL_LEN = mValue;
        }
        else if (mKey.equals(R.string.HEART_CONTACT)) {
            mHEART_CONTACT = mValue;
        }
        else if (mKey.equals(R.string.HEART_BEATS)) {
            mHEART_BEATS = mValue;
        }
        else if (mKey.equals(R.string.HEART_ENERGY)) {
            mHEART_ENERGY = mValue;
        }
        else if (mKey.equals(R.string.HEART_RR)) {
            mHEART_RR = mValue;
        }
        else if (mKey.equals(R.string.TEMP_CELSIUS)) {
            mTEMP_CELSIUS = mValue;
        }
        else if (mKey.equals(R.string.TEMP_FAHREN)) {
            mTEMP_FAHREN = mValue;
        }
        else if (mKey.equals(R.string.TEMP_TIME)) {
            mTEMP_TIME = mValue;
        }
        else if (mKey.equals(R.string.TEMP_TYPE)) {
            mTEMP_TYPE = mValue;
        }
        else if (mKey.equals(R.string.SCALE_GROUP)) {
            mSCALE_GROUP = mValue;
        }
        else if (mKey.equals(R.string.SCALE_CAT)) {
            mSCALE_CAT = mValue;
        }
        else if (mKey.equals(R.string.SCALE_GEN)) {
            mSCALE_GEN = mValue;
        }
        else if (mKey.equals(R.string.SCALE_AGE)) {
            mSCALE_AGE = mValue;
        }
        else if (mKey.equals(R.string.SCALE_HEIGHT)) {
            mSCALE_HEIGHT = mValue;
        }
        else if (mKey.equals(R.string.SCALE_WEIGHT)) {
            mSCALE_WEIGHT = mValue;
        }
        else if (mKey.equals(R.string.SCALE_FAT)) {
            mSCALE_FAT = mValue;
        }
        else if (mKey.equals(R.string.SCALE_BONE)) {
            mSCALE_BONE = mValue;
        }
        else if (mKey.equals(R.string.SCALE_MUSCLE)) {
            mSCALE_MUSCLE = mValue;
        }
        else if (mKey.equals(R.string.SCALE_VISC)) {
            mSCALE_VISC = mValue;
        }
        else if (mKey.equals(R.string.SCALE_WATER)) {
            mSCALE_WATER = mValue;
        }
        else if (mKey.equals(R.string.SCALE_CAL)) {
            mSCALE_CAL = mValue;
        }
        else if (mKey.equals(R.string.KOREX_DATE)) {
            mKOREX_DATE = mValue;
        }
        else if (mKey.equals(R.string.KOREX_EXDATE)) {
            mKOREX_EXDATE = mValue;
        }
        else if (mKey.equals(R.string.KOREX_STEP)) {
            mKOREX_STEP = mValue;
        }
        else if (mKey.equals(R.string.KOREX_STATE)) {
            mKOREX_STATE = mValue;
        }
        else if (mKey.equals(R.string.KOREX_CAL)) {
            mKOREX_CAL = mValue;
        }
        else if (mKey.equals(R.string.BARO_PRESSURE)) {
            mBARO_PRESSURE = mValue;
        }
        else if (mKey.equals(R.string.BARO_ALTITUDE)) {
            mBARO_ALTITUDE = mValue;
        }
        else if (mKey.equals(R.string.BARO_REFERENCE)) {
            mBARO_REFERENCE = mValue;
        }
        else {
            Log.e("ERROR", "Error on if chain, Devices");
        }

        Log.d("TEST TO STORE", mBATTERY_STATE + " / " + mCSC_CRANK_REV  + " / "
                + mCSC_CRANK_SPPED  + " / " + mCSC_WHEEL_REV + " / " + mCSC_WHEEL_SPEED
                + " / " + mKey + "ready to store");

    }
}
