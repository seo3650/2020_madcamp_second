package com.example.tab;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
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
import com.facebook.AccessToken;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.tab.Fragment4.items;
import static com.example.tab.MainActivity.url;
import static com.example.tab.MainActivity.userId;

public class PokemonList extends Fragment {

    private static final String TAG = "PokemonList";

    IPokemonDex iPokemonDex;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    RecyclerView pokemon_list_recyclerview;

    static PokemonList instance;

    private View view;
    private RoundCornerProgressBar progressBar;
    private ShareButton shareButton;

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
        view = inflater.inflate(R.layout.fragment_pokemon_list, container, false);
        pokemon_list_recyclerview = (RecyclerView) view.findViewById(R.id.pokemon_list_recyclerview);
        pokemon_list_recyclerview.setHasFixedSize(true);
        pokemon_list_recyclerview.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        ItemOffsetDecoration itemOffsetDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.spacing);
        pokemon_list_recyclerview.addItemDecoration(itemOffsetDecoration);

        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        progressBar.setMax(items.size());
        progressBar.bringToFront();
        progressBar.setElevation(3000);
        shareButton = (ShareButton) view.findViewById(R.id.fb_share_button_at_dogam);
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
            public void onResponseReceived(HashSet<String> foundItems) {
                progressBar.setProgress(0);
                progressBar.setProgress(foundItems.size());
                if (progressBar.getMax() > 0) { // TODO: change condition
                    new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Success!")
                            .setContentText("You found all items!\n"+ "Excellent:)")
                            .setConfirmText("Share it")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();

                                    /* Facebook share */
                                    /* Get certificate */
                                    Bitmap bitmap = BitmapFactory.decodeResource(
                                            getContext().getResources(), R.drawable.diploma);
                                    /* Get player name */
                                    getUserName(new FacebookResponse() {
                                        @Override
                                        public void onResponseReceived(String res) {
                                            String name = res.split("\"")[3];


                                        }
                                    });

                                    /* Merge with two bitmap */

                                    SharePhoto photo = new SharePhoto.Builder()
                                            .setBitmap(bitmap)
                                            .build();
                                    SharePhotoContent content = new SharePhotoContent.Builder()
                                            .addPhoto(photo)
                                            .build();
                                    shareButton.setShareContent(content);
                                }
                            })
                            .show();
                }
            }
        });
        pokemon_list_recyclerview.setAdapter(adapter);
    }

    interface FacebookResponse {
        void onResponseReceived(String res);
    }

    public interface PokemonResponse {
        void onResponseReceived(HashSet<String> foundItems);
    }

    private void getUserName(FacebookResponse facebookResponse) {
        String token = AccessToken.getCurrentAccessToken().getToken();
        if (userId == null || token == null) {
            return;
        }

        /* Init retrofit */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://graph.facebook.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FacebookService service = retrofit.create(FacebookService.class);

        service.getName(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                Log.d("FacebookService", "res:" + response.body());
                if (response.body() == null){
                    return;
                }
                String name = "Undefined";

                try {
                    name = response.body().string();
                } catch (IOException e) {
                    name = "Undefined";
                } finally {
                    facebookResponse.onResponseReceived(name);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.d("FacebookService", "Failed API call with call: " + call
                        + ", exception:  " + t);
            }
        });
    }
}