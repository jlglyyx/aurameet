package com.chat.lib_common.im.message;

import android.os.Parcel;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rong.common.ParcelUtils;
import io.rong.common.rlog.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

@MessageTag(
        value = "AS:PPVMsg",
        flag = MessageTag.ISCOUNTED
)
public class PPVMessage extends MessageContent {
    private static final String TAG = "PPVMessage";
    // 私密照片集: PPics 私密视频集: PVideos
    private String type;
    // [{"albumId":"", "albumUrl":"", "cover":"", "duration":0}]
    private String content;
    public static final Creator<PPVMessage> CREATOR = new Creator<PPVMessage>() {
        public PPVMessage createFromParcel(Parcel source) {
            return new PPVMessage(source);
        }

        public PPVMessage[] newArray(int size) {
            return new PPVMessage[size];
        }
    };

    protected PPVMessage() {
    }

    public PPVMessage(String type, String content) {
        this.setType(type);
        this.setContent(content);
    }

    public static PPVMessage obtain(String type, String content) {
        PPVMessage model = new PPVMessage();
        model.setType(type);
        model.setContent(content);
        return model;
    }

    public byte[] encode() {
        JSONObject jsonObj = super.getBaseJsonObject();

        try {
            if (TextUtils.isEmpty(this.getType())) {
                this.type = "";
            }
            if (TextUtils.isEmpty(this.getContent())) {
                this.content = "";
            }

            jsonObj.put("type", this.getEmotion(this.getType()));
            jsonObj.put("content", this.getEmotion(this.getContent()));
        } catch (JSONException var4) {
            JSONException e = var4;
            RLog.e("PPVMessage", "JSONException " + e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            UnsupportedEncodingException e = var3;
            RLog.e("PPVMessage", "UnsupportedEncodingException ", e);
            return null;
        }
    }

    public PPVMessage(byte[] data) {
        if (data == null) {
            RLog.e("PPVMessage", "data is null ");
        } else {
            String jsonStr = null;

            try {
                if (data.length >= 40960) {
                    RLog.e("PPVMessage", "PPVMessage length is larger than 40KB, length :" + data.length);
                }

                jsonStr = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException var5) {
                UnsupportedEncodingException e = var5;
                RLog.e("PPVMessage", "UnsupportedEncodingException ", e);
            }

            if (jsonStr == null) {
                RLog.e("PPVMessage", "jsonStr is null ");
            } else {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    super.parseBaseJsonObject(jsonObj);
                    if (jsonObj.has("type")) {
                        this.setType(jsonObj.optString("type"));
                    }
                    if (jsonObj.has("content")) {
                        this.setContent(jsonObj.optString("content"));
                    }
                } catch (JSONException var4) {
                    JSONException e = var4;
                    RLog.e("PPVMessage", "JSONException " + e.getMessage());
                }

            }
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToBaseInfoParcel(dest);
        ParcelUtils.writeToParcel(dest, this.type);
        ParcelUtils.writeToParcel(dest, this.content);
    }

    public PPVMessage(Parcel in) {
        super.readFromBaseInfoParcel(in);
        this.setType(ParcelUtils.readFromParcel(in));
        this.setContent(ParcelUtils.readFromParcel(in));
    }

    public int describeContents() {
        return 0;
    }

    public List<String> getSearchableWord() {
        List<String> words = new ArrayList();
        words.add(this.type);
        words.add(this.content);
        return words;
    }

    private String getEmotion(String content) {
        Pattern pattern = Pattern.compile("\\[/u([0-9A-Fa-f]+)\\]");
        Matcher matcher = pattern.matcher(content);

        StringBuffer sb;
        int inthex;
        for (sb = new StringBuffer(); matcher.find(); matcher.appendReplacement(sb, String.valueOf(Character.toChars(inthex)))) {
            String matchStr = matcher.group(1);
            inthex = 0;
            if (matchStr != null) {
                inthex = Integer.parseInt(matchStr, 16);
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public String toString() {
        return "PPVMessage{type='" + this.type + '\'' + ", content='" + this.content + '\'' + ", extra='" + this.getExtra() + '\'' + '}';
    }
}