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
        value = "AS:VideoMsg",
        flag = MessageTag.ISCOUNTED
)
public class VideoMessage extends MessageContent {
    private static final String TAG = "VideoMessage";
    private String content;
    public static final Creator<VideoMessage> CREATOR = new Creator<VideoMessage>() {
        public VideoMessage createFromParcel(Parcel source) {
            return new VideoMessage(source);
        }

        public VideoMessage[] newArray(int size) {
            return new VideoMessage[size];
        }
    };

    protected VideoMessage() {
    }

    public VideoMessage(String content) {
        this.setContent(content);
    }

    public static VideoMessage obtain(String text) {
        VideoMessage model = new VideoMessage();
        model.setContent(text);
        return model;
    }

    public byte[] encode() {
        JSONObject jsonObj = super.getBaseJsonObject();

        try {
            if (TextUtils.isEmpty(this.getContent())) {
                this.content = "";
            }

            jsonObj.put("content", this.getEmotion(this.getContent()));
        } catch (JSONException var4) {
            JSONException e = var4;
            RLog.e("VideoMessage", "JSONException " + e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            UnsupportedEncodingException e = var3;
            RLog.e("VideoMessage", "UnsupportedEncodingException ", e);
            return null;
        }
    }

    public VideoMessage(byte[] data) {
        if (data == null) {
            RLog.e("VideoMessage", "data is null ");
        } else {
            String jsonStr = null;

            try {
                if (data.length >= 40960) {
                    RLog.e("VideoMessage", "VideoMessage length is larger than 40KB, length :" + data.length);
                }

                jsonStr = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException var5) {
                UnsupportedEncodingException e = var5;
                RLog.e("VideoMessage", "UnsupportedEncodingException ", e);
            }

            if (jsonStr == null) {
                RLog.e("VideoMessage", "jsonStr is null ");
            } else {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    super.parseBaseJsonObject(jsonObj);
                    if (jsonObj.has("content")) {
                        this.setContent(jsonObj.optString("content"));
                    }
                } catch (JSONException var4) {
                    JSONException e = var4;
                    RLog.e("VideoMessage", "JSONException " + e.getMessage());
                }

            }
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToBaseInfoParcel(dest);
        ParcelUtils.writeToParcel(dest, this.content);
    }

    public VideoMessage(Parcel in) {
        super.readFromBaseInfoParcel(in);
        this.setContent(ParcelUtils.readFromParcel(in));
    }

    public int describeContents() {
        return 0;
    }

    public List<String> getSearchableWord() {
        List<String> words = new ArrayList();
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

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public String toString() {
        return "VideoMessage{content='" + this.content + '\'' + ", extra='" + this.getExtra() + '\'' + '}';
    }
}