package school.coding.techkids.musicplayer_longth.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import school.coding.techkids.musicplayer_longth.R;
import school.coding.techkids.musicplayer_longth.adapters.TopSongAdapter;
import school.coding.techkids.musicplayer_longth.databases.MusicTypeModel;
import school.coding.techkids.musicplayer_longth.databases.TopSongModel;
import school.coding.techkids.musicplayer_longth.events.OnClickMusicType;
import school.coding.techkids.musicplayer_longth.events.OnClickTopSong;
import school.coding.techkids.musicplayer_longth.managers.MusicManager;
import school.coding.techkids.musicplayer_longth.managers.ScreenManager;
import school.coding.techkids.musicplayer_longth.model.topSongJSON.top_song.TopSongJSONModel;
import school.coding.techkids.musicplayer_longth.model.topSongJSON.top_song.TopSongRespondJSON;
import school.coding.techkids.musicplayer_longth.model.topSongJSON.top_song.Image;
import school.coding.techkids.musicplayer_longth.network.GetTopSong;
import school.coding.techkids.musicplayer_longth.network.RetrofitFactory;

/**
 * Created by trongphuong1011 on 7/20/2017.
 */

public class TopSongFragment extends Fragment implements View.OnClickListener {
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.rv_playlist)
    RecyclerView rvPlaylist;
    @BindView(R.id.tv_style_music)
    TextView tvMusicStyle;
    @BindView(R.id.tv_numberSongs)
    TextView tvNumberSongs;
    @BindView(R.id.iv_back_top_song)
    ImageView ivBackTopSong;
    private static final String TAG = MusicTypeFragment.class.toString();
    List<TopSongModel> topSongModelList = new ArrayList<>();
    TopSongAdapter topSongAdapter;
    private MusicTypeModel musicTypeModel;


    public TopSongFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_song, container, false);
        ButterKnife.bind(this, view);
        setupUI(view);
        loadDatas();
        return view;
    }



    private void loadDatas() {
        final GetTopSong getTopSong = RetrofitFactory.getInstance().create(GetTopSong.class);
        getTopSong.getTopSong(musicTypeModel.getId()).enqueue(new Callback<TopSongRespondJSON>() {
            @Override
            public void onResponse(Call<TopSongRespondJSON> call, Response<TopSongRespondJSON> response) {
//                List<TopSongJSONModel> entry = response.body().getFeed().getEntry();
                for (TopSongJSONModel topSongJSONModel : response.body().getFeed().getEntry()) {
                    TopSongModel topSongModel = new TopSongModel();
                    topSongModel.setSongName(topSongJSONModel.getSongName());
                    topSongModel.setSingerName(topSongJSONModel.getSongArtist());
                    for (Image image : topSongJSONModel.getSongImage()) {
                        if (image.getAttribute().getHeight() == 170) {
                            topSongModel.setImage(image);
                            topSongModelList.add(topSongModel);
                        }
                    }
                }
                tvNumberSongs.setText(topSongModelList.size() + " songs");
                topSongAdapter.notifyDataSetChanged();
                Log.d(TAG, "\nOnResponseList:\n" + topSongModelList);

            }

            @Override
            public void onFailure(Call<TopSongRespondJSON> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.toString());
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUI(View view) {
        //eventBus
        EventBus.getDefault().register(this);
        ButterKnife.bind(this, view);
        topSongAdapter = new TopSongAdapter(getContext(), topSongModelList);
        rvPlaylist.setAdapter(topSongAdapter);
        appBar.setBackgroundResource(musicTypeModel.getImageID());
        tvMusicStyle.setText(musicTypeModel.getKey().toUpperCase());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvPlaylist.setLayoutManager(manager);
        topSongAdapter.setOnItemClick(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvPlaylist.addItemDecoration(dividerItemDecoration);

        ivBackTopSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getFragmentManager().getBackStackEntryCount()>0){
                    getFragmentManager().popBackStack();
                }
                else {
                    Toast.makeText(getContext(), "R i p",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Subscribe(sticky = true)
    public void onReceivedMusicType(OnClickMusicType onClickMusicType) {
        musicTypeModel = onClickMusicType.getMusicTypeModel();
    }

    @Override
    public void onClick(View view) {
//        TopSongJSONModel topSongJSONModel = (TopSongJSONModel) view.getTag();
        TopSongModel topSongModel = (TopSongModel) view.getTag();
//        MiniPlayerFragment miniPlayerFragment = new MiniPlayerFragment();
        EventBus.getDefault().postSticky(new OnClickTopSong(topSongModel));
//        ScreenManager.openFragment(getActivity().getSupportFragmentManager(), miniPlayerFragment,R.layout.fragment_mini_player );
        MusicManager.loadSearchSong(topSongModel, getContext());
        setDataforMiniPlayer(topSongModel);
    }
    private void setDataforMiniPlayer(TopSongModel topSongModel){
        RelativeLayout rlMiniPlayer = getActivity().findViewById(R.id.rlayout_mini);
        rlMiniPlayer.setVisibility(View.VISIBLE);

        ImageView ivAvatarMiniPlayer = getActivity().findViewById(R.id.iv_avatar_mini_player);
        TextView tvSongMiniPlayer = getActivity().findViewById(R.id.tv_song_name_mini_player);
        TextView tvArtistMiniPlayer = getActivity().findViewById(R.id.tv_artist_mini_player);

        tvSongMiniPlayer.setText(topSongModel.getSongName().getLabel());
        tvArtistMiniPlayer.setText(topSongModel.getSingerName().getLabel());
        Picasso.with(getContext()).load(topSongModel.getImage().getLabel())
                .transform(new CropCircleTransformation()).into(ivAvatarMiniPlayer);
    }
}
