package com.example.tab.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tab.Fragment4;
import com.example.tab.ImageService;
import com.example.tab.Model.Pokemon;
import com.example.tab.PokemonList;
import com.example.tab.R;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

import static com.example.tab.Fragment4.getFromDatabase;
import com.example.tab.Fragment4.ImageResponse;


public class PokemonListAdapter extends RecyclerView.Adapter<PokemonListAdapter.MyViewHolder> {

    Context context;
    List<String> pokemonList;
//    private int foundItems;
    private HashSet<String> foundItems;
    private PokemonList.PokemonResponse pokemonResponse;


    public PokemonListAdapter(Context context, List<String> pokemonList,  PokemonList.PokemonResponse pokemonResponse) {
        this.context = context;
        this.pokemonList = pokemonList;
        this.foundItems = new HashSet<>();
        this.pokemonResponse = pokemonResponse;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.pokemon_list_item,parent, false );

        return new MyViewHolder(itemView);
    }


    // Image and item name set here
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.pokemon_name.setText(pokemonList.get(position));

        getFromDatabase(  pokemonList.get(position)  ,new ImageResponse() {
            @Override
            public void onResponseReceived(Bitmap res) {
                if (res == null) {
                    Log.d("ImageService", "Download Failed. Loading default image.");
                    Glide.with(context).load(getDrawable(pokemonList.get(position))  ).into(holder.pokemon_image);
                    return;
                }
                foundItems.add(pokemonList.get(position));
                Log.d("ImageService", "Download Success");

                Glide.with(context).load(res).into(holder.pokemon_image);
                pokemonResponse.onResponseReceived(foundItems);
            }
        });

        //Load image
        //Glide.with(context).load(pokemonList.get(position).getImg()).into(holder.pokemon_image);


        //Set name

    }

    private int getDrawable(String item){
        switch (item) {
            case "Computer":
                return R.drawable.computer_silhouette;
            case "Mobile Phone":
                return R.drawable.phone_silhouette;
            case "Chair":
                return R.drawable.chair_silhouette;
            case "Clock":
                return R.drawable.clock_silhouette;
            case "Glasses":
                return R.drawable.glasses;
            default:
                return -1;
        }


    }



    @Override
    public int getItemCount() {
        return pokemonList.size();
    }
//    public int getFoundItems() { return foundItems; }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView pokemon_image;
        TextView pokemon_name;


        public MyViewHolder(View itemView) {
            super(itemView);


            pokemon_image = (ImageView) itemView.findViewById(R.id.pokemon_image);
            pokemon_name = (TextView) itemView.findViewById(R.id.txt_pokemon_name);
        }

    }





}
