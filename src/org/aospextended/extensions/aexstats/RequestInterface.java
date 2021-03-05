package org.aospextended.extensions.aexstats;

import io.reactivex.Observable;

import org.aospextended.extensions.aexstats.models.ServerRequest;
import org.aospextended.extensions.aexstats.models.ServerResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by ishubhamsingh on 25/9/17.
 */

public interface RequestInterface {

    @POST("stats/")
    Observable<ServerResponse> operation(@Body ServerRequest request);

}
