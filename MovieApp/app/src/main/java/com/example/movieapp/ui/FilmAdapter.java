package com.example.movieapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieapp.R;
import com.example.movieapp.data.model.Film;
import com.example.movieapp.data.network.ApiConfig;

import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    public static final int TYPE_TOP_RATED = 0;
    public static final int TYPE_LATEST = 1;
    public static final int TYPE_DISCOVER = 2;
    public static final int TYPE_BOOKMARK = 3;
    private List<Film> filmList;
    private int viewType;
    private OnItemClickListener listener;
    private OnRemoveClickListener removeListener;

    public interface OnItemClickListener {
        void onItemClick(Film film);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public FilmAdapter(List<Film> filmList) {
        this.filmList = filmList;
        this.viewType = TYPE_TOP_RATED;
    }

    public FilmAdapter(List<Film> filmList, int viewType) {
        this.filmList = filmList;
        this.viewType = viewType;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.removeListener = listener;
    }

    public void updateData(List<Film> newFilmList) {
        this.filmList = newFilmList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == TYPE_TOP_RATED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_film, parent, false);
        } else if (viewType == TYPE_BOOKMARK) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bookmark, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_film2, parent, false);
        }
            
        return new FilmViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = filmList.get(position);
        holder.bind(film);
    }

    @Override
    public int getItemCount() {
        return filmList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    public class FilmViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMovieCover;
        private TextView tvMovieTitle;
        private TextView tvRating;
        private RatingBar rbMovieRating;
        private TextView tvDescription;
        private TextView tvGenre;
        private TextView tvReleaseYear;
        private TextView tvRatingLabel;
        private ImageView ivMoviePoster;
        private TextView tvBookmarkTitle;
        private TextView tvBookmarkRating;
        private RatingBar rbBookmarkRating;
        private TextView tvBookmarkReleaseYear;
        
        private int holderViewType;

        public FilmViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.holderViewType = viewType;

            if (viewType == TYPE_BOOKMARK) {
                initBookmarkViews(itemView);
            } else {
                initStandardViews(itemView, viewType);
            }

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(filmList.get(position));
                }
            });
        }

        private void initBookmarkViews(View itemView) {
            ivMoviePoster = itemView.findViewById(R.id.ivMoviePoster);
            tvBookmarkTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvBookmarkRating = itemView.findViewById(R.id.tvRating);
            rbBookmarkRating = itemView.findViewById(R.id.rbRating);
            tvBookmarkReleaseYear = itemView.findViewById(R.id.tvReleaseYear);

            ImageView ivRemove = itemView.findViewById(R.id.ivRemove);
            if (ivRemove != null) {
                ivRemove.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && removeListener != null) {
                        removeListener.onRemoveClick(position);
                    }
                });
            }
        }

        private void initStandardViews(View itemView, int viewType) {
            ivMovieCover = itemView.findViewById(R.id.ivMovieCover);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            rbMovieRating = itemView.findViewById(R.id.rbMovieRating);

            if (viewType == TYPE_LATEST || viewType == TYPE_DISCOVER) {
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvGenre = itemView.findViewById(R.id.tvGenre);
                tvReleaseYear = itemView.findViewById(R.id.tvReleaseYear);
                tvRatingLabel = itemView.findViewById(R.id.tvRatingLabel);
            }
        }

        public void bind(Film film) {
            if (holderViewType == TYPE_BOOKMARK) {
                bindBookmarkView(film);
            } else {
                bindStandardView(film);
            }
        }

        private void bindBookmarkView(Film film) {
            tvBookmarkTitle.setText(film.getTitle());
            
            String formattedRating = String.format("%.1f", film.getRating());
            tvBookmarkRating.setText(formattedRating);
            
            float starRating = film.getRating() / 2;
            rbBookmarkRating.setRating(starRating);
            
            if (tvBookmarkReleaseYear != null && film.getReleaseDate() != null) {
                tvBookmarkReleaseYear.setText(film.getReleaseYear());
            }

            Glide.with(itemView.getContext())
                .load(ApiConfig.getImageUrl(film.getPosterPath()))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(ivMoviePoster);
        }

        private void bindStandardView(Film film) {
            tvMovieTitle.setText(film.getTitle());

            String formattedRating = String.format("%.1f", film.getRating());
            tvRating.setText(formattedRating);

            float starRating = film.getRating() / 2;
            if (rbMovieRating != null) {
                rbMovieRating.setRating(starRating);
            }

            if (holderViewType == TYPE_LATEST || holderViewType == TYPE_DISCOVER) {
                bindLatestSpecificView(film);
            }

            Glide.with(itemView.getContext())
                .load(ApiConfig.getImageUrl(film.getPosterPath()))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(ivMovieCover);
        }

        private void bindLatestSpecificView(Film film) {
            if (tvGenre != null) {
                tvGenre.setText("Action, Adventure");
            }

            if (tvReleaseYear != null && film.getReleaseDate() != null) {
                tvReleaseYear.setText(film.getReleaseYear());
            }

            if (tvDescription != null) {
                tvDescription.setText(film.getOverview());
            }
        }
    }
}