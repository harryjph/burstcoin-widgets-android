package com.harrysoft.burstinfowidget.service

import android.content.Context

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.harry1453.burst.explorer.service.NetworkService

import io.reactivex.Single

class AndroidNetworkService(context: Context) : NetworkService {

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    override fun fetchData(url: String): Single<String> {
        return Single.create { emitter ->
            val request = StringRequest(url, Response.Listener { emitter.onSuccess(it) }, Response.ErrorListener { emitter.onError(it) })
            requestQueue.add(request)
            emitter.setCancellable { request.cancel() }
        }
    }
}
