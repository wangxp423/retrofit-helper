package com.xcheng.okhttp.request;

import com.xcheng.okhttp.util.ParamUtil;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 通用的GET请求
 * Created by chengxin on 2017/6/22.
 */
public class GetRequest extends OkRequest {
    private final HttpUrl httpUrl;

    private GetRequest(Builder builder) {
        super(builder);
        @SuppressWarnings("ConstantConditions")
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url()).newBuilder();
        final boolean encoded = builder.encoded;
        for (Map.Entry<String, String> entry : params().entrySet()) {
            if (!encoded) {
                urlBuilder.addEncodedQueryParameter(ParamUtil.encode(entry.getKey()), ParamUtil.encode(entry.getValue()));
            } else {
                urlBuilder.addEncodedQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        httpUrl = urlBuilder.build();
    }

    public HttpUrl httpUrl() {
        return httpUrl;
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(httpUrl).headers(headers()).tag(tag()).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        //是否已经编码过了
        private boolean encoded = false;

        public Builder encoded(boolean encoded) {
            this.encoded = encoded;
            return this;
        }

        @Override
        public GetRequest build() {
            //忽略method设置
            method(OkRequest.GET);
            return new GetRequest(this);
        }
    }
}
