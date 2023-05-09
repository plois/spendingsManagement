package com.android.mma.spendingcollectorservice;

import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;

public class PaymentTemplate {
    private static String[] alipayTemplate1 = {"Done","支付成功","","Payment Method"};
    private static String[] alipayTemplate2 = {"Done","支付成功","","Recipient","","Order Amount"};
    private static String[] wechatTemplate1 = {"Payment successful","","¥","","Done"};
    private static String[] wechatTemplate2 = {"Success","¥","","Payee","","Done"};

    //returns the amount payed and null otherwise
    public static String canMapToAlipay1(AccessibilityNodeInfo root, File logFile){
        //writeToFile(logFile,"starting mapping with alipay1");
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.addLast(root);
        String amount = "";
        int index = 0;
        while(!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            CharSequence text = node.getText();
            CharSequence className = node.getClassName();
            if(text != null && className != null && alipayTemplate1[index].equals("") && className.toString().equals("android.widget.TextView")){
                amount = text.toString();
                index += 1;
                //writeToFile(logFile,"found match for " + text.toString());
            }else if (text != null && className != null && text.toString().equals(alipayTemplate1[index]) && className.toString().equals("android.widget.TextView")) {
                //writeToFile(logFile,"found match for " + alipayTemplate1[index]);
                index += 1;
                if(index == alipayTemplate1.length){
                    return amount;
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    deque.addLast(child);
                }
            }
        }
        return null;
    }

    //returns the amount payed and the recipient and null otherwise
    public static String[] canMapToAlipay2(AccessibilityNodeInfo root, File logFile){
        //writeToFile(logFile,"starting mapping with alipay2");
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.addLast(root);
        int index = 0;
        String[] amountAndMerchant = new String[2];
        while(!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            CharSequence text = node.getText();
            CharSequence className = node.getClassName();
            if(text != null && className != null && (index == 2) && className.toString().equals("android.widget.TextView")){
                amountAndMerchant[0] = text.toString();
                index += 1;
                //writeToFile(logFile,"found match for " + text.toString());
            }else if(text != null && className != null && (index == 4) && className.toString().equals("android.widget.TextView")){
                amountAndMerchant[1] = text.toString();
                index += 1;
                //writeToFile(logFile,"found match for " + text.toString());
            }else if (text != null && className != null && text.toString().equals(alipayTemplate2[index]) && className.toString().equals("android.widget.TextView")) {
                //writeToFile(logFile,"found match for " + alipayTemplate2[index]);
                index += 1;
                if(index == alipayTemplate2.length){
                    return amountAndMerchant;
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    deque.addLast(child);
                }
            }
        }
        return null;
    }

    //returns the amount payed and the recipient and null otherwise
    public static String[] canMapToWechat(AccessibilityNodeInfo root, File logFile){
        //writeToFile(logFile,"starting mapping with wechat");
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.addLast(root);
        int index = 0;
        String[] merchantAndAmount = new String[2];
        while(!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            CharSequence text = node.getText();
            CharSequence className = node.getClassName();
            if(text != null && className != null && (index == 1) && className.toString().equals("android.widget.TextView")){
                merchantAndAmount[0] = text.toString();
                index += 1;
                //writeToFile(logFile,"found match for " + text.toString());
            }else if(text != null && className != null && (index == 3) && className.toString().equals("android.widget.TextView")){
                merchantAndAmount[1] = text.toString();
                index += 1;
                //writeToFile(logFile,"found match for " + text.toString());
            }else if (text != null && className != null && text.toString().equals(wechatTemplate1[index]) && className.toString().equals("android.widget.TextView")) {
                //writeToFile(logFile,"found match for " + wechatTemplate1[index]);
                index += 1;
                if(index == wechatTemplate1.length){
                    return merchantAndAmount;
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    deque.addLast(child);
                }
            }
        }
        return null;
    }

    //returns the amount payed and the recipient and null otherwise
    public static String[] canMapToWechat2(AccessibilityNodeInfo root, File logFile){
        //writeToFile(logFile,"starting mapping with wechat");
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.addLast(root);
        int index = 0;
        String[] amountAndMerchant = new String[2];
        while(!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            CharSequence text = node.getText();
            CharSequence className = node.getClassName();
            if(text != null && className != null && (index == 2) && className.toString().equals("android.widget.TextView")){
                amountAndMerchant[0] = text.toString();
                index += 1;
                //writeToFile(logFile,"found match for " + text.toString());
            }else if(text != null && className != null && (index == 4) && className.toString().equals("android.widget.TextView")){
                amountAndMerchant[1] = text.toString();
                index += 1;
                //writeToFile(logFile,"found match for " + text.toString());
            }else if (text != null && className != null && text.toString().equals(wechatTemplate2[index]) && className.toString().equals("android.widget.TextView")) {
                //writeToFile(logFile,"found match for " + wechatTemplate1[index]);
                index += 1;
                if(index == wechatTemplate2.length){
                    return amountAndMerchant;
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    deque.addLast(child);
                }
            }
        }
        return null;
    }

    private static void writeToFile(File file,String data){
        String separator = System.getProperty("line.separator");
        try {
            boolean created = file.createNewFile();
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

}
