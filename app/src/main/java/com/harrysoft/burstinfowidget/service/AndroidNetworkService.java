package com.harrysoft.burstinfowidget.service;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.harry1453.burst.explorer.service.NetworkService;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Single;

public class AndroidNetworkService implements NetworkService {

    private final RequestQueue requestQueue;

    public AndroidNetworkService(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    @NotNull
    @Override
    public Single<String> fetchData(@NotNull String url) {
        return Single.create(e -> requestQueue.add(new StringRequest(url, e::onSuccess, e::onError)));
    }
}
