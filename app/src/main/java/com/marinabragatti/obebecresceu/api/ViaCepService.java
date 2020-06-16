package com.marinabragatti.obebecresceu.api;

import com.marinabragatti.obebecresceu.model.Cep;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ViaCepService {

    @GET("{cep}/json/")
    Call<Cep> recuperarCep(@Path("cep") String cep);
}
