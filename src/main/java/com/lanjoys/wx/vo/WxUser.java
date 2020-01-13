package com.lanjoys.wx.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class WxUser extends Errorable {
    private int subscribe;
    private String openid;
    private String nickname;
    private int sex;
    private String city;
    private String country;
    private String province;
    private String language;
    private String headimgurl;
    private long subscribe_time;
    private String unionid;
    private String remark;
    private int groupid;
    private List<Integer> tagid_list;
    private String subscribe_scene;
    private String qr_scene;
    private String qr_scene_str;

    public boolean isSubscribe() {
        return subscribe == 1;
    }
}
