package com.example.movieapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieapp.R;
import com.example.movieapp.data.model.Cast;
import com.example.movieapp.data.network.ApiConfig;

import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private final List<Cast> castList;

    public CastAdapter(List<Cast> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.bind(cast);
    }

    @Override
    public int getItemCount() {
        return castList.size();
    }

    public static class CastViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCastPhoto;
        private final TextView tvActorName;
        private final TextView tvCharacterName;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCastPhoto = itemView.findViewById(R.id.ivCastPhoto);
            tvActorName = itemView.findViewById(R.id.tvActorName);
            tvCharacterName = itemView.findViewById(R.id.tvCharacterName);
        }

        public void bind(Cast cast) {
            tvActorName.setText(cast.getName());
            tvCharacterName.setText(cast.getCharacter());

            Glide.with(itemView.getContext())
                    .load(ApiConfig.getImageUrl(cast.getProfilePath()))
                    .placeholder(R.drawable.placeholder_person)
                    .error(R.drawable.error_person)
                    .into(ivCastPhoto);
        }
    }
}