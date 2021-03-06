package brainbreaker.popularmovies.Activities;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import brainbreaker.popularmovies.Adapters.ReviewListAdapter;
import brainbreaker.popularmovies.Extras.Connectivity;
import brainbreaker.popularmovies.Listeners.FetchVideoListener;
import brainbreaker.popularmovies.Listeners.ReviewListLoadedListener;
import brainbreaker.popularmovies.Models.FavouriteMovies;
import brainbreaker.popularmovies.Models.MovieClass;
import brainbreaker.popularmovies.Models.ReviewClass;
import brainbreaker.popularmovies.MyApplication;
import brainbreaker.popularmovies.R;
import brainbreaker.popularmovies.Utilities.FetchReviews;
import brainbreaker.popularmovies.Utilities.FetchVideo;

/**
 * A placeholder fragment containing a simple view.
 */
public class DescriptionActivityFragment extends Fragment implements ReviewListLoadedListener, FetchVideoListener{
    String movieID;
    String movietitle;
    String posterURL;
    String moviedescription;
    String movierating;
    String movierelease;

    String TAG = "DescriptionActivityFragment";
    public static final String ARG_ITEM_ID = "item_id";

    TextView movieTitle;
    TextView description;
    TextView rating;
    TextView release;
    ImageView videoImageView;
    ImageView playButton;
    ListView reviewListView;
    TextView emptyReviewList;

    boolean alreadyFav = false;

    public static ArrayList<String> FavouriteMovieNames = new ArrayList<>();
    ProgressDialog progress;
    public static ArrayList<FavouriteMovies> FavouriteList = new ArrayList<>();

