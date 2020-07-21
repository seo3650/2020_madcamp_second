package com.example.tab;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.common.BaseRoundCornerProgressBar;
import com.example.tab.Adapter.PokemonListAdapter;
import com.example.tab.Common.Common;
import com.example.tab.Common.ItemOffsetDecoration;
import com.example.tab.Model.Pokedex;
import com.example.tab.Retrofit.IPokemonDex;
import com.example.tab.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static com.example.tab.Fragment4.items;

public class PokemonList extends Fragment {

    private static final String TAG = "PokemonList";

    IPokemonDex iPokemonDex;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    RecyclerView pokemon_list_recyclerview;

    static PokemonList instance;

    private RoundCornerProgressBar progressBar;
//    private ProgressBar progressBar;

    public static PokemonList getInstance(){
        if(instance == null){
            instance = new PokemonList();
        }
        return instance;
    }

    public PokemonList() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pokemon_list, container, false);

        pokemon_list_recyclerview = (RecyclerView) view.findViewById(R.id.pokemon_list_recyclerview);
        pokemon_list_recyclerview.setHasFixedSize(true);
        pokemon_list_recyclerview.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        ItemOffsetDecoration itemOffsetDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.spacing);
        pokemon_list_recyclerview.addItemDecoration(itemOffsetDecoration);

        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        progressBar.setMax(items.size());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        fetchData(items);
    }

    private void fetchData(ArrayList<String> items){

        //Retrofit retrofit = RetrofitClient.getInstance();
        //iPokemonDex = retrofit.create(IPokemonDex.class);
        /*
        compositeDisposable.add(iPokemonDex.getListPokemon()
        .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pokedex>() {
                    @Override
                    public void accept(Pokedex pokedex) throws Exception {
                        Common.commonPokemonList = pokedex.getPokemon();
                        PokemonListAdapter adapter = new PokemonListAdapter(getActivity(), Common.commonPokemonList);

                        pokemon_list_recyclerview.setAdapter(adapter);

                    }
                })

        );

         */
        PokemonListAdapter adapter = new PokemonListAdapter(getActivity(), items, new PokemonResponse() {
            @Override
            public void onResponseReceived(int foundItems) {
                progressBar.setProgress(0);
                progressBar.setProgress(foundItems);
            }
        });
        pokemon_list_recyclerview.setAdapter(adapter);
    }

    public interface PokemonResponse {
        void onResponseReceived(int foundItems);
    }
}