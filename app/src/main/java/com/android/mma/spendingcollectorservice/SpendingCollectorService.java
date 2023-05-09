// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.android.mma.spendingcollectorservice;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;
import java.util.TimeZone;

public class SpendingCollectorService extends AccessibilityService {

    @Override
    protected void onServiceConnected(){

    }

    private void logStructure(AccessibilityNodeInfo root){
        File logFile;
        Long tsLong = System.currentTimeMillis();
        String ts = formatDate(tsLong);
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        CharSequence packageName = root.getPackageName();
        if((packageName != null) && packageName.equals("com.tencent.mm")){
            logFile = getLogFile("wechat.txt");
            if(logFile == null){
                return;
            }
            writeToFile(logFile,ts + " : READ NEW SCREEN CONTENT");
        }
        else if((packageName != null) && packageName.equals("com.eg.android.AlipayGphone")){
            logFile = getLogFile("alipay.txt");
            if(logFile == null){
                return;
            }
            writeToFile(logFile,ts + " : READ NEW SCREEN CONTENT");
        }else {
            return;
        }
        while(!deque.isEmpty()){
            AccessibilityNodeInfo node = deque.removeFirst();
            CharSequence text = node.getText();
            if(text != null){
                writeToFile(logFile,ts + " : TEXT / " + text.toString());
                Log.w("log","TEXT : " + text.toString());
                CharSequence className = node.getClassName();
                if(className != null){
                    writeToFile(logFile,ts +" : CLASSNAME / " + className.toString());
                }
                Rect outbounds = new Rect();
                node.getBoundsInScreen(outbounds);
                writeToFile(logFile,ts +" : POSITION / " + outbounds.toString());
                CharSequence contentDescr = node.getContentDescription();
                if(contentDescr != null){
                    writeToFile(logFile, ts + " : CONTENT DESCRIPTION / " + contentDescr.toString());
                }

            }
            for (int i = 0; i < node.getChildCount(); i++){
                AccessibilityNodeInfo child = node.getChild(i);
                if(child != null){
                    deque.addLast(child);
                }
            }
        }
    }

    private void logWindowContent(AccessibilityNodeInfo root) {
        CharSequence packageName = root.getPackageName();
        File logFile = getLogFile("spendingLogs.txt");
        Long tsLong = System.currentTimeMillis();
        String ts = formatDate(tsLong);
        if((packageName != null) && packageName.equals("com.eg.android.AlipayGphone")){
            //writeToFile(logFile,ts + "// trying to find a match");
            String amount = PaymentTemplate.canMapToAlipay1(root,logFile);
            if(amount != null && logFile != null){
                writeToFile(logFile,ts + " - Alipay " + amount);
            }else{
                String[] amountAndMerchant = PaymentTemplate.canMapToAlipay2(root,logFile);
                if(amountAndMerchant != null){
                    writeToFile(logFile,ts + " " + amountAndMerchant[1] + " Alipay " + amountAndMerchant[0]);
                }
            }
        }else if((packageName != null) && packageName.equals("com.tencent.mm")){
            //writeToFile(logFile,ts + "// trying to find a match");
            String[] merchantAndAmount = PaymentTemplate.canMapToWechat(root,logFile);
            if(merchantAndAmount != null){
                writeToFile(logFile,ts + " " + merchantAndAmount[0] + " Wechat " + merchantAndAmount[1]);
            }else {
                String[] amountAndMerchant = PaymentTemplate.canMapToWechat2(root,logFile);
                if(amountAndMerchant != null){
                    writeToFile(logFile,ts + " " + amountAndMerchant[1] + " Wechat " + amountAndMerchant[0]);
                }
            }
        }
    }

    private File getLogFile(String filename){
        if(isExternalStorageWritable()){
            File file = new File(getExternalFilesDir(null),filename);
            if(!file.exists()){
                file.getParentFile().mkdirs();
            }
            return file;
        }
        return null;
    }

    private void writeToFile(File file,String data){
        String separator = System.getProperty("line.separator");
        try {
            file.createNewFile();
            OutputStream fout = new FileOutputStream(file,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fout);
            myOutWriter.append(data);
            myOutWriter.append(separator);
            myOutWriter.close();
            fout.flush();
            fout.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            return true;
        }
        return false;
    }

    private String formatDate(long milliseconds){
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+8"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if(eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if(root != null){
                logWindowContent(root);
                logStructure(root);
            }
        }
    }


    @Override
    public void onInterrupt() {

    }
}
