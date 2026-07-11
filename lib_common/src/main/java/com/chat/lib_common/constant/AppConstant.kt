package com.chat.lib_common.constant

import com.chat.lib_common.util.getCache

object AppConstant {

    object ClientInfo {


        const val TAG_LOG = "httpLog"

        const val BASE_DEV_URL = "http://192.168.8.182:7777/"

        const val BASE_DEV_URL_183 = "http://192.168.8.183:7777/"

        const val BASE_TEST_URL = "http://testapi.vila-app.com/"

        const val BASE_REAL_URL = "https://api.jolt-chat.com/"


        const val BASE_PRIVACY_POLICY_URL = "https://www.jolt-chat.com/joit_privacy_policy.html"

        const val BASE_SERVICE_POLICY_URL = "https://www.jolt-chat.com/joit_service_policy.html"




        //google 开关 上线改成 true
        const val OPEN_GOOGLE = false

//        const val BASE_DEFAULT_URL = BASE_REAL_URL
        const val BASE_DEFAULT_URL = BASE_TEST_URL
//        const val BASE_DEFAULT_URL = BASE_DEV_URL
//        const val BASE_DEFAULT_URL = BASE_DEV_URL_183

        val BASE_URL = getCache(Constant.URL,BASE_DEFAULT_URL)


        const val BASE_IMAGE_URL = "https://cdn.jolt-chat.com/"


        const val CONNECT_TIMEOUT = 60*1000L

        const val READ_TIMEOUT = 60*1000L

        const val WRITE_TIMEOUT = 60*1000L


    }

    object Constant {

        var isShowBuy = false

        var ppvsEnable = false

        var threePay = false


        const val PPV_BLUR_RADIUS = 50

        const val PPV_BLUR_VIDEO_RADIUS = 20

        const val PAGE_SIZE_COUNT = 20

        var MEDIA_ENABLE_TIME = 1000 * 60 * 30 + 1000

        var LAST_OPEN_VIP_TIME = System.currentTimeMillis()

        var LAST_OPEN_VIP_COUNT = 0

        const val IS_LOGIN = "isLogin"

        const val TOKEN = "token"

        const val UPDATE = "update"

        const val HAS_SWIP = "hasSwip"

        const val TARGET_ID = "targetId"

        const val EXTRA_DATA = "extra_data"

        const val IS_CAN_SEND = "isCanSend"

        const val TOUCH_TYPE = "touchType"

        const val IS_THREE_PAY = "threePay"


        const val PUSH_MARK = "pushMark"

        const val IS_OFFLINE = "is_offline"

        const val PUSH_EXTRA = "pushExtra"

        const val IS_NOTICE_INTO = "is_notice_into"

        const val IS_HAS_LOCATION = "is_has_location"

        const val IS_HAS_NOTICE = "is_has_notice"

        const val PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbEGPNvCNLIrOa/HuxBy4HpugfwqT4S0pq+Sd4SVDA4C5LAr3qlBXks/TRWbq557SdACURIC1wLL1kvhkCtt8gqKU1jwgu45ZG5zCiKHchLJJSJKd1bFGiBaPhsytaWPeuoNfAMFBNQs17LUjpe5qq1qtOVvOhxUib2jrOOxp96QIDAQAB"

        const val LOGIN_TYPE = "loginType"

        const val EMAIL = "Email"

        const val GOOGLE = "Google"

        const val URL = "url"

        const val TITLE = "title"

        const val PAGE = "page"

        const val DATA = "data"

        const val MODEL_DATA = "model_data"

        const val PRIVILEGE_DATA = "privilege_data"

        const val HAS_SHOW_LIMIT = "has_show_limit"

        const val IS_ME = "isMe"

        const val IS_PRIVATE = "isPrivate"

        const val POSITION = "position"

        const val TYPE = "type"

        const val ID = "id"

        const val IS_TURN_ONS = "is_turn_ons"

        const val SHOW_FLASH = "show_flash"

        const val USER_INFO = "user_info"

        const val HOBBY_TAG = "hobby_tag"

        const val TURN_TAG = "turn_tag"

        const val PROFESSION = "profession"

        const val SOCIAL_AIM = "SocialAim"

        const val IS_SUBSCRIPTION = "is_subscription"

        const val VIP_CACHE = "vip_cache"

        const val IS_VPN = "is_vpn"

        const val IS_REVIEW_VERSION = "reviewVersion"

        const val NOTICE_CLICK = "notice_click"

        const val NOTICE_TYPE = "notice_type"

