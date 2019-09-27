package com.shanwtime.basicmq.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhouxiaoming on 2015/8/28.
 */
public class ParamsValid {

    private static final Pattern pattern = Pattern.compile("^\\d+(,\\d+)*");

    private int returnCode;

    private String message;

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ParamsValid validAppId(String appId) {
        if (returnCode != 0) {
            return this;
        }
        if (StringUtils.isBlank(appId)) {
            returnCode = ParamErrorType.MISS_APP_ID.getValue();
            message = ParamErrorType.MISS_APP_ID.getName();
        }
        return this;
    }

    public ParamsValid validNotNull(String paramName, Object paramValue) {
        if (returnCode != 0) {
            return this;
        }
        if (paramValue == null) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid validNotNullOrEmpty(String paramName, String paramValue) {
        if (returnCode != 0) {
            return this;
        }
        if (StringUtils.isBlank(paramValue)) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid validMoreThenZero(String paramName, Integer paramValue) {
        if (returnCode != 0) {
            return this;
        }
        if (paramValue == null || paramValue <= 0) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid validMoreThenZero(String paramName, Long paramValue) {
        if (returnCode != 0) {
            return this;
        }
        if (paramValue == null || paramValue <= 0) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid validEmptyCollection(String paramName, Collection collection) {
        if (returnCode != 0) {
            return this;
        }
        if (CollectionUtils.isEmpty(collection)) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid validSpecialChar(String paramName, String value) {
        if (returnCode != 0) {
            return this;
        }
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        boolean specialChar = StringHelper.isSpecialChar(value);
        if (specialChar) {
            returnCode = ParamErrorType.WRONG_FORMAT.getValue();
            message = ParamErrorType.WRONG_FORMAT.getName() + paramName;
        }
        return this;
    }


    public ParamsValid validListMoreThenZero(String paramName, String paramValue) {
        if (returnCode != 0) {
            return this;
        }
        if (StringUtils.isEmpty(paramValue)) {
            return this;
        }

        if (!pattern.matcher(paramValue).matches()) {
            returnCode = ParamErrorType.WRONG_FORMAT.getValue();
            message = ParamErrorType.WRONG_FORMAT.getName() + paramName;
            return this;
        }
        return this;
    }

    public ParamsValid validNotEmpty(String paramName, Collection<?> collection) {
        if (returnCode != 0) {
            return this;
        }
        if (collection == null || collection.isEmpty()) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid validMoreThenOrEqualsZero(String paramName, Integer paramValue) {
        if (returnCode != 0) {
            return this;
        }
        if (paramValue == null || paramValue < 0) {
            returnCode = ParamErrorType.MUST_MORE_THAN_OR_EQUALS_ZERO.getValue();
            message = ParamErrorType.MUST_MORE_THAN_OR_EQUALS_ZERO.getName() + paramName;
        }
        return this;
    }

    public ParamsValid valid(String paramName, Supplier<Boolean> booleanSupplier) {
        if (returnCode != 0) {
            return this;
        }
        if (!booleanSupplier.get()) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid valid(String paramName, Boolean status) {
        if (returnCode != 0) {
            return this;
        }
        if (!status) {
            returnCode = ParamErrorType.MISS_REQUIRE.getValue();
            message = ParamErrorType.MISS_REQUIRE.getName() + paramName;
        }
        return this;
    }

    public ParamsValid validTelNumber(String telNumber) {
        if (returnCode != 0) {
            return this;
        }
        final String regexMobile = "^1\\d{10}$";
        final String regexPhone = "^((0\\d{2,3})-)(\\d{7,8})(-(\\d{3,}))?$";
        if (telNumber == null || !(Pattern.matches(regexMobile, telNumber) || Pattern.matches(regexPhone, telNumber))) {
            returnCode = ParamErrorType.WRONG_FORMAT.getValue();
            message = ParamsValid.getErrorMessageWhenWrongFormat("电话号码格式不正确");
        }
        return this;
    }

    public ParamsValid validCoordinate(BigDecimal lon, BigDecimal lat, boolean checkNull) {
        if (returnCode != 0) {
            return this;
        }
        if (!CoordinateHelper.valid(lon, lat, checkNull)) {
            returnCode = ParamErrorType.WRONG_FORMAT.getValue();
            message = ParamsValid.getErrorMessageWhenWrongFormat("经纬度数值超出范围(minX=-180.0,maxX=180.0"
                    + ",minY=-90.0,maxY=90.0)");
        }
        return this;
    }

    public ParamsValid validIp(String ip) {
        if (returnCode != 0) {
            return this;
        }
        if (!IpHelper.verifyIp(ip)) {
            returnCode = ParamErrorType.WRONG_FORMAT.getValue();
            message = ParamsValid.getErrorMessageWhenWrongFormat("ip地址格式不正确");
        }
        return this;
    }

    public boolean isValid() {
        return returnCode == 0 && StringUtils.isBlank(message);
    }

    public Protocol showInValidMessage() {
        return new Protocol(returnCode, message);
    }

    private static String getErrorMessageWhenWrongFormat(String errorTipMsg) {
        return ParamErrorType.WRONG_FORMAT.getName() + errorTipMsg;
    }
}