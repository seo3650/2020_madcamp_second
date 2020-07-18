package com.example.tab.Retrofit;

import com.example.tab.Model.Pokedex;

//import java.util.Observable;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface IPokemonDex {

    @GET("pokedex.json")
    Observable<Pokedex> getListPokemon();

}
