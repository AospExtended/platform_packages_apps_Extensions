package org.aospextended.extensions.aexstats;

import org.aospextended.extensions.aexstats.models.ServerRequest;
import org.aospextended.extensions.aexstats.models.ServerResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by ishubhamsingh on 25/9/17.
 */

public interface RequestInterface {

    @POST("aexstats_api/")
    Observable<ServerResponse> operation(@Body ServerRequest request);

}