        const val BIZ_ID = "bizId"

        const val LOGIN_NOTICE = "login_notice"

        const val HAS_MESSAGE_NOTICE = "has_message_notice"

        const val LOCATION_NOTICE = "location_notice"

        const val START_NOTICE = "start_notice"

        const val HAS_EXPOSURE = "has_exposure"

        const val PAY_FLASH_CHAT = "FlashChat"

        const val PAY_VIP = "Vip"

        const val PAY_PRIVATE_PHOTO = "PrivatePhoto"

        const val PAY_PRIVATE_VIDEO = "PrivateVideo"



    }

    object RIMConstant {

        const val RIM_TOKEN = "rim_token"

        const val APP_DEV_KEY = "25wehl3u243nw"
        //
        const val APP_TEST_KEY = "8luwapkv87ycl"

        const val APP_REAL_KEY = "8brlm7uf8y5g3"

        const val SYSTEM_NOTICE = "as-system"

        const val RC_TXT_MSG = "RC:TxtMsg"

        const val RC_IMG_MSG = "RC:ImgMsg"

        const val RC_IMG_VIDEO = "AS:VideoMsg"

        const val RC_NTF_MSG = "RC:InfoNtf"

        const val RC_CMD_MSG = "RC:CmdMsg"


        const val RC_PP_VM_MSG = "AS:PPVMsg"

        const val RC_TURN_ONS_MSG = "AS:TurnOns"

        const val RC_LIMIT_MESSAGE_MSG = "AS:LimitMessage"


        const val RC_SEND_TEXT_MSG = "Text"

        const val RC_SEND_PUBLIC_IMAGE_MSG = "Pic"

        const val RC_SEND_PRIVATE_IMAGE_S_MSG = "PPics"

        const val RC_SEND_PRIVATE_IMAGE_MSG = "PPic"

        const val RC_SEND_PRIVATE_VIDEO_MSG = "PVideo"


        const val RC_SEND_PRIVATE_VIDEO_S_MSG = "PVideos"

        const val CMD_MATCH_SUCCESS = "MatchSuccess"

        const val CMD_NEW_VISITOR = "NewVisitor"

        const val CMD_NEW_WHO_LIKE_ME = "NewWhoLikeMe"

        const val CMD_FLASH_CHAT = "FlashChat"
    }

    object EventConstant{


        const val REGISTER_MESSAGE = "register_message"

        const val CLEAR_READ_MESSAGE = "clear_read_message"

        const val CLEAR_BLACK_READ_MESSAGE = "clear_black_read_message"

        const val EVENT_TO_TOP_MESSAGE = "event_to_top_message"

        const val EVENT_REFRESH_MATCH_MESSAGE_ITEM = "event_refresh_match_message_item"

        const val EVENT_REFRESH_MESSAGE_LIST = "event_refresh_message_list"

        const val EVENT_REFRESH_WLM_LIST = "event_refresh_wlm_list"

        const val EVENT_REFRESH_CARD_LIST = "event_refresh_card_list"

        const val EVENT_REFRESH_LIKE_AND_VISITOR = "event_refresh_like_and_visitor"

        const val EVENT_OPERATION_CARD = "event_operation_card"

        const val EVENT_OPERATION_I_LIKE = "event_operation_i_like"

        const val EVENT_OPERATION_VISITOR = "event_operation_visitor"

        const val EVENT_IS_BUY_GET_USER_INFO = "event_is_buy_get_user_info"


        const val APP_ENTERED_FOREGROUND = " app_entered_foreground"

        const val APP_INIT_ADJUST= " app_init_adjust"


        const val EVENT_FINISH = " event_finish"

        const val EVENT_SET_PAGE = " event_set_page"

        const val EVENT_SHOW_SWIP_GUIDE = " event_show_swip_guide"

        const val EVENT_SET_LIKE_PAGE = " event_set_like_page"

        const val EVENT_SWIP_PAGE = " event_swip_page"

        const val EVENT_GET_UNREAD_COUNT = "event_get_unread_count"

        const val EVENT_GET_UNREAD_NOTICE_COUNT = "event_get_unread_notice_count"

        const val EVENT_BLOCK_USER = "event_block_user"

        const val EVENT_LOGIN_OUT = "event_login_out"

        const val EVENT_UPDATE_USER_INFO = " event_update_user_info"


    }




    object NoticeChannel{

        const val MESSAGE_CHANNEL_ID = "message_channel_id"

        const val MESSAGE_NOTICE = "MessageNotice"

    }
}