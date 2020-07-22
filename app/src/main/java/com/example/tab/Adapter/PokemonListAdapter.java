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
import com.wajahatkarim3.easyflipview.EasyFlipView;


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

        switch (pokemonList.get(position)) {
            case "Computer":
                holder.pokemon_back_content.setText("Computer\n\n실습실에서 볼 수 있는 듀얼스크린 컴퓨터");
                break;
            case "Mobile Phone":
                holder.pokemon_back_content.setText("Computer\n\nGithub 알람이 와있다.");
                break;
            case "Clock":
                holder.pokemon_back_content.setText("Clock\n\n정작 볼일이 별로 없는 벽시계");
                break;
            case "Chair":
                holder.pokemon_back_content.setText("Chair\n\n프로그래머의 온기가 남아있다.");
                break;
            case "Vehicle":
                holder.pokemon_back_content.setText("Vehicle\n\n카이스트에서 가장 흔히 볼 수 있는 탈것.");
                break;
            case "Umbrella":
                holder.pokemon_back_content.setText("Umbrella\n\n장마철 필수템");
                break;
            case "Stairs":
                holder.pokemon_back_content.setText("Stairs\n\n계단이다. 특별히 눈에 띄는 점은 없다.");
                break;
            case "Sky":
                holder.pokemon_back_content.setText("Sky\n\n실습실에선 보기 힘든 하늘");
                break;
            case "Plant":
                holder.pokemon_back_content.setText("Plant\n\n비싸 보인다.");
                break;
            case "Pattern":
                holder.pokemon_back_content.setText("Pattern\n\n어케 찾았을까?");
                break;
            case "Car":
                holder.pokemon_back_content.setText("Car\n\nN1 출근 필수템");
                break;
            case "Asphalt":
                holder.pokemon_back_content.setText("Asphalt\n\n차도에 많이 깔려 있다.");
                break;
            case "Building":
                holder.pokemon_back_content.setText("Building\n\n직장인의 위시리스트 NO.1 아이템");
                break;
        }

        getFromDatabase(  pokemonList.get(position)  ,new ImageResponse() {
            @Override
            public void onResponseReceived(Bitmap res) {
                Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                Bitmap bmp = Bitmap.createBitmap(100, 100, conf);
//                Glide.with(context).load(bmp).into(holder.pokemon_back_image);
                if (res == null) {
                    Log.d("ImageService", "Download Failed. Loading default image.");
                    Glide.with(context).load(getDrawable(pokemonList.get(position))  ).into(holder.pokemon_image);
                    return;
                }
                foundItems.add(pokemonList.get(position));
                holder.pokemon_card.setFlipEnabled(true);
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
        TextView pokemon_back_content;
        EasyFlipView pokemon_card;

        public MyViewHolder(View itemView) {
            super(itemView);

            pokemon_image = (ImageView) itemView.findViewById(R.id.pokemon_image);
            pokemon_name = (TextView) itemView.findViewById(R.id.txt_pokemon_name);
            pokemon_back_content = (TextView) itemView.findViewById(R.id.txt_pokemon_back);
            pokemon_card = (EasyFlipView) itemView.findViewById(R.id.pokemon_card);
            pokemon_card.setFlipEnabled(false);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Pokemon item", "Click");
                    int pos = getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) { return; }
                    String name = pokemonList.get(pos);
                    if (foundItems.contains(name)) {
                        pokemon_card.flipTheView();
                    }

                }
            });
        }


    }





}