    public DescriptionActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_description, container, false);
        movieTitle = (TextView) rootView.findViewById(R.id.movieTitle);
        description = (TextView) rootView.findViewById(R.id.description);
        rating = (TextView) rootView.findViewById(R.id.rating);
        release = (TextView) rootView.findViewById(R.id.release);
        emptyReviewList = (TextView) rootView.findViewById(R.id.emptyReviewList);
        playButton = (ImageView) rootView.findViewById(R.id.videoPreviewPlayButton);
        reviewListView = (ListView) rootView.findViewById(R.id.reviewListView);
        videoImageView = (ImageView) rootView.findViewById(R.id.poster);

        LinearLayout descriptionLayout = (LinearLayout) rootView.findViewById(R.id.description_layout);
        TextView noItemSelectedTV = (TextView) rootView.findViewById(R.id.selectItemTextView);

        if (getActivity().getIntent().getExtras()!=null) {
            movieID = getActivity().getIntent().getExtras().getString("MovieID");
            movietitle = getActivity().getIntent().getExtras().getString("MovieTitle");
            posterURL = getActivity().getIntent().getExtras().getString("PosterURL");
            moviedescription = getActivity().getIntent().getExtras().getString("MovieDescription");
            movierating = getActivity().getIntent().getExtras().getString("MovieRating");
            movierelease = getActivity().getIntent().getExtras().getString("MovieRelease");
        }
        else if(getArguments()!=null) {
            Bundle bundle = getArguments();
            Log.e("MovieID",bundle.getString("MovieID"));
            movieID = bundle.getString("MovieID");
            movietitle = bundle.getString("MovieTitle");
            posterURL = bundle.getString("PosterURL");
            moviedescription = bundle.getString("MovieDescription");
            movierating = bundle.getString("MovieRating");
            movierelease = bundle.getString("MovieRelease");
        }
        else {
            descriptionLayout.setVisibility(View.GONE);
            movieID = "";
            movietitle = "";
            posterURL = "";
            moviedescription = "";
            movierating = "";
            movierelease = "";
            noItemSelectedTV.setVisibility(View.VISIBLE);
            if (!Connectivity.isOnline(getActivity())){
                noItemSelectedTV.setText("No Internet Connection");
            }
        }

        movieTitle.setText(movietitle);
        description.setText(moviedescription);
        rating.setText("Rating: "+movierating + "/10.0");
        release.setText("Release Date: "+movierelease);
        playButton.setVisibility(View.GONE);

        final ImageButton fav = (ImageButton) rootView.findViewById(R.id.favourite);
        //If a movie name is added to favourites show the highlighted star(Get The fav movie list from Shared Preferences).
        for (int i = 0; i< MyApplication.getfavMovieNameList(getActivity()).size(); i++)
        {
            if (MyApplication.getfavMovieNameList(getActivity()).get(i).equals(movietitle)){
                fav.setBackgroundResource(android.R.drawable.star_big_on);
                FavouriteMovies favouriteMovie = new FavouriteMovies(MyApplication.retrieveFavList(getActivity()).get(i).getMovie(),MyApplication.retrieveFavList(getActivity()).get(i).getReviews());
                ReviewListAdapter adapter = new ReviewListAdapter(getActivity(),favouriteMovie.getReviews());
                reviewListView.setAdapter(adapter);
            }
        }
        // SHOW A PROGRESS DIALOG
        progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading Video...");
        progress.setCancelable(false);
        progress.show();

        // Now we are fetching video key
        FetchVideo fetchVideo = new FetchVideo(this);
        fetchVideo.execute(movieID);

        FetchReviews fetchReviews = new FetchReviews(DescriptionActivityFragment.this);
        fetchReviews.execute(movieID);

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0; i<MyApplication.retrieveFavList(getActivity()).size();i++){
                    MovieClass movie = MyApplication.retrieveFavList(getActivity()).get(i).getMovie();
                    if (movie.getid().equals(movieID)){
                        Toast.makeText(getActivity(),"Movie is already available as favourite",Toast.LENGTH_LONG).show();
                        alreadyFav = true;
                        return;
                    }
                }

                if (!alreadyFav){
                    fav.setBackgroundResource(android.R.drawable.star_big_on);
                    Toast.makeText(getActivity(),"Movie added as favourite",Toast.LENGTH_LONG).show();
                    ArrayList<ReviewClass> reviewlist = new ArrayList<ReviewClass>();
                    int len = reviewListView.getCount();
                    for (int i = 0; i < len; i++) {
                        ReviewClass currentReview = (ReviewClass) reviewListView.getAdapter().getItem(i);
                        reviewlist.add(currentReview);
                    }
                    MovieClass movieObject = new MovieClass(movietitle, posterURL, moviedescription, movierating, movierelease,movieID,true);
                    FavouriteList.add(new FavouriteMovies(movieObject, reviewlist));
                    FavouriteMovieNames.add(movietitle);
                    MyApplication.saveFavList(getActivity(),FavouriteList);
                    MyApplication.setfavMovieName(getActivity(),FavouriteMovieNames);
                }

            }
        });
        return rootView;
    }

    @Override
    public void fetchVideoListener(final String videoKey) {
        try {
            // Sample Thumbnail URL: http://img.youtube.com/vi/VIDEO_ID/default.jpg

            Uri VideoThumbnailURL = Uri.parse("http://img.youtube.com/vi").buildUpon()
                    .appendPath(videoKey)
                    .appendPath("default.jpg").build();

            Log.e("VideoThumbnailURL",VideoThumbnailURL.toString());

            Picasso.with(getActivity())
                    .load(VideoThumbnailURL)
                    .resize(500,200)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(videoImageView);


            if(progress.isShowing()){
                progress.dismiss();
            }
            playButton.setVisibility(View.VISIBLE);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        // Playing the video in Youtube App.
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri VideoURL = Uri.parse("https://www.youtube.com/watch").buildUpon()
                        .appendQueryParameter("v",videoKey).build();
                Log.e("BUILT URL", VideoURL.toString());
                startActivity(new Intent(Intent.ACTION_VIEW, VideoURL));
            }
        });
    }

    @Override
    public void reviewListLoaded(ArrayList<ReviewClass> reviewList) {
        try {
            ReviewListAdapter adapter = new ReviewListAdapter(getActivity(),reviewList);
            reviewListView.setAdapter(adapter);
            reviewListView.setEmptyView(emptyReviewList);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}